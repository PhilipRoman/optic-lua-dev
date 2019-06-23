package optic.lua.asm;

import optic.lua.messages.CompilationFailure;
import optic.lua.optimization.ProvenType;
import optic.lua.util.Trees;
import org.antlr.runtime.tree.*;

import java.util.*;

import static nl.bigo.luaparser.Lua53Lexer.*;
import static optic.lua.asm.ExprNode.*;
import static optic.lua.asm.ListNode.exprList;
import static optic.lua.asm.StepFactory.assign;
import static optic.lua.util.Trees.childrenOf;

/**
 * A builder for compiling expressions/statements in form of <code>a.b.c()</code> or <code>a.b.c = x</code>.
 * To flatten such code patterns, create a new builder from the first element of the "chain", add remaining elements
 * using {@link #add(Tree)} and finally use one of the finalization methods to get the result:
 * <ul>
 * <li>{@link #buildExpression()}</li>
 * <li>{@link #buildStatement()}</li>
 * <li>{@link #getSelf()}</li>
 * <li>{@link #getLastIndexKey()}</li>
 * </ul>
 */
final class ChainedAccessBuilder {
	private final List<VoidNode> steps = new ArrayList<>(4);
	private ListNode current; // the current result of the whole expression
	private ExprNode self; // if the next operation is "colon call", this will be the "self" object
	private Op lastOp = null;
	private ExprNode lastKey = null; // if the last operation was a table index, this is the key that was used
	private final Flattener flattener;

	/**
	 * @param flattener the {@link Flattener} to use while flattening child expressions
	 * @param current   the first value in the "chain" (such as "a" in <code>a.b.c()</code>)
	 */
	ChainedAccessBuilder(Flattener flattener, ExprNode current) {
		this.flattener = flattener;
		this.current = current;
		this.self = current;
	}

	/**
	 * Add another element to the "chain". The type of tree must be INDEX, CALL or COL_CALL
	 *
	 * @param tree the child node to add
	 * @throws CompilationFailure rethrown if flattening the child node fails
	 */
	public void add(Tree tree) throws CompilationFailure {
		var t = (CommonTree) tree;

		if (lastOp == Op.CALL || lastOp == Op.COL_CALL)
			current = firstOnly(current);

		switch (t.getType()) {
			case INDEX:
				addIndex(t);
				break;
			case CALL:
				addCall(t, false);
				break;
			case COL_CALL:
				addCall(t, true);
				break;
			default:
				throw new IllegalArgumentException(Trees.reverseLookupName(tree.getType()));
		}
	}

	private void addIndex(CommonTree tree) throws CompilationFailure {
		Trees.expect(INDEX, tree);
		var key = flattener.flattenExpression((CommonTree) tree.getChild(0));
		steps.addAll(key.block());
		Register next = Register.ofType(ProvenType.OBJECT);
		steps.add(assign(next, tableIndex(firstOnly(current), firstOnly(key.value()))));
		self = firstOnly(current);
		current = next;
		lastKey = firstOnly(key.value());
		lastOp = Op.INDEX;
	}

	private void addCall(CommonTree tree, boolean colon) throws CompilationFailure {
		Trees.expect(colon ? COL_CALL : CALL, tree);
		List<ListNode> args = new ArrayList<>();
		if (colon)
			args.add(self);
		for (var child : childrenOf(tree))
			args.add(flattener.flattenExpression((CommonTree) child).applyTo(steps));
		current = invocation(firstOnly(current), InvocationMethod.CALL, exprList(args));
		lastKey = null;
		lastOp = colon ? Op.COL_CALL : Op.CALL;
	}

	/**
	 * Returns the "chain" as a function call statement.
	 */
	List<VoidNode> buildStatement() {
		if (lastOp == Op.CALL || lastOp == Op.COL_CALL) {
			var list = new ArrayList<>(steps);
			list.add(StepFactory.discard(current));
			return list;
		}
		return Collections.unmodifiableList(steps);
	}

	/**
	 * Returns the resulting expression (either a function call or an index access).
	 */
	FlatExpr buildExpression() {
		return new FlatExpr(steps, current);
	}

	/**
	 * Returns the last expression in the "chain" which may be used as a target of an index assignment or a colon call
	 */
	ExprNode getSelf() {
		return self;
	}

	/**
	 * Returns the key expression of the last index access. Useful when compiling table index assignment.
	 */
	ExprNode getLastIndexKey() {
		if (lastKey != null)
			return lastKey;
		else
			throw new IllegalStateException("Last key does not exist!");
	}

	private enum Op {
		INDEX, CALL, COL_CALL
	}
}

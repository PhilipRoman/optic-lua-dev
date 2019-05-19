package optic.lua.asm;

import optic.lua.messages.CompilationFailure;
import optic.lua.optimization.ProvenType;
import optic.lua.util.Trees;
import org.antlr.runtime.tree.*;
import org.jetbrains.annotations.Contract;

import java.util.*;

import static nl.bigo.luaparser.Lua53Lexer.*;
import static optic.lua.util.Trees.childrenOf;

final class ChainedAccessBuilder {
	private RValue current;
	private RValue self;
	private RValue lastKey = null;
	private Op lastOp = null;
	private final List<Step> steps = new ArrayList<>(4);
	private final Flattener flattener;

	ChainedAccessBuilder(Flattener flattener, RValue current) {
		this.flattener = flattener;
		this.current = current;
		this.self = current;
	}

	public void add(Tree tree) throws CompilationFailure {
		var t = (CommonTree) tree;

		if (lastOp == Op.CALL || lastOp == Op.COL_CALL)
			current = current.discardRemaining().applyTo(steps);

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
		Register next = RegisterFactory.create(ProvenType.OBJECT);
		steps.add(StepFactory.tableIndex(current, key.value(), next));
		self = current;
		current = next;
		lastKey = key.value();
		lastOp = Op.INDEX;
	}

	private void addCall(CommonTree tree, boolean colon) throws CompilationFailure {
		Trees.expect(colon ? COL_CALL : CALL, tree);
		List<RValue> args = new ArrayList<>();
		if (colon)
			args.add(self);
		for (var child : childrenOf(tree))
			args.add(flattener.flattenExpression((CommonTree) child).applyTo(steps));
		normalizeValueList(args);
		self = current = RValue.invocation(current, InvocationMethod.CALL, args);
		lastKey = null;
		lastOp = colon ? Op.COL_CALL : Op.CALL;
	}

	List<Step> buildStatement() {
		if (lastOp == Op.CALL || lastOp == Op.COL_CALL) {
			var list = new ArrayList<>(steps);
			list.add(StepFactory.discard((RValue.Invocation) current));
			return list;
		}
		return Collections.unmodifiableList(steps);
	}

	FlatExpr buildExpression() {
		return new FlatExpr(steps, current);
	}

	RValue getSelf() {
		return self;
	}

	RValue getLastIndexKey() {
		if (lastKey != null)
			return lastKey;
		else
			throw new IllegalStateException("Last key does not exist!");
	}

	@Contract(mutates = "this, param1")
	private void normalizeValueList(List<RValue> values) {
		int valueCount = values.size();
		for (int i = 0; i < valueCount - 1; i++) {
			values.set(i, discardRemaining(values.get(i)));
		}
	}

	@Contract(mutates = "this")
	private RValue discardRemaining(RValue vararg) {
		if (vararg.isVararg()) {
			Register r = RegisterFactory.create(ProvenType.OBJECT);
			steps.add(StepFactory.select(r, vararg, 0));
			return r;
		}
		return vararg;
	}

	private enum Op {
		INDEX, CALL, COL_CALL
	}
}

package optic.lua.asm;

import nl.bigo.luaparser.Lua53Walker;
import optic.lua.messages.CompilationFailure;
import optic.lua.optimization.ProvenType;
import optic.lua.util.Trees;
import static optic.lua.util.Trees.childrenOf;
import org.antlr.runtime.tree.*;
import org.jetbrains.annotations.*;

import java.util.*;

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

		if(lastOp == Op.CALL || lastOp == Op.COL_CALL)
			current = current.discardRemaining().applyTo(steps);

		switch(t.getType()) {
			case Lua53Walker.INDEX:
				addIndex(t);
				break;
			case Lua53Walker.CALL:
				addCall(t);
				break;
			case Lua53Walker.COL_CALL:
				addColCall(t);
				break;
			default:
				throw new IllegalArgumentException(Trees.reverseLookupName(tree.getType()));
		}
	}

	private void addIndex(CommonTree tree) throws CompilationFailure {
		Trees.expect(Lua53Walker.INDEX, tree);
		var key = flattener.flattenExpression((CommonTree) tree.getChild(0));
		steps.addAll(key.block());
		Register next = RegisterFactory.create(ProvenType.OBJECT);
		steps.add(StepFactory.tableIndex(current, key.value(), next));
		self = current;
		current = next;
		lastKey = key.value();
		lastOp = Op.INDEX;
	}

	private void addCall(CommonTree tree) throws CompilationFailure {
		Trees.expect(Lua53Walker.CALL, tree);
		List<RValue> args = new ArrayList<>();
		for(var child : childrenOf(tree))
			args.add(flattener.flattenExpression((CommonTree) child).applyTo(steps));
		self = current = RValue.invocation(current, InvocationMethod.CALL, args);
		lastKey = null;
		lastOp = Op.CALL;
	}

	private void addColCall(CommonTree tree) throws CompilationFailure {
		Trees.expect(Lua53Walker.COL_CALL, tree);
		List<RValue> args = new ArrayList<>();
		args.add(self);
		for(var child : childrenOf(tree))
			args.add(flattener.flattenExpression((CommonTree) child).applyTo(steps));
		self = current = RValue.invocation(current, InvocationMethod.CALL, args);
		lastKey = null;
		lastOp = Op.COL_CALL;
	}

	public List<Step> buildStatement() {
		if(lastOp == Op.CALL || lastOp == Op.COL_CALL) {
			var list = new ArrayList<Step>(steps);
			list.add(StepFactory.discard((RValue.Invocation) current));
			return list;
		}
		return Collections.unmodifiableList(steps);
	}

	public FlatExpr buildExpression() {
		return new FlatExpr(steps, current);
	}

	public RValue getSelf() {
		return self;
	}

	public RValue getLastIndexKey() {
		if(lastKey != null)
			return lastKey;
		else
			throw new IllegalStateException("Last key does not exist!");
	}

	@Contract(mutates = "this")
	private List<RValue> normalizeValueList(List<RValue> registers) {
		var values = new ArrayList<RValue>(registers.size());
		int valueIndex = 0;
		int valueCount = registers.size();
		for (var register : registers) {
			boolean isLastValue = valueIndex == valueCount - 1;
			if (isLastValue) {
				values.add(register);
			} else {
				values.add(discardRemaining(register));
			}
			valueIndex++;
		}
		return values;
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

	private static enum Op {
		INDEX, CALL, COL_CALL
	}
}

package optic.lua.asm;

import nl.bigo.luaparser.Lua52Walker;
import optic.lua.messages.CompilationFailure;
import optic.lua.util.Trees;
import org.antlr.runtime.tree.*;

import java.util.*;

final class NestedFieldBuilder {
	private final RValue start;
	private final List<FlatExpr> keys = new ArrayList<>(4);
	private final Flattener flattener;

	NestedFieldBuilder(Flattener flattener, RValue start) {
		this.flattener = flattener;
		this.start = start;
	}

	public void add(Tree t) throws CompilationFailure {
		var tree = (CommonTree) Trees.expect(Lua52Walker.INDEX, t);
		var expr = flattener.flattenExpression((CommonTree) tree.getChild(0));
		keys.add(expr);
	}

	public FlatExpr build() {
		List<Step> steps = new ArrayList<>();
		RValue table = start;
		for(var key : keys) {
			steps.addAll(key.block());
			Register next = RegisterFactory.create();
			steps.add(StepFactory.tableIndex(table, key.value(), next));
			table = next;
		}
		return new FlatExpr(steps, table);
	}
}

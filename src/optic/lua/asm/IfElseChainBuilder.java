package optic.lua.asm;

import nl.bigo.luaparser.Lua53Walker;
import optic.lua.messages.CompilationFailure;
import optic.lua.util.Trees;
import org.antlr.runtime.tree.*;

import java.util.*;

final class IfElseChainBuilder {
	private final List<Conditional> chain = new ArrayList<>(4);
	private final Flattener flattener;

	IfElseChainBuilder(Flattener flattener) {
		this.flattener = flattener;
	}

	public void add(Tree tree) throws CompilationFailure {
		Trees.expect(Lua53Walker.CONDITION, tree);
		assert tree.getChildCount() == 2 || tree.getChildCount() == 3;
		FlatExpr condition = flattener.flattenExpression((CommonTree) tree.getChild(0)).discardRemaining();
		AsmBlock body = flattener.flattenBlock((CommonTree) Trees.expectChild(Lua53Walker.CHUNK, tree, 1), BlockMeaning.IF_BODY);
		chain.add(new Conditional(condition, body));
	}

	LinkedHashMap<FlatExpr, AsmBlock> build() {
		var map = new LinkedHashMap<FlatExpr, AsmBlock>(chain.size());
		for (var c : chain) {
			map.put(c.condition, c.thenBlock);
		}
		return map;
	}

	@Override
	public String toString() {
		return chain.toString();
	}

	private static class Conditional {
		private final FlatExpr condition;
		private final AsmBlock thenBlock;

		private Conditional(FlatExpr condition, AsmBlock thenBlock) {
			this.condition = condition;
			this.thenBlock = thenBlock;
		}
	}
}

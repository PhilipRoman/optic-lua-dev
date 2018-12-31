package optic.lua.asm;

import nl.bigo.luaparser.Lua52Walker;
import optic.lua.messages.CompilationFailure;
import optic.lua.util.Trees;
import org.antlr.runtime.tree.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class IfElseChainBuilder {
	private final List<Conditional> chain = new ArrayList<>(4);
	private final Flattener flattener;

	IfElseChainBuilder(Flattener flattener) {
		this.flattener = flattener;
	}

	public void add(Tree tree) throws CompilationFailure {
		Trees.expect(Lua52Walker.CONDITION, tree);
		assert tree.getChildCount() == 2 || tree.getChildCount() == 3;
		FlatExpr condition = flattener.flattenExpression((CommonTree) tree.getChild(0));
		AsmBlock body = flattener.flattenBlock((CommonTree) Trees.expectChild(Lua52Walker.CHUNK, tree, 1));
		final AsmBlock elseBlock;
		if (tree.getChildCount() == 3) {
			elseBlock = flattener.flattenBlock((CommonTree) Trees.expectChild(Lua52Walker.CHUNK, tree, 2));
		} else {
			elseBlock = null;
		}
		chain.add(new Conditional(condition, body, elseBlock));
	}

	Map<FlatExpr, AsmBlock> build() {
		var map = new LinkedHashMap<FlatExpr, AsmBlock>(chain.size());
		for(var c : chain) {
			map.put(c.condition, c.thenBlock);
		}
		return map;
	}

	private static class Conditional {
		private final FlatExpr condition;
		private final AsmBlock thenBlock;
		private final AsmBlock elseBlock;

		private Conditional(FlatExpr condition, AsmBlock thenBlock, @Nullable AsmBlock elseBlock) {
			this.condition = condition;
			this.thenBlock = thenBlock;
			this.elseBlock = elseBlock;
		}
	}

	@Override
	public String toString() {
		return chain.toString();
	}
}

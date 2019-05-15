package optic.lua.asm;

import optic.lua.messages.CompilationFailure;
import org.antlr.runtime.tree.*;

import java.util.List;

interface Flattener {
	AsmBlock flatten(CommonTree tree, List<VariableInfo> locals, BlockMeaning meaning) throws CompilationFailure;

	FlatExpr flattenExpression(CommonTree tree) throws CompilationFailure;

	/**
	 * Convenience overload for {@link #flattenExpression(CommonTree)}
	 */
	default FlatExpr flattenExpression(Tree tree) throws CompilationFailure {
		return flattenExpression((CommonTree) tree);
	}

	default AsmBlock flattenBlock(CommonTree tree, BlockMeaning meaning) throws CompilationFailure {
		return flatten(tree, List.of(), meaning);
	}
}

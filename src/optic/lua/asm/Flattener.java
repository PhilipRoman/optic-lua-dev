package optic.lua.asm;

import optic.lua.messages.CompilationFailure;
import org.antlr.runtime.tree.*;

import java.util.List;

public interface Flattener {
	AsmBlock flatten(CommonTree tree, boolean boundary, List<VariableInfo> locals) throws CompilationFailure;

	FlatExpr flattenExpression(CommonTree tree) throws CompilationFailure;

	/**
	 * Convenience overload for {@link #flattenExpression(CommonTree)}
	 */
	default FlatExpr flattenExpression(Tree tree) throws CompilationFailure {
		return flattenExpression((CommonTree) tree);
	}

	default AsmBlock flattenBlock(CommonTree tree) throws CompilationFailure {
		return flatten(tree, false, List.of());
	}
}

package optic.lua.asm;

import optic.lua.messages.CompilationFailure;
import org.antlr.runtime.tree.CommonTree;

import java.util.List;

public interface Flattener {
	AsmBlock flatten(CommonTree tree, boolean boundary, List<VariableInfo> locals) throws CompilationFailure;

	FlatExpr flattenExpression(CommonTree tree) throws CompilationFailure;

	default AsmBlock flattenBlock(CommonTree tree) throws CompilationFailure {
		return flatten(tree, false, List.of());
	}
}

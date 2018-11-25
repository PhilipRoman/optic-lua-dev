package optic.lua.asm;

import optic.lua.messages.*;
import org.antlr.runtime.tree.CommonTree;

@FunctionalInterface
public interface SyntaxTreeFlattener {
	AsmBlock flatten(CommonTree tree, MessageReporter reporter) throws CompilationFailure;
}

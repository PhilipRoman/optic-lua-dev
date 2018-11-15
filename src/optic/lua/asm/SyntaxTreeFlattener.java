package optic.lua.asm;

import optic.lua.messages.*;
import org.antlr.runtime.tree.CommonTree;

import java.util.List;

@FunctionalInterface
public interface SyntaxTreeFlattener {
	List<Step> flatten(CommonTree tree, MessageReporter reporter) throws CompilationFailure;
}

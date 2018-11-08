package optic.lua.ssa;

import optic.lua.messages.*;
import org.antlr.runtime.tree.CommonTree;

import java.util.List;

@FunctionalInterface
public interface SSATranslator {
	List<Step> translate(CommonTree tree, MessageReporter reporter) throws CompilationFailure;
}

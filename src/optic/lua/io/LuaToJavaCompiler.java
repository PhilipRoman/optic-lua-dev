package optic.lua.io;

import optic.lua.asm.*;
import optic.lua.codegen.java.JavaCodeOutput;
import optic.lua.messages.*;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.tree.CommonTree;

final class LuaToJavaCompiler {
	String compile(String lua, Options options) throws CompilationFailure {
		CommonTree ast = new JavaParser().parse(new ANTLRStringStream(lua));
		AsmBlock asm = MutableFlattener.flatten(ast, options);
		return new JavaCodeOutput(options).generate(asm);
	}
}

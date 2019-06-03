package optic.lua.io;

import optic.lua.asm.*;
import optic.lua.codegen.java.JavaCodeOutput;
import optic.lua.messages.*;
import optic.lua.util.Trees;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.tree.CommonTree;

import java.io.PrintStream;

final class LuaToJavaCompiler {
	LuaToJavaCompiler() {
	}

	private static void dumpAstRecursive(CommonTree tree, int depth) {
		PrintStream out = System.out;
		out.println("|  ".repeat(depth) + tree.toString());
		for (Object o : Trees.childrenOf(tree)) {
			if (o instanceof CommonTree)
				dumpAstRecursive(((CommonTree) o), depth + 1);
			else
				out.println("|  ".repeat(depth) + o);
		}
	}

	String compile(String lua, String className, Options options) throws CompilationFailure {
		CommonTree ast = new LuaSourceParser().parse(new ANTLRStringStream(lua));
		if (options.get(StandardFlags.DUMP_AST)) {
			dumpAstRecursive(ast, 0);
		}
		AsmBlock asm = MutableFlattener.flatten(ast, options);
		return new JavaCodeOutput(options).generate(className, asm);
	}
}

package optic.lua.io;

import optic.lua.codegen.java.JavaCodeOutput;
import optic.lua.messages.CompilationFailure;
import optic.lua.runtime.LuaContext;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ScriptEvaluator;
import org.slf4j.*;

import java.util.Scanner;

class JaninoCompilerBase {
	static final String GENERATED_CLASS_NAME = "LuaSource";
	static final String MAIN_METHOD_NAME = "mainChunk";
	private static final Logger log = LoggerFactory.getLogger(JaninoCompilerBase.class);

	static void cookInto(ScriptEvaluator evaluator, String java) throws CompilationFailure {
		evaluator.setDebuggingInformation(true, true, true);
		evaluator.setReturnType(Object[].class);
		evaluator.setClassName(GENERATED_CLASS_NAME);
		evaluator.setMethodName(MAIN_METHOD_NAME);
		evaluator.setParameters(new String[]{
				JavaCodeOutput.INJECTED_CONTEXT_PARAM_NAME,
				JavaCodeOutput.INJECTED_ARGS_PARAM_NAME
		}, new Class[]{
				LuaContext.class,
				Object[].class
		});
		try {
			evaluator.cook(java);
		} catch (CompileException e) {
			logCompilationError(java, e);
			throw new CompilationFailure();
		}
	}

	private static void logCompilationError(String source, CompileException e) {
		if (!log.isErrorEnabled())
			return;
		boolean hasLocation = e.getLocation() != null;
		String sourceInfo = hasLocation ? ", source code: " + findLine(source, e.getLocation().getLineNumber()) : "";
		String lineInfo = hasLocation ? " (line " + e.getLocation().getLineNumber() + ")" : "";
		log.error("Compilation error" + lineInfo + " " + sourceInfo, e);
	}

	private static String findLine(String source, int line) {
		Scanner scanner = new Scanner(source);
		for (int i = 1; i < line; i++) {
			scanner.nextLine();
		}
		return scanner.nextLine();
	}

}

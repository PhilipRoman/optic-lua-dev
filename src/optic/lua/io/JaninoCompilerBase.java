package optic.lua.io;

import optic.lua.messages.CompilationFailure;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.SimpleCompiler;
import org.slf4j.*;

import java.util.Scanner;

final class JaninoCompilerBase {
	private static final Logger log = LoggerFactory.getLogger(JaninoCompilerBase.class);

	static void cookInto(SimpleCompiler evaluator, String java) throws CompilationFailure {
		evaluator.setDebuggingInformation(true, true, true);
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

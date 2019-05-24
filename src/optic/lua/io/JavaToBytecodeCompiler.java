package optic.lua.io;

import optic.lua.messages.CompilationFailure;
import org.slf4j.*;

import java.util.Map;


public final class JavaToBytecodeCompiler {
	private static final Logger log = LoggerFactory.getLogger(JavaToBytecodeCompiler.class);

	public Map<String, byte[]> compile(String java) throws CompilationFailure {
		var evaluator = new BytecodeStealingEvaluator();
		JaninoCompilerBase.cookInto(evaluator, java);
		return evaluator.getBytecodes();
	}
}

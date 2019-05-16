package optic.lua.io;

import optic.lua.messages.CompilationFailure;
import org.slf4j.*;


public final class JavaToBytecodeCompiler {
	private static final Logger log = LoggerFactory.getLogger(JavaToBytecodeCompiler.class);

	public byte[] compile(String java) throws CompilationFailure {
		var evaluator = new BytecodeStealingEvaluator();
		JaninoCompilerBase.cookInto(evaluator, java);
		var resultMap = evaluator.getBytecodes();
		assert resultMap.size() == 1;
		return resultMap.values().iterator().next();
	}
}

package optic.lua.io;

import optic.lua.messages.CompilationFailure;

import java.util.Map;


final class JavaToBytecodeCompiler {
	public JavaToBytecodeCompiler() {
	}

	Map<String, byte[]> compile(String java) throws CompilationFailure {
		var evaluator = new BytecodeStealingEvaluator();
		new JaninoCompilerBase().cookInto(evaluator, java);
		return evaluator.getBytecodes();
	}
}

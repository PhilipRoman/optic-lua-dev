package optic.lua.io;

import optic.lua.messages.CompilationFailure;
import org.codehaus.janino.ScriptEvaluator;

import java.lang.reflect.Method;
import java.util.Objects;

final class JavaToMethodCompiler {
	public Method compile(String source) throws CompilationFailure {
		var evaluator = new ScriptEvaluator();
		JaninoCompilerBase.cookInto(evaluator, source);
		return Objects.requireNonNull(evaluator.getMethod());
	}
}

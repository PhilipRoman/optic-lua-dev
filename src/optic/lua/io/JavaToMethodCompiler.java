package optic.lua.io;

import optic.lua.messages.CompilationFailure;
import optic.lua.runtime.LuaContext;
import org.codehaus.janino.SimpleCompiler;

import java.lang.reflect.Method;
import java.util.Objects;

final class JavaToMethodCompiler {
	public Method compile(String source, String className) throws CompilationFailure {
		Objects.requireNonNull(source);
		Objects.requireNonNull(className);
		var evaluator = new SimpleCompiler();
		JaninoCompilerBase.cookInto(evaluator, source);
		try {
			return evaluator.getClassLoader().loadClass(className).getMethod("run", LuaContext.class, Object[].class);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Could not find method run(LuaContext, Object[])");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Could not find class " + className);
		}
	}
}

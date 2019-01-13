package optic.lua.files;

import optic.lua.codegen.java.JavaCodeOutput;
import optic.lua.messages.*;
import optic.lua.runtime.LuaContext;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ScriptEvaluator;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class Compiler {
	private final MessageReporter reporter;

	public Compiler(MessageReporter reporter) {
		this.reporter = reporter;
	}

	public void run(InputStream input, int nTimes) {
		run(input, nTimes, LuaContext.create(), List.of());
	}

	public void run(InputStream input, int nTimes, LuaContext context, List<Object> args) {
		var evaluator = compile(new BufferedInputStream(input));
		for (int i = 0; i < nTimes; i++) {
			measure(evaluator, context, args);
		}
	}

	private ScriptEvaluator compile(InputStream input) {
		var evaluator = new ScriptEvaluator();
		evaluator.setParameters(new String[]{
				JavaCodeOutput.INJECTED_CONTEXT_PARAM_NAME,
				JavaCodeOutput.INJECTED_ARGS_PARAM_NAME
		}, new Class[]{
				LuaContext.class,
				Object[].class
		});
		try {
			evaluator.cook(input);
		} catch (CompileException | IOException e) {
			throw new RuntimeException(e);
		}
		return evaluator;
	}

	private void measure(ScriptEvaluator evaluator, LuaContext context, List<Object> args) {
		long start = System.nanoTime();
		try {
			evaluator.evaluate(new Object[]{context, args.toArray()});
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}
		reporter.report(durationInfo(System.nanoTime() - start));
	}

	private Message durationInfo(long nanos) {
		var error = Message.create("Program took " + (nanos / (int) 1e6) + " ms");
		error.setLevel(Level.INFO);
		error.setPhase(Phase.RUNTIME);
		return error;
	}
}

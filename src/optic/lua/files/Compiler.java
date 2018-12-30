package optic.lua.files;

import optic.lua.messages.*;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ScriptEvaluator;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

public class Compiler {
	private final MessageReporter reporter;

	public Compiler(MessageReporter reporter) {
		this.reporter = reporter;
	}

	public void run(InputStream input, int nTimes) {
		var evaluator = compile(new BufferedInputStream(input));
		for (int i = 0; i < nTimes; i++) {
			measure(evaluator);
		}
	}

	private ScriptEvaluator compile(InputStream input) {
		var evaluator = new ScriptEvaluator();
		try {
			evaluator.cook(input);
		} catch (CompileException | IOException e) {
			throw new RuntimeException(e);
		}
		return evaluator;
	}

	private void measure(ScriptEvaluator evaluator) {
		long start = System.nanoTime();
		try {
			evaluator.evaluate(new Object[0]);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause().getMessage());
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

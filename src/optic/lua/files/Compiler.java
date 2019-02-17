package optic.lua.files;

import optic.lua.codegen.java.JavaCodeOutput;
import optic.lua.messages.*;
import optic.lua.runtime.LuaContext;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ScriptEvaluator;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.*;
import java.util.*;

public class Compiler {
	private final Context context;

	public Compiler(Context context) {
		this.context = context;
	}

	public void run(InputStream input, int nTimes) throws CompilationFailure {
		run(input, nTimes, LuaContext.create(), List.of());
	}

	public void run(InputStream input, int nTimes, LuaContext luaContext, List<Object> args) throws CompilationFailure {
		final ScriptEvaluator evaluator;
		final byte[] data;
		try {
			data = input.readAllBytes();
		} catch (IOException e) {
			context.reporter().report(ioError(e));
			return;
		}
		try {
			evaluator = compile(data);
		} catch (CompileException e) {
			context.reporter().report(compilationError(data, e));
			if (context.options().get(StandardFlags.DUMP_ON_INTERNAL_ERROR)) {
				try {
					var debugFile = Files.createTempFile(Paths.get(""), "GENERATED_SOURCE_", ".java");
					Files.write(debugFile, data);
				} catch (IOException e1) {
					throw new UncheckedIOException("IOException during debug data dump", e1);
				}
			}
			throw new CompilationFailure();
		}
		for (int i = 0; i < nTimes; i++) {
			measure(evaluator, luaContext, args);
		}
	}

	private ScriptEvaluator compile(byte[] source) throws CompileException {
		var evaluator = new ScriptEvaluator();
		evaluator.setParameters(new String[]{
				JavaCodeOutput.INJECTED_CONTEXT_PARAM_NAME,
				JavaCodeOutput.INJECTED_ARGS_PARAM_NAME
		}, new Class[]{
				LuaContext.class,
				Object[].class
		});
		try {
			evaluator.cook(new ByteArrayInputStream(source));
		} catch (IOException e) {
			// we're cooking a ByteArrayInputStream, no errors should occur here
			throw new AssertionError();
		}
		return evaluator;
	}

	private void measure(ScriptEvaluator evaluator, LuaContext luaContext, List<Object> args) {
		long start = System.nanoTime();
		try {
			evaluator.evaluate(new Object[]{luaContext, args.toArray()});
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof Error) {
				throw (Error) e.getCause();
			}
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			}
			throw new RuntimeException(e.getCause());
		}
		context.reporter().report(durationInfo(System.nanoTime() - start));
	}

	private Message durationInfo(long nanos) {
		var msg = Message.create("Program took " + (nanos / (int) 1e6) + " ms");
		msg.setLevel(Level.INFO);
		msg.setPhase(Phase.RUNTIME);
		return msg;
	}

	private Message ioError(IOException e) {
		var msg = Message.create("IOException: " + e.getMessage());
		msg.setCause(e);
		msg.setPhase(Phase.RUNTIME);
		msg.setLevel(Level.ERROR);
		return msg;
	}

	private Message compilationError(byte[] source, CompileException e) {
		int line = e.getLocation().getLineNumber();
		Scanner scanner = new Scanner(new ByteArrayInputStream(source));
		for (int i = 1; i < line; i++) {
			scanner.nextLine();
		}
		String sourceCode = scanner.nextLine();
		var msg = Message.create("Compilation error (line " + line + "), source code: >>>>> " + sourceCode + " <<<<< ");
		msg.setCause(e);
		msg.setPhase(Phase.COMPILING);
		msg.setLevel(Level.ERROR);
		return msg;
	}
}

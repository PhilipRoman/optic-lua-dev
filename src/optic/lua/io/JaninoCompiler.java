package optic.lua.io;

import optic.lua.codegen.java.JavaCodeOutput;
import optic.lua.messages.*;
import optic.lua.runtime.LuaContext;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ScriptEvaluator;
import org.slf4j.*;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;

final class JaninoCompiler {
	private static final Logger log = LoggerFactory.getLogger(JaninoCompiler.class);

	public static final String GENERATED_CLASS_NAME = "LuaSource";
	public static final String GENERATED_METHOD_NAME = "mainChunk";
	private static final WeakHashMap<ByteBuffer, ScriptEvaluator> scriptCache = new WeakHashMap<>();
	private static final int MAX_CACHED_SIZE = 4096;
	private final Options options;
	private final boolean useCache;

	public JaninoCompiler(Options options) {
		this.options = options;
		this.useCache = options.get(StandardFlags.CACHE_JAVA_COMPILING);
	}

	private static String findLine(byte[] source, int line) {
		Scanner scanner = new Scanner(new ByteArrayInputStream(source));
		for (int i = 1; i < line; i++) {
			scanner.nextLine();
		}
		return scanner.nextLine();
	}

	public Method compile(byte[] data) throws CompilationFailure {
		final ScriptEvaluator evaluator;
		try {
			evaluator = createEvaluator(data);
		} catch (CompileException e) {
			logCompilationError(data, e);
			throw new CompilationFailure();
		}
		return evaluator.getMethod();
	}

	public Object[] run(InputStream input, int nTimes, LuaContext luaContext, List<Object> args) throws CompilationFailure {
		final byte[] data;
		Objects.requireNonNull(input);
		try {
			data = input.readAllBytes();
		} catch (IOException e) {
			log.error("IO error while reading from input stream (" + input.getClass() + ")", e);
			throw new CompilationFailure();
		}

		if (options.get(StandardFlags.DUMP_JAVA)) {
			try {
				var debugFile = Files.createTempFile(Paths.get(""), "dump_", ".out");
				Files.write(debugFile, data);
			} catch (IOException e1) {
				throw new UncheckedIOException("IOException during debug data dump", e1);
			}
		}

		var method = compile(data);
		Object[] result = null;
		var runner = new Runner(options);
		for (int i = 0; i < nTimes; i++) {
			result = runner.run(method, luaContext, args);
		}
		return Objects.requireNonNull(result);
	}

	private ScriptEvaluator createEvaluator(byte[] source) throws CompileException {
		if (useCache && source.length <= MAX_CACHED_SIZE && scriptCache.containsKey(ByteBuffer.wrap(source))) {
			return scriptCache.get(ByteBuffer.wrap(source));
		}
		var evaluator = new ScriptEvaluator();
		evaluator.setDebuggingInformation(true, true, true);
		evaluator.setReturnType(Object[].class);
		evaluator.setClassName(GENERATED_CLASS_NAME);
		evaluator.setMethodName(GENERATED_METHOD_NAME);
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
		if (useCache && source.length <= MAX_CACHED_SIZE) {
			scriptCache.put(ByteBuffer.wrap(source), evaluator);
		}
		return evaluator;
	}

	private void logCompilationError(byte[] source, CompileException e) {
		if (!log.isErrorEnabled())
			return;
		boolean hasLocation = e.getLocation() != null;
		String sourceInfo = hasLocation ? ", source code: " + findLine(source, e.getLocation().getLineNumber()) : "";
		String lineInfo = hasLocation ? " (line " + e.getLocation().getLineNumber() + ")" : "";
		log.error("Compilation error" + lineInfo + " " + sourceInfo, e);
	}
}

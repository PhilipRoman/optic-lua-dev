package optic.lua.io;

import optic.lua.codegen.java.JavaCodeOutput;
import optic.lua.messages.*;
import optic.lua.runtime.*;

import java.io.*;
import java.util.*;

public class InteractiveShell {
	private final Reader in;
	private final PrintWriter out;
	private final PrintWriter err;
	private final Bundle bundle;
	private final WeakHashMap<String, byte[]> scriptCache = new WeakHashMap<>();
	private final MessageReporter reporter = new StandardMessageReporter(System.err)
			.filter(msg -> msg.level().compareTo(Level.WARNING) >= 0);
	private final Options options = new Options();

	{
		options.enable(StandardFlags.CACHE_JAVA_COMPILING);
		options.enable(StandardFlags.CACHE_LUA_COMPILING);
		options.disable(StandardFlags.LOOP_SPLIT);
		options.disable(StandardFlags.PARALLEL);
		options.disable(StandardFlags.VERIFY);
		options.disable(StandardFlags.DEBUG_COMMENTS);
	}

	public InteractiveShell(InputStream in, OutputStream out, OutputStream err, Bundle bundle) {
		this.in = new InputStreamReader(in);
		this.out = new PrintWriter(new OutputStreamWriter(out));
		this.err = new PrintWriter(new OutputStreamWriter(err));
		this.bundle = bundle;
	}

	public void run() {
		LuaContext context = LuaContext.create(bundle);
		context.in = in;
		context.out = out;
		context.err = err;
		var scanner = new Scanner(in);
		out.println(String.format("%s / %s %s",
				context.getGlobal("_VERSION"),
				System.getProperty("java.vm.name"),
				System.getProperty("java.vm.version")
		));
		out.println("Type \"!exit\" to exit; prefix line with \"=\" to evaluate expressions");
		out.flush();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
//			long startTime = System.nanoTime();
			if (line.equals("!exit")) {
				break;
			}
			if (line.startsWith("=")) {
				line = "return " + line.substring(1);
			}
			final Object[] result;
			try {
				result = evaluate(line, context);
			} catch (CompilationFailure failure) {
				continue;
			} catch (RuntimeException e) {
				shortenStackTrace(e);
				reporter.report(Message.createError("Uncaught error", e));
				continue;
			}
			if (result != null && result.length > 0) {
				StandardLibrary.print(out, result);
			}
			out.flush();
//			System.err.println("(" + ((System.nanoTime() - startTime) / 1e6) + "ms)");
		}
		out.println("Exiting!");
		out.flush();
	}

	private Object[] evaluate(String line, LuaContext context) throws CompilationFailure {
		JavaCompiler compiler = new JavaCompiler(new Context(options, reporter));
		ByteArrayInputStream input = new ByteArrayInputStream(compileToJava(line));
		return compiler.run(input, 1, context, List.of());
	}

	private byte[] compileToJava(String script) throws CompilationFailure {
		boolean useCache = options.get(StandardFlags.CACHE_LUA_COMPILING);
		if (useCache && scriptCache.containsKey(script)) {
			return scriptCache.get(script);
		}
		var buffer = new ByteArrayOutputStream(512);
		var pipeline = new SingleSourceCompiler(
				new Context(options, reporter),
				CodeSource.ofString(script, "<stdin>"),
				List.of(JavaCodeOutput.writingTo(buffer))
		);
		pipeline.run();
		byte[] bytes = buffer.toByteArray();
		if (useCache) {
			scriptCache.put(script, bytes);
		}
		return bytes;
	}

	private void shortenStackTrace(RuntimeException e) {
		// the name combination we're looking for
		// any frames after these will be discarded
		final String className = JavaCompiler.GENERATED_CLASS_NAME;
		final String methodName = JavaCompiler.GENERATED_METHOD_NAME;
		var trace = e.getStackTrace();
		for (int i = 0; i < trace.length; i++) {
			var frame = trace[i];
			if (frame.getClassName().equals(className) && frame.getMethodName().equals(methodName)) {
				e.setStackTrace(Arrays.copyOfRange(trace, 0, i + 1));
				return;
			}
		}
		System.out.println("#####");
	}
}

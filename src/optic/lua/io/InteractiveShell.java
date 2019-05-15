package optic.lua.io;

import optic.lua.messages.*;
import optic.lua.runtime.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;

public final class InteractiveShell {
	private static final Logger log = LoggerFactory.getLogger(InteractiveShell.class);
	private final Reader in;
	private final PrintWriter out;
	private final PrintWriter err;
	private final Bundle bundle;
	private final Options options;

	public InteractiveShell(InputStream in, OutputStream out, OutputStream err, Bundle bundle, Options options) {
		this.in = new InputStreamReader(in);
		this.out = new PrintWriter(new OutputStreamWriter(out));
		this.err = new PrintWriter(new OutputStreamWriter(err));
		this.bundle = bundle;
		this.options = options;
	}

	private static void logTimeTakenToCompile(long nanos) {
		long millis = nanos / (long) 1e6;
		log.info("Compiled expression in {}ms", millis);
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
				log.error("Uncaught error", e);
				continue;
			}
			if (result != null && result.length > 0) {
				StandardLibrary.print(out, result);
			}
			out.flush();
			context.resetCallSites();
		}
		out.println("Exiting!");
		out.flush();
	}

	private Object[] evaluate(String line, LuaContext context) throws CompilationFailure {
		long start = System.nanoTime();
		String java = new LuaToJavaCompiler().compile(line, options);
		if (options.get(StandardFlags.SHOW_TIME)) {
			logTimeTakenToCompile(System.nanoTime() - start);
		}
		var method = new JavaToMethodCompiler().compile(java);
		return new Runner(options).run(method, context, List.of());
	}

	/**
	 * Returns the {@link Options} used by this shell.
	 * The returned object may be freely modified; changes will be reflected in the behavior of this shell.
	 */
	public Options options() {
		return options;
	}

	private void shortenStackTrace(RuntimeException e) {
		// the name combination we're looking for
		// any frames after these will be discarded
		final String className = JaninoCompilerBase.GENERATED_CLASS_NAME;
		final String methodName = JaninoCompilerBase.MAIN_METHOD_NAME;
		var trace = e.getStackTrace();
		for (int i = 0; i < trace.length; i++) {
			var frame = trace[i];
			if (frame.getClassName().equals(className) && frame.getMethodName().equals(methodName)) {
				e.setStackTrace(Arrays.copyOfRange(trace, 0, i + 1));
				return;
			}
		}
	}
}

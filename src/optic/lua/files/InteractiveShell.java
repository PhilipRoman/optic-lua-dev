package optic.lua.files;

import optic.lua.*;
import optic.lua.codegen.java.JavaCodeOutput;
import optic.lua.messages.*;
import optic.lua.runtime.*;

import java.io.*;
import java.util.*;

public class InteractiveShell {
	private final Reader in;
	private final PrintWriter out;
	private final PrintWriter err;
	private final WeakHashMap<String, byte[]> scriptCache = new WeakHashMap<>();
	private final MessageReporter reporter = new StandardMessageReporter(System.err).filter(msg -> msg.level() == Level.ERROR);
	private final Options options = new Options();

	{
		options.enable(StandardFlags.CACHE_JAVA_COMPILING);
		options.enable(StandardFlags.CACHE_LUA_COMPILING);
		options.disable(StandardFlags.LOOP_SPLIT);
		options.disable(StandardFlags.PARALLEL);
		options.disable(StandardFlags.VERIFY);
		options.disable(StandardFlags.DEBUG_COMMENTS);
	}

	public InteractiveShell(InputStream in, OutputStream out, OutputStream err) {
		this.in = new InputStreamReader(in);
		this.out = new PrintWriter(new OutputStreamWriter(out));
		this.err = new PrintWriter(new OutputStreamWriter(err));
	}

	public void run() {
		LuaContext context = LuaContext.create();
		context.in = in;
		context.out = out;
		context.err = err;
		var scanner = new Scanner(in);
		out.println(context.getGlobal("_VERSION") + " (interactive shell):");
		out.flush();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			long startTime = System.nanoTime();
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
				failure.printStackTrace();
				continue;
			}
			if (result != null && result.length > 0) {
				StandardLibrary.print(out, result);
			}
			out.flush();
			System.err.println("(" + ((System.nanoTime() - startTime) / 1e6) + "ms)");
		}
		out.println("Exiting!");
		out.flush();
	}

	private Object[] evaluate(String line, LuaContext context) throws CompilationFailure {
		return new Compiler(new Context(options, reporter)).run(
				new ByteArrayInputStream(compileToJava(line)),
				1,
				context,
				List.of()
		);
	}

	private byte[] compileToJava(String script) throws CompilationFailure {
		boolean useCache = options.get(StandardFlags.CACHE_LUA_COMPILING);
		if (useCache && scriptCache.containsKey(script)) {
			return scriptCache.get(script);
		}
		var pipeline = new Pipeline(
				options,
				reporter,
				CodeSource.ofString(script)
		);
		var javaSourceOutput = new ByteArrayOutputStream(512);
		pipeline.registerPlugin(JavaCodeOutput.writingTo(javaSourceOutput));
		pipeline.run();
		byte[] bytes = javaSourceOutput.toByteArray();
		if (useCache) {
			scriptCache.put(script, bytes);
		}
		return bytes;
	}
}

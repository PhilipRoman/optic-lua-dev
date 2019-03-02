package optic.lua.io;

import optic.lua.*;
import optic.lua.codegen.java.JavaCodeOutput;
import optic.lua.messages.*;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;

public class BundleCompiler {
	private final Context context;

	public BundleCompiler(Context context) {
		this.context = context;
	}

	public Bundle compile(Collection<Path> paths) throws CompilationFailure {
		long start = System.nanoTime();
		Map<Path, Method> map = new HashMap<>(paths.size());
		for (Path path : paths) {
			map.put(path, compile(path));
		}
		long nanos = System.nanoTime() - start;
		context.reporter().report(compiledFiles(paths.size(), nanos));
		return new Bundle(map);
	}

	private static Message compiledFiles(int numberOfFiles, long nanos) {
		long millis = nanos / (long) 1e6;
		var msg = Message.createInfo("Compiled " + numberOfFiles + " files in " + millis + "ms");
		msg.setPhase(Phase.COMPILING);
		return msg;
	}

	private Method compile(Path path) throws CompilationFailure {
		var pipeline = new Pipeline(
				context.options(),
				context.reporter(),
				CodeSource.ofFile(path.toString())
		);
		var buffer = new ByteArrayOutputStream();
		pipeline.registerPlugin(JavaCodeOutput.writingTo(buffer));
		try {
			pipeline.run();
		} catch (CompilationFailure e) {
			System.err.print("Failed!");
			e.printStackTrace();
			System.exit(1);
		}
		return new Compiler(context).compile(buffer.toByteArray());
	}
}

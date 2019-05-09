package optic.lua.io;

import optic.lua.codegen.java.JavaCodeOutput;
import optic.lua.messages.*;
import org.slf4j.*;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.*;

public final class BundleCompiler {
	private static final Logger log = LoggerFactory.getLogger(BundleCompiler.class);

	private final Options options;

	public BundleCompiler(Options options) {
		this.options = options;
	}

	private static void logTimeTaken(int numberOfFiles, long nanos) {
		long millis = nanos / (long) 1e6;
		log.info("Compiled {} files in {}ms", numberOfFiles, millis);
	}

	public Bundle compile(Collection<Path> paths) throws CompilationFailure {
		long start = System.nanoTime();
		Map<Path, Method> map = new HashMap<>(paths.size());
		for (Path path : paths) {
			map.put(path, compileFile(path));
		}
		long nanos = System.nanoTime() - start;
		if (options.get(StandardFlags.SHOW_TIME))
			logTimeTaken(paths.size(), nanos);
		return new Bundle(map);
	}

	private Method compileFile(Path path) throws CompilationFailure {
		var javaBuffer = new ByteArrayOutputStream();
		var pipeline = new SingleSourceCompiler(
				options,
				CodeSource.ofFile(path.toString()),
				List.of(JavaCodeOutput.writingTo(javaBuffer))
		);
		try {
			pipeline.run();
		} catch (CompilationFailure e) {
			System.err.print("Failed!");
			e.printStackTrace();
			System.exit(1);
		}
		byte[] javaSourceBytes = javaBuffer.toByteArray();

		if (options.get(StandardFlags.DUMP_JAVA)) {
			try {
				var debugFile = Files.createTempFile(Paths.get(""), "GENERATED_SOURCE_", ".java");
				Files.write(debugFile, javaSourceBytes);
			} catch (IOException e1) {
				throw new UncheckedIOException("IOException during debug data dump", e1);
			}
		}

		return new JaninoCompiler(options).compile(javaSourceBytes);
	}
}

package optic.lua.io;

import optic.lua.messages.*;
import org.slf4j.*;

import java.io.IOException;
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
		boolean hasError = false;
		for (Path path : paths) {
			try {
				map.put(path, compileFile(path));
			} catch (CompilationFailure e) {
				log.error("Failed to compile {}", path);
				hasError = true;
			}
		}
		if (hasError)
			throw new CompilationFailure();
		long nanos = System.nanoTime() - start;
		if (options.get(StandardFlags.SHOW_TIME))
			logTimeTaken(paths.size(), nanos);
		return new Bundle(map);
	}

	private Method compileFile(Path path) throws CompilationFailure {
		final String lua;
		try {
			lua = Files.readString(path);
		} catch (IOException e) {
			log.error("Couldn't read Lua file", e);
			throw new CompilationFailure();
		}
		String className = FilePathCodec.encode(path.toString());
		String java = new LuaToJavaCompiler().compile(lua, className, options);
		if (options.get(StandardFlags.DUMP_JAVA)) {
			System.out.println(java);
		}

		return new JavaToMethodCompiler().compile(java, className);
	}
}

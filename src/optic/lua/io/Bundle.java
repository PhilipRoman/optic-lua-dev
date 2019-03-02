package optic.lua.io;

import optic.lua.runtime.LuaContext;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;

public final class Bundle {
	private final Map<String, Method> compiledFiles;

	public static Bundle emptyBundle() {
		return new Bundle(Map.of());
	}

	public Bundle(Map<Path, Method> compiledFiles) {
		var temp = new HashMap<String, Method>(compiledFiles.size());
		compiledFiles.forEach((k, v) -> temp.put(encodePath(k.toString()), v));
		this.compiledFiles = Map.copyOf(temp);
	}

	private static String encodePath(String path) {
		String string = path;
		string = string.replace('\\', '/');
		if (string.startsWith("./")) {
			string = string.substring(2);
		}
		return FilePathCodec.encode(string);
	}

	public Optional<Method> findCompiled(String path) {
		return Optional.ofNullable(compiledFiles.get(encodePath(path)));
	}

	public List<String> listFiles() {
		var list = new ArrayList<String>();
		for (var k : compiledFiles.keySet()) {
			list.add(FilePathCodec.decode(k));
		}
		return list;
	}

	public Object[] doFile(String name, LuaContext context, List<Object> args) {
		var method = findCompiled(name).orElseThrow(() -> new NoSuchElementException(name));
		return new Runner().run(method, context, args);
	}
}

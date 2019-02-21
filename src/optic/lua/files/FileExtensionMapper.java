package optic.lua.files;

import org.jetbrains.annotations.*;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class FileExtensionMapper implements FileMapper {
	private static final Pattern EXTENSION = Pattern.compile("^\\.[a-zA-Z0-9_]+$");
	private static final FileExtensionMapper LUA_TO_JAVA = new FileExtensionMapper(".lua", ".java");
	private final String from;
	private final String to;

	private FileExtensionMapper(String from, String to) {
		this.from = from;
		this.to = to;
	}

	@Contract(pure = true)
	@NotNull
	public static FileExtensionMapper create(String from, String to) {
		if (from.equals(".lua") && to.equals(".java")) {
			return LUA_TO_JAVA;
		}
		if (!EXTENSION.matcher(from).matches()) {
			throw new IllegalArgumentException("file extension should start with a dot!");
		}
		if (!EXTENSION.matcher(to).matches()) {
			throw new IllegalArgumentException("file extension should start with a dot!");
		}
		return new FileExtensionMapper(from, to);
	}

	@Override
	public Path map(Path source) {
		String fileName = source.getFileName().toString();
		if (!fileName.endsWith(from)) {
			return source;
		}
		String name = fileName.substring(0, fileName.length() - from.length()) + to;
		return source.getParent().resolve(name);
	}
}

package optic.lua.io;

import java.nio.file.Path;

public class FileLocationMapper implements FileMapper {
	private final Path from;
	private final Path to;

	public FileLocationMapper(Path from, Path to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public Path map(Path source) {
		return to.resolve(from.relativize(source));
	}
}

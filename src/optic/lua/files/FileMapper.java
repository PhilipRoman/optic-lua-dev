package optic.lua.files;

import java.nio.file.Path;

@FunctionalInterface
public interface FileMapper {
	/**
	 * @param mappers Array of {@link FileMapper FileMappers} to apply in given order
	 * @return the combined function
	 */
	static FileMapper allOf(FileMapper... mappers) {
		return source -> {
			for (var mapper : mappers) {
				source = mapper.map(source);
			}
			return source;
		};
	}

	/**
	 * @param source Path of the original lua file
	 * @return Path to the respective class file
	 */
	Path map(Path source);
}

package optic.lua;

import optic.lua.messages.*;
import optic.lua.util.StringUtils;
import org.antlr.runtime.*;
import org.jetbrains.annotations.*;

import java.nio.file.*;
import java.util.*;

public interface CodeSource {
	/**
	 * @return new character stream from this source
	 * @implNote do <b>NOT</b> return the same stream when called again!
	 * It is expected that all work to obtain the data happens within this method
	 * for purposes such as error reporting, benchmarking, etc.
	 */
	@NotNull
	CharStream newCharStream(Context context) throws CompilationFailure;

	/**
	 * @return a human-friendly, short name which identifies this source. The name
	 * is not expected to change.
	 */
	@NotNull
	String name();

	/**
	 * @return returns the file path associated with this code source, if present
	 */
	Optional<Path> path();

	static CodeSource ofFile(String filePath) {
		var path = Paths.get(filePath).toAbsolutePath();
		return new CodeSourceImpl(path.toString(), () -> new ANTLRFileStream(filePath), path);
	}

	static CodeSource ofString(String programText) {
		var name = '"' + StringUtils.limit(programText, 10, "...") + '"';
		return new CodeSourceImpl(name, () -> new ANTLRStringStream(programText), null);
	}

	static CodeSource custom(String name, CharStreamSupplier streamSupplier) {
		return new CodeSourceImpl(name, streamSupplier, null);
	}

	class CodeSourceImpl implements CodeSource {
		private final String name;
		private final CharStreamSupplier streamSupplier;
		@Nullable
		private final Path path;

		private CodeSourceImpl(String name, CharStreamSupplier supplier, @Nullable Path path) {
			this.name = name;
			streamSupplier = supplier;
			this.path = path;
		}

		@Override
		public String toString() {
			return name();
		}

		@NotNull
		@Override
		public CharStream newCharStream(Context context) throws CompilationFailure {
			final CharStream stream;
			try {
				stream = streamSupplier.get();
			} catch (Exception e) {
				var msg = Message.create("Could not obtain character stream");
				msg.setCause(e);
				msg.setLevel(Level.ERROR);
				context.reporter().report(msg);
				throw new CompilationFailure();
			}
			Objects.requireNonNull(stream);
			return stream;
		}

		@NotNull
		@Override
		public String name() {
			return name;
		}

		@Override
		public Optional<Path> path() {
			return Optional.ofNullable(path);
		}
	}

	@FunctionalInterface
	interface CharStreamSupplier {
		/**
		 * @return new character stream
		 * @implNote do <b>NOT</b> return the same stream when called again!
		 */
		@NotNull
		@Contract("-> new")
		CharStream get() throws Exception;
	}
}

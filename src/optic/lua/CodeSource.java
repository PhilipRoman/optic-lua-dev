package optic.lua;

import optic.lua.messages.*;
import org.antlr.runtime.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.Objects;

public interface CodeSource {
	/**
	 * @return new character stream from this source
	 * @implNote do <b>NOT</b> return the same stream when called again!
	 * It is expected that all work to obtain the data happens within this method
	 * for purposes such as error reporting, benchmarking, etc.
	 */
	@NotNull
	CharStream newCharStream(MessageReporter reporter) throws CompilationFailure;

	/**
	 * @return a human-friendly, short name which identifies this source. The name
	 * is not expected to change.
	 */
	@NotNull
	String name();

	static CodeSource ofFile(String path) throws IOException {
		var name = new File(path).getCanonicalPath();
		return CodeSource.create(name, () -> new ANTLRFileStream(path));
	}

	static CodeSource create(String name, CharStreamSupplier streamSupplier) {
		return new CodeSourceImpl(name, streamSupplier);
	}

	class CodeSourceImpl implements CodeSource {
		private final String name;
		private final CharStreamSupplier streamSupplier;

		private CodeSourceImpl(String name, CharStreamSupplier supplier) {
			this.name = name;
			streamSupplier = supplier;
		}

		@Override
		public String toString() {
			return name();
		}

		@NotNull
		@Override
		public CharStream newCharStream(MessageReporter reporter) throws CompilationFailure {
			final CharStream stream;
			try {
				stream = streamSupplier.get();
			} catch (Exception e) {
				var msg = Message.create("Could not obtain character stream");
				msg.setCause(e);
				msg.setLevel(Level.ERROR);
				reporter.report(msg);
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

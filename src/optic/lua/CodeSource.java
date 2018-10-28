package optic.lua;

import org.antlr.runtime.*;

import java.io.*;

public interface CodeSource {
	CharStream charStream();

	String name();

	static CodeSource ofFile(String path) throws IOException {
		var stream = new ANTLRFileStream(path);
		var name = new File(path).getCanonicalPath();
		return CodeSource.create(name, stream);
	}

	static CodeSource create(String name, CharStream charStream) {
		return new CodeSource() {
			public CharStream charStream() {
				return charStream;
			}

			public String name() {
				return name;
			}

			public String toString() {
				return name();
			}
		};
	}
}

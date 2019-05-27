package optic.lua.codegen;

import org.jetbrains.annotations.Contract;

import java.io.PrintStream;

public interface ResultBuffer {
	@Contract(mutates = "param4")
	void writeToRecursive(PrintStream out, String indent, int depth, LineNumberCounter counter);

	class LineNumberCounter {
		private int lineNumber = 1;

		@Contract(mutates = "this")
		void increment() {
			lineNumber++;
		}

		int getLineNumber() {
			return lineNumber;
		}
	}
}

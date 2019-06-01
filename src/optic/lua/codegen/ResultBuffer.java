package optic.lua.codegen;

import java.io.PrintStream;

public interface ResultBuffer {
	void writeToRecursive(PrintStream out, String indent, int depth);
}

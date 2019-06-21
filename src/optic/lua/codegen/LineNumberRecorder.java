package optic.lua.codegen;

import java.io.PrintStream;
import java.util.List;

public class LineNumberRecorder implements ResultBuffer {
	private final List<Integer> table;
	private final int sourceLine;

	public LineNumberRecorder(List<Integer> lineTable, int sourceLine) {
		table = lineTable;
		this.sourceLine = sourceLine;
	}

	@Override
	public void writeToRecursive(PrintStream out, String indent, int depth, LineNumberCounter counter) {
		table.add(counter.getLineNumber());
		table.add(sourceLine);
		counter.increment();
	}

	@Override
	public String toString() {
		throw new UnsupportedOperationException();
	}
}

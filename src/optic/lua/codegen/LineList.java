package optic.lua.codegen;

import java.io.PrintStream;
import java.util.*;

public final class LineList implements ResultBuffer {
	private final List<ResultBuffer> children;

	public LineList() {
		children = new ArrayList<>(8);
	}

	public LineList(String line) {
		this();
		children.add(Line.of(line));
	}

	public void addLine(Object... line) {
		var accumulator = new ArrayList<>();
		for (Object o : line) {
			if (o instanceof LineList) {
				if (!accumulator.isEmpty()) {
					children.add(Line.join(accumulator.toArray()));
					accumulator.clear();
				}
				children.add((ResultBuffer) o);
			} else
				accumulator.add(o);
		}

		if (!accumulator.isEmpty())
			children.add(Line.join(accumulator.toArray()));
	}

	public void writeTo(PrintStream out, String indent) {
		writeToRecursive(out, indent, -1, new LineNumberCounter());
	}

	public void writeToRecursive(PrintStream out, String indent, int depth, LineNumberCounter counter) {
		for (var child : children) {
			child.writeToRecursive(out, indent, depth + 1, counter);
		}
	}

	public void addAllChildren(List<ResultBuffer> children) {
		this.children.addAll(children);
	}

	public void addChild(ResultBuffer child) {
		children.add(child);
	}

	@Override
	public String toString() {
		throw new UnsupportedOperationException(getClass().toString());
	}

	public void prependString(String string) {
		children.add(0, Line.of(string));
	}
}

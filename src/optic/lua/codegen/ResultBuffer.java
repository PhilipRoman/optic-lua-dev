package optic.lua.codegen;

import java.io.PrintStream;
import java.util.*;

public final class ResultBuffer {
	/**
	 * Contains only CharSequences and ResultBuffers
	 */
	private final List<Object> elements;

	public ResultBuffer() {
		elements = new ArrayList<>(8);
	}

	public void add(Object... line) {
		StringBuilder builder = new StringBuilder();
		for (var o : line) {
			builder.append(o);
		}
		elements.add(builder);
	}

	public void addChild(ResultBuffer buffer) {
		elements.add(buffer);
	}

	public void insertIn(ResultBuffer other) {
		other.elements.addAll(elements);
	}

	public void writeTo(PrintStream out, String indent) {
		writeToRecursive(out, indent, 0);
	}

	private void writeToRecursive(PrintStream out, String indent, int depth) {
		for (var e : elements) {
			var prefix = indent.repeat(depth);
			if (e instanceof CharSequence) {
				out.print(prefix);
				out.println(e);
			} else {
				((ResultBuffer) e).writeToRecursive(out, indent, depth + 1);
			}
		}
	}

	public void addBlock(Iterable<ResultBuffer> lines) {
		var buffer = new ResultBuffer();
		for (var b : lines) {
			buffer.elements.addAll(b.elements);
		}
		addChild(buffer);
	}
}

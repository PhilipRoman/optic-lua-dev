package optic.lua.codegen;

import java.io.PrintStream;
import java.util.*;

public final class Line implements ResultBuffer {
	protected final String content;

	protected Line(String content) {
		this.content = content;
	}

	private static String concat(List<?> line) {
		if (line.isEmpty())
			return "";
		StringBuilder builder = new StringBuilder();
		for (Object o : line)
			builder.append(o);
		return builder.toString();
	}

	public static ResultBuffer join(Object... line) {
		boolean multiLine = false;
		for (Object o : line) {
			if (o instanceof LineList) {
				multiLine = true;
				break;
			}
		}
		if (multiLine) {
			LineList list = new LineList();
			list.addLine(line);
			return list;
		} else {
			return new Line(concat(Arrays.asList(line)));
		}
	}

	public static Line of(String content) {
		return new Line(content);
	}

	@Override
	public void writeToRecursive(PrintStream out, String indent, int depth, LineNumberCounter counter) {
		out.println(indent.repeat(depth) + content);
		counter.increment();
	}

	@Override
	public String toString() {
		return content;
	}
}

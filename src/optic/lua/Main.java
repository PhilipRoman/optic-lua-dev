package optic.lua;

import optic.lua.codegen.CodeOutput;
import optic.lua.messages.*;
import optic.lua.ssa.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import java.io.*;
import java.util.List;
import java.util.stream.*;

public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		var codeSource = CodeSource.ofFile("sample.lua");
		var pipeline = new Pipeline(
				codeSource,
				MutableFlattener::flatten,
				new LogMessageReporter(log, new SimpleMessageFormat()),
				new PrintingCodeOutput(System.err)
		);
		try {
			pipeline.run();
		} catch (CompilationFailure e) {
			System.err.print("Failed!");
			System.exit(1);
		}
	}

	private static class LogMessageReporter implements MessageReporter {
		private final Logger log;
		private final MessageFormat<String> format;

		private LogMessageReporter(Logger log, MessageFormat<String> format) {
			this.log = log;
			this.format = format;
		}

		@Override
		public void report(Message message) {
			var text = format.format(message);
			switch (message.level()) {
				case TRACE:
					log.trace(text);
					break;
				case DEBUG:
					log.debug(text);
					break;
				case HINT:
					log.info(text);
					break;
				case INFO:
					log.info(text);
					break;
				case WARNING:
					log.warn(text);
					break;
				case ERROR:
					log.error(text);
			}
		}
	}

	private static class SimpleMessageFormat implements MessageFormat<String> {
		@NotNull
		@Override
		public String format(Message message) {
			var b = new StringBuilder();
			b.append('[');
			b.append(message.level());
			b.append("] ");
			message.source().map(CodeSource::name).ifPresent(str -> b.append(str).append(' '));
			var line = message.line();
			if (line.isPresent()) {
				b.append(line.getAsInt());
				var column = message.column();
				if (column.isPresent()) {
					b.append(':');
					b.append(column.getAsInt());
				}
			}
			b.append(" - ");
			b.append(message.message());
			message.cause().map(Throwable::getMessage).ifPresent(msg -> b.append(" caused by ").append(msg));
			return b.toString();
		}
	}

	private static class PrintingCodeOutput implements CodeOutput {
		private final PrintStream out;

		private PrintingCodeOutput(PrintStream out) {
			this.out = out;
		}

		@Override
		public void output(List<Step> steps, MessageReporter reporter) {
			steps.forEach(step -> print(step, 0));
		}

		private void print(Step step, int depth) {
			String indent = "    " + Stream.generate(() -> "|   ")
					.limit(depth)
					.collect(Collectors.joining());
			out.println(indent + step);
			step.children().forEach(s -> print(s, depth + 1));
		}
	}
}
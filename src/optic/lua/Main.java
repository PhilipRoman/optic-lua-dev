package optic.lua;

import optic.lua.asm.*;
import optic.lua.codegen.java.JavaCodeOutput;
import optic.lua.files.Compiler;
import optic.lua.messages.*;
import optic.lua.verify.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import java.io.PrintStream;
import java.nio.file.Files;
import java.util.stream.*;

import static optic.lua.messages.StandardFlags.*;

public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		var codeSource = CodeSource.ofFile("samples/sum-loop.lua");
		var temp = Files.createTempFile("optic_lua_", ".java");
		var options = new Options();
		options.enable(KEEP_COMMENTS);
		options.enable(DEBUG_COMMENTS);
		options.enable(PARALLEL);
		options.enable(VERIFY);
		options.enable(UNBOX);
		options.set(INDENT, "\t");
		var pipeline = new Pipeline(
				options,
				new LogMessageReporter(log, new SimpleMessageFormat()),
				codeSource
		);
		pipeline.registerPlugin(SingleAssignmentVerifier::new);
		pipeline.registerPlugin(SingleRegisterUseVerifier::new);
		pipeline.registerPlugin(JavaCodeOutput.writingTo(Files.newOutputStream(temp)));
		try {
			pipeline.run();
		} catch (CompilationFailure e) {
			System.err.print("Failed!");
			e.printStackTrace();
			System.exit(1);
		}
		Files.copy(temp, System.err);
		new Compiler(new LogMessageReporter(log, new SimpleMessageFormat())).run(Files.newInputStream(temp), 20);
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
			message.cause().ifPresent(cause -> log.error("Stack trace", cause));
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

	private static class PrintingCodeOutput implements CompilerPlugin {
		private final PrintStream out;
		private final AsmBlock body;

		private PrintingCodeOutput(PrintStream out, AsmBlock body) {
			this.out = out;
			this.body = body;
		}

		private static CompilerPlugin.Factory factory(PrintStream out) {
			return (block, context) -> new PrintingCodeOutput(out, block);
		}

		@Override
		public AsmBlock apply() {
			body.steps().forEach(step -> print(step, 0));
			out.flush();
			return body;
		}

		@Override
		public boolean concurrent() {
			return true;
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
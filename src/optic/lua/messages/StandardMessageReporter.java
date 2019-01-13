package optic.lua.messages;

import java.io.PrintStream;

public class StandardMessageReporter implements MessageReporter {
	private final PrintStream stream;
	private final MessageFormat<? extends CharSequence> format;

	public StandardMessageReporter() {
		this(System.out, new SimpleMessageFormat());
	}

	public StandardMessageReporter(PrintStream stream) {
		this(stream, new SimpleMessageFormat());
	}

	public StandardMessageReporter(PrintStream stream, MessageFormat<? extends CharSequence> format) {
		this.stream = stream;
		this.format = format;
	}

	@Override
	public void report(Message message) {
		stream.println(format.format(message));
		message.cause().ifPresent(cause -> cause.printStackTrace(stream));
	}
}

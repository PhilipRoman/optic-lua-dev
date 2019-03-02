package optic.lua.messages;

import org.slf4j.Logger;

public class LogMessageReporter implements MessageReporter {
	private final Logger log;
	private final MessageFormat<String> format;

	public LogMessageReporter(Logger log, MessageFormat<String> format) {
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

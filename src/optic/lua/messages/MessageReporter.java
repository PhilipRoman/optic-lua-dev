package optic.lua.messages;

import optic.lua.CodeSource;

import java.util.function.*;

/**
 * Sink for emitted messages. Instances of this class are immutable, but may be
 * wrapped for additional functionality using {@link #with}, {@link #filter} and
 * specialized variants such as {@link #withSource} and {@link #withPhase}
 */
@FunctionalInterface
public interface MessageReporter {
	void report(Message message);

	default MessageReporter with(Consumer<MessageBuilder> setter) {
		return msg -> {
			var editable = MessageBuilder.from(msg);
			setter.accept(editable);
			this.report(editable);
		};
	}

	default MessageReporter withSource(CodeSource source) {
		return with(msg -> msg.setSource(source));
	}

	default MessageReporter withPhase(Phase phase) {
		return with(msg -> msg.setPhase(phase));
	}

	default MessageReporter filter(Predicate<Message> filter) {
		return msg -> {
			if (filter.test(msg)) {
				report(msg);
			}
		};
	}
}

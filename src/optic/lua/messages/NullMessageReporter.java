package optic.lua.messages;

import optic.lua.CodeSource;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class NullMessageReporter implements MessageReporter {
	@Override
	public void report(Message message) {
		// do nothing
	}

	@Override
	public MessageReporter with(Consumer<MessageBuilder> setter) {
		return this;
	}

	@Override
	public MessageReporter withSource(CodeSource source) {
		return this;
	}

	@Override
	public MessageReporter withPhase(Phase phase) {
		return this;
	}

	@Override
	public MessageReporter filter(Predicate<Message> filter) {
		return this;
	}
}

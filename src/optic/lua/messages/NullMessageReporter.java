package optic.lua.messages;

import optic.lua.io.CodeSource;

import java.util.function.*;

@Deprecated
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

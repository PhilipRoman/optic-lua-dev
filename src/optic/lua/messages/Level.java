package optic.lua.messages;

import java.util.function.Predicate;

@Deprecated
public enum Level implements Predicate<Message> {
	TRACE, DEBUG, HINT, INFO, WARNING, ERROR;

	public final boolean canBeFatal() {
		return this == ERROR;
	}

	@Override
	public boolean test(Message msg) {
		return msg.level().compareTo(this) >= 0;
	}
}

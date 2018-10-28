package optic.lua.messages;

@FunctionalInterface
public interface MessageReporter {
	void report(Message message);
}

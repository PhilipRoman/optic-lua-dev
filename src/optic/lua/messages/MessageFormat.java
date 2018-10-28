package optic.lua.messages;

@FunctionalInterface
public interface MessageFormat {
	String format(Message message);
}

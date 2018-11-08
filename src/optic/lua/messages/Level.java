package optic.lua.messages;

public enum Level {
	TRACE, DEBUG, HINT, INFO, WARNING, ERROR;

	public final boolean canBeFatal() {
		return this == ERROR;
	}
}

package optic.lua.messages;

public enum Tag {
	UNSUPPORTED_FEATURE,
	USER_CODE,
	IO_ERROR,
	BUG,
	BAD_INPUT,
	PARSER, STATISTICS;

	@Override
	public String toString() {
		return name().toLowerCase().replace('_', '-');
	}
}

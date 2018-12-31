package optic.lua.messages;

public enum StandardFlags implements Option<Boolean> {
	FIRST_NUM_OPERATORS,
	KEEP_COMMENTS,
	DEBUG_COMMENTS,
	PARALLEL,
	VERIFY;

	@Override
	public Boolean defaultValue() {
		return false;
	}
}

package optic.lua.messages;

public enum StandardFlags implements Option<Boolean> {
	KEEP_COMMENTS(true),
	DEBUG_COMMENTS(false),
	PARALLEL(Runtime.getRuntime().availableProcessors() > 2),
	VERIFY(false),
	UNBOX(true);
	private final boolean defaultValue;

	StandardFlags(boolean value) {
		defaultValue = value;
	}

	@Override
	public Boolean defaultValue() {
		return defaultValue;
	}
}

package optic.lua.messages;

public enum StandardFlags implements Option<Boolean> {
	KEEP_COMMENTS(true),
	DEBUG_COMMENTS(false),
	PARALLEL(Runtime.getRuntime().availableProcessors() > 2),
	VERIFY(false),
	SSA_SPLIT(true),
	LOOP_SPLIT(false),
	DUMP_JAVA(false),
	CACHE_JAVA_COMPILING(true),
	CACHE_LUA_COMPILING(true),
	ALLOW_UPVALUE_VARARGS(false),
	SHOW_TIME(false),
	SHOW_RT_STATS(false);
	private final boolean defaultValue;

	StandardFlags(boolean value) {
		defaultValue = value;
	}

	@Override
	public Boolean defaultValue() {
		return defaultValue;
	}
}

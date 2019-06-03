package optic.lua.messages;

/**
 * A set of commonly used boolean-value options ("<i>flags</i>").
 */
public enum StandardFlags implements Option<Boolean> {
	KEEP_COMMENTS(true),
	DEBUG_COMMENTS(false),
	PARALLEL(Runtime.getRuntime().availableProcessors() > 2),
	VERIFY(false),
	SSA_SPLIT(true),
	LOOP_SPLIT(false),
	/**
	 * Whether or not to output the generated intermediate Java source code.
	 */
	DUMP_JAVA(false),
	CACHE_JAVA_COMPILING(true),
	CACHE_LUA_COMPILING(true),
	/**
	 * Non-standard option: if true, vararg symbol ("...") can be accessed from nested scopes as if it were an upvalue.
	 */
	ALLOW_UPVALUE_VARARGS(false),
	SHOW_TIME(false),
	/**
	 * Whether or not to show runtime statistics for various call sites.
	 */
	SHOW_RT_STATS(false),
	/**
	 * Whether or not to write respective JVM class files to the file system.
	 */
	GENERATE_CLASSES(false),
	/**
	 * Whether or not to output the abstract syntax tree of Lua code.
	 */
	DUMP_AST(false);
	private final boolean defaultValue;

	StandardFlags(boolean value) {
		defaultValue = value;
	}

	@Override
	public Boolean defaultValue() {
		return defaultValue;
	}
}

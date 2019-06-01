package optic.lua.asm;

/**
 * The meaning of a particular code block.
 */
public enum BlockMeaning {
	MAIN_CHUNK, LOOP_BODY, IF_BODY, DO_BLOCK, FUNCTION_BODY;

	/**
	 * Whether or not local variables past this block should be accessed as upvalues
	 */
	public boolean hasLexicalBoundary() {
		return this == FUNCTION_BODY;
	}
}

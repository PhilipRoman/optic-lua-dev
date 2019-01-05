package optic.lua.asm;

public enum BlockMeaning {
	MAIN_CHUNK, LOOP_BODY, IF_BODY, DO_BLOCK, FUNCTION_BODY;

	public boolean isConditional() {
		switch (this) {
			case MAIN_CHUNK:
			case DO_BLOCK:
				return false;
			default:
				return true;
		}
	}

	public boolean hasLexicalBoundary() {
		return this == FUNCTION_BODY;
	}
}

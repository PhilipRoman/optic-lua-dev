package optic.lua;

import optic.lua.asm.AsmBlock;
import optic.lua.messages.*;

@FunctionalInterface
public interface CompilerPlugin {
	AsmBlock apply() throws CompilationFailure;

	/**
	 * Concurrent plugins can be run in background, result of {@link #apply} will not be used.
	 * Examples of such plugins include verification, code outputting, etc.
	 */
	default boolean concurrent() {
		return false;
	}

	@FunctionalInterface
	interface Factory {
		CompilerPlugin create(AsmBlock block, Context context);
	}
}

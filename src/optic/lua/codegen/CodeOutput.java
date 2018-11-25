package optic.lua.codegen;

import optic.lua.asm.AsmBlock;
import optic.lua.messages.*;

/**
 * End converter from intermediate representation to resulting code.
 * There are no restrictions on what format code can be converted to.
 * If you wish to use this function with side effects instead of return
 * value, use {@link Void} as the type parameter.
 */
@FunctionalInterface
public interface CodeOutput {
	void output(AsmBlock body, MessageReporter reporter) throws CompilationFailure;
}

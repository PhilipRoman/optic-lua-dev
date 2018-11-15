package optic.lua.codegen;

import optic.lua.messages.*;
import optic.lua.asm.Step;

import java.util.List;

/**
 * End converter from intermediate representation to resulting code.
 * There are no restrictions on what format code can be converted to.
 * If you wish to use this function with side effects instead of return
 * value, use {@link Void} as the type parameter.
 */
@FunctionalInterface
public interface CodeOutput {
	void output(List<Step> steps, MessageReporter reporter) throws CompilationFailure;
}

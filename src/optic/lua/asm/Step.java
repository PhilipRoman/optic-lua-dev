package optic.lua.asm;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * Obtain instances of this interface using {@link StepFactory} methods.
 */
public interface Step {
	@NotNull
	default Stream<Step> children() {
		return Stream.empty();
	}
}

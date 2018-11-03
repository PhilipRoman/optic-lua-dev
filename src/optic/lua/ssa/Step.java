package optic.lua.ssa;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * Obtain instances of this interface using {@link StepFactory} methods.
 */
public interface Step {
	@NotNull
	StepType getType();

	@NotNull
	default String typeName() {
		return getType().name().toLowerCase();
	}

	@NotNull
	default Stream<Step> children() {
		return Stream.empty();
	}
}

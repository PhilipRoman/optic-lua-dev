package optic.lua.flat;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

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

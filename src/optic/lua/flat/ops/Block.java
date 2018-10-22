package optic.lua.flat.ops;

import optic.lua.flat.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class Block implements Step {
	private final List<Step> steps;
	private final StepType type;

	public Block(StepType type, List<Step> steps) {
		this.type = Objects.requireNonNull(type);
		this.steps = List.copyOf(steps);
	}

	@Override
	public @NotNull StepType getType() {
		return type;
	}

	@Override
	public @NotNull Stream<Step> children() {
		return steps.stream();
	}

	@Override
	public String toString() {
		return typeName();
	}
}

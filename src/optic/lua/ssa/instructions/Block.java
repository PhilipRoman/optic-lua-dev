package optic.lua.ssa.instructions;

import optic.lua.ssa.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public final class Block implements Step {
	private final List<Step> steps;

	public Block(List<Step> steps) {
		this.steps = List.copyOf(steps);
	}

	@Override
	public @NotNull Stream<Step> children() {
		return steps.stream();
	}

	@Override
	public String toString() {
		return "do";
	}
}

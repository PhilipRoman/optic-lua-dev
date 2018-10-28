package optic.lua.ssa.instructions;

import optic.lua.ssa.*;
import org.jetbrains.annotations.NotNull;

public class GetVarargs implements Step {
	private final StepType type;
	private final Register to;

	public GetVarargs(StepType type, Register to) {
		this.type = type;
		this.to = to;
	}

	@Override
	public @NotNull StepType getType() {
		return type;
	}

	@Override
	public String toString() {
		return typeName() + " " + to + " = ...";
	}
}

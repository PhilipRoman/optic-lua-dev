package optic.lua.ssa.instructions;

import optic.lua.ssa.*;
import org.jetbrains.annotations.NotNull;

public class Declare implements Step {
	private final StepType type;
	private final String name;

	public Declare(StepType type, String name) {
		assert type == StepType.DECLARE;
		this.type = type;
		this.name = name;
	}

	@NotNull
	@Override
	public StepType getType() {
		return type;
	}

	@Override
	public String toString() {
		return typeName() + " " + name;
	}
}

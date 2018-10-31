package optic.lua.ssa.instructions;

import optic.lua.ssa.*;
import org.jetbrains.annotations.NotNull;

public class Dereference implements Step {
	private final StepType type;
	private final Register register;
	private final String name;

	public Dereference(StepType type, Register register, String name) {
		this.type = type;
		this.register = register;
		this.name = name;
	}

	@NotNull
	@Override
	public StepType getType() {
		return type;
	}

	@Override
	public String toString() {
		return typeName() + " " + register + " = " + name;
	}
}

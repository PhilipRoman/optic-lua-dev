package optic.lua.flat.ops;

import optic.lua.flat.*;

public class Dereference implements Step {
	private final StepType type;
	private final Register register;
	private final String name;

	public Dereference(StepType type, Register register, String name) {
		this.type = type;
		this.register = register;
		this.name = name;
	}

	@Override
	public StepType getType() {
		return type;
	}

	@Override
	public String toString() {
		return typeName() + " " + register + " = " + name;
	}
}

package optic.lua.ssa.instructions;

import optic.lua.ssa.*;

public final class Dereference implements Step {
	private final Register register;
	private final String name;

	public Dereference(Register register, String name) {
		this.register = register;
		this.name = name;
	}

	@Override
	public String toString() {
		return "lookup " + register + " = " + name;
	}
}

package optic.lua.ssa.instructions;

import optic.lua.ssa.*;

public final class Declare implements Step {
	private final String name;

	public Declare(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "local " + name;
	}
}

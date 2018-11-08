package optic.lua.ssa.instructions;

import optic.lua.ssa.*;

public final class GetVarargs implements Step {
	private final Register to;

	public GetVarargs(Register to) {
		this.to = to;
	}

	@Override
	public String toString() {
		return "varargs " + to + " = ...";
	}
}

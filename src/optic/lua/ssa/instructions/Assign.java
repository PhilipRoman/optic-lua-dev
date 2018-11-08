package optic.lua.ssa.instructions;

import optic.lua.ssa.*;

import java.util.List;

public final class Assign implements Step {
	private final List<Register> sources;
	private final List<LValue> targets;

	public Assign(List<LValue> targets, List<Register> sources) {
		this.targets = targets;
		this.sources = sources;
	}

	@Override
	public String toString() {
		return "assign " + targets + " = " + sources;
	}
}

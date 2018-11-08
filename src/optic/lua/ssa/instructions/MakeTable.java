package optic.lua.ssa.instructions;

import optic.lua.ssa.*;

import java.util.Map;

public final class MakeTable implements Step {
	private final Map<Register, Register> values;
	private final Register result;

	public MakeTable(Map<Register, Register> values, Register result) {
		this.values = values;
		this.result = result;
	}

	@Override
	public String toString() {
		return "table " + result + " = " + values;
	}
}

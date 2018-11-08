package optic.lua.ssa.instructions;

import optic.lua.ssa.*;

import java.util.List;

public final class Return implements Step {
	private final List<Register> registers;

	public Return(List<Register> registers) {
		this.registers = registers;
	}

	@Override
	public String toString() {
		return "return " + registers;
	}
}

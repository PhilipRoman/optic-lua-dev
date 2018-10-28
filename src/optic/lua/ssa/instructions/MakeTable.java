package optic.lua.ssa.instructions;

import optic.lua.ssa.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MakeTable implements Step {
	private final StepType type;
	private final Map<Register, Register> values;
	private final Register result;

	public MakeTable(StepType type, Map<Register, Register> values, Register result) {
		this.type = type;
		this.values = values;
		this.result = result;
	}

	@Override
	public @NotNull StepType getType() {
		return type;
	}

	@Override
	public String toString() {
		return typeName() + " " + result + " = " + values;
	}
}

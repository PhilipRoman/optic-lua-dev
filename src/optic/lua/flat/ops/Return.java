package optic.lua.flat.ops;

import optic.lua.flat.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Return implements Step {
	private final StepType type;
	private final List<Register> registers;

	public Return(StepType type, List<Register> registers) {
		this.type = type;
		this.registers = registers;
	}

	@NotNull
	@Override
	public StepType getType() {
		return type;
	}

	@Override
	public String toString() {
		return typeName() + " " + registers;
	}
}

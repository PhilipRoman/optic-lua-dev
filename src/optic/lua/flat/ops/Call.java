package optic.lua.flat.ops;

import optic.lua.flat.*;

import java.util.List;

public class Call implements Step {
	private final StepType type;
	private final Register function;
	private final List<Register> args;

	public Call(StepType type, Register function, List<Register> args) {
		this.type = type;
		this.function = function;
		this.args = args;
	}

	@Override
	public StepType getType() {
		return type;
	}

	@Override
	public String toString() {
		return typeName() + " " + function + "(" + args + ")";
	}
}

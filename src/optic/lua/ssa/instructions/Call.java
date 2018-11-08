package optic.lua.ssa.instructions;

import optic.lua.ssa.*;

import java.util.List;

public final class Call implements Step {
	private final Register function;
	private final Register output;
	private final List<Register> args;

	public Call(Register function, List<Register> args) {
		this(function, args, Register.unused());
	}

	public Call(Register function, List<Register> args, Register output) {
		this.function = function;
		this.output = output;
		this.args = args;
	}

	@Override
	public String toString() {
		boolean resultUnused = output.equals(Register.unused());
		return resultUnused
				? String.format("call %s(%s)", function, args)
				: String.format("call %s = %s(%s)", output, function, args);
	}
}

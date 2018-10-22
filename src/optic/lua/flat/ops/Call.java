package optic.lua.flat.ops;

import optic.lua.flat.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Call implements Step {
	private final StepType type;
	private final Register function;
	private final Register output;
	private final List<Register> args;

	public Call(StepType type, Register function, List<Register> args) {
		this(type, function, args, Register.unused());
	}

	public Call(StepType call, Register function, List<Register> args, Register output) {
		type = call;
		this.function = function;
		this.output = output;
		this.args = args;
	}

	@NotNull
	@Override
	public StepType getType() {
		return type;
	}

	@Override
	public String toString() {
		boolean resultUnused = output.equals(Register.unused());
		return resultUnused
				? String.format("%s %s(%s)", typeName(), function, args)
				: String.format("%s %s = %s(%s)", typeName(), output, function, args);
	}
}

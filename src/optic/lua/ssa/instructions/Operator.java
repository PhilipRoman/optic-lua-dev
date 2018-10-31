package optic.lua.ssa.instructions;

import optic.lua.ssa.*;
import org.jetbrains.annotations.NotNull;

public class Operator implements Step {
	private final StepType type;
	private final Register a, b, target;
	private final String symbol;

	public Operator(StepType type, Register a, Register b, Register target, String symbol) {
		this.type = type;
		this.a = a;
		this.b = b;
		this.target = target;
		this.symbol = symbol;
	}

	public Operator(StepType type, Register b, Register target, String symbol) {
		this.type = type;
		this.a = null;
		this.b = b;
		this.target = target;
		this.symbol = symbol;
	}

	@NotNull
	@Override
	public StepType getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("%s %s = %s %s %s",
				typeName(),
				target,
				(a != null ? a : ""), symbol, b);
	}
}

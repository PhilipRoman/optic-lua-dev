package optic.lua.ssa.instructions;

import optic.lua.ssa.*;

public final class Operator implements Step {
	private final Register a, b, target;
	private final String symbol;

	public Operator(Register a, Register b, Register target, String symbol) {
		this.a = a;
		this.b = b;
		this.target = target;
		this.symbol = symbol;
	}

	public Operator(Register b, Register target, String symbol) {
		this.a = null;
		this.b = b;
		this.target = target;
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return String.format("op %s = %s %s %s",
				target,
				(a != null ? a : ""), symbol, b);
	}
}

package optic.lua.ssa.instructions;

import optic.lua.ssa.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.*;

public final class ForRangeLoop implements Step {
	private final String varName;
	private final Register from;
	private final Register to;
	private final List<Step> block;

	public ForRangeLoop(String varName, Register from, Register to, List<Step> block) {
		this.varName = varName;
		this.from = from;
		this.to = to;
		this.block = List.copyOf(block);
	}

	@Override
	public String toString() {
		return "for " + varName + " = " + from + ", " + to;
	}

	@NotNull
	@Override
	public Stream<Step> children() {
		return block.stream();
	}
}

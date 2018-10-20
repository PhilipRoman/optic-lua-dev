package optic.lua.flat.ops;

import optic.lua.flat.*;

import java.util.List;
import java.util.stream.*;

public class ForRangeLoop implements Step {
	private final StepType type;
	private final String varName;
	private final Register from;
	private final Register to;
	private final List<Step> block;

	public ForRangeLoop(StepType type, String varName, Register from, Register to, List<Step> block) {
		this.type = type;
		this.varName = varName;
		this.from = from;
		this.to = to;
		this.block = List.copyOf(block);
	}

	@Override
	public StepType getType() {
		return type;
	}

	@Override
	public String toString() {
		return typeName() + " " + varName + " = " + from + ", " + to;
	}

	@Override
	public Stream<Step> children() {
		return block.stream();
	}
}

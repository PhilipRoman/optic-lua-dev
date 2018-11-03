package optic.lua.ssa.instructions;

import optic.lua.ssa.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public class Branch implements Step {
	private final StepType type;
	private final Register condition;
	private final List<Step> body;

	public Branch(StepType type, Register condition, List<Step> body) {
		this.type = type;
		this.condition = condition;
		this.body = body;
	}

	@NotNull
	@Override
	public StepType getType() {
		return type;
	}

	@Override
	public String toString() {
		return typeName() + " if " + condition;
	}

	@Override
	public @NotNull Stream<Step> children() {
		return body.stream();
	}
}

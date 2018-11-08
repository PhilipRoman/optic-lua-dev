package optic.lua.ssa.instructions;

import optic.lua.ssa.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public final class Branch implements Step {
	private final Register condition;
	private final List<Step> body;

	public Branch(Register condition, List<Step> body) {
		this.condition = condition;
		this.body = body;
	}

	@Override
	public String toString() {
		return "if " + condition;
	}

	@Override
	public @NotNull Stream<Step> children() {
		return body.stream();
	}
}

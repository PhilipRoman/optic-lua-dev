package optic.lua.ssa.instructions;

import optic.lua.ssa.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public final class FunctionLiteral implements Step {
	private final List<Step> body;
	private final Register assignTo;
	private final ParameterList params;

	public FunctionLiteral(List<Step> body, Register assignTo, ParameterList params) {
		this.body = List.copyOf(body);
		this.assignTo = assignTo;
		this.params = params;
	}

	@Override
	public String toString() {
		return "function " + assignTo + " = function(" + params + ")";
	}

	@Override
	public @NotNull Stream<Step> children() {
		return body.stream();
	}
}

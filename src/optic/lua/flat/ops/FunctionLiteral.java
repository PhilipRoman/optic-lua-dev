package optic.lua.flat.ops;

import optic.lua.flat.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public class FunctionLiteral implements Step {
	private final StepType type;
	private final List<Step> body;
	private final Register assignTo;
	private final ParameterList params;

	public FunctionLiteral(StepType type, List<Step> body, Register assignTo, ParameterList params) {
		this.type = type;
		this.body = List.copyOf(body);
		this.assignTo = assignTo;
		this.params = params;
	}

	@NotNull
	@Override
	public StepType getType() {
		return type;
	}

	@Override
	public String toString() {
		return typeName() + " " + assignTo + " = function(" + params + ")";
	}

	@Override
	public @NotNull Stream<Step> children() {
		return body.stream();
	}
}

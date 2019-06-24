package optic.lua.asm;

import java.util.*;

public final class AsmBlock {
	private final List<VoidNode> steps;
	private final Map<String, VariableInfo> locals;

	AsmBlock(List<VoidNode> steps, Map<String, VariableInfo> locals) {
		this.steps = steps;
		this.locals = locals;
	}

	public Map<String, VariableInfo> locals() {
		return locals;
	}

	public List<VoidNode> steps() {
		return steps;
	}
}

package optic.lua.asm;

import java.util.*;
import java.util.stream.Stream;

public final class AsmBlock {
	private final List<Step> steps;
	private final Map<String, VariableInfo> locals;

	AsmBlock(List<Step> steps, Map<String, VariableInfo> locals) {
		this.steps = steps;
		this.locals = locals;
	}

	public Stream<Step> recursiveStream() {
		return steps.stream()
				.flatMap(AsmBlock::expandStepRecursive);
	}

	public Stream<Step> stream() {
		return steps.stream();
	}

	private static Stream<Step> expandStepRecursive(Step step) {
		return step.children().flatMap(AsmBlock::expandStepRecursive);
	}

	public Map<String, VariableInfo> locals() {
		return locals;
	}

	public List<Step> steps() {
		return steps;
	}
}

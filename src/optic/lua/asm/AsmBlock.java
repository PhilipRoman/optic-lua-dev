package optic.lua.asm;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class AsmBlock {
	private final List<Step> steps;
	private final Map<String, VariableInfo> locals;

	AsmBlock(List<Step> steps, Map<String, VariableInfo> locals) {
		this.steps = steps;
		this.locals = locals;
	}

	private static Stream<Step> expandStepRecursive(Step step) {
		return step.children().stream().flatMap(AsmBlock::expandStepRecursive);
	}

	public Stream<Step> recursiveStream() {
		return steps.stream()
				.flatMap(AsmBlock::expandStepRecursive);
	}

	public Stream<Step> stream() {
		return steps.stream();
	}

	public Map<String, VariableInfo> locals() {
		return locals;
	}

	public List<Step> steps() {
		return steps;
	}

	public void forEachRecursive(Consumer<Step> action) {
		forEachRecursiveChild(steps, action);
	}

	private void forEachRecursiveChild(List<Step> steps, Consumer<Step> action) {
		for (var step : steps) {
			action.accept(step);
			forEachRecursiveChild(step.children(), action);
		}
	}
}

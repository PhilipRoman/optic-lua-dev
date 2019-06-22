package optic.lua.asm;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class AsmBlock {
	private final List<VoidNode> steps;
	private final Map<String, VariableInfo> locals;

	AsmBlock(List<VoidNode> steps, Map<String, VariableInfo> locals) {
		this.steps = steps;
		this.locals = locals;
	}

	private static Stream<VoidNode> expandStepRecursive(VoidNode step) {
		return step.children().stream().flatMap(AsmBlock::expandStepRecursive);
	}

	public Stream<VoidNode> recursiveStream() {
		return steps.stream()
				.flatMap(AsmBlock::expandStepRecursive);
	}

	public Stream<VoidNode> stream() {
		return steps.stream();
	}

	public Map<String, VariableInfo> locals() {
		return locals;
	}

	public List<VoidNode> steps() {
		return steps;
	}

	public void forEachRecursive(Consumer<VoidNode> action) {
		forEachRecursiveChild(steps, action);
	}

	private void forEachRecursiveChild(List<VoidNode> steps, Consumer<VoidNode> action) {
		for (var step : steps) {
			action.accept(step);
			forEachRecursiveChild(step.children(), action);
		}
	}
}

package optic.lua.ssa;

import java.util.*;

class FlatVoid {
	private static final FlatVoid EMPTY_STATEMENT = new FlatVoid(List.of());
	final List<Step> steps;

	FlatVoid(List<Step> steps) {
		assert !steps.isEmpty();
		this.steps = steps;
	}

	List<Step> steps() {
		return steps;
	}

	FlatVoid and(List<Step> added) {
		List<Step> list = new ArrayList<>(steps.size() + added.size());
		list.addAll(steps);
		list.addAll(added);
		return new FlatVoid(list);
	}

	FlatVoid and(Step step) {
		return and(List.of(step));
	}

	static FlatVoid start() {
		return EMPTY_STATEMENT;
	}

	FlatVoid prependComment(String comment) {
		List<Step> list = new ArrayList<>(steps.size() + 1);
		list.add(StepFactory.comment(comment));
		list.addAll(steps);
		return new FlatVoid(list);
	}

	FlatValue resultWillBeIn(Register register) {
		return new FlatValue(steps, register);
	}
}

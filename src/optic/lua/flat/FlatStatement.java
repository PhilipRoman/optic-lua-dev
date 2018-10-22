package optic.lua.flat;

import java.util.*;

class FlatStatement {
	private static final FlatStatement EMPTY_STATEMENT = new FlatStatement(List.of());
	final List<Step> steps;

	FlatStatement(List<Step> steps) {
		assert !steps.isEmpty();
		this.steps = steps;
	}

	List<Step> steps() {
		return steps;
	}

	FlatStatement and(List<Step> added) {
		List<Step> list = new ArrayList<>(steps.size() + added.size());
		list.addAll(steps);
		list.addAll(added);
		return new FlatStatement(list);
	}

	FlatStatement and(Step step) {
		return and(List.of(step));
	}

	static FlatStatement start() {
		return EMPTY_STATEMENT;
	}

	FlatStatement prependComment(String comment) {
		List<Step> list = new ArrayList<>(steps.size() + 1);
		list.add(StepFactory.comment(comment));
		list.addAll(steps);
		return new FlatStatement(list);
	}

	FlatExpression resultWillBeIn(Register register) {
		return new FlatExpression(steps, register);
	}
}

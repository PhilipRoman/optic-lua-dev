package optic.lua.flat;

import java.util.*;

class FlatStatement {
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

	static FlatStatement create() {
		return new FlatStatement(List.of());
	}
}

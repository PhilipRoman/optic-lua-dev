package optic.lua.ssa;

import java.util.*;

import static java.util.Objects.requireNonNull;

final class FlatValue extends FlatVoid {
	private final Register result;

	FlatValue(List<Step> steps, Register result) {
		super(steps);
		assert !steps.isEmpty();
		this.result = result;
	}

	Register result() {
		return result;
	}

	FlatValue and(List<Step> added) {
		List<Step> list = new ArrayList<>(steps.size() + added.size());
		list.addAll(steps);
		list.addAll(added);
		return new FlatValue(list, result);
	}

	FlatValue and(Step step) {
		return and(List.of(step));
	}

	static FlatValue createExpression(List<Step> steps, Register result) {
		requireNonNull(steps);
		requireNonNull(result);
		return new FlatValue(List.copyOf(steps), result);
	}
}

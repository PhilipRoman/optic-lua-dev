package optic.lua.flat;

import java.util.*;

import static java.util.Objects.requireNonNull;

final class FlatExpression extends FlatStatement {
	private final Register result;

	private FlatExpression(List<Step> steps, Register result) {
		super(steps);
		assert !steps.isEmpty();
		this.result = result;
	}

	Register result() {
		return result;
	}

	FlatExpression and(List<Step> added) {
		List<Step> list = new ArrayList<>(steps.size() + added.size());
		list.addAll(steps);
		list.addAll(added);
		return new FlatExpression(list, result);
	}

	FlatExpression and(Step step) {
		return and(List.of(step));
	}

	FlatExpression putResultIn(Register register) {
		return new FlatExpression(steps, register);
	}

	static FlatExpression create(List<Step> steps, Register result) {
		requireNonNull(steps);
		requireNonNull(result);
		return new FlatExpression(List.copyOf(steps), result);
	}
}

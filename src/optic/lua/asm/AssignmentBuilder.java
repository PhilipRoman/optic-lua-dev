package optic.lua.asm;

import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Helper class to compile variable assignments. Create a new builder using {@link #AssignmentBuilder(VariableResolver)},
 * add elements with {@link #addValues(List)}, {@link #addValue(Register)} or {@link #addVariable(LValue)} and then
 * retrieve the result using {@link #build()}.
 */
final class AssignmentBuilder {
	// left-hand side of the assignment
	private final List<LValue> variables = new ArrayList<>(1);
	// right-hand side of the assignment
	private final List<RValue> values = new ArrayList<>(1);
	// the resolver to resolve names
	private final VariableResolver resolver;

	AssignmentBuilder(VariableResolver resolver) {
		this.resolver = resolver;
	}

	public void addValue(Register value) {
		values.add(value);
	}

	void addValues(List<RValue> newValues) {
		values.addAll(newValues);
	}

	void addVariable(LValue info) {
		variables.add(info);
	}

	List<Step> build() {
		List<Step> steps = new ArrayList<>(4);
		// by how many variables the left side is ahead of right side
		int overflow = 0;
		int nonVarargRValueCount = nonVarargRValueCount();
		RValue vararg = vararg(); // null if right-hand side don't end with a vararg
		for (int i = 0; i < variables.size(); i++) {
			LValue variable = variables.get(i);
			if (i < nonVarargRValueCount) {
				// regular, one-to-one assignment
				RValue value = values.get(i);
				steps.add(createWriteStep(variable, value));
			} else if (vararg == null) {
				// the left side has surpassed the right side
				// fill remaining variables with nil
				steps.add(createWriteStep(variable, RValue.nil()));
			} else {
				// the left side has surpassed the right side but the last expression can yield multiple values
				// fill the remaining variables by selecting values from the last expression
				steps.add(createWriteStep(variable, RValue.selectNth(vararg, overflow)));
				overflow++;
			}
		}
		return steps;
	}

	/**
	 * How many non-vararg expressions does the right-hand side have?
	 */
	private int nonVarargRValueCount() {
		return values.size() - (vararg() != null ? 1 : 0);
	}

	private Step createWriteStep(LValue left, RValue right) {
		if (left instanceof LValue.TableField) {
			return StepFactory.tableWrite((LValue.TableField) left, right);
		} else if (left instanceof LValue.Name) {
			var name = ((LValue.Name) left);
			VariableInfo info = resolver.resolve(name.name());
			if (info == null) {
				return StepFactory.write(VariableInfo.global(name.name()), right);
			}
			info.addTypeDependency(right::typeInfo);
			info.markAsWritten();
			return StepFactory.write(info, right);
		}
		throw new AssertionError();
	}

	@Nullable
	private RValue vararg() {
		if (!values.isEmpty()) {
			var last = values.get(values.size() - 1);
			return last.isVararg() ? last : null;
		}
		return null;
	}
}

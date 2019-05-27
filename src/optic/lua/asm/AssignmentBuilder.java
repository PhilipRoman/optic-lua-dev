package optic.lua.asm;

import optic.lua.asm.LValue.*;
import optic.lua.optimization.ProvenType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.*;

import java.util.*;

/**
 * Helper class to compile variable assignments. Create a new builder using {@link #AssignmentBuilder(VariableResolver)},
 * add elements with {@link #addValues(List)}, {@link #addValue(Register)} or {@link #addVariable(LValue)} and then
 * retrieve the result using {@link #build()}.
 */
final class AssignmentBuilder {
	private final Logger log = LoggerFactory.getLogger(AssignmentBuilder.class);
	private final List<LValue> variables = new ArrayList<>(1);
	private final List<RValue> values = new ArrayList<>(1);
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
		int nonVarargRegisterCount = nonVarargRegisterCount();
		RValue vararg = vararg();
		for (int i = 0; i < variables.size(); i++) {
			LValue variable = variables.get(i);
			if (i < nonVarargRegisterCount) {
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
				Register selected = RegisterFactory.create(ProvenType.OBJECT);
				steps.add(StepFactory.select(selected, vararg, overflow));
				steps.add(createWriteStep(variable, selected));
				overflow++;
			}
		}
		return steps;
	}

	private int nonVarargRegisterCount() {
		return values.size() - (vararg() != null ? 1 : 0);
	}

	private Step createWriteStep(LValue left, RValue right) {
		if (left instanceof LValue.TableField) {
			return StepFactory.tableWrite((TableField) left, right);
		} else if (left instanceof LValue.Name) {
			var name = ((Name) left);
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

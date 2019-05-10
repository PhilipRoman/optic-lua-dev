package optic.lua.asm;

import optic.lua.asm.LValue.*;
import optic.lua.optimization.ProvenType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.*;

import java.util.*;

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
		int overflow = 0;
		for (int i = 0; i < variables.size(); i++) {
			LValue variable = variables.get(i);
			if (i < nonVarargRegisterCount()) {
				RValue value = values.get(i);
				steps.add(createWriteStep(variable, value));
			} else if (vararg() == null) {
				steps.add(createWriteStep(variable, RValue.nil()));
			} else {
				Register selected = RegisterFactory.create(ProvenType.OBJECT);
				steps.add(StepFactory.select(selected, vararg(), overflow));
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

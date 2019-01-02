package optic.lua.asm;

import optic.lua.asm.LValue.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class AssignmentBuilder {
	private final List<LValue> variables = new ArrayList<>(1);
	private final List<Register> values = new ArrayList<>(1);
	private final VariableResolver resolver;

	public AssignmentBuilder(VariableResolver resolver) {
		this.resolver = resolver;
	}

	public void addValue(Register value) {
		values.add(value);
	}

	void addValues(List<Register> newValues) {
		values.addAll(newValues);
	}

	public void addVariable(LValue info) {
		variables.add(info);
	}

	public List<Step> build() {
		List<Step> steps = new ArrayList<>(4);
		int overflow = 0;
		for (int i = 0; i < variables.size(); i++) {
			LValue variable = variables.get(i);
			if (i < nonVarargRegisterCount()) {
				Register value = values.get(i);
				steps.add(createWriteStep(variable, value));
			} else if (vararg() == null) {
				var nil = RegisterFactory.create();
				steps.add(StepFactory.constNil(nil));
				steps.add(createWriteStep(variable, nil));
			} else {
				Register selected = RegisterFactory.create();
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

	private Step createWriteStep(LValue left, Register value) {
		if (left instanceof LValue.TableField) {
			return StepFactory.tableWrite((TableField) left, value);
		} else if (left instanceof LValue.Name) {
			var name = ((Name) left);
			VariableInfo info = resolver.resolve(name.name());
			if (info == null) {
				return StepFactory.write(VariableInfo.global(name.name()), value);
			}
			info.update(value.status());
			info.markAsWritten();
			return StepFactory.write(info, value);
		}
		throw new AssertionError();
	}

	@Nullable
	private Register vararg() {
		if (!values.isEmpty()) {
			var last = values.get(values.size() - 1);
			return last.isVararg() ? last : null;
		}
		return null;
	}
}

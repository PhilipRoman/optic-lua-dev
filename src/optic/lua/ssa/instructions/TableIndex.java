package optic.lua.ssa.instructions;

import optic.lua.ssa.*;
import org.jetbrains.annotations.NotNull;

public class TableIndex implements Step {
	private final StepType type;
	private final Register table;
	private final Register key;
	private final Register out;

	public TableIndex(StepType type, Register table, Register key, Register out) {
		this.type = type;
		this.table = table;
		this.key = key;
		this.out = out;
	}

	@Override
	public @NotNull StepType getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("%s %s = %s[%s]", typeName(), out, table, key);
	}
}

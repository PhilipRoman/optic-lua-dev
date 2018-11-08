package optic.lua.ssa.instructions;

import optic.lua.ssa.*;

public final class TableIndex implements Step {
	private final Register table;
	private final Register key;
	private final Register out;

	public TableIndex(Register table, Register key, Register out) {
		this.table = table;
		this.key = key;
		this.out = out;
	}

	@Override
	public String toString() {
		return String.format("lookup %s = %s[%s]", out, table, key);
	}
}

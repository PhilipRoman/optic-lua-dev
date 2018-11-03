package optic.lua.ssa;

/**
 * Represents a location which may be assigned a value.
 * Can be either a variable (represented by a string) or
 * a table assignment in form of <code>table[key]</code>
 * represented as two {@link Register registers}: table and key.
 */
public final class LValue {
	private final boolean isTable;
	private final Register table;
	private final Register key;
	private final String name;

	private LValue(boolean isTable, Register table, Register key, String name) {
		this.isTable = isTable;
		this.table = table;
		this.key = key;
		this.name = name;
		if (isTable) {
			assert table != null && key != null && name == null;
		} else {
			assert table == null && key == null && name != null;
		}
	}

	static LValue tableKey(Register table, Register key) {
		return new LValue(true, table, key, null);
	}

	static LValue variable(String name) {
		return new LValue(false, null, null, name);
	}

	@Override
	public String toString() {
		return isTable ? String.format("%s[%s]", table, key) : name;
	}
}

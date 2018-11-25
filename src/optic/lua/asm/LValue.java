package optic.lua.asm;

/**
 * Represents a location which may be assigned a value.
 * Can be either a variable (represented by a string) or
 * a table assignment in form of <code>table[key]</code>
 * represented as two {@link Register registers}: table and key.
 */
public interface LValue {
	static LValue tableField(Register table, Register key) {
		return new TableField(table, key);
	}

	static LValue variable(String name) {
		return new Name(name);
	}

	final class TableField implements LValue {
		private final Register table;
		private final Register key;

		private TableField(Register table, Register key) {
			this.table = table;
			this.key = key;
		}

		@Override
		public String toString() {
			return table + "[" + key + "]";
		}
	}

	final class Name implements LValue {
		private final String name;

		private Name(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}

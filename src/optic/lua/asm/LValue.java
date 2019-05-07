package optic.lua.asm;

/**
 * Represents a location which may be assigned a value.
 * Can be either a [name][Name] or
 * a [table field][TableField]
 * represented as two registers: table and key.
 */
public interface LValue {
	class TableField implements LValue {
		private final RValue table;
		private final RValue key;

		TableField(RValue table, RValue key) {
			this.table = table;
			this.key = key;
		}

		public RValue table() {
			return table;
		}

		public RValue key() {
			return key;
		}

		@Override
		public String toString() {
			return table + "[" + key + "]";
		}
	}

	class Name implements LValue {
		private final String name;

		Name(String name) {
			this.name = name;
		}

		public String name() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}

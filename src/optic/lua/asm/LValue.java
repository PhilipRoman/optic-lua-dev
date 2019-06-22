package optic.lua.asm;

/**
 * Represents a location which may be assigned a value.
 * Can be either a name or
 * a table field, represented as two registers: table and key.
 */
public interface LValue {
	class TableField implements LValue {
		private final ExprNode table;
		private final ExprNode key;

		TableField(ExprNode table, ExprNode key) {
			this.table = table;
			this.key = key;
		}

		public ExprNode table() {
			return table;
		}

		public ExprNode key() {
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

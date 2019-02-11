package optic.lua.optimization;

public enum ProvenType {
	INTEGER(1), NUMBER(3), OBJECT(7);

	private final int code;
	private static final ProvenType[] table = {null, INTEGER, null, NUMBER, null, null, null, OBJECT};

	ProvenType(int i) {
		this.code = i;
	}

	public ProvenType and(ProvenType other) {
		return table[this.code | other.code];
	}

	public boolean isNumeric() {
		return this == INTEGER || this == NUMBER;
	}
}

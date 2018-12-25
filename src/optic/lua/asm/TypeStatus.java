package optic.lua.asm;

public enum TypeStatus {
	NONE(0), NUMBER(1), OBJECT(2), HYBRID(1 | 2);

	private final int code;
	private static final String[] toStringTable = {
			"NONE", "NUMBER", "OBJECT", "NUMBER|OBJECT"
	};
	private static final TypeStatus[] statusByCode = {NONE, NUMBER, OBJECT, HYBRID};


	TypeStatus(int i) {
		this.code = i;
	}

	public TypeStatus and(TypeStatus other) {
		return statusByCode[this.code | other.code];
	}

	@Override
	public String toString() {
		return toStringTable[code];
	}
}

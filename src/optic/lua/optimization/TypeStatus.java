package optic.lua.optimization;

public enum TypeStatus {
	UNKNOWN(0), NUMBER(1), OBJECT(3);

	private final int code;
	private static final TypeStatus[] statusByCode = {UNKNOWN, NUMBER, null, OBJECT};

	TypeStatus(int i) {
		this.code = i;
	}

	public TypeStatus and(TypeStatus other) {
		return statusByCode[this.code | other.code];
	}
}

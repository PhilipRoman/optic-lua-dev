package optic.lua.optimization;

public enum ProvenType {
	UNKNOWN(0), NUMBER(1), OBJECT(3);

	private final int code;
	private static final ProvenType[] statusByCode = {UNKNOWN, NUMBER, null, OBJECT};

	ProvenType(int i) {
		this.code = i;
	}

	public ProvenType and(ProvenType other) {
		return statusByCode[this.code | other.code];
	}
}

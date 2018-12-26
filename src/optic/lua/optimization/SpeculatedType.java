package optic.lua.optimization;

public enum SpeculatedType {
	UNKNOWN(0), NUMBER(1), TABLE(2), META(3);

	private final int code;
	private static final SpeculatedType[] statusByCode = {UNKNOWN, NUMBER, TABLE, META};

	SpeculatedType(int code) {
		this.code = code;
	}

	public SpeculatedType and(SpeculatedType other) {
		return statusByCode[this.code | other.code];
	}
}

package optic.lua.optimization;

import java.util.function.Supplier;

/**
 * Representation of a "highest proven type" of some expression. These types propagate through various operations and assignments.
 */
public enum ProvenType implements Supplier<ProvenType> {
	INTEGER(1), NUMBER(3), OBJECT(7);

	// efficient lookup table for combining types
	private static final ProvenType[] table = {null, INTEGER, null, NUMBER, null, null, null, OBJECT};
	// a binary code for this value
	private final int code;

	ProvenType(int i) {
		this.code = i;
	}

	/**
	 * Returns the least specific type of the two.
	 */
	public ProvenType and(ProvenType other) {
		return table[this.code | other.code];
	}

	/**
	 * Whether or not this type describes a numeric value.
	 */
	public boolean isNumeric() {
		return this == INTEGER || this == NUMBER;
	}

	/**
	 * Whether or not this type is equal or more specific than the other type.
	 */
	public boolean subtypeOf(ProvenType type) {
		return this.code <= type.code;
	}

	@Override
	public ProvenType get() {
		return this;
	}
}

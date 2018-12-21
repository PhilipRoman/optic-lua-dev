package optic.lua.runtime;

final class DynamicString extends Dynamic {
	private static final DynamicString EMPTY = new DynamicString("");
	private static final DynamicString[] chars = new DynamicString[128];

	static {
		for (char i = 0; i < chars.length; i++) {
			chars[i] = new DynamicString(String.valueOf(i));
		}
	}

	final String value;

	protected DynamicString(String value) {
		super(Dynamic.STRING);
		this.value = value;
	}

	public static DynamicString of(String str) {
		switch (str.length()) {
			case 0:
				return EMPTY;
			case 1:
				char c = str.charAt(0);
				return c < 128 ? chars[c] : new DynamicString(String.valueOf(c));
			default:
				return new DynamicString(str);
		}
	}

	DynamicString sub(int from, int to) {
		from = normalizeIndex(from);
		to = normalizeIndex(to);
		// negative indices are counted from end
		if (to <= 0 || from > to || from >= value.length()) {
			return EMPTY;
		}
		if (from == to) {
			return chars[value.charAt(from)];
		}
		return new DynamicString(value.substring(from, to + 1));
	}

	DynamicString sub(int from) {
		from = normalizeIndex(from);
		if (from >= value.length()) {
			return EMPTY;
		}
		return new DynamicString(value.substring(from));
	}

	private int normalizeIndex(int n) {
		if (n < 0) {
			return Math.max(0, value.length() + n);
		}
		return Math.min(value.length(), n - 1);
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof DynamicString && ((DynamicString) obj).value.equals(value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return value;
	}

	public String value() {
		return value;
	}
}

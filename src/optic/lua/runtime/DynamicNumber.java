package optic.lua.runtime;

final class DynamicNumber extends Dynamic {
	final double value;
	private static final DynamicNumber[] cache = new DynamicNumber[128];
	private static final String[] toStringCache = new String[10];

	static {
		for (int i = 0; i < cache.length; i++) {
			cache[i] = new DynamicNumber(i);
		}
		for (int i = 0; i < toStringCache.length; i++) {
			toStringCache[i] = Integer.toString(i);
		}
	}

	DynamicNumber(double value) {
		super(Dynamic.NUMBER);
		this.value = value;
	}

	public static DynamicNumber of(double x) {
		int i;
		if ((i = (int) x) == x && i >= 0 && i < cache.length) {
			return cache[i];
		}
		return new DynamicNumber(x);
	}

	public double value() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof DynamicNumber && ((DynamicNumber) obj).value == value;
	}

	@Override
	public int hashCode() {
		return (int) value;
	}

	@Override
	public String toString() {
		int i;
		return (i = (int) value) == value && (i < toStringCache.length) && i >= 0 ? toStringCache[i] : Double.toString(i);
	}
}

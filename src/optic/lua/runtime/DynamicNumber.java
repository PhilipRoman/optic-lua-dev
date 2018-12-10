package optic.lua.runtime;

final class DynamicNumber extends Dynamic {
	final double value;
	private static final DynamicNumber[] cache = new DynamicNumber[64];

	static {
		for (int i = 0; i < cache.length; i++) {
			cache[i] = new DynamicNumber(i);
		}
	}

	DynamicNumber(double value) {
		super(Dynamic.NUMBER);
		this.value = value;
	}

	public static DynamicNumber of(double x) {
		int i;
		if (x >= 0 && x < cache.length && (i = (int) x) == x) {
			return cache[i];
		}
		return new DynamicNumber(x);
	}

	public double value() {
		return value;
	}
}

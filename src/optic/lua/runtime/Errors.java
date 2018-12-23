package optic.lua.runtime;

class Errors {
	static void forbidden() {
		throw new UnsupportedOperationException();
	}

	static void argument(int n, String expected) {
		throw new IllegalArgumentException("Bad parameter #" + n + " (" + expected + " expected)");
	}

	public static void notInt(Object value) {
		throw new IllegalArgumentException("Value " + value + " has no integer representation");
	}
}

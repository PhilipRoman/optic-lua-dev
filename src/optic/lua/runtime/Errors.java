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

	public static void attemptToCall(Object func) {
		String type = StandardLibrary.type(func);
		String explanation = type.equals("userdata") ? " (" + func.getClass().getCanonicalName() + ')' : "";
		throw new IllegalArgumentException("attempt to call a " + type + " value" + explanation);
	}
}

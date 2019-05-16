package optic.lua.runtime;

import org.jetbrains.annotations.Contract;

final class Errors {
	@Contract("-> fail")
	static void forbidden() {
		throw new UnsupportedOperationException();
	}

	@Contract("_, _ -> fail")
	static void argument(int n, String expected) {
		throw new IllegalArgumentException("Bad parameter #" + n + " (" + expected + " expected)");
	}

	@Contract("_ -> fail")
	public static void notInt(Object value) {
		throw new IllegalArgumentException("Value " + value + " has no integer representation");
	}

	@Contract("_ -> fail")
	public static void attemptToCall(Object func) {
		String type = StandardLibrary.type(func);
		String explanation = type.equals("userdata") ? " (" + func.getClass().getCanonicalName() + ')' : "";
		throw new IllegalArgumentException("attempt to call a " + type + " value" + explanation);
	}
}

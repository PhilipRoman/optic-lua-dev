package optic.lua.runtime;

import org.jetbrains.annotations.Contract;

final class Errors {
	public static RuntimeException argument(int n, String expected) {
		throw new IllegalArgumentException("Bad parameter #" + n + " (" + expected + " expected)");
	}

	public static RuntimeException attemptTo(String action, Object func) {
		String type = StandardLibrary.type(func);
		String explanation = type.equals("userdata") ? " (" + func.getClass().getCanonicalName() + ')' : "";
		throw new IllegalArgumentException("attempt to " + action + " a " + type + " value" + explanation);
	}
}

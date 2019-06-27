package optic.lua.runtime;

final class Errors {
	public static RuntimeException argument(int n, String expected) {
		return new IllegalArgumentException("Bad parameter #" + n + " (" + expected + " expected)");
	}

	public static RuntimeException attemptTo(String action, Object func) {
		String type = StandardLibrary.type(func);
		String explanation = type.equals("userdata") ? " (" + func.getClass().getCanonicalName() + ')' : "";
		return new IllegalArgumentException("attempt to " + action + " a " + type + " value" + explanation);
	}

	public static RuntimeException cannotConvert(Object value, String type) {
		return new IllegalArgumentException("Can't convert " + StandardLibrary.toString(value) + " to " + type);
	}

	private Errors() {
	}
}

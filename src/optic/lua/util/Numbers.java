package optic.lua.util;

public final class Numbers {
	private Numbers() {
	}

	public static boolean isInt(double x) {
		return Double.isFinite(x) && (int) x == x;
	}

	public static Object normalize(Object x) {
		if (x instanceof Number) {
			var n = (Number) x;
			return isInt(n.doubleValue()) ? n.intValue() : n;
		}
		return x;
	}
}

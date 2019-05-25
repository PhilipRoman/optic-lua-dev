package optic.lua.util;

public final class Numbers {
	private Numbers() {
	}

	public static boolean isInt(double x) {
		return Double.isFinite(x) && (int) x == x;
	}
}

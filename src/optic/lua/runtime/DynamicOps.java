package optic.lua.runtime;

@RuntimeApi
public class DynamicOps {
	@RuntimeApi
	public static double toNumber(Dynamic value) {
		return -1;
	}

	@RuntimeApi
	public static boolean isTruthy(Dynamic value) {
		return false;
	}

	@RuntimeApi
	public static Dynamic operator(Dynamic a, String symbol, Dynamic b) {
		return null;
	}

	@RuntimeApi
	public static MultiValue call(Dynamic function, MultiValue args) {
		return null;
	}
}

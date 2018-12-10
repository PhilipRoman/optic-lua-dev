package optic.lua.runtime;

@RuntimeApi
public class DynamicOps {
	@RuntimeApi
	public static double toNumber(Dynamic value) {
		if (value.type == Dynamic.NUMBER) return ((DynamicNumber) value).value;
		if (value.type == Dynamic.STRING) {
			return Double.parseDouble(((DynamicString) value).value);
		}
		Errors.forbidden();
		return -1;
	}

	@RuntimeApi
	public static double toNumber(DynamicNumber value) {
		return value.value;
	}

	@RuntimeApi
	public static boolean isTruthy(Dynamic value) {
		return value != DynamicNil.nil() && value != DynamicBool.FALSE;
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

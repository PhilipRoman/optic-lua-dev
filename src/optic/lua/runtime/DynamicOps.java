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

	public static int toInt(Dynamic value) {
		if (value.type == Dynamic.NUMBER) {
			double d = ((DynamicNumber) value).value;
			if ((int) d == d) {
				return (int) d;
			} else {
				Errors.notInt(value);
				return -1;
			}
		}
		if (value.type == Dynamic.STRING) {
			try {
				return Integer.parseInt(((DynamicString) value).value);
			} catch (NumberFormatException ignored) {
				// fall through to error handling
			}
		}
		Errors.notInt(value);
		return -1;
	}
}

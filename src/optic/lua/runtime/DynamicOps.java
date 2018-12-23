package optic.lua.runtime;

@RuntimeApi
public class DynamicOps {
	@RuntimeApi
	public static Object operator(Object a, String symbol, Object b) {
		return null;
	}

	@RuntimeApi
	public static Object index(Object obj, Object key) {
		return null;
	}

	@RuntimeApi
	public static void setIndex(Object obj, Object key, Object value) {
	}

	@RuntimeApi
	public static boolean isTrue(Object obj) {
		return !(obj == null) && !(obj == Boolean.FALSE);
	}

	@RuntimeApi
	public static boolean isTrue(boolean b) {
		return b;
	}
}

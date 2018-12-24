package optic.lua.runtime;

import java.util.Objects;

import static optic.lua.runtime.StandardLibrary.toNumber;

@RuntimeApi
public class DynamicOps {
	@RuntimeApi
	public static Object operator(Object a, String symbol, Object b) {
		switch (symbol) {
			case "+":
				return toNumber(a) + toNumber(b);
			case "-":
				return toNumber(a) - toNumber(b);
			case "*":
				return toNumber(a) * toNumber(b);
			case "/":
				return toNumber(a) / toNumber(b);
			case "==":
				if (a instanceof Number && b instanceof Number) {
					return ((Number) a).doubleValue() == ((Number) b).doubleValue();
				}
				if (a instanceof CharSequence && b instanceof CharSequence) {
					return a.toString().contentEquals((CharSequence) b);
				}
				return Objects.equals(a, b);
		}
		return null;
	}

	@RuntimeApi
	public static Object index(Object obj, Object key) {
		if (obj instanceof LuaTable) {
			return ((LuaTable) obj).get(key);
		}
		throw new IllegalArgumentException("attempt to index a " + StandardLibrary.type(obj) + " value");
	}

	@RuntimeApi
	public static void setIndex(Object obj, Object key, Object value) {
		if (obj instanceof LuaTable) {
			((LuaTable) obj).set(key, value);
		}
		throw new IllegalArgumentException("attempt to index a " + StandardLibrary.type(obj) + " value");
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

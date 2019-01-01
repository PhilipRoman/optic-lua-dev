package optic.lua.runtime;

import java.util.Objects;

import static optic.lua.runtime.StandardLibrary.toNumber;

@RuntimeApi
public class DynamicOps {
	public static Object add(Object a, Object b) {
		return (double) a + (double) b;
	}

	public static Object mul(Object a, Object b) {
		return (double) a * (double) b;
	}

	public static Object sub(Object a, Object b) {
		return (double) a - (double) b;
	}

	public static Object div(Object a, Object b) {
		return (double) a / (double) b;
	}

	public static double add(double a, Object b) {
		return a + (double) b;
	}

	public static double mul(double a, Object b) {
		return a * (double) b;
	}

	public static double sub(double a, Object b) {
		return a - (double) b;
	}

	public static double div(double a, Object b) {
		return a / (double) b;
	}

	public static double add(double a, double b) {
		return a + b;
	}

	public static double mul(double a, double b) {
		return a * b;
	}

	public static double sub(double a, double b) {
		return a - b;
	}

	public static double div(double a, double b) {
		return a / b;
	}

	public static boolean eq(double a, double b) {
		return a == b;
	}

	public static boolean eq(Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			return ((Number) a).doubleValue() == ((Number) b).doubleValue();
		}
		if (a instanceof CharSequence && b instanceof CharSequence) {
			return a.toString().contentEquals((CharSequence) b);
		}
		return Objects.equals(a, b);
	}

	public static boolean le(double a, double b) {
		return a <= b;
	}

	public static boolean le(Object a, Object b) {
		return (double) a <= (double) b;
	}

	public static boolean lt(double a, double b) {
		return a < b;
	}

	public static boolean lt(Object a, Object b) {
		return (double) a < (double) b;
	}

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
				return eq(a, b);
			case "..":
				Objects.requireNonNull(a);
				Objects.requireNonNull(b);
				return StandardLibrary.toString(a) + StandardLibrary.toString(b);
		}
		return null;
	}

	@RuntimeApi
	public static Object operator(double a, String symbol, Object b) {
		switch (symbol) {
			case "+":
				return a + toNumber(b);
			case "-":
				return a - toNumber(b);
			case "*":
				return a * toNumber(b);
			case "/":
				return a / toNumber(b);
			case "==":
				if (b instanceof Number) {
					return a == ((Number) b).doubleValue();
				}
				if (b instanceof CharSequence) {
					return false;
				}
				return false;
			case "..":
				Objects.requireNonNull(b);
				return StandardLibrary.toString(a) + StandardLibrary.toString(b);
		}
		return null;
	}

	@RuntimeApi
	public static Object operator(double a, String symbol, double b) {
		switch (symbol) {
			case "+":
				return a + b;
			case "-":
				return a - b;
			case "*":
				return a * b;
			case "/":
				return a / b;
			case "==":
				return a == b;
			case "..":
				return StandardLibrary.toString(a) + StandardLibrary.toString(b);
		}
		return null;
	}

	@RuntimeApi
	public static Object index(Object obj, Object key) {
		if (obj instanceof LuaTable) {
			return ((LuaTable) obj).get(key);
		}
		throw new IllegalArgumentException("attempt to index a " + StandardLibrary.type(obj) + " value (table=" + obj + ", key=" + key + ")");
	}

	@RuntimeApi
	public static void setIndex(Object obj, Object key, Object value) {
		if (obj instanceof LuaTable) {
			((LuaTable) obj).set(key, value);
		} else {
			throw new IllegalArgumentException("attempt to index a " + StandardLibrary.type(obj) + " value (table=" + obj + ", key=" + key + ", value=" + value + ")");
		}
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

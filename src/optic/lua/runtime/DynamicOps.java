package optic.lua.runtime;

import java.util.Objects;

@RuntimeApi
public class DynamicOps {
	public static Object add(LuaContext ctx, Object a, Object b) {
		return StandardLibrary.toNumber(a) + StandardLibrary.toNumber(b);
	}

	public static Object mul(LuaContext ctx, Object a, Object b) {
		return StandardLibrary.toNumber(a) * StandardLibrary.toNumber(b);
	}

	public static Object sub(LuaContext ctx, Object a, Object b) {
		return StandardLibrary.toNumber(a) - StandardLibrary.toNumber(b);
	}

	public static Object div(LuaContext ctx, Object a, Object b) {
		return StandardLibrary.toNumber(a) / StandardLibrary.toNumber(b);
	}

	public static double add(LuaContext ctx, double a, Object b) {
		return a + StandardLibrary.toNumber(b);
	}

	public static double mul(LuaContext ctx, double a, Object b) {
		return a * StandardLibrary.toNumber(b);
	}

	public static double sub(LuaContext ctx, double a, Object b) {
		return a - StandardLibrary.toNumber(b);
	}

	public static double div(LuaContext ctx, double a, Object b) {
		return a / StandardLibrary.toNumber(b);
	}

	public static long add(LuaContext ctx, long a, long b) {
		return a + b;
	}

	public static long mul(LuaContext ctx, long a, long b) {
		return a * b;
	}

	public static long sub(LuaContext ctx, long a, long b) {
		return a - b;
	}

	public static double div(LuaContext ctx, long a, long b) {
		return a / (double) b;
	}

	public static double add(LuaContext ctx, double a, double b) {
		return a + b;
	}

	public static double mul(LuaContext ctx, double a, double b) {
		return a * b;
	}

	public static double sub(LuaContext ctx, double a, double b) {
		return a - b;
	}

	public static double div(LuaContext ctx, double a, double b) {
		return a / b;
	}

	public static boolean eq(LuaContext ctx, double a, double b) {
		return a == b;
	}

	public static boolean eq(LuaContext ctx, Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			return ((Number) a).doubleValue() == ((Number) b).doubleValue();
		}
		if (a instanceof CharSequence && b instanceof CharSequence) {
			return a.toString().contentEquals((CharSequence) b);
		}
		return Objects.equals(a, b);
	}

	public static boolean le(LuaContext ctx, double a, double b) {
		return a <= b;
	}

	public static boolean le(LuaContext ctx, Object a, Object b) {
		return StandardLibrary.toNumber(a) <= StandardLibrary.toNumber(b);
	}

	public static boolean lt(LuaContext ctx, double a, double b) {
		return a < b;
	}

	public static boolean lt(LuaContext ctx, Object a, Object b) {
		return StandardLibrary.toNumber(a) < StandardLibrary.toNumber(b);
	}

	public static String concat(LuaContext ctx, Object a, Object b) {
		return a.toString() + b;
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

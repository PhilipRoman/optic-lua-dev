package optic.lua.runtime;

import optic.lua.util.Numbers;

import java.util.Objects;

@RuntimeApi
public class DynamicOps {
	private static double toNum(Object x) {
		return StandardLibrary.strictToNumber(x);
	}

	static long toInt(Object a) {
		if (a.getClass() == Long.class || a.getClass() == Integer.class) {
			return ((Number) a).longValue();
		}
		double d = StandardLibrary.strictToNumber(a);
		if (Numbers.isInt(d)) {
			return (long) d;
		}
		throw new IllegalArgumentException("value " + StandardLibrary.toString(a) + " has no integer representation");
	}

	public static Object add(LuaContext ctx, Object a, Object b) {
		return toNum(a) + toNum(b);
	}

	public static Object mul(LuaContext ctx, Object a, Object b) {
		return toNum(a) * toNum(b);
	}

	public static Object sub(LuaContext ctx, Object a, Object b) {
		return toNum(a) - toNum(b);
	}

	public static Object div(LuaContext ctx, Object a, Object b) {
		return toNum(a) / toNum(b);
	}

	public static double add(LuaContext ctx, double a, Object b) {
		return a + toNum(b);
	}

	public static double mul(LuaContext ctx, double a, Object b) {
		return a * toNum(b);
	}

	public static double sub(LuaContext ctx, double a, Object b) {
		return a - toNum(b);
	}

	public static double div(LuaContext ctx, double a, Object b) {
		return a / toNum(b);
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

	public static Object mod(LuaContext ctx, Object a, Object b) {
		return toNum(a) % toNum(b);
	}

	public static long mod(LuaContext ctx, long a, long b) {
		return a % b;
	}

	public static double mod(LuaContext ctx, double a, double b) {
		return a % b;
	}

	public static double mod(LuaContext ctx, Object a, double b) {
		return toNum(a) % b;
	}

	public static double mod(LuaContext ctx, Object a, long b) {
		return toNum(a) % b;
	}

	public static Object pow(LuaContext ctx, Object a, Object b) {
		return Math.pow(toNum(a), toNum(b));
	}

	public static double pow(LuaContext ctx, Object a, double b) {
		return Math.pow(toNum(a), b);
	}

	public static double pow(LuaContext ctx, double a, Object b) {
		return Math.pow(a, toNum(b));
	}

	public static double pow(LuaContext ctx, long base, long exp) {
		return Math.pow(base, exp);
		/*long result = 1;
		while (exp != 0) {
			if ((exp & 1) == 1)
				result *= base;
			exp >>= 1;
			base *= base;
		}
		return result;*/
	}

	public static double pow(LuaContext ctx, double a, double b) {
		return Math.pow(a, b);
	}

	public static double pow(LuaContext ctx, int base, int exp) {
		return Math.pow(base, exp);
		/*long result = 1;
		while (exp != 0) {
			if ((exp & 1) == 1)
				result *= base;
			exp >>= 1;
			base *= base;
		}
		return result;*/
	}

	public static long bor(LuaContext ctx, long a, long b) {
		return a | b;
	}

	public static long bxor(LuaContext ctx, long a, long b) {
		return a ^ b;
	}

	public static long band(LuaContext ctx, long a, long b) {
		return a & b;
	}

	public static long shl(LuaContext ctx, long a, long b) {
		return a << b;
	}

	public static long shr(LuaContext ctx, long a, long b) {
		return a >> b;
	}

	public static long bor(LuaContext ctx, Object a, Object b) {
		return toInt(a) | toInt(b);
	}

	public static long bxor(LuaContext ctx, Object a, Object b) {
		return toInt(a) ^ toInt(b);
	}

	public static long band(LuaContext ctx, Object a, Object b) {
		return toInt(a) & toInt(b);
	}

	public static long shl(LuaContext ctx, Object a, Object b) {
		return toInt(a) << toInt(b);
	}

	public static long shr(LuaContext ctx, Object a, Object b) {
		return toInt(a) >> toInt(b);
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
		return toNum(a) <= toNum(b);
	}

	public static boolean lt(LuaContext ctx, double a, double b) {
		return a < b;
	}

	public static boolean lt(LuaContext ctx, Object a, Object b) {
		return toNum(a) < toNum(b);
	}

	public static boolean ge(LuaContext ctx, double a, double b) {
		return a >= b;
	}

	public static boolean ge(LuaContext ctx, Object a, Object b) {
		return toNum(a) >= toNum(b);
	}

	public static boolean gt(LuaContext ctx, double a, double b) {
		return a > b;
	}

	public static boolean gt(LuaContext ctx, Object a, Object b) {
		return toNum(a) > toNum(b);
	}

	public static Object bnot(LuaContext ctx, Object i) {
		return ~toInt(i);
	}

	public static long bnot(LuaContext ctx, long i) {
		return ~i;
	}

	public static int len(LuaContext ctx, Object value) {
		return value instanceof CharSequence
				? ((CharSequence) value).length()
				: ((LuaTable) value).length();
	}

	public static String concat(LuaContext ctx, Object a, Object b) {
		return StandardLibrary.strictToString(a) + StandardLibrary.strictToString(b);
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

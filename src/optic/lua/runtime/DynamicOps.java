package optic.lua.runtime;

import java.util.Objects;

@RuntimeApi
public class DynamicOps {
	public static Object add(Object a, Object b) {
		return StandardLibrary.toNumber(a) + StandardLibrary.toNumber(b);
	}

	public static Object mul(Object a, Object b) {
		return StandardLibrary.toNumber(a) * StandardLibrary.toNumber(b);
	}

	public static Object sub(Object a, Object b) {
		return StandardLibrary.toNumber(a) - StandardLibrary.toNumber(b);
	}

	public static Object div(Object a, Object b) {
		return StandardLibrary.toNumber(a) / StandardLibrary.toNumber(b);
	}

	public static double add(double a, Object b) {
		return a + StandardLibrary.toNumber(b);
	}

	public static double mul(double a, Object b) {
		return a * StandardLibrary.toNumber(b);
	}

	public static double sub(double a, Object b) {
		return a - StandardLibrary.toNumber(b);
	}

	public static double div(double a, Object b) {
		return a / StandardLibrary.toNumber(b);
	}

	public static long add(long a, long b) {
		return a + b;
	}

	public static long mul(long a, long b) {
		return a * b;
	}

	public static long sub(long a, long b) {
		return a - b;
	}

	public static double div(long a, long b) {
		return a / (double)b;
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
		return StandardLibrary.toNumber(a) <= StandardLibrary.toNumber(b);
	}

	public static boolean lt(double a, double b) {
		return a < b;
	}

	public static boolean lt(Object a, Object b) {
		return StandardLibrary.toNumber(a) < StandardLibrary.toNumber(b);
	}

	public static String concat(Object a, Object b) {
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

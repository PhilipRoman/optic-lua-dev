package optic.lua.runtime;

import java.util.*;

public class StandardLibrary {
	private static final boolean USE_CLASS_PREDICTION = true;

	public static double toNumber(double d) {
		return d;
	}

	public static double toNumber(Double d) {
		Objects.requireNonNull(d);
		return d;
	}

	public static Double toNumber(String s) {
		Objects.requireNonNull(s);
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static Double toNumber(Object o) {
		if (o == null) {
			return null;
		}
		if (USE_CLASS_PREDICTION && o.getClass() == String.class) {
			return toNumber((String) o);
		}
		if (USE_CLASS_PREDICTION && o.getClass() == Double.class) {
			return (double) o;
		}
		if (o instanceof CharSequence) {
			return toNumber(o.toString());
		}
		if (o instanceof Number) {
			return ((Number) o).doubleValue();
		}
		return null;
	}

	public static CharSequence toString(Object o) {
		if (o instanceof CharSequence) {
			return (CharSequence) o;
		}
		return Objects.toString(o, "nil");
	}

	public static String toString(double d) {
		return Double.toString(d);
	}

	public static void print(Object... o) {
		int lim = o.length - 1;
		for (int i = 0; i < lim; i++) {
			System.out.print(o[i]);
			System.out.write('\t');
		}
		System.out.println(o[lim]);
	}

	public static void print(double d) {
		System.out.println(d);
	}

	public static void print(CharSequence s) {
		System.out.println(s == null ? "nil" : s);
	}

	public static void print(String s) {
		System.out.println(s == null ? "nil" : s);
	}

	public static void print(Object s) {
		System.out.println(s == null ? "nil" : s);
	}

	public static CharSequence tableConcat(Object arg) {
		if (arg instanceof LuaTable) {
			return tableConcat((LuaTable) arg);
		}
		throw new IllegalArgumentException("Expected table, got " + arg);
	}

	public static CharSequence tableConcat(LuaTable table) {
		int length = table.length();
		ArrayList<?> array = table.array;
		int size = 0;
		for (int i = 0; i < length; i++) {
			Object o = array.get(i);
			if (o instanceof Number) {
				size += 8;
			} else if (o instanceof CharSequence) {
				size += ((CharSequence) o).length();
			} else {
				throw new IllegalArgumentException("Illegal value (" + toString(o) + "), expected string or number");
			}
		}
		StringBuilder builder = new StringBuilder(size);
		for (int i = 0; i < length; i++) {
			Object o = array.get(i);
			builder.append(o);
		}
		return builder;
	}
}

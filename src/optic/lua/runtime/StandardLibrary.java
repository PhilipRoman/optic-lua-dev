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
		if (USE_CLASS_PREDICTION && o.getClass() == Double.class) {
			return (double) o;
		}
		if (USE_CLASS_PREDICTION && o.getClass() == String.class) {
			return toNumber((String) o);
		}
		if (o instanceof Number) {
			return ((Number) o).doubleValue();
		}
		if (o instanceof CharSequence) {
			return toNumber(o.toString());
		}
		return null;
	}

	public static String toString(Object o) {
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
		int size = 0;
		for (int i = 1; i <= length; i++) {
			Object o = table.get(i);
			if (o instanceof Number) {
				size += 8;
			} else if (o instanceof CharSequence) {
				size += ((CharSequence) o).length();
			} else {
				throw new IllegalArgumentException("Illegal value (" + toString(o) + "), expected string or number");
			}
		}
		StringBuilder builder = new StringBuilder(size);
		for (int i = 1; i <= length; i++) {
			Object o = table.get(i);
			builder.append(o);
		}
		return builder.length() < 16 ? builder.toString() : builder;
	}

	public static String type(Object x) {
		if (x == null) {
			return "nil";
		}
		if (x instanceof CharSequence) {
			return "string";
		}
		if (x instanceof Number) {
			return "number";
		}
		if (x instanceof LuaFunction) {
			return "function";
		}
		if (x instanceof LuaTable) {
			return "table";
		}
		if (x.getClass() == Boolean.class) {
			return "bool";
		}
		return "userdata";
	}
}

package optic.lua.runtime;

import optic.lua.util.Numbers;

import java.io.*;
import java.util.*;

public class StandardLibrary {
	public static double toNumber(double d) {
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
		if (o instanceof Number) {
			return ((Number) o).doubleValue();
		}
		if (o instanceof CharSequence) {
			return toNumber(o.toString());
		}
		return null;
	}

	static double strictToNumber(Object o) {
		if (o == null) {
			throw new IllegalArgumentException(toString(o));
		}
		if (o instanceof Number) {
			return ((Number) o).doubleValue();
		}
		if (o instanceof CharSequence) {
			try {
				return Double.parseDouble(o.toString());
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("\"" + o + "\" is not a valid number: " + e.getMessage());
			}
		}
		throw new IllegalArgumentException(toString(o));
	}

	public static String toString(Object o) {
		if (o instanceof Number) {
			double d = ((Number) o).doubleValue();
			return Numbers.isInt(d) ? Long.toString((long) d) : Double.toString(d);
		}
		return Objects.toString(o, "nil");
	}

	public static String toString(double d) {
		return Double.toString(d);
	}

	public static void print(PrintWriter out, Object... o) {
		int lim = o.length - 1;
		for (int i = 0; i < lim; i++) {
			out.print(toString(o[i]));
			out.write('\t');
		}
		out.println(toString(o[lim]));
		out.flush();
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
		System.err.println(x);
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

	public static String strictToString(Object x) {
		if (x instanceof CharSequence || x instanceof Number) {
			return x.toString();
		}
		throw new IllegalArgumentException(toString(x));
	}
}

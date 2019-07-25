package optic.lua.runtime;

import java.lang.reflect.Array;
import java.util.Objects;

@RuntimeApi
public final class DynamicOps {
	@RuntimeApi
	public static double toNum(Object o) {
		if (o == null) {
			throw new NullPointerException();
		}
		if (o instanceof Number) {
			return ((Number) o).doubleValue();
		}
		if (o instanceof CharSequence) {
			try {
				return Double.parseDouble(o.toString());
			} catch (NumberFormatException e) {
				// fall through
			}
		}
		throw Errors.cannotConvert(o, "number");
	}

	@RuntimeApi
	public static long toInt(Object a) {
		if (a.getClass() == Long.class || a.getClass() == Integer.class) {
			return ((Number) a).longValue();
		}
		double d = toNum(a);
		long i = (long) d;
		if (i == d) {
			return i;
		}
		throw Errors.cannotConvert(a, "integer");
	}

	private static int toInt32(Object a) {
		if (a.getClass() == Long.class || a.getClass() == Integer.class) {
			return ((Number) a).intValue();
		}
		double d = toNum(a);
		int i = (int) d;
		if (i == d) {
			return i;
		}
		throw Errors.cannotConvert(a, "integer (32 bit)");
	}

	@RuntimeApi
	public static long toInt(long n) {
		return n;
	}

	@RuntimeApi
	public static double toNum(double n) {
		return n;
	}

	@RuntimeApi
	public static Object getObjArray(Object array, long len) {
		return ((Object[]) array)[(int) len];
	}

	@RuntimeApi
	public static Object add(LuaContext ctx, Object a, Object b) {
		return toNum(a) + toNum(b);
	}

	@RuntimeApi
	public static Object mul(LuaContext ctx, Object a, Object b) {
		return toNum(a) * toNum(b);
	}

	@RuntimeApi
	public static Object sub(LuaContext ctx, Object a, Object b) {
		return toNum(a) - toNum(b);
	}

	@RuntimeApi
	public static Object div(LuaContext ctx, Object a, Object b) {
		return toNum(a) / toNum(b);
	}

	@RuntimeApi
	public static double add(LuaContext ctx, double a, Object b) {
		return a + toNum(b);
	}

	@RuntimeApi
	public static double mul(LuaContext ctx, double a, Object b) {
		return a * toNum(b);
	}

	@RuntimeApi
	public static double sub(LuaContext ctx, double a, Object b) {
		return a - toNum(b);
	}

	@RuntimeApi
	public static double div(LuaContext ctx, double a, Object b) {
		return a / toNum(b);
	}

	@RuntimeApi
	public static long add(LuaContext ctx, long a, long b) {
		return a + b;
	}

	@RuntimeApi
	public static long mul(LuaContext ctx, long a, long b) {
		return a * b;
	}

	@RuntimeApi
	public static long sub(LuaContext ctx, long a, long b) {
		return a - b;
	}

	@RuntimeApi
	public static double div(LuaContext ctx, long a, long b) {
		return a / (double) b;
	}

	@RuntimeApi
	public static double add(LuaContext ctx, double a, double b) {
		return a + b;
	}

	@RuntimeApi
	public static double mul(LuaContext ctx, double a, double b) {
		return a * b;
	}

	@RuntimeApi
	public static double sub(LuaContext ctx, double a, double b) {
		return a - b;
	}

	@RuntimeApi
	public static double div(LuaContext ctx, double a, double b) {
		return a / b;
	}

	@RuntimeApi
	public static Object mod(LuaContext ctx, Object a, Object b) {
		return toNum(a) % toNum(b);
	}

	@RuntimeApi
	public static long mod(LuaContext ctx, long a, long b) {
		return a % b;
	}

	@RuntimeApi
	public static double mod(LuaContext ctx, double a, double b) {
		return a % b;
	}

	@RuntimeApi
	public static double mod(LuaContext ctx, Object a, double b) {
		return toNum(a) % b;
	}

	@RuntimeApi
	public static double mod(LuaContext ctx, Object a, long b) {
		return toNum(a) % b;
	}

	@RuntimeApi
	public static Object pow(LuaContext ctx, Object a, Object b) {
		return Math.pow(toNum(a), toNum(b));
	}

	@RuntimeApi
	public static double pow(LuaContext ctx, Object a, double b) {
		return Math.pow(toNum(a), b);
	}

	@RuntimeApi
	public static double pow(LuaContext ctx, double a, Object b) {
		return Math.pow(a, toNum(b));
	}

	@RuntimeApi
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

	@RuntimeApi
	public static double pow(LuaContext ctx, double a, double b) {
		return Math.pow(a, b);
	}

	@RuntimeApi
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

	@RuntimeApi
	public static long bor(LuaContext ctx, long a, long b) {
		return a | b;
	}

	@RuntimeApi
	public static long bxor(LuaContext ctx, long a, long b) {
		return a ^ b;
	}

	@RuntimeApi
	public static long band(LuaContext ctx, long a, long b) {
		return a & b;
	}

	@RuntimeApi
	public static long shl(LuaContext ctx, long a, long b) {
		return a << b;
	}

	@RuntimeApi
	public static long shr(LuaContext ctx, long a, long b) {
		return a >> b;
	}

	@RuntimeApi
	public static long bor(LuaContext ctx, Object a, Object b) {
		return toInt(a) | toInt(b);
	}

	@RuntimeApi
	public static long bxor(LuaContext ctx, Object a, Object b) {
		return toInt(a) ^ toInt(b);
	}

	@RuntimeApi
	public static long band(LuaContext ctx, Object a, Object b) {
		return toInt(a) & toInt(b);
	}

	@RuntimeApi
	public static long shl(LuaContext ctx, Object a, Object b) {
		return toInt(a) << toInt(b);
	}

	@RuntimeApi
	public static long shr(LuaContext ctx, Object a, Object b) {
		return toInt(a) >> toInt(b);
	}

	@RuntimeApi
	public static boolean eq(LuaContext ctx, double a, double b) {
		return a == b;
	}

	@RuntimeApi
	public static boolean eq(LuaContext ctx, Object a, Object b) {
		if (a instanceof Number && b instanceof Number) {
			return ((Number) a).doubleValue() == ((Number) b).doubleValue();
		}
		if (a instanceof CharSequence && b instanceof CharSequence) {
			return a.toString().contentEquals((CharSequence) b);
		}
		return Objects.equals(a, b);
	}

	@RuntimeApi
	public static boolean le(LuaContext ctx, double a, double b) {
		return a <= b;
	}

	@RuntimeApi
	public static boolean le(LuaContext ctx, Object a, Object b) {
		return toNum(a) <= toNum(b);
	}

	@RuntimeApi
	public static boolean lt(LuaContext ctx, double a, double b) {
		return a < b;
	}

	@RuntimeApi
	public static boolean lt(LuaContext ctx, Object a, Object b) {
		return toNum(a) < toNum(b);
	}

	@RuntimeApi
	public static boolean ge(LuaContext ctx, double a, double b) {
		return a >= b;
	}

	@RuntimeApi
	public static boolean ge(LuaContext ctx, Object a, Object b) {
		return toNum(a) >= toNum(b);
	}

	@RuntimeApi
	public static boolean gt(LuaContext ctx, double a, double b) {
		return a > b;
	}

	@RuntimeApi
	public static boolean gt(LuaContext ctx, Object a, Object b) {
		return toNum(a) > toNum(b);
	}

	@RuntimeApi
	public static Object bnot(LuaContext ctx, Object i) {
		return ~toInt(i);
	}

	@RuntimeApi
	public static long bnot(LuaContext ctx, long i) {
		return ~i;
	}

	@RuntimeApi
	public static int len(LuaContext ctx, Object value) {
		if (value instanceof CharSequence)
			return ((CharSequence) value).length();
		else if (value instanceof LuaTable)
			return ((LuaTable) value).length();
		else if (value != null && value.getClass().isArray())
			return Array.getLength(value);
		else
			throw Errors.attemptTo("get length of", value);
	}

	@RuntimeApi
	public static String concat(LuaContext ctx, Object a, Object b) {
		return StandardLibrary.strictToString(a) + StandardLibrary.strictToString(b);
	}

	@RuntimeApi
	public static boolean toBool(Object obj) {
		return !(obj == null) && !(obj == Boolean.FALSE);
	}

	@RuntimeApi
	public static LuaTable table(Object... entries) {
		LuaTable table = new LuaTable();
		for (int i = 0; i < entries.length; i += 2) {
			table.set(entries[i], entries[i + 1]);
		}
		return table;
	}

	@RuntimeApi
	public static LuaTable table() {
		return new LuaTable();
	}

	@RuntimeApi
	public static LuaTable table(Object k1, Object v1) {
		LuaTable table = new LuaTable();
		table.set(k1, v1);
		return table;
	}

	@RuntimeApi
	public static LuaTable table(Object k1, Object v1, Object k2, Object v2) {
		LuaTable table = new LuaTable();
		table.set(k1, v1);
		table.set(k2, v2);
		return table;
	}

	@RuntimeApi
	public static LuaTable varargTable(long key, Object[] trailing, Object... entries) {
		LuaTable table = new LuaTable();
		for (int i = 0; i < entries.length; i += 2) {
			table.set(entries[i], entries[i + 1]);
		}
		int keyIndex = (int) key;
		for (int i = 0; i < trailing.length; i++) {
			table.set((double) (keyIndex + i), trailing[i]);
		}
		return table;
	}

	@RuntimeApi
	public static Object index(Object obj, Object key) {
		if (obj instanceof LuaTable) {
			return ((LuaTable) obj).get(key);
		}
		if (obj != null && obj.getClass().isArray()) {
			return Array.get(obj, toInt32(key) - 1);
		}
		throw Errors.attemptTo("index", obj);
	}

	@RuntimeApi
	public static Object index(Object obj, String key) {
		if (obj instanceof LuaTable) {
			return ((LuaTable) obj).get(key);
		}
		throw Errors.attemptTo("index", obj);
	}

	@RuntimeApi
	public static Object index(Object obj, long key) {
		if (obj instanceof LuaTable) {
			return ((LuaTable) obj).get(key);
		}
		if (obj != null && obj.getClass().isArray()) {
			return Array.get(obj, Math.toIntExact(key - 1));
		}
		throw Errors.attemptTo("index", obj);
	}

	@RuntimeApi
	public static void setIndex(Object obj, Object key, Object value) {
		if (obj instanceof LuaTable) {
			((LuaTable) obj).set(key, value);
		} else if (obj != null && obj.getClass().isArray()) {
			Array.set(obj, toInt32(key) - 1, value);
		} else {
			throw Errors.attemptTo("index", obj);
		}
	}

	@RuntimeApi
	public static void setIndex(Object obj, String key, Object value) {
		if (obj instanceof LuaTable) {
			((LuaTable) obj).set(key, value);
		} else {
			throw Errors.attemptTo("index", obj);
		}
	}

	@RuntimeApi
	public static void setIndex(Object obj, long key, Object value) {
		if (obj instanceof LuaTable) {
			((LuaTable) obj).set(key, value);
		} else if (obj != null && obj.getClass().isArray()) {
			Array.set(obj, Math.toIntExact(key - 1), value);
		} else {
			throw Errors.attemptTo("index", obj);
		}
	}

	@RuntimeApi
	public static Object[] call(LuaContext context, Object func, Object[] args) {
		if (func instanceof LuaFunction) {
			return ((LuaFunction) func).call(context, args);
		}
		throw Errors.attemptTo("call", func);
	}

	private DynamicOps() {
	}
}

package optic.lua.runtime;

import java.util.*;

@RuntimeApi
public final class EnvOps {
	@RuntimeApi
	public static Object get(UpValue _ENV, String key) {
		return ((LuaTable) _ENV.value).get(key);
	}

	@RuntimeApi
	public static void set(UpValue _ENV, String key, Object value) {
		((LuaTable) _ENV.value).set(key, value);
	}

	private static int adjustFromIndex(int from, int length) {
		return from < 0 ? length + from : from - 1;
	}

	private static int adjustToIndex(int to, int length) {
		if (to < 0) {
			to = length + to + 1;
		}
		return (to <= length) ? to : length;
	}

	public static LuaTable createEnv() {
		LuaTable env = new LuaTable();
		env.set("print", new LuaFunction("print") {
			public Object[] call(LuaContext context1, Object... args) {
				StandardLibrary.print(context1.out, args);
				return ListOps.EMPTY;
			}
		});
		HashMap<Object, Object> optic = new HashMap<>(5);
		env.set("optic", LuaTable.ofMap(optic));
		env.set("pairs", new LuaFunction("pairs") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				LuaTable table = (LuaTable) args[0];
				return new Object[]{table.pairsIterator()};
			}
		});
		env.set("ipairs", new LuaFunction("pairs") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				LuaTable table = (LuaTable) args[0];
				return new Object[]{table.ipairsIterator()};
			}
		});
		env.set("type", new LuaFunction("type") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				if (args.length == 0) {
					throw new IllegalArgumentException("Bad argument #1, expected value");
				}
				Object value = args[0];
				return ListOps.list(StandardLibrary.type(value));
			}
		});
		env.set("tostring", new LuaFunction("tostring") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				return ListOps.list(StandardLibrary.toString(args[0]));
			}
		});
		env.set("tonumber", new LuaFunction("tonumber") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				return ListOps.list(StandardLibrary.toNumber(args[0]));
			}
		});
		HashMap<Object, Object> tableLib = new HashMap<>(1);
		tableLib.put("concat", new LuaFunction("table.concat") {
			public Object[] call(LuaContext context, Object... args) {
				if (args.length == 0) {
					throw new IllegalArgumentException("Bad argument #1, expected value");
				}
				Object table = args[0];
				Object delimiter = ListOps.get(args, 1);
				String delimiterString = delimiter == null ? "" : StandardLibrary.strictToString(delimiter);
				return ListOps.list(StandardLibrary.tableConcat(table, delimiterString));
			}
		});
		env.set("table", LuaTable.ofMap(tableLib));
		env.set("assert", new LuaFunction("assert") {
			public Object[] call(LuaContext context, Object... args) {
				if (args.length == 0) {
					throw Errors.argument(1, "value");
				}
				if (DynamicOps.isTrue(args[0])) {
					return args;
				} else {
					String msg = args.length >= 2 ? StandardLibrary.toString(args[1]) : "Assertion failed!";
					throw new AssertionError(msg);
				}
			}
		});
		env.set("error", new LuaFunction("error") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				throw new RuntimeException(args.length > 0 ? StandardLibrary.toString(args[0]) : "");
			}
		});
		env.set("pcall", new LuaFunction("pcall") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				if (args.length == 0) {
					throw Errors.argument(1, "value");
				}
				Object f = args[0];
				Object[] params = ListOps.sublist(args, 1);
				try {
					Object[] result = DynamicOps.call(context, f, params);
					return ListOps.append(result, true);
				} catch (RuntimeException e) {
					String msg = e.getMessage();
					if (msg == null) {
						return ListOps.listWithBoolean(false);
					}
					return ListOps.list(false, msg);
				}
			}
		});
		HashMap<Object, Object> os = new HashMap<>(1);
		os.put("time", new LuaFunction("os.time") {
			public Object[] call(LuaContext context, Object... args) {
				return ListOps.list((System.currentTimeMillis() / 1000));
			}
		});
		env.set("os", LuaTable.ofMap(os));
		HashMap<Object, Object> stringLib = new HashMap<>(8);
		stringLib.put("sub", new LuaFunction("string.sub") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				String str = StandardLibrary.strictToString(args[0]);
				int from = (int) DynamicOps.toInt(args[1]);
				from = adjustFromIndex(from, str.length());
				Object thirdArgument = ListOps.get(args, 2);
				int to = thirdArgument == null ? str.length() : (int) DynamicOps.toInt(thirdArgument);
				to = adjustToIndex(to, str.length());
				if (to < from || from >= str.length()) {
					return ListOps.list("");
				}
				return ListOps.list(str.substring(from, to));
			}
		});
		stringLib.put("len", new LuaFunction("string.len") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				String str = StandardLibrary.strictToString(args[0]);
				return ListOps.list(str.length());
			}
		});
		stringLib.put("lower", new LuaFunction("string.lower") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				String str = StandardLibrary.strictToString(args[0]);
				return ListOps.list(str.toLowerCase());
			}
		});
		stringLib.put("upper", new LuaFunction("string.upper") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				String str = StandardLibrary.strictToString(args[0]);
				return ListOps.list(str.toUpperCase());
			}
		});
		stringLib.put("format", new LuaFunction("string.format") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				String str = StandardLibrary.strictToString(args[0]);
				Object[] params = Arrays.copyOfRange(args, 1, args.length);
				return ListOps.list(String.format(str, params));
			}
		});
		stringLib.put("rep", new LuaFunction("string.rep") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				String str = StandardLibrary.strictToString(args[0]);
				int n = (int) DynamicOps.toInt(args[1]);
				return ListOps.list(str.repeat(Math.max(0, n)));
			}
		});
		stringLib.put("byte", new LuaFunction("string.byte") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				String str = StandardLibrary.strictToString(args[0]);
				Object secondArgument = ListOps.get(args, 1);
				int from = secondArgument == null ? 1 : (int) DynamicOps.toInt(secondArgument);
				Object thirdArgument = ListOps.get(args, 2);
				int to = thirdArgument == null ? (secondArgument == null ? 1 : from) : (int) DynamicOps.toInt(thirdArgument);
				from = adjustFromIndex(from, str.length());
				to = adjustToIndex(to, str.length());
				if (to < from || from >= str.length()) {
					return ListOps.EMPTY;
				}
				int length = to - from;
				Object[] result = new Object[length];
				for (int i = 0; i < length; i++) {
					result[i] = (byte) str.charAt(from + i);
				}
				return result;
			}
		});
		env.set("string", LuaTable.ofMap(stringLib));
		HashMap<Object, Object> math = new HashMap<>(16);
		math.put("sqrt", new LuaFunction("math.sqrt") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return ListOps.list(Math.sqrt(value));
			}
		});
		math.put("sin", new LuaFunction("math.sin") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return ListOps.list(Math.sin(value));
			}
		});
		math.put("cos", new LuaFunction("math.cos") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return ListOps.list(Math.cos(value));
			}
		});
		math.put("atan2", new LuaFunction("math.atan2") {
			public Object[] call(LuaContext context, Object... args) {
				double y = DynamicOps.toNum(args[0]);
				double x = DynamicOps.toNum(args[1]);
				return ListOps.list(Math.atan2(y, x));
			}
		});
		math.put("atan", new LuaFunction("math.atan") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return ListOps.list(Math.atan(value));
			}
		});
		math.put("abs", new LuaFunction("math.abs") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return ListOps.list(Math.abs(value));
			}
		});
		math.put("ceil", new LuaFunction("math.ceil") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return ListOps.list(Math.ceil(value));
			}
		});
		math.put("floor", new LuaFunction("math.floor") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return ListOps.list(Math.floor(value));
			}
		});
		math.put("deg", new LuaFunction("math.deg") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return ListOps.list(Math.toDegrees(value));
			}
		});
		math.put("exp", new LuaFunction("math.exp") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return ListOps.list(Math.exp(value));
			}
		});
		math.put("huge", Double.POSITIVE_INFINITY);
		math.put("maxinteger", Long.MAX_VALUE);
		math.put("mininteger", Long.MIN_VALUE);
		math.put("pi", Math.PI);
		math.put("log", new LuaFunction("math.log") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return ListOps.list(Math.log(value));
			}
		});
		math.put("rad", new LuaFunction("math.rad") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return ListOps.list(Math.toRadians(value));
			}
		});
		math.put("tan", new LuaFunction("math.tan") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return ListOps.list(Math.tan(value));
			}
		});
		Object[] numberTypeFloat = {"number"};
		Object[] numberTypeInt = {"integer"};
		math.put("type", new LuaFunction("math.type") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return (long) value == value ? numberTypeInt : numberTypeFloat;
			}
		});
		math.put("max", new LuaFunction("math.max") {
			public Object[] call(LuaContext context, Object... args) {
				double a = DynamicOps.toNum(args[0]);
				double b = DynamicOps.toNum(args[1]);
				return ListOps.list(Math.max(a, b));
			}
		});
		math.put("min", new LuaFunction("math.min") {
			public Object[] call(LuaContext context, Object... args) {
				double a = DynamicOps.toNum(args[0]);
				double b = DynamicOps.toNum(args[1]);
				return ListOps.list(Math.min(a, b));
			}
		});
		math.put("ult", new LuaFunction("math.ult") {
			public Object[] call(LuaContext context, Object... args) {
				long a = DynamicOps.toInt(args[0]);
				long b = DynamicOps.toInt(args[1]);
				// java.lang.Long::compareUnsigned
				return ListOps.listWithBoolean(a + Long.MIN_VALUE < b + Long.MIN_VALUE);
			}
		});
		env.set("math", LuaTable.ofMap(math));
		env.set("_VERSION", "optic-lua [pre-alpha]");
		return env;
	}

	private EnvOps() {
	}
}

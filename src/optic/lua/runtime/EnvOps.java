package optic.lua.runtime;

import java.util.*;

@RuntimeApi
public class EnvOps {
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

	@RuntimeApi
	public static LuaTable createEnv() {
		LuaTable env = LuaTable.allocate(64);
		env.set("print", new LuaFunction("print") {
			public Object[] call(LuaContext context1, Object... args) {
				StandardLibrary.print(context1.out, args);
				return ListOps.empty();
			}
		});
		env.set("pairs", new LuaFunction("pairs") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				var table = (LuaTable) args[0];
				return new Object[]{table.pairsIterator()};
			}
		});
		env.set("type", new LuaFunction("type") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				if (args.length == 0) {
					throw new IllegalArgumentException("Bad argument #1, expected value");
				}
				Object value = args[0];
				return ListOps.create(StandardLibrary.type(value));
			}
		});
		env.set("tostring", new LuaFunction() {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				return ListOps.create(StandardLibrary.toString(args[0]));
			}
		});
		env.set("tonumber", new LuaFunction() {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				return ListOps.create(StandardLibrary.toNumber(args[0]));
			}
		});
		env.set("table", LuaTable.ofMap(Map.of(
				"concat", new LuaFunction("table.concat") {
					public Object[] call(LuaContext context, Object... args) {
						if (args.length == 0) {
							throw new IllegalArgumentException("Bad argument #1, expected value");
						}
						Object table = args[0];
						Object delimiter = ListOps.get(args, 1);
						String delimiterString = delimiter == null ? "" : StandardLibrary.strictToString(delimiter);
						return ListOps.create(StandardLibrary.tableConcat(table, delimiterString));
					}
				}
		)));
		env.set("assert", new LuaFunction("assert") {
			public Object[] call(LuaContext context, Object... args) {
				if (args.length == 0) {
					throw new IllegalArgumentException("Bad argument #1, expected value");
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
					throw new IllegalArgumentException("Bad argument #1, expected value");
				}
				Object f = args[0];
				Object[] params = ListOps.sublist(args, 1);
				try {
					Object[] result = FunctionOps.call(f, context, params);
					return ListOps.concat(result, true);
				} catch (RuntimeException e) {
					String msg = e.getMessage();
					if (msg == null) {
						return ListOps.create(false);
					}
					return ListOps.create(false, msg);
				}
			}
		});
		env.set("os", LuaTable.ofMap(Map.of(
				"time", new LuaFunction("os.time") {
					public Object[] call(LuaContext context, Object... args) {
						return ListOps.create((System.currentTimeMillis() / 1000));
					}
				}
		)));
		env.set("string", LuaTable.ofMap(Map.of(
				"sub", new LuaFunction("string.sub") {
					@Override
					public Object[] call(LuaContext context, Object... args) {
						String str = StandardLibrary.strictToString(args[0]);
						int from = (int) DynamicOps.toInt(args[1]);
						from = adjustFromIndex(from, str.length());
						Object thirdArgument = ListOps.get(args, 2);
						int to = thirdArgument == null ? str.length() : (int) DynamicOps.toInt(thirdArgument);
						to = adjustToIndex(to, str.length());
						if (to < from || from >= str.length()) {
							return ListOps.create("");
						}
						return ListOps.create(str.substring(from, to));
					}
				},
				"len", new LuaFunction("string.len") {
					@Override
					public Object[] call(LuaContext context, Object... args) {
						String str = StandardLibrary.strictToString(args[0]);
						return ListOps.create(str.length());
					}
				},
				"lower", new LuaFunction("string.lower") {
					@Override
					public Object[] call(LuaContext context, Object... args) {
						String str = StandardLibrary.strictToString(args[0]);
						return ListOps.create(str.toLowerCase());
					}
				},
				"upper", new LuaFunction("string.upper") {
					@Override
					public Object[] call(LuaContext context, Object... args) {
						String str = StandardLibrary.strictToString(args[0]);
						return ListOps.create(str.toUpperCase());
					}
				},
				"format", new LuaFunction("string.format") {
					@Override
					public Object[] call(LuaContext context, Object... args) {
						String str = StandardLibrary.strictToString(args[0]);
						Object[] params = Arrays.copyOfRange(args, 1, args.length);
						return ListOps.create(String.format(str, params));
					}
				},
				"rep", new LuaFunction("string.rep") {
					@Override
					public Object[] call(LuaContext context, Object... args) {
						String str = StandardLibrary.strictToString(args[0]);
						int n = (int) DynamicOps.toInt(args[1]);
						return ListOps.create(str.repeat(Math.max(0, n)));
					}
				},
				"byte", new LuaFunction("string.byte") {
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
							return ListOps.empty();
						}
						int length = to - from;
						Object[] result = new Object[length];
						for (int i = 0; i < length; i++) {
							result[i] = (byte) str.charAt(from + i);
						}
						return result;
					}
				}
		)));
		env.set("math", LuaTable.ofMap(Map.of(
				"floor", new LuaFunction("math.floor") {
					public Object[] call(LuaContext context, Object... args) {
						return ListOps.create(Math.floor(StandardLibrary.strictToNumber(args[0])));
					}
				},
				"sqrt", new LuaFunction("math.sqrt") {
					public Object[] call(LuaContext context, Object... args) {
						double value = StandardLibrary.strictToNumber(args[0]);
						return ListOps.create(Math.sqrt(value));
					}
				},
				"sin", new LuaFunction("math.sin") {
					public Object[] call(LuaContext context, Object... args) {
						double value = StandardLibrary.strictToNumber(args[0]);
						return ListOps.create(Math.sin(value));
					}
				},
				"cos", new LuaFunction("math.cos") {
					public Object[] call(LuaContext context, Object... args) {
						double value = StandardLibrary.strictToNumber(args[0]);
						return ListOps.create(Math.cos(value));
					}
				},
				"atan2", new LuaFunction("math.atan2") {
					public Object[] call(LuaContext context, Object... args) {
						double y = StandardLibrary.strictToNumber(args[0]);
						double x = StandardLibrary.strictToNumber(args[1]);
						return ListOps.create(Math.atan2(y, x));
					}
				},
				"atan", new LuaFunction("math.atan") {
					public Object[] call(LuaContext context, Object... args) {
						double value = StandardLibrary.strictToNumber(args[0]);
						return ListOps.create(Math.atan(value));
					}
				}
		)));
		env.set("_VERSION", "optic-lua [pre-alpha]");
		return env;
	}
}

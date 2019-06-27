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

	static LuaTable createEnv() {
		LuaTable env = new LuaTable();
		env.set("print", new LuaFunction("print") {
			public Object[] call(LuaContext context1, Object... args) {
				StandardLibrary.print(context1.out, args);
				return ListOps.EMPTY;
			}
		});
		env.set("optic", LuaTable.ofMap(Map.of(
				"bundle", new LuaFunction("optic.bundle") {
					public Object[] call(LuaContext context, Object... args) {
						return ListOps.list(LuaTable.ofArray(context.bundle.listFiles()));
					}
				},
				"array", new LuaFunction("optic.array") {
					@Override
					public Object[] call(LuaContext context, Object... args) {
						return ListOps.list((Object) args);
					}
				},
				"numarray", new LuaFunction("optic.numarray") {
					@Override
					public Object[] call(LuaContext context, Object... args) {
						double[] array = new double[args.length];
						for (int i = 0; i < args.length; i++) {
							array[i] = DynamicOps.toNum(args[i]);
						}
						return ListOps.list((Object) array);
					}
				},
				"intarray", new LuaFunction("optic.intarray") {
					@Override
					public Object[] call(LuaContext context, Object... args) {
						long[] array = new long[args.length];
						for (int i = 0; i < args.length; i++) {
							array[i] = DynamicOps.toInt(args[i]);
						}
						return ListOps.list((Object) array);
					}
				},
				"newarray", new LuaFunction("optic.newarray") {
					@Override
					public Object[] call(LuaContext context, Object... args) {
						return ListOps.list((Object) new Object[(int) DynamicOps.toInt(args[0])]);
					}
				}
		)));
		env.set("pairs", new LuaFunction("pairs") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				if (args[0].getClass().isArray()) {
					return ListOps.list(new ArrayPairsIterator(args[0]));
				}
				var table = (LuaTable) args[0];
				return ListOps.list(table.ipairsIterator());
			}
		});
		env.set("ipairs", new LuaFunction("pairs") {
			@Override
			public Object[] call(LuaContext context, Object... args) {
				if (args[0].getClass().isArray()) {
					return ListOps.list(new ArrayPairsIterator(args[0]));
				}
				var table = (LuaTable) args[0];
				return ListOps.list(table.ipairsIterator());
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
		env.set("table", LuaTable.ofMap(Map.of(
				"concat", new LuaFunction("table.concat") {
					public Object[] call(LuaContext context, Object... args) {
						if (args.length == 0) {
							throw new IllegalArgumentException("Bad argument #1, expected value");
						}
						Object table = args[0];
						Object delimiter = ListOps.get(args, 1);
						String delimiterString = delimiter == null ? "" : StandardLibrary.strictToString(delimiter);
						return ListOps.list(StandardLibrary.tableConcat(table, delimiterString));
					}
				}
		)));
		env.set("assert", new LuaFunction("assert") {
			public Object[] call(LuaContext context, Object... args) {
				if (args.length == 0) {
					throw Errors.argument(1, "value");
				}
				if (DynamicOps.toBool(args[0])) {
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
		env.set("os", LuaTable.ofMap(Map.of(
				"time", new LuaFunction("os.time") {
					public Object[] call(LuaContext context, Object... args) {
						return ListOps.list((System.currentTimeMillis() / 1000));
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
							return ListOps.list("");
						}
						return ListOps.list(str.substring(from, to));
					}
				},
				"len", new LuaFunction("string.len") {
					@Override
					public Object[] call(LuaContext context, Object... args) {
						String str = StandardLibrary.strictToString(args[0]);
						return ListOps.list(str.length());
					}
				},
				"lower", new LuaFunction("string.lower") {
					@Override
					public Object[] call(LuaContext context, Object... args) {
						String str = StandardLibrary.strictToString(args[0]);
						return ListOps.list(str.toLowerCase());
					}
				},
				"upper", new LuaFunction("string.upper") {
					@Override
					public Object[] call(LuaContext context, Object... args) {
						String str = StandardLibrary.strictToString(args[0]);
						return ListOps.list(str.toUpperCase());
					}
				},
				"format", new LuaFunction("string.format") {
					@Override
					public Object[] call(LuaContext context, Object... args) {
						String str = StandardLibrary.strictToString(args[0]);
						Object[] params = Arrays.copyOfRange(args, 1, args.length);
						return ListOps.list(String.format(str, params));
					}
				},
				"rep", new LuaFunction("string.rep") {
					@Override
					public Object[] call(LuaContext context, Object... args) {
						String str = StandardLibrary.strictToString(args[0]);
						int n = (int) DynamicOps.toInt(args[1]);
						return ListOps.list(str.repeat(Math.max(0, n)));
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
							return ListOps.EMPTY;
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
				"sqrt", new LuaFunction("math.sqrt") {
					public Object[] call(LuaContext context, Object... args) {
						double value = DynamicOps.toNum(args[0]);
						return ListOps.list(Math.sqrt(value));
					}
				},
				"sin", new LuaFunction("math.sin") {
					public Object[] call(LuaContext context, Object... args) {
						double value = DynamicOps.toNum(args[0]);
						return ListOps.list(Math.sin(value));
					}
				},
				"cos", new LuaFunction("math.cos") {
					public Object[] call(LuaContext context, Object... args) {
						double value = DynamicOps.toNum(args[0]);
						return ListOps.list(Math.cos(value));
					}
				},
				"atan2", new LuaFunction("math.atan2") {
					public Object[] call(LuaContext context, Object... args) {
						double y = DynamicOps.toNum(args[0]);
						double x = DynamicOps.toNum(args[1]);
						return ListOps.list(Math.atan2(y, x));
					}
				},
				"atan", new LuaFunction("math.atan") {
					public Object[] call(LuaContext context, Object... args) {
						double value = DynamicOps.toNum(args[0]);
						return ListOps.list(Math.atan(value));
					}
				},
				"abs", new LuaFunction("math.abs") {
					public Object[] call(LuaContext context, Object... args) {
						double value = DynamicOps.toNum(args[0]);
						return ListOps.list(Math.abs(value));
					}
				},
				"ceil", new LuaFunction("math.ceil") {
					public Object[] call(LuaContext context, Object... args) {
						double value = DynamicOps.toNum(args[0]);
						return ListOps.list(Math.ceil(value));
					}
				},
				"floor", new LuaFunction("math.floor") {
					public Object[] call(LuaContext context, Object... args) {
						double value = DynamicOps.toNum(args[0]);
						return ListOps.list(Math.floor(value));
					}
				},
				"deg", new LuaFunction("math.deg") {
					public Object[] call(LuaContext context, Object... args) {
						double value = DynamicOps.toNum(args[0]);
						return ListOps.list(Math.toDegrees(value));
					}
				},
				"exp", new LuaFunction("math.exp") {
					public Object[] call(LuaContext context, Object... args) {
						double value = DynamicOps.toNum(args[0]);
						return ListOps.list(Math.exp(value));
					}
				}
		)));
		LuaTable math = (LuaTable) env.get("math");
		math.set("huge", Double.POSITIVE_INFINITY);
		math.set("maxinteger", Long.MAX_VALUE);
		math.set("mininteger", Long.MIN_VALUE);
		math.set("pi", Math.PI);
		math.set("log", new LuaFunction("math.log") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return ListOps.list(Math.log(value));
			}
		});
		math.set("rad", new LuaFunction("math.rad") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return ListOps.list(Math.toRadians(value));
			}
		});
		math.set("tan", new LuaFunction("math.tan") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return ListOps.list(Math.tan(value));
			}
		});
		Object[] numberTypeFloat = {"number"};
		Object[] numberTypeInt = {"integer"};
		math.set("type", new LuaFunction("math.type") {
			public Object[] call(LuaContext context, Object... args) {
				double value = DynamicOps.toNum(args[0]);
				return (long) value == value ? numberTypeInt : numberTypeFloat;
			}
		});
		math.set("max", new LuaFunction("math.max") {
			public Object[] call(LuaContext context, Object... args) {
				double a = DynamicOps.toNum(args[0]);
				double b = DynamicOps.toNum(args[1]);
				return ListOps.list(Math.max(a, b));
			}
		});
		math.set("min", new LuaFunction("math.min") {
			public Object[] call(LuaContext context, Object... args) {
				double a = DynamicOps.toNum(args[0]);
				double b = DynamicOps.toNum(args[1]);
				return ListOps.list(Math.min(a, b));
			}
		});
		math.set("ult", new LuaFunction("math.ult") {
			public Object[] call(LuaContext context, Object... args) {
				long a = DynamicOps.toInt(args[0]);
				long b = DynamicOps.toInt(args[1]);
				// java.lang.Long::compareUnsigned
				return ListOps.listWithBoolean(a + Long.MIN_VALUE < b + Long.MIN_VALUE);
			}
		});
		env.set("_VERSION", "optic-lua [pre-alpha]");
		return env;
	}

	private EnvOps() {
	}
}

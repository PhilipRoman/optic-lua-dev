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

	@RuntimeApi
	public static LuaTable createEnv() {
		LuaTable env = LuaTable.allocate(64);
		env.set("print", new LuaFunction("print") {
			public Object[] call(LuaContext context1, Object... args) {
				StandardLibrary.print(context1.out, args);
				return ListOps.empty();
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
		env.set("table", LuaTable.ofMap(Map.of(
				"concat", new LuaFunction("table.concat") {
					public Object[] call(LuaContext context, Object... args) {
						if (args.length == 0) {
							throw new IllegalArgumentException("Bad argument #1, expected value");
						}
						Object table = args[0];
						return ListOps.create(StandardLibrary.tableConcat(table));
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
			public Object[] call(LuaContext context, Object... args1) {
				throw new RuntimeException(StandardLibrary.toString(ListOps.get(args1, 0)));
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
						return ListOps.create((double) (System.currentTimeMillis() / 1000));
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
		return env;
	}
}

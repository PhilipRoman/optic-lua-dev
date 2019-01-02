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
		env.set("print", LuaFunction.of(objects -> {
			StandardLibrary.print(objects);
			return ListOps.empty();
		}));
		env.set("type", LuaFunction.of(args -> {
			if (args.length == 0) {
				throw new IllegalArgumentException("Bad argument #1, expected value");
			}
			Object value = args[0];
			return ListOps.create(StandardLibrary.type(value));
		}));
		env.set("table", LuaTable.ofMap(Map.of(
				"concat", LuaFunction.of(args -> {
					if (args.length == 0) {
						throw new IllegalArgumentException("Bad argument #1, expected value");
					}
					Object table = args[0];
					return ListOps.create(StandardLibrary.tableConcat(table));
				})
		)));
		env.set("assert", LuaFunction.of(args -> {
			if (args.length == 0) {
				throw new IllegalArgumentException("Bad argument #1, expected value");
			}
			if (DynamicOps.isTrue(args[0])) {
				return args;
			} else {
				String msg = args.length >= 2 ? StandardLibrary.toString(args[1]) : "Assertion failed!";
				throw new AssertionError(msg);
			}
		}));
		env.set("error", LuaFunction.of(args -> {
			throw new RuntimeException(StandardLibrary.toString(ListOps.get(args, 0)));
		}));
		env.set("pcall", LuaFunction.of(args -> {
			if (args.length == 0) {
				throw new IllegalArgumentException("Bad argument #1, expected value");
			}
			Object f = args[0];
			Object[] params = ListOps.sublist(args, 1);
			try {
				Object[] result = FunctionOps.call(f, params);
				return ListOps.concat(result, true);
			} catch (RuntimeException e) {
				String msg = e.getMessage();
				if (msg == null) {
					return ListOps.create(false);
				}
				return ListOps.create(false, msg);
			}
		}));
		env.set("os", LuaTable.ofMap(Map.of(
				"time", LuaFunction.of(args -> ListOps.create((double) (System.currentTimeMillis() / 1000)))
		)));
		env.set("math", LuaTable.ofMap(Map.of(
				"floor", LuaFunction.of(args -> ListOps.create(
						Math.floor(Objects.requireNonNull(StandardLibrary.toNumber(ListOps.get(args, 0))))
				)),
				"sqrt", LuaFunction.of(args -> {
					Double value = StandardLibrary.toNumber(ListOps.get(args, 0));
					assert value != null;
					return ListOps.create(Math.sqrt(value));
				})
		)));
		return env;
	}
}

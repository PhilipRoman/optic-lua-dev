package optic.lua.runtime;

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
		LuaTable env = new LuaTable(64, 0);
		env.set("print", LuaFunction.of(objects -> {
			StandardLibrary.print(objects);
			return ListOps.empty();
		}));
		return env;
	}
}

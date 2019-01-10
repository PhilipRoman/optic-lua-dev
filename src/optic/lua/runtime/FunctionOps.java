package optic.lua.runtime;

@RuntimeApi
public class FunctionOps {
	@RuntimeApi
	public static Object[] call(Object func, LuaContext context, Object... args) {
		if (func instanceof LuaFunction) {
			return ((LuaFunction) func).call(context, args);
		}
		throw new IllegalArgumentException("attempt to call a " + StandardLibrary.type(func) + " value");
	}

	@RuntimeApi
	public static Object[] call(Object func, LuaContext contex, Object[] trailing, Object... args) {
		if (func instanceof LuaFunction) {
			return ((LuaFunction) func).call(contex, ListOps.concat(trailing, args));
		}
		throw new IllegalArgumentException("attempt to call a " + StandardLibrary.type(func) + " value");
	}
}

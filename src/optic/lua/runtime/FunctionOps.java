package optic.lua.runtime;

@RuntimeApi
public class FunctionOps {
	@RuntimeApi
	public static Object[] call(Object func, Object... args) {
		if (func instanceof LuaFunction) {
			return ((LuaFunction) func).call(args);
		}
		throw new IllegalArgumentException("attempt to call a " + StandardLibrary.type(func) + " value");
	}

	@RuntimeApi
	public static Object[] call(Object func, Object[] trailing, Object... args) {
		if (func instanceof LuaFunction) {
			return ((LuaFunction) func).call(ListOps.concat(trailing, args));
		}
		throw new IllegalArgumentException("attempt to call a " + StandardLibrary.type(func) + " value");
	}
}

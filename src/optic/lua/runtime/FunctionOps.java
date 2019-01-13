package optic.lua.runtime;

@RuntimeApi
public class FunctionOps {
	@RuntimeApi
	public static Object[] call(Object func, LuaContext context) {
		if (func instanceof LuaFunction) {
			return ((LuaFunction) func).call(context, ListOps.empty());
		}
		Errors.attemptToCall(func);
		return null;
	}

	@RuntimeApi
	public static Object[] call(Object func, LuaContext context, Object... args) {
		if (func instanceof LuaFunction) {
			return ((LuaFunction) func).call(context, args);
		}
		Errors.attemptToCall(func);
		return null;
	}

	@RuntimeApi
	public static Object[] call(Object func, LuaContext context, Object[] trailing, Object... args) {
		if (func instanceof LuaFunction) {
			return ((LuaFunction) func).call(context, ListOps.concat(trailing, args));
		}
		Errors.attemptToCall(func);
		return null;
	}
	@RuntimeApi
	public static Object[] call(UpValue u, LuaContext context) {
		return call(u.value, context);
	}

	@RuntimeApi
	public static Object[] call(UpValue u, LuaContext context, Object... args) {
		return call(u.value, context, args);
	}

	@RuntimeApi
	public static Object[] call(UpValue u, LuaContext context, Object[] trailing, Object... args) {
		return call(u.value, context, trailing, args);
	}
}

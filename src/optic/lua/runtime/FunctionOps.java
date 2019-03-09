package optic.lua.runtime;

@RuntimeApi
public class FunctionOps {
	@RuntimeApi
	public static Object[] call(LuaContext context, Object func, Object[] args) {
		if (func instanceof LuaFunction) {
			return ((LuaFunction) func).call(context, args);
		}
		Errors.attemptToCall(func);
		return null;
	}
}

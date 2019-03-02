package optic.lua.runtime;

import optic.lua.asm.InvocationMethod;

@RuntimeApi
public class FunctionOps {
	@RuntimeApi
	public static Object[] call(String callSiteId, Object func, LuaContext context) {
		if (func instanceof LuaFunction) {
			InvocationSite.get(callSiteId, InvocationMethod.CALL).update(func, ListOps.empty());
			return ((LuaFunction) func).call(context, ListOps.empty());
		}
		Errors.attemptToCall(func);
		return null;
	}

	@RuntimeApi
	public static Object[] call(String callSiteId, Object func, LuaContext context, Object... args) {
		if (func instanceof LuaFunction) {
			InvocationSite.get(callSiteId, InvocationMethod.CALL).update(func, args);
			return ((LuaFunction) func).call(context, args);
		}
		Errors.attemptToCall(func);
		return null;
	}

	@RuntimeApi
	public static Object[] call(String callSiteId, Object func, LuaContext context, Object[] trailing, Object... args) {
		if (func instanceof LuaFunction) {
			Object[] allArgs = ListOps.concat(trailing, args);
			InvocationSite.get(callSiteId, InvocationMethod.CALL).update(func, allArgs);
			return ((LuaFunction) func).call(context, allArgs);
		}
		Errors.attemptToCall(func);
		return null;
	}
/*
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
*/
}

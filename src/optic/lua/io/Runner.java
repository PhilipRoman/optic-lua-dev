package optic.lua.io;

import optic.lua.runtime.LuaContext;

import java.lang.reflect.*;
import java.util.List;

public class Runner {
	public Object[] run(Method method, LuaContext luaContext, List<Object> args) {
		final Object[] result;
		try {
			result = (Object[]) method.invoke(null, new Object[]{luaContext, args.toArray()});
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof Error) {
				throw (Error) e.getCause();
			}
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			}
			throw new RuntimeException(e.getCause());
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		}
		return result;
	}
}

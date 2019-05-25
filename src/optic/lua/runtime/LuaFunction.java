package optic.lua.runtime;

import optic.lua.runtime.invoke.*;

@RuntimeApi
public abstract class LuaFunction {
	private final FunctionFactory site;

	@RuntimeApi
	public LuaFunction(FunctionFactory site) {
		this.site = site;
	}

	@RuntimeApi
	LuaFunction(String friendlyName) {
		this.site = new SimpleFunctionFactory(friendlyName);
	}

	@RuntimeApi
	public abstract Object[] call(LuaContext context, Object... args);

	@Override
	public String toString() {
		return "function 0x" + Integer.toHexString(hashCode());
	}

	public FunctionFactory constructionSite() {
		return site;
	}
}

package optic.lua.runtime;

import optic.lua.runtime.invoke.*;

@RuntimeApi
public abstract class LuaFunction {
	private final FunctionConstructionSite site;

	@RuntimeApi
	public LuaFunction(FunctionConstructionSite site) {
		this.site = site;
	}

	LuaFunction(String friendlyName) {
		this.site = new SimpleFunctionConstructionSite(friendlyName);
	}

	@RuntimeApi
	public abstract Object[] call(LuaContext context, Object... args);

	@Override
	public String toString() {
		return "function 0x" + Integer.toHexString(hashCode());
	}

	public FunctionConstructionSite constructionSite() {
		return site;
	}
}

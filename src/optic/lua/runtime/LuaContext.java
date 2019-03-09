package optic.lua.runtime;

import optic.lua.io.Bundle;
import optic.lua.runtime.invoke.*;

import java.io.*;
import java.util.*;

public class LuaContext {
	public Reader in = new InputStreamReader(System.in);
	public PrintWriter out = new PrintWriter(System.out);
	public PrintWriter err = new PrintWriter(System.err);
	public Bundle bundle = null;
	private CallSiteFactory callSiteFactory = new InstrumentedCallSiteFactory();
	private FunctionConstructionSiteFactory functionConstructionSiteFactory = new SimpleFunctionConstructionSiteFactory();
	private List<CallSite> callSites = new ArrayList<>(32);
	@RuntimeApi
	public Object _ENV;

	public static LuaContext create() {
		return create(Bundle.emptyBundle());
	}

	public static LuaContext create(Bundle bundle) {
		var ctx = new LuaContext();
		ctx._ENV = EnvOps.createEnv();
		ctx.bundle = bundle;
		return ctx;
	}

	@RuntimeApi
	public Object getGlobal(String name) {
		return TableOps.index(_ENV, name);
	}

	@RuntimeApi
	public void setGlobal(String name, Object value) {
		TableOps.setIndex(_ENV, name, value);
	}

	@RuntimeApi
	public CallSite callSite(int id) {
		CallSite site = callSiteFactory.create(id);
		callSites.add(site);
		return site;
	}

	@RuntimeApi
	public FunctionConstructionSite functionConstructionSite(int id) {
		return functionConstructionSiteFactory.create(id);
	}

	public Collection<CallSite> getCallSites() {
		return List.copyOf(callSites);
	}
}

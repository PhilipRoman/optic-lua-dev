package optic.lua.runtime;

import optic.lua.runtime.invoke.*;

import java.io.*;
import java.util.*;

public final class LuaContext {
	public Reader in = new InputStreamReader(System.in);
	public PrintWriter out = new PrintWriter(System.out);
	public PrintWriter err = new PrintWriter(System.err);
	@RuntimeApi
	public Object _ENV;
	public CallSiteFactory callSiteFactory = new SimpleCallSiteFactory();
	public FunctionMetafactory functionMetafactory = new SimpleFunctionMetafactory();
	public TableMetafactory tableCreationFactory = new SimpleTableMetafactory();
	private List<CallSite> callSites = new ArrayList<>(32);

	private LuaContext() {
	}

	@Deprecated
	public static LuaContext create(Object bundle) {return create();}

	@RuntimeApi
	public static LuaContext create() {
		LuaContext ctx = new LuaContext();
		ctx._ENV = EnvOps.createEnv();
		return ctx;
	}

	@RuntimeApi
	public Object getGlobal(String name) {
		return DynamicOps.index(_ENV, name);
	}

	@RuntimeApi
	public void setGlobal(String name, Object value) {
		DynamicOps.setIndex(_ENV, name, value);
	}

	@RuntimeApi
	public CallSite callSite(int id) {
		CallSite site = callSiteFactory.create(id);
		callSites.add(site);
		return site;
	}

	@RuntimeApi
	public FunctionFactory functionFactory(int id) {
		return functionMetafactory.create(id);
	}

	@RuntimeApi
	public TableFactory tableFactory(int id) {
		return tableCreationFactory.create(id);
	}

	public Collection<CallSite> getCallSites() {
		return List.copyOf(callSites);
	}

	public void resetCallSites() {
		callSites.clear();
	}
}

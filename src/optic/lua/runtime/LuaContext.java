package optic.lua.runtime;

import optic.lua.io.Bundle;

import java.io.*;

public class LuaContext {
	public Reader in = new InputStreamReader(System.in);
	public PrintWriter out = new PrintWriter(System.out);
	public PrintWriter err = new PrintWriter(System.err);
	public Bundle bundle = null;
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

	public Object getGlobal(String name) {
		return TableOps.index(_ENV, name);
	}

	public void setGlobal(String name, Object value) {
		TableOps.setIndex(_ENV, name, value);
	}
}

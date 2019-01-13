package optic.lua.runtime;

import java.io.*;

public class LuaContext {
	public Reader in = new InputStreamReader(System.in);
	public PrintWriter out = new PrintWriter(System.out);
	public PrintWriter err = new PrintWriter(System.err);
	@RuntimeApi
	public Object _ENV;

	public Object getGlobal(String name) {
		return TableOps.index(_ENV, name);
	}

	public void setGlobal(String name, Object value) {
		TableOps.setIndex(_ENV, name, value);
	}

	public static LuaContext create() {
		var ctx = new LuaContext();
		ctx._ENV = EnvOps.createEnv();
		return ctx;
	}
}

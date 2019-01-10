package optic.lua.runtime;

import java.io.*;

public class LuaContext {
	InputStream in = System.in;
	PrintStream out = System.out;
	PrintStream err = System.err;
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

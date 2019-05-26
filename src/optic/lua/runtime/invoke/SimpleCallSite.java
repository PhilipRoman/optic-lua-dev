package optic.lua.runtime.invoke;

import optic.lua.runtime.*;

import java.io.PrintStream;

public final class SimpleCallSite implements CallSite {
	private final int id;

	public SimpleCallSite(int id) {
		this.id = id;
	}

	@Override
	public Object[] invoke(LuaContext context, Object function, Object[] args) {
		return ((LuaFunction) function).call(context, args);
	}

	@Override
	public void printTo(PrintStream out) {
		out.println(this);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " #" + id;
	}
}

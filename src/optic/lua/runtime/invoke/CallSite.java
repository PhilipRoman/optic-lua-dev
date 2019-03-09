package optic.lua.runtime.invoke;

import optic.lua.runtime.LuaContext;

import java.io.PrintStream;

public interface CallSite {
	Object[] invoke(LuaContext context, Object function, Object[] args);

	void printTo(PrintStream out);
}

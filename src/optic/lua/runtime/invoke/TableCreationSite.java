package optic.lua.runtime.invoke;

import optic.lua.runtime.LuaTable;

public interface TableCreationSite {
	LuaTable create(Object... entries);

	LuaTable createWithVararg(Object key, Object[] varargs, Object... entries);
}

package optic.lua.runtime.invoke;

import optic.lua.runtime.*;

public final class SimpleTableCreationSite implements TableCreationSite {
	public SimpleTableCreationSite(int id) {
	}

	@Override
	public LuaTable create(Object... entries) {
		return TableOps.create(entries);
	}

	@Override
	public LuaTable createWithVararg(Object key, Object[] varargs, Object... entries) {
		return TableOps.createWithVararg(key, varargs, entries);
	}
}

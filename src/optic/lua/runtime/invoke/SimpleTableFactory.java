package optic.lua.runtime.invoke;

import optic.lua.runtime.*;

public final class SimpleTableFactory implements TableFactory {
	public SimpleTableFactory(int id) {
	}

	@Override
	public LuaTable create(Object... entries) {
		return DynamicOps.table(entries);
	}

	@Override
	public LuaTable createWithVararg(Object key, Object[] varargs, Object... entries) {
		return DynamicOps.varargTable(key, varargs, entries);
	}
}

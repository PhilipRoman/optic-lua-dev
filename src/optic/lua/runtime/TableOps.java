package optic.lua.runtime;

@RuntimeApi
public final class TableOps {
	@RuntimeApi
	public static LuaTable create(Object... entries) {
		LuaTable table = new LuaTable(entries.length >> 1, 0);
		for (int i = 0; i < entries.length; i += 2) {
			table.set(entries[i], entries[i + 1]);
		}
		return table;
	}

	@RuntimeApi
	public static LuaTable createWithVararg(Object key, Object[] trailing, Object... entries) {
		LuaTable table = new LuaTable(entries.length >> 1, trailing.length + 4);
		for (int i = 0; i < entries.length; i += 2) {
			table.set(entries[i], entries[i + 1]);
		}
		return table;
	}
}

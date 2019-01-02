package optic.lua.runtime;

@RuntimeApi
public final class TableOps {
	@RuntimeApi
	public static LuaTable create(Object... entries) {
		LuaTable table = LuaTable.allocate(entries.length >> 1);
		for (int i = 0; i < entries.length; i += 2) {
			table.set(entries[i], entries[i + 1]);
		}
		return table;
	}

	@RuntimeApi
	public static LuaTable createWithVararg(Object key, Object[] trailing, Object... entries) {
		LuaTable table = LuaTable.allocate((entries.length >> 1) + trailing.length);
		for (int i = 0; i < entries.length; i += 2) {
			table.set(entries[i], entries[i + 1]);
		}
		return table;
	}

	@RuntimeApi
	public static Object index(Object obj, Object key) {
		if (obj instanceof LuaTable) {
			return ((LuaTable) obj).get(key);
		}
		throw new IllegalArgumentException("attempt to index a " + StandardLibrary.type(obj) + " value (table=" + obj + ", key=" + key + ")");
	}

	@RuntimeApi
	public static void setIndex(Object obj, Object key, Object value) {
		if (obj instanceof LuaTable) {
			((LuaTable) obj).set(key, value);
		} else {
			throw new IllegalArgumentException("attempt to index a " + StandardLibrary.type(obj) + " value (table=" + obj + ", key=" + key + ", value=" + value + ")");
		}
	}
}

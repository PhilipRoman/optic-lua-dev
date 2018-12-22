package optic.lua.runtime;

@RuntimeApi
public final class TableOps {
	@RuntimeApi
	public static Dynamic create(Dynamic... entries) {
		DynamicTable table = new DynamicTable(entries.length >> 1, 0);
		for (int i = 0; i < entries.length; i += 2) {
			table.set(entries[i], entries[i + 1]);
		}
		return table;
	}

	@RuntimeApi
	public static Dynamic createWithVararg(Dynamic key, MultiValue trailing, Dynamic... entries) {
		DynamicTable table = new DynamicTable(entries.length >> 1, trailing.length() + 4);
		for (int i = 0; i < entries.length; i += 2) {
			table.set(entries[i], entries[i + 1]);
		}
		return table;
	}
}

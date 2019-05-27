package optic.lua.runtime;

import java.util.Map.Entry;
import java.util.TreeMap;

@RuntimeApi
public final class LineNumberTable {
	// [J1, L1, J2, L2, ...] where J = Java line and L = Lua line
	// one-based indexing
	private final int[] rawData;
	// lazily initialized but more efficient lookup table
	private TreeMap<Integer, Integer> table = null;

	@RuntimeApi
	public LineNumberTable(int... rawData) {
		if ((rawData.length & 1) != 0) {
			throw new IllegalArgumentException("Length must be even (was " + rawData.length + ")");
		}
		this.rawData = rawData;
	}

	private static TreeMap<Integer, Integer> buildTable(int[] data) {
		TreeMap<Integer, Integer> table = new TreeMap<>();
		for (int i = 0; i < data.length; i += 2) {
			table.put(data[i], data[i + 1]);
		}
		return table;
	}

	// one-based indexing
	public int translateToLuaLine(int index) {
		if (table == null)
			table = buildTable(rawData);
		Entry<Integer, Integer> entry = table.floorEntry(index);
		return entry == null ? -1 : entry.getValue();
	}
}

package optic.lua.runtime;

import java.util.Arrays;

public final class LineNumberTable {
	private final int[] offsets;

	public LineNumberTable(int... offsets) {
		this.offsets = offsets;
	}

	public int translateToLuaLine(int index) {
		if (index <= offsets[0]) {
			return 1;
		}
		int pos = Arrays.binarySearch(offsets, index);
		return Math.abs(pos + 1);
	}
}

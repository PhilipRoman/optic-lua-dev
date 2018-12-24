package optic.lua.runtime;

import java.util.*;

public class LuaTable {
	private final HashMap<Object, Object> hash;
	private int length;
	private int maxIndex;

	public static LuaTable ofMap(Map<?, ?> entries) {
		LuaTable table = new LuaTable(entries.size());
		entries.forEach(table::set);
		return table;
	}

	public static LuaTable ofArray(List<?> values) {
		LuaTable table = new LuaTable(values.size());
		for (int i = 0; i < values.size(); i++) {
			Object val = values.get(i);
			if (val != null) {
				table.set(i + 1, val);
			}
		}
		return table;
	}

	public static LuaTable copyOf(LuaTable src) {
		return new LuaTable(new HashMap<>(src.hash), src.length, src.maxIndex);
	}

	public static LuaTable allocate(int size) {
		return new LuaTable(size);
	}

	private LuaTable(int hashSize) {
		hash = new HashMap<>(hashSize);
		length = 0;
		maxIndex = 0;
	}

	private LuaTable(HashMap<Object, Object> hash, int length, int maxIndex) {
		this.hash = hash;
		this.length = length;
		this.maxIndex = maxIndex;
	}

	public Object get(Object key) {
		Objects.requireNonNull(key);
		if (key instanceof Number) {
			key = ((Number) key).doubleValue();
		}
		return hash.get(key);
	}

	public Object get(String key) {
		return hash.get(key);
	}

	public Object get(int key) {
		return hash.get((double) key);
	}

	public void set(Object key, Object value) {
		Objects.requireNonNull(key);
		if (key instanceof Number) {
			key = ((Number) key).doubleValue();
		}
		if (value == null) {
			hash.remove(key);
		} else {
			hash.put(key, value);
		}
		if (isInt(key)) {
			updateLength();
			updateMaxIndex();
		}
	}

	public void set(String key, Object value) {
		set((Object) key, value);
	}

	int length() {
		return length;
	}

	int maxIndex() {
		return maxIndex;
	}

	private void updateMaxIndex() {
		double best = 0;
		for (Object x : hash.keySet()) {
			if (isInt(x)) {
				double v = (double) x;
				if (v > best) {
					best = v;
				}
			}
		}
		maxIndex = (int) best;
	}

	private boolean isInt(Object o) {
		if (!(o instanceof Number)) {
			return false;
		}
		return ((Number) o).doubleValue() == ((Number) o).intValue();
	}

	private void updateLength() {
		double found;
		for (double d = 1; ; d++) {
			if (!hash.containsKey(d)) {
				found = d;
				break;
			}
		}
		length = (int) found - 1;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		return "table 0x" + Integer.toHexString(hashCode());
	}

	Object arrayGet(int i) {
		return get(i - 1);
	}
}

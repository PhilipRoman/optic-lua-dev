package optic.lua.runtime;

import java.util.*;

public class LuaTable {
	final ArrayList<Object> array;
	private final HashMap<Object, Object> hash;
	private int length;

	public static LuaTable ofMap(Map<?, ?> entries) {
		LuaTable table = new LuaTable(entries.size(), 0);
		entries.forEach(table::set);
		return table;
	}

	public static LuaTable ofArray(List<?> values) {
		return new LuaTable(new HashMap<>(0), new ArrayList<>(values), values.size());
	}

	public static LuaTable copyOf(LuaTable src) {
		return new LuaTable(new HashMap<>(src.hash), new ArrayList<>(src.array), src.length);
	}

	LuaTable(int hashSize, int arraySize) {
		this(new HashMap<>(hashSize), new ArrayList<>(arraySize), 0);
	}

	LuaTable(HashMap<Object, Object> hash, ArrayList<Object> array, int length) {
		this.hash = hash;
		this.array = array;
		this.length = length;
	}

	public Object get(Object key) {
		Objects.requireNonNull(key);
		if (key instanceof Number) {
			Number num = (Number) key;
			double x = num.doubleValue();
			int i;
			if ((i = (int) x) == x && i >= 1 && i <= array.size()) {
				return array.get(i-1);
			}
		}
		return hash.get(key);
	}

	public Object get(String key) {
		return hash.get(key);
	}

	public Object get(int key) {
		if (key >= 1 && key <= array.size()) {
			return array.get(key-1);
		} else {
			return hash.get(key);
		}
	}

	public void set(Object key, Object value) {
		Objects.requireNonNull(key);
		boolean remove = value == null;
		if (key instanceof Number) {
			Number num = (Number) key;
			double x = num.doubleValue();
			int i;
			if ((i = (int) x) == x && i >= 1) {
				int size = array.size();
				if (i-1 < size) {
					if (remove) {
						if (length >= i) {
							length = i - 1;
						}
					} else {
						array.set(i-1, value);
					}
					return;
				} else if (i-1 == size) {
					if (remove) {
						length--;
						array.remove(i);
					} else {
						length++;
						array.add(value);
					}
					return;
				}
			}
		}
		if (remove) {
			hash.remove(key);
		} else {
			hash.put(key, value);
		}
	}

	public void set(String key, Object value) {
		hash.put(key, value);
	}

	int length() {
		return length;
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
		return array.get(i-1);
	}
}

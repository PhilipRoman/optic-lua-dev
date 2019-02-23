package optic.lua.runtime;

import java.util.*;

public class LuaTable {
	private final HashMap<Object, Object> hash;
	private int length;
	private int maxIndex;
	private boolean staleLength = false;
	private boolean staleMaxIndex = false;

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

	public Object get(Object key) {
		Objects.requireNonNull(key);
		if (key.getClass() != Double.class && key instanceof Number) {
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
		if (key.getClass() != Double.class && key instanceof Number) {
			key = ((Number) key).doubleValue();
		}
		if (value == null) {
			hash.remove(key);
		} else {
			hash.put(key, value);
		}
		if (isInt(key)) {
			staleLength = true;
			staleMaxIndex = true;
		}
	}

	public void set(String key, Object value) {
		set((Object) key, value);
	}

	int length() {
		if (staleLength) {
			updateLength();
			staleLength = false;
		}
		return length;
	}

	int maxIndex() {
		if (staleMaxIndex) {
			updateMaxIndex();
			staleMaxIndex = false;
		}
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
		for (long i = 1; ; i++) {
			if (!hash.containsKey((double) i)) {
				found = i;
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

	public Iterator pairsIterator() {
		var it = hash.entrySet().iterator();
		return new Iterator() {
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public Object next() {
				var next = it.next();
				return new Object[]{next.getKey(), next.getValue()};
			}

			@Override
			public String toString() {
				return "[iterator pairs(" + LuaTable.this + ")]";
			}
		};
	}
}

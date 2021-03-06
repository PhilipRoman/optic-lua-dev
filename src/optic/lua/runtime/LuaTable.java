package optic.lua.runtime;

import java.util.*;
import java.util.Map.Entry;

public class LuaTable {
	private static final Object[] EMPTY_ARRAY = {};
	private final HashMap<Object, Object> hash;
	// entries with keys of [1 .. length] are stored in array part
	// remember that array part is zero-indexed
	private Object[] array = EMPTY_ARRAY;
	private int length;

	LuaTable() {
		hash = new HashMap<>();
		length = 0;
	}

	static LuaTable ofMap(Map<?, ?> entries) {
		LuaTable table = new LuaTable();
		entries.forEach(table::set);
		return table;
	}

	static LuaTable ofArray(List<?> values) {
		LuaTable table = new LuaTable();
		for (int i = 0; i < values.size(); i++) {
			Object val = values.get(i);
			if (val != null) {
				table.set(i + 1, val);
			}
		}
		return table;
	}

	public Object get(Object key) {
		if (key instanceof Number) {
			double d = ((Number) key).doubleValue();
			int i = (int) d;
			if (i == d && i <= length && i >= 1)
				return array[i - 1];
			// key is a Number but not a Double
			if (key.getClass() != Double.class)
				key = d; // autobox to Double
		}
		return hash.get(key);
	}

	public Object get(String key) {
		return hash.get(key);
	}

	public Object get(long key) {
		if (key <= length && key >= 1)
			return array[(int) (key) - 1];
		return hash.get((double) key);
	}

	public void set(Object key, Object value) {
		if (key == null)
			throw new NullPointerException();
		if (key instanceof Number) {
			double d = ((Number) key).doubleValue();
			int i = (int) d;
			if (i == d && i <= length + 1 && i >= 1) {
				setArray(i, value);
				return;
			}
			// key is a Number but not a Double
			if (key.getClass() != Double.class)
				key = d; // autobox to Double
		}
		if (value == null)
			hash.remove(key);
		else
			hash.put(key, value);
	}

	public void set(String key, Object value) {
		if (value == null) {
			hash.remove(key);
		} else {
			hash.put(key, value);
		}
	}

	public void set(long key, Object value) {
		if (key <= length + 1 && key >= 1) {
			setArray((int) key, value);
			return;
		}
		Double boxed = (double) key;
		if (value == null)
			hash.remove(boxed);
		else
			hash.put(boxed, value);
	}

	private void setArray(int key, Object value) {
		if (value == null) {
			removeFromArray(key);
			return;
		}
		if (key == length + 1) {
			appendToArray(value);
			return;
		}
		array[key - 1] = value;
	}

	private void appendToArray(Object value) {
		if (array.length <= length) {
			int newLength = array.length == 0 ? 4 : array.length * 4;
			array = Arrays.copyOf(array, newLength);
		}
		// example:
		// array: [1, 2, 3]; length = 3
		array[length++] = value;
		// array: [1, 2, 3, 4]; length = 4
		// check if hash part contains entry for 5:
		Object next = hash.remove((double) length + 1);
		// recursively append
		if (next != null)
			appendToArray(next);
	}

	private void removeFromArray(int key) {
		int oldLength = length;
		length = key - 1;
		for (int i = key; i < oldLength; i++)
			hash.put((double) (i + 1), array[i]);
		for (int i = key; i < array.length; i++)
			array[i] = null;
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

	Iterator<Object[]> pairsIterator() {
		return new PairsIterator(hash.entrySet().iterator(), array, length);
	}

	Iterator<Object[]> ipairsIterator() {
		return new IpairsIterator(array, length);
	}

	private class PairsIterator implements Iterator<Object[]> {
		private final Object[] wrapper = new Object[2];
		private final Iterator<Entry<Object, Object>> hashIterator;
		private final int length;
		private int arrayIndex = 0;
		private final Object[] array;

		PairsIterator(Iterator<Entry<Object, Object>> hashIterator, Object[] array, int length) {
			this.hashIterator = hashIterator;
			this.array = array;
			this.length = length;
		}

		@Override
		public boolean hasNext() {
			return arrayIndex < length || hashIterator.hasNext();
		}

		@Override
		public Object[] next() {
			if (arrayIndex < length) {
				wrapper[1] = array[arrayIndex];
				wrapper[0] = ++arrayIndex;
			} else if (hashIterator.hasNext()) {
				Entry<Object, Object> next = hashIterator.next();
				wrapper[0] = next.getKey();
				wrapper[1] = next.getValue();
			} else {
				wrapper[0] = null;
				wrapper[1] = null;
			}
			return wrapper;
		}

		@Override
		public String toString() {
			return "[iterator pairs(" + LuaTable.this + ")]";
		}
	}

	private class IpairsIterator implements Iterator<Object[]> {
		private final Object[] wrapper = new Object[2];
		private int arrayIndex = 0;
		private final int length;
		private final Object[] array;

		IpairsIterator(Object[] array, int length) {
			this.array = array;
			this.length = length;
		}

		@Override
		public boolean hasNext() {
			return arrayIndex < length;
		}

		@Override
		public Object[] next() {
			if (arrayIndex < length) {
				wrapper[1] = array[arrayIndex];
				wrapper[0] = ++arrayIndex;
			} else {
				wrapper[0] = null;
				wrapper[1] = null;
			}
			return wrapper;
		}

		@Override
		public String toString() {
			return "[iterator ipairs(" + LuaTable.this + ")]";
		}
	}
}

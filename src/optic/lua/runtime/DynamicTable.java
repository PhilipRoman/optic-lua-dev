package optic.lua.runtime;

import java.util.*;

public class DynamicTable extends Dynamic {
	private final ArrayList<Dynamic> array;
	private final HashMap<Dynamic, Dynamic> hash;
	private int length;

	public static DynamicTable ofMap(Map<?, ?> entries) {
		DynamicTable table = new DynamicTable(entries.size(), 0);
		entries.forEach((k, v) -> table.set(Dynamic.of(k), Dynamic.of(v)));
		return table;
	}

	public static DynamicTable ofArray(List<?> values) {
		ArrayList<Dynamic> converted = new ArrayList<>(values.size());
		for (var value : values) {
			converted.add(Dynamic.of(value));
		}
		return new DynamicTable(new HashMap<>(0), converted, values.size());
	}

	public static DynamicTable copyOf(DynamicTable src) {
		return new DynamicTable(new HashMap<>(src.hash), new ArrayList<>(src.array), src.length);
	}

	DynamicTable(int hashSize, int arraySize) {
		this(new HashMap<>(hashSize), new ArrayList<>(arraySize), 0);
	}

	DynamicTable(HashMap<Dynamic, Dynamic> hash, ArrayList<Dynamic> array, int length) {
		super(Dynamic.TABLE);
		this.hash = hash;
		this.array = array;
		this.length = length;
	}

	@Override
	public Dynamic get(Dynamic key) {
		if (key.type == NUMBER) {
			DynamicNumber num = (DynamicNumber) key;
			double x = num.value;
			int i;
			if ((i = (int) x) == x && i >= 1 && i <= array.size()) {
				return array.get(i-1);
			}
		}
		return hash.getOrDefault(key, DynamicNil.nil());
	}

	@Override
	public Dynamic get(String key) {
		return hash.getOrDefault(DynamicString.of(key), DynamicNil.nil());
	}

	public Dynamic get(Object key) {
		return hash.getOrDefault(Dynamic.of(key), DynamicNil.nil());
	}

	public Dynamic get(int key) {
		if (key >= 1 && key <= array.size()) {
			return array.get(key-1);
		} else {
			return hash.getOrDefault(DynamicNumber.of(key), DynamicNil.nil());
		}
	}

	@Override
	public void set(Dynamic key, Dynamic value) {
		if (key == DynamicNil.nil()) {
			Errors.forbidden();
			return;
		}
		boolean remove = value == DynamicNil.nil();
		if (key.type == NUMBER) {
			DynamicNumber num = (DynamicNumber) key;
			double x = num.value;
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

	@Override
	public void set(String key, Dynamic value) {
		hash.put(DynamicString.of(key), value);
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

	Dynamic arrayGet(int i) {
		return array.get(i-1);
	}
}

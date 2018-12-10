package optic.lua.runtime;

import java.util.*;

public class DynamicTable extends Dynamic {
	private final ArrayList<Dynamic> array;
	private final HashMap<Dynamic, Dynamic> hash;

	DynamicTable(int hashSize, ArrayList<Dynamic> array) {
		super(Dynamic.TABLE);
		hash = new HashMap<>(hashSize);
		this.array = array;
	}

	@Override
	public Dynamic get(Dynamic key) {
		if (key.type == NUMBER) {
			DynamicNumber num = (DynamicNumber) key;
			double x = num.value;
			int i;
			if ((i = (int) x) == x && i > 0 && i < array.size()) {
				return array.get(i);
			}
		}
		return hash.get(key);
	}

	@Override
	public Dynamic get(String key) {
		return hash.get(Dynamic.of(key));
	}

	@Override
	public void set(Dynamic key, Dynamic value) {
		if (key.type == NUMBER) {
			DynamicNumber num = (DynamicNumber) key;
			double x = num.value;
			int i;
			if ((i = (int) x) == x && i > 0) {
				int size = array.size();
				if (i < size) {
					array.set(i, value);
				} else if (i == size) {
					array.add(value);
				}
			}
		}
		hash.put(key, value);
	}

	@Override
	public void set(String key, Dynamic value) {
		super.set(key, value);
	}
}

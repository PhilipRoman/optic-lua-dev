package optic.lua.runtime;

import java.lang.reflect.Array;
import java.util.Iterator;

class ArrayPairsIterator implements Iterator<Object[]> {
	private final int len;
	private final Object array;
	private final Object[] container = new Object[2];
	private int pos = 0;

	ArrayPairsIterator(Object array) {
		this.array = array;
		this.len = Array.getLength(array);
	}

	@Override
	public boolean hasNext() {
		return pos < len;
	}

	@Override
	public Object[] next() {
		if (pos < len) {
			container[0] = pos;
			container[1] = Array.get(array, pos++);
		}
		return container;
	}
}

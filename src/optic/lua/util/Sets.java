package optic.lua.util;

import java.util.*;

public class Sets {
	@SuppressWarnings("unchecked")
	public static <E> Set<E> merge(Set<E> a, Set<E> b) {
		if (a.size() == 0) {
			return b;
		}
		if (b.size() == 0) {
			return a;
		}
		Object[] array = new Object[a.size() + b.size()];
		System.arraycopy(a.toArray(), 0, array, 0, a.size());
		System.arraycopy(b.toArray(), 0, array, a.size(), b.size());
		return new HashSet<>(Arrays.asList((E[]) array));
	}
}

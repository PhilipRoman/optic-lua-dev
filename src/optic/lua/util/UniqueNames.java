package optic.lua.util;

import java.util.concurrent.atomic.AtomicInteger;

public final class UniqueNames {
	private static final AtomicInteger counter = new AtomicInteger();

	private UniqueNames() {
	}

	public static String next() {
		int i = counter.incrementAndGet();
		return "v" + Integer.toHexString(i).toUpperCase();
	}
}

package optic.lua.flat;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.toHexString;

public class Register {
	private final String name;
	private static final AtomicInteger counter = new AtomicInteger();

	Register() {
		name = "v" + randomSuffix();
	}

	private static String randomSuffix() {
		return toHexString(counter.getAndIncrement()).toUpperCase();
	}

	@Override
	public String toString() {
		return name;
	}
}

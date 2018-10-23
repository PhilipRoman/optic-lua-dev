package optic.lua.flat;

import org.jetbrains.annotations.*;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.toHexString;

public class Register {
	private static final AtomicInteger counter = new AtomicInteger();
	private static final Register UNUSED_REGISTER = new Register("_", true);
	private final String name;
	private final boolean isVararg;

	private Register() {
		this(false);
	}

	private Register(boolean isVararg) {
		this("v" + randomSuffix(), isVararg);
	}

	private Register(String name, boolean isVararg) {
		this.name = name;
		this.isVararg = isVararg;
	}

	@NotNull
	@Contract(" -> new")
	static Register createVararg() {
		return new Register(true);
	}

	@NotNull
	@Contract(" -> new")
	static Register create() {
		return new Register(false);
	}

	private static String randomSuffix() {
		return toHexString(counter.getAndIncrement()).toUpperCase();
	}

	@NotNull
	public static Register unused() {
		return UNUSED_REGISTER;
	}

	@Override
	public String toString() {
		return name + (isVararg ? "@" : "");
	}

	boolean isVararg() {
		return isVararg;
	}
}

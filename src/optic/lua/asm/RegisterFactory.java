package optic.lua.asm;

import org.jetbrains.annotations.*;

public class RegisterFactory {
	private static final Register UNUSED_REGISTER = new Register("_", true);

	@NotNull
	@Contract(" -> new")
	static Register createVararg() {
		return new Register(true);
	}

	@NotNull
	@Contract(" -> new")
	public static Register create() {
		return new Register(false);
	}

	@NotNull
	public static Register unused() {
		return UNUSED_REGISTER;
	}
}

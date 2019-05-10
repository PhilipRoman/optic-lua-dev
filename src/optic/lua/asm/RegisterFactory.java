package optic.lua.asm;

import optic.lua.optimization.ProvenType;
import org.jetbrains.annotations.*;

import java.util.function.Supplier;

public class RegisterFactory {
	@Contract(" -> new")
	static Register createVararg() {
		return new Register(true, ProvenType.OBJECT);
	}

	@NotNull
	@Contract("_ -> new")
	public static Register create(Supplier<ProvenType> type) {
		return new Register(false, type);
	}

	@NotNull
	public static Register unused() {
		return Register.UNUSED;
	}
}

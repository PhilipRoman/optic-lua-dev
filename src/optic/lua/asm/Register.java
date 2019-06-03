package optic.lua.asm;

import optic.lua.optimization.ProvenType;
import optic.lua.util.UniqueNames;

import java.util.function.Supplier;

/**
 * Register is the basic unit of ASM form. Each assignment targets a new, unique register.
 * Some registers have *vararg capabilities*, which means that they may store a list
 * of values rather than a single value. There also exists the *unused register* which
 * can be used if the value of an expression is not needed (like when calling `print("hello")`).
 * <p>
 * Use [RegisterFactory] to obtain instances of this class.
 */
public final class Register implements RValue {
	static final Register UNUSED = new Register("_", true, ProvenType.OBJECT);

	private final String name;

	private final boolean vararg;
	private final Supplier<ProvenType> type;

	private Register(String name, boolean vararg, Supplier<ProvenType> type) {
		this.name = name;
		this.vararg = vararg;
		this.type = type;
	}

	Register(boolean isVararg, Supplier<ProvenType> type) {
		this(UniqueNames.next(), isVararg, type);
	}

	@Override
	public <T, X extends Throwable> T accept(RValueVisitor<T, X> visitor) throws X {
		return visitor.visitRegister(this);
	}

	@Override
	public String toString() {
		return name + (vararg ? "@" : "");
	}

	boolean isUnused() {
		return name.equals("_");
	}

	@Override
	public boolean isVararg() {
		return vararg;
	}

	public String name() {
		return name;
	}

	@Override
	public boolean isPure() {
		return true;
	}

	@Override
	public ProvenType typeInfo() {
		return type.get();
	}
}

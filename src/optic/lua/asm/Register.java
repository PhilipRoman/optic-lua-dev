package optic.lua.asm;

import optic.lua.optimization.ProvenType;
import optic.lua.util.UniqueNames;
import org.jetbrains.annotations.*;

import java.util.function.Supplier;

/**
 * Register is the basic unit of ASM form. Each assignment targets a new, unique register.
 * Use the factory method {@link #ofType(Supplier)} to obtain instances of this class.
 */
public final class Register implements ExprNode {
	private final String name;
	private final Supplier<ProvenType> type;

	private Register(Supplier<ProvenType> type) {
		this.name = UniqueNames.next();
		this.type = type;
	}

	@NotNull
	@Contract(value = "_ -> new", pure = true)
	public static Register ofType(Supplier<ProvenType> type) {
		return new Register(type);
	}

	@Override
	public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
		return visitor.visitRegister(this);
	}

	@Override
	public String toString() {
		return name;
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

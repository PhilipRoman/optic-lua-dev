package optic.lua.asm;

import optic.lua.optimization.StaticType;
import optic.lua.util.UniqueNames;
import org.jetbrains.annotations.*;

/**
 * The equivalent of {@link Register} for storing multi-valued expressions.
 */
public final class ArrayRegister implements ListNode {
	private final String name;

	private ArrayRegister() {
		this.name = UniqueNames.next();
	}

	@NotNull
	@Contract(value = "-> new", pure = true)
	public static ArrayRegister create() {
		return new ArrayRegister();
	}

	@Override
	public <T, X extends Throwable> T accept(ExpressionVisitor<T, X> visitor) throws X {
		return visitor.visitArrayRegister(this);
	}

	@Override
	public StaticType childTypeInfo(int i) {
		return StaticType.OBJECT;
	}

	@Override
	public String toString() {
		return name + "...";
	}

	public String name() {
		return name;
	}

	@Override
	public boolean isPure() {
		return true;
	}
}

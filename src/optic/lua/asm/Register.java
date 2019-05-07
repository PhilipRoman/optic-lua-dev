package optic.lua.asm;

import optic.lua.optimization.*;
import optic.lua.util.UniqueNames;

import java.util.List;
import java.util.function.Supplier;

/**
 * Register is the basic unit of ASM form. Each assignment targets a new, unique register.
 * Some registers have *vararg capabilities*, which means that they may store a list
 * of values rather than a single value. There also exists the *unused register* which
 * can be used if the value of an expression is not needed (like when calling `print("hello")`).
 * <p>
 * Use [RegisterFactory] to obtain instances of this class.
 */
public class Register implements RValue {
	private final String name;

	private final boolean vararg;
	private final CombinedCommonType statusDependencies = new CombinedCommonType();

	Register(String name, boolean vararg) {
		this.name = name;
		this.vararg = vararg;
	}

	Register(boolean isVararg) {
		this(UniqueNames.next(), isVararg);
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
		return statusDependencies.get();
	}

	void updateStatus(ProvenType provenType) {
		statusDependencies.add(provenType);
	}

	void addTypeDependency(Supplier<ProvenType> dependency) {
		statusDependencies.add(dependency);
	}

	public String toDebugString() {
		String varargSuffix = vararg ? "..." : "";
		return "(" + typeInfo() + varargSuffix + " \"" + name + "\")";
	}

	@Override
	public FlatExpr discardRemaining() {
		if (!vararg) {
			return new FlatExpr(List.of(), this);
		}
		var first = RegisterFactory.create();
		first.updateStatus(ProvenType.OBJECT);
		return new FlatExpr(List.of(StepFactory.select(first, this, 0)), first);
	}
}

package optic.lua.asm;

import optic.lua.optimization.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static optic.lua.asm.VariableMode.*;
import static optic.lua.optimization.StaticType.*;

/**
 * A mutable container of information associated with a single variable.
 */
public class VariableInfo {
	private final String name;
	private boolean isFinal = true;
	private boolean isUpvalue = false;
	private boolean initialized = false;
	private boolean isEnv = false;
	private CombinedType combinedType = new CombinedType();
	private ExprNode lastAssignedExpression = ExprNode.nil();

	VariableInfo(String name) {
		this.name = name;
	}

	/**
	 * Creates an upvalue variable named "_ENV" for which the {@link #isEnv()} method returns true.
	 */
	static VariableInfo createEnv() {
		var v = new VariableInfo("_ENV");
		v.markAsUpvalue();
		v.markAsInitialized();
		v.isEnv = true;
		return v;
	}

	static VariableInfo global(String name) {
		return new GlobalVariableInfo(name);
	}

	VariableInfo nextIncarnation() {
		if (isUpvalue) {
			throw new UnsupportedOperationException();
		}
		return new VariableInfo(name + "__next");
	}

	void markAsUpvalue() {
		isUpvalue = true;
	}

	@NotNull
	public VariableMode getMode() {
		return isUpvalue ? UPVALUE : LOCAL;
	}

	void markAsWritten() {
		if (initialized) {
			isFinal = false;
		} else {
			initialized = true;
		}
	}

	void markAsInitialized() {
		initialized = true;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public String toDebugString() {
		return String.format("%s%s %s %s (%s)",
				isFinal() ? "final " : "",
				getMode().name().toLowerCase(),
				typeInfo(),
				name,
				Integer.toHexString(hashCode()));
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public StaticType typeInfo() {
		if (!initialized)
			throw new IllegalStateException(this + " not initialized");
		var t = combinedType.get();
		if (!isUpvalue)
			return t;
		if (t == FUNCTION || t == INTEGER || t == NUMBER || t == OBJECT || t == TABLE)
			return t;
		return OBJECT; // not all types have respective up-value specializations
	}

	void update(StaticType other) {
		combinedType.add(other);
	}

	void addTypeDependency(Supplier<StaticType> source) {
		combinedType.add(source);
	}

	ExprNode getLastAssignedExpression() {
		return lastAssignedExpression;
	}

	void setLastAssignedExpression(ExprNode expr) {
		this.lastAssignedExpression = expr;
	}

	public boolean isEnv() {
		return isEnv;
	}

	private static final class GlobalVariableInfo extends VariableInfo {
		private GlobalVariableInfo(String name) {
			super(name);
		}

		@Override
		VariableInfo nextIncarnation() {
			throw new UnsupportedOperationException();
		}

		@Override
		public @NotNull VariableMode getMode() {
			return GLOBAL;
		}

		@Override
		public boolean isFinal() {
			return false;
		}

		@Override
		void markAsUpvalue() {
			throw new UnsupportedOperationException();
		}

		@Override
		void markAsWritten() {
			throw new UnsupportedOperationException();
		}

		@Override
		public StaticType typeInfo() {
			return OBJECT;
		}

		@Override
		public String toString() {
			return "global " + getName();
		}
	}
}

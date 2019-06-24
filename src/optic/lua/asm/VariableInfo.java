package optic.lua.asm;

import optic.lua.optimization.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static optic.lua.asm.VariableMode.*;

/**
 * A mutable container of information associated with a single variable.
 */
public class VariableInfo {
	private final String name;
	private boolean isFinal = true;
	private boolean isUpvalue = false;
	private boolean initialized = false;
	private boolean isEnv = false;
	private CombinedCommonType type = new CombinedCommonType();

	VariableInfo(String name) {
		this.name = name;
	}

	/**
	 * Creates an upvalue variable named "_ENV" for which the {@link #isEnv()} method returns true.
	 */
	static VariableInfo createEnv() {
		var v = new VariableInfo("_ENV");
		v.markAsUpvalue();
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

	public boolean isFinal() {
		return isFinal;
	}

	public String toDebugString() {
		return String.format("%s%s %s %s (%s)",
				isFinal() ? "final " : "",
				getMode().name().toLowerCase(),
				type.get(),
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
		return isUpvalue ? StaticType.OBJECT : type.get();
	}

	void enableObjects() {
		update(StaticType.OBJECT);
	}

	void enableNumbers() {
		update(StaticType.NUMBER);
	}

	void update(StaticType other) {
		type.add(other);
	}

	void addTypeDependency(Supplier<StaticType> source) {
		type.add(source);
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
			return StaticType.OBJECT;
		}

		@Override
		public String toString() {
			return "global " + getName();
		}
	}
}

package optic.lua.asm;

import optic.lua.asm.instructions.VariableMode;
import optic.lua.optimization.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static optic.lua.asm.instructions.VariableMode.*;

public class VariableInfo {
	private boolean isFinal = true;
	private boolean isUpvalue = false;
	private boolean initialized = false;
	private boolean isEnv = false;
	private final String name;
	private CombinedCommonType type = new CombinedCommonType();

	VariableInfo(String name) {
		this.name = name;
	}

	static VariableInfo createEnv() {
		var v = new VariableInfo("_ENV");
		v.markAsUpvalue();
		v.isEnv = true;
		return v;
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

	static VariableInfo global(String name) {
		return new GlobalVariableInfo(name);
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

	public ProvenType typeInfo() {
		return isUpvalue ? ProvenType.OBJECT : type.get();
	}

	void enableObjects() {
		type.add(ProvenType.OBJECT);
	}

	void enableNumbers() {
		type.add(ProvenType.NUMBER);
	}

	void update(ProvenType other) {
		type.add(other);
	}

	void addTypeDependency(Supplier<ProvenType> source) {
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
		public ProvenType typeInfo() {
			return ProvenType.OBJECT;
		}

		@Override
		public String toString() {
			return "global " + getName();
		}
	}
}

package optic.lua.asm;

import optic.lua.asm.instructions.VariableMode;
import optic.lua.optimization.ProvenType;
import org.jetbrains.annotations.NotNull;

import static optic.lua.asm.instructions.VariableMode.*;

public class VariableInfo {
	private boolean isFinal = true;
	private boolean isUpvalue = false;
	private boolean initialized = false;
	private final String name;
	private ProvenType status = ProvenType.UNKNOWN;

	VariableInfo(String name) {
		this.name = name;
	}

	void markAsUpvalue() {
		isUpvalue = true;
	}

	@NotNull
	public VariableMode getMode() {
		return isUpvalue ? UPVALUE : LOCAL;
	}

	void markAsWritten() {
		if(initialized) {
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
		String mode = getMode().toString().toLowerCase();
		return "variable(" + (isFinal() ? "final " : "") + mode + " \"" + name + "\" " + status + ")";
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public ProvenType status() {
		return isUpvalue ? ProvenType.OBJECT : status;
	}

	void enableObjects() {
		status = status.and(ProvenType.OBJECT);
	}

	void enableNumbers() {
		status = status.and(ProvenType.NUMBER);
	}

	void update(ProvenType other) {
		status = status.and(other);
	}

	private static final class GlobalVariableInfo extends VariableInfo {
		private GlobalVariableInfo(String name) {
			super(name);
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
		public ProvenType status() {
			return ProvenType.OBJECT;
		}

		@Override
		public String toString() {
			return "global " + getName();
		}
	}
}

package optic.lua.asm;

import optic.lua.asm.instructions.VariableMode;
import org.jetbrains.annotations.NotNull;

import static optic.lua.asm.instructions.VariableMode.*;

public class VariableInfo {
	private boolean isFinal = true;
	private boolean isUpvalue = false;
	private final String name;

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
		isFinal = true;
	}

	public boolean isFinal() {
		return isFinal;
	}

	static VariableInfo global(String name) {
		return new GlobalVariableInfo(name);
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
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
			throw new UnsupportedOperationException();
		}

		@Override
		void markAsUpvalue() {
			throw new UnsupportedOperationException();
		}

		@Override
		void markAsWritten() {
			throw new UnsupportedOperationException();
		}
	}
}

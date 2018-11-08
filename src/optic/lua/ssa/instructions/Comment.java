package optic.lua.ssa.instructions;

import optic.lua.ssa.*;

public final class Comment implements Step {
	private final String text;

	public Comment(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return "// " + text;
	}
}

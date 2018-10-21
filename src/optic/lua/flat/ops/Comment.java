package optic.lua.flat.ops;

import optic.lua.flat.*;
import org.jetbrains.annotations.NotNull;

public class Comment implements Step {
	private final String text;

	public Comment(String text) {
		this.text = text;
	}

	@Override
	public @NotNull StepType getType() {
		return StepType.COMMENT;
	}

	@Override
	public String toString() {
		return "// " + text;
	}
}

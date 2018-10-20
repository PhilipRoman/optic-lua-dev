package optic.lua.flat.ops;

import optic.lua.flat.*;

public class Declare implements Step {
	private final StepType type;
	private final String name;

	public Declare(StepType type, String name) {
		assert type == StepType.DECLARE;
		this.type = type;
		this.name = name;
	}

	@Override
	public StepType getType() {
		return type;
	}

	@Override
	public String toString() {
		return typeName() + " " + name;
	}
}

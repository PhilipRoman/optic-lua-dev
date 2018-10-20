package optic.lua.flat.ops;

import optic.lua.flat.*;

import java.util.*;

import static optic.lua.flat.StepType.*;

public class Assign implements Step {
	private static final Set<StepType> validTypes = EnumSet.of(ASSIGN, ASSIGN_LOCAL);
	private final StepType type;
	private final List<Register> sources;
	private final List<String> targets;

	public Assign(StepType type, List<String> targets, List<Register> sources) {
		assert validTypes.contains(type);
		this.type = type;
		this.targets = targets;
		this.sources = sources;
	}

	@Override
	public StepType getType() {
		return type;
	}

	@Override
	public String toString() {
		return typeName() + " " + targets + " = " + sources;
	}
}

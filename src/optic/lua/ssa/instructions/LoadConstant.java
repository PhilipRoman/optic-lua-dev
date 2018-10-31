package optic.lua.ssa.instructions;


import optic.lua.ssa.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LoadConstant implements Step {
	private final StepType type;
	private final Object constant;
	private final Register target;
	private static final Map<StepType, Class<?>> classMap = new EnumMap<>(Map.of(
			StepType.NUMBER, Double.class,
			StepType.STRING, String.class,
			StepType.BOOL, Boolean.class
	));

	public LoadConstant(StepType type, Register target, Object constant) {
		this.target = target;
		assert classMap.get(type).isInstance(constant);
		this.type = type;
		this.constant = constant;
	}

	@NotNull
	@Override
	public StepType getType() {
		return type;
	}

	public Object getConstant() {
		return constant;
	}

	@Override
	public String toString() {
		return typeName() + " " + target + " = " + constant;
	}

	public Register getTarget() {
		return target;
	}
}

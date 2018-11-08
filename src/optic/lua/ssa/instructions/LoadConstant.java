package optic.lua.ssa.instructions;


import optic.lua.ssa.*;
import org.jetbrains.annotations.*;

import java.util.*;

public final class LoadConstant implements Step {
	private final Object constant;
	private final Register target;
	private static final Set<Class<?>> allowedTypes = Set.of(String.class, Double.class, Boolean.class);

	public LoadConstant(@NotNull Register target, @Nullable Object constant) {
		this.target = target;
		assert constant == null || allowedTypes.contains(constant.getClass());
		this.constant = constant;
	}

	public Object getConstant() {
		return constant;
	}

	@Override
	public String toString() {
		return "constant " + target + " = " + constant;
	}

	public Register getTarget() {
		return target;
	}
}

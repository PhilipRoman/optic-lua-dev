package optic.lua.asm;

import org.jetbrains.annotations.Nullable;

public interface VariableResolver {
	@Nullable
	VariableInfo resolve(String name);
}

package optic.lua.asm;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Obtain instances of this interface using {@link StepFactory} methods.
 */
public interface Step {
	default List<Step> children() {
		return List.of();
	}

	@Nullable
	default Register modified() {
		return null;
	}
}

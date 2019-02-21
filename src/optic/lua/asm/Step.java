package optic.lua.asm;

import java.util.List;

/**
 * Obtain instances of this interface using {@link StepFactory} methods.
 */
public interface Step {
	default List<Step> children() {
		return List.of();
	}

	<T, X extends Throwable> T accept(StepVisitor<T, X> visitor) throws X;
}

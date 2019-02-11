package optic.lua.asm;

import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.Consumer;

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

	@NotNull
	default Collection<RValue> observed() {
		List<RValue> list = new ArrayList<>(4);
		forEachObserved(list::add);
		return list;
	}

	default void forEachObserved(Consumer<RValue> action) {
	}
}

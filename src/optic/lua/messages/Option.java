package optic.lua.messages;

/**
 * Instances of this interface represent particular options which can be stored in {@link Options}.
 *
 * @param <T> The type of value this option can be set to
 */
@FunctionalInterface
public interface Option<T> {
	Option<String> INDENT = () -> "\t";

	T defaultValue();
}

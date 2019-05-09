package optic.lua.messages;

import org.jetbrains.annotations.NotNull;

/**
 * use {@link #format(Message)} to convert messages to a different format
 *
 * @param <T> The type to which this {@link MessageFormat} converts messages
 */
@FunctionalInterface
@Deprecated
public interface MessageFormat<T> {
	@NotNull
	T format(Message message);
}

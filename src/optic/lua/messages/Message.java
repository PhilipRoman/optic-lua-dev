package optic.lua.messages;

import optic.lua.CodeSource;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Compiler message to be used with {@link MessageReporter}
 */
public interface Message {
	/**
	 * @return the source code line associated with this message (if present)
	 */
	@NotNull
	OptionalInt line();

	/**
	 * @return the source code column associated with this message (if present)
	 */
	@NotNull
	OptionalInt column();

	/**
	 * @return the message text
	 */
	@NotNull
	String message();

	/**
	 * @return the source code input associated with this message
	 */
	@NotNull
	Optional<CodeSource> source();

	/**
	 * @return the phase during which the message was made
	 */
	@NotNull
	Phase phase();

	/**
	 * @return the severity of this message
	 */
	@NotNull
	Level level();

	/**
	 * @return the exception that lead to creation of this message (if present)
	 */
	@NotNull
	Optional<Throwable> cause();

	/**
	 * @return mutable message with the given message
	 */
	static MessageBuilder create(String message) {
		Objects.requireNonNull(message);
		return new MessageImpl(message);
	}
}

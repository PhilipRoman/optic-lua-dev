package optic.lua.messages;

import optic.lua.CodeSource;
import org.jetbrains.annotations.*;

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
	@Nullable
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
	 * @return a set of tags describing this message
	 */
	@NotNull
	Set<Tag> tags();

	/**
	 * @return mutable message with the given text
	 */
	static MessageBuilder create(String message) {
		Objects.requireNonNull(message);
		return new MessageImpl(message);
	}

	/**
	 * @return mutable message with the given text and {@link Level#ERROR} level
	 */
	static MessageBuilder createError(String message) {
		var msg = create(message);
		msg.setLevel(Level.ERROR);
		return msg;
	}

	/**
	 * @return mutable message with the given text and {@link Level#ERROR} level
	 */
	static MessageBuilder createError(String message, Throwable cause) {
		var msg = create(message);
		msg.setLevel(Level.ERROR);
		msg.setCause(cause);
		return msg;
	}

	/**
	 * @return mutable message with the given text and {@link Level#INFO} level
	 */
	static Message createInfo(String message) {
		var msg = create(message);
		msg.setLevel(Level.INFO);
		return msg;
	}
}

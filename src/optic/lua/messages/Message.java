package optic.lua.messages;

import optic.lua.CodeSource;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public interface Message {
	@NotNull
	OptionalInt line();

	@NotNull
	OptionalInt column();

	@NotNull
	String message();

	@NotNull
	Optional<CodeSource> source();

	@NotNull
	Phase phase();

	@NotNull
	Level level();

	@NotNull
	Optional<Throwable> cause();

	static MessageBuilder create(String message) {
		Objects.requireNonNull(message);
		return new MessageImpl(message);
	}
}

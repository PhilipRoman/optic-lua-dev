package optic.lua.messages;

import optic.lua.CodeSource;
import org.jetbrains.annotations.*;

import java.util.*;

class MessageImpl implements Message, MessageBuilder {
	private int line = -1;
	private int column = -1;
	@NotNull
	private final String message;
	private CodeSource source = null;
	private Level level = null;
	private Phase phase = null;
	private Throwable cause = null;

	static MessageImpl copyOf(Message src) {
		if (src instanceof MessageImpl) {
			var m = (MessageImpl) src;
			return new MessageImpl(
					m.line, m.column, m.message, m.source, m.level, m.phase, m.cause
			);
		} else
			return new MessageImpl(
					src.line().orElse(-1), src.column().orElse(-1),
					src.message(),
					src.source().orElse(null),
					src.level(), src.phase(),
					src.cause().orElse(null)
			);
	}

	private MessageImpl(int line, int column, @NotNull String message, CodeSource source, Level level, Phase phase, Throwable cause) {
		this.line = line;
		this.column = column;
		this.message = message;
		this.source = source;
		this.level = level;
		this.phase = phase;
		this.cause = cause;
	}

	MessageImpl(@NotNull String message) {
		Objects.requireNonNull(message);
		this.message = message;
	}

	@NotNull
	@Override
	public OptionalInt line() {
		return line >= 0 ? OptionalInt.of(line) : OptionalInt.empty();
	}

	@NotNull
	@Override
	public OptionalInt column() {
		return column >= 0 ? OptionalInt.of(column) : OptionalInt.empty();
	}

	@NotNull
	@Override
	public String message() {
		return message;
	}

	@NotNull
	@Override
	public Optional<CodeSource> source() {
		return Optional.ofNullable(source);
	}

	@Nullable
	@Override
	public Phase phase() {
		return phase;
	}

	@NotNull
	@Override
	public Level level() {
		return Objects.requireNonNull(level);
	}

	@Override
	public @NotNull Optional<Throwable> cause() {
		return Optional.ofNullable(cause);
	}

	@Override
	public void setSource(CodeSource source) {
		this.source = source;
	}

	@Override
	public void setLevel(Level level) {
		this.level = level;
	}

	@Override
	public void setPhase(Phase phase) {
		this.phase = phase;
	}

	@Override
	public void setLine(int line) {
		this.line = line;
	}

	@Override
	public void setColumn(int column) {
		this.column = column;
	}

	@Override
	public void setCause(Throwable cause) {
		this.cause = cause;
	}
}

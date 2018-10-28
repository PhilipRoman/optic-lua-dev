package optic.lua.messages;

import optic.lua.CodeSource;
import org.jetbrains.annotations.NotNull;

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

	@NotNull
	@Override
	public Phase phase() {
		return Objects.requireNonNull(phase);
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
		checkInit(this.phase, "source");
		this.source = source;
	}

	@Override
	public void setLevel(Level level) {
		checkInit(this.level, "level");
		this.level = level;
	}

	@Override
	public void setPhase(Phase phase) {
		checkInit(this.phase, "phase");
		this.phase = phase;
	}

	@Override
	public void setLine(int line) {
		checkInitInt(this.line, "line");
		this.line = line;
	}

	@Override
	public void setColumn(int column) {
		checkInitInt(this.column, "column");
		this.column = column;
	}

	@Override
	public void setCause(Throwable cause) {
		checkInit(this.cause, "cause");
		this.cause = cause;
	}

	private static void checkInit(Object field, String name) {
		if (field != null) throw new IllegalStateException(name + " is already initialized");
	}

	private static void checkInitInt(int field, String name) {
		if (field >= 0) throw new IllegalStateException(name + " is already initialized");
	}
}

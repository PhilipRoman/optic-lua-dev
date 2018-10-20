package optic.lua;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public final class Identifier {
	@NotNull
	private final String text;
	private static final Pattern pattern = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$");

	private Identifier(@NotNull String text) {
		this.text = text;
	}

	@NotNull
	public static Identifier valueOf(String text) {
		requireNonNull(text);
		if (!pattern.matcher(text).matches()) {
			throw new IllegalArgumentException("Identifier does not match " + pattern.pattern());
		}
		return new Identifier(text);
	}

	@Override
	@NotNull
	public String toString() {
		return "Identifier/" + text;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Identifier && ((Identifier) obj).text.equals(text);
	}
}

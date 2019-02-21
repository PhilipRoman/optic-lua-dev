package optic.lua.messages;

import java.util.Set;

/**
 * Throw this method from anywhere to indicate that compilation can't be continued because of an error
 */
public class CompilationFailure extends Exception {
	private final Set<Tag> tags;

	public CompilationFailure(Tag... tags) {
		this.tags = Set.of(tags);
	}

	public CompilationFailure(Tag tag) {
		this.tags = Set.of(tag);
	}

	public CompilationFailure() {
		this.tags = Set.of();
	}

	@Override
	public String getMessage() {
		return "Compilation failure - " + tags.toString();
	}
}

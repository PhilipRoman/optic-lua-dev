package optic.lua.messages;

import java.util.*;

public final class Options {
	private final Map<Option<?>, Object> table = new HashMap<>(8);

	public Options() {
	}

	public <T> void set(Option<T> key, T value) {
		table.put(key, value);
	}

	public void enable(Option<Boolean> key) {
		table.put(key, true);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Option<T> key) {
		return (T) table.getOrDefault(key, key.defaultValue());
	}
}

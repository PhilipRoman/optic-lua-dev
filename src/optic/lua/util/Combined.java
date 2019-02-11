package optic.lua.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public abstract class Combined<T> {
	private final List<@NotNull Supplier<@NotNull T>> sources = new ArrayList<>();
	private volatile boolean recursionDetector = false;

	protected abstract T reduce(T a, T b);

	protected abstract boolean isAlreadyMax(T value);

	protected abstract T emptyValue();

	public T get() {
		if (recursionDetector) {
			return emptyValue();
		}
		if(sources.isEmpty()) {
			return emptyValue();
		}
		Iterator<Supplier<T>> iterator = sources.iterator();
		T accumulator = callWithoutRecursion(iterator.next());
		while (iterator.hasNext()) {
			T value = callWithoutRecursion(iterator.next());
			accumulator = reduce(accumulator, value);
			// shortcut
			if (isAlreadyMax(accumulator)) {
				return accumulator;
			}
		}
		return accumulator;
	}

	private T callWithoutRecursion(Supplier<T> function) {
		recursionDetector = true;
		T value = function.get();
		recursionDetector = false;
		return value;
	}

	public void add(Supplier<T> supplier) {
		Objects.requireNonNull(supplier);
		sources.add(supplier);
	}

	public void add(T value) {
		Objects.requireNonNull(value);
		sources.add(() -> value);
	}

	@Override
	public String toString() {
		return get().toString();
	}
}

package optic.lua.runtime;

import java.util.Arrays;
import java.util.stream.IntStream;

@RuntimeApi
public final class MultiValue {
	private static final MultiValue EMPTY = new MultiValue();
	private static final MultiValue[] SINGLE_INT_CACHE = IntStream.range(0, 16)
			.mapToObj(DynamicNumber::of)
			.map(MultiValue::of)
			.toArray(MultiValue[]::new);
	final Dynamic[] values;

	private MultiValue(Dynamic... values) {
		this.values = values;
	}

	@RuntimeApi
	public static MultiValue of(Dynamic... values) {
		if (values.length == 0) {
			return EMPTY;
		}
		return new MultiValue(values);
	}

	@RuntimeApi
	public static MultiValue of() {
		return EMPTY;
	}

	@RuntimeApi
	public static MultiValue of(MultiValue trailing, Dynamic... values) {
		if (trailing.length() == 0) {
			return of(values);
		}
		Dynamic[] array = Arrays.copyOf(values, values.length + trailing.length());
		System.arraycopy(trailing.values, 0, array, values.length + 1, trailing.length());
		return new MultiValue(array);
	}

	static MultiValue singleInt(int i) {
		if (i >= 0 && i < SINGLE_INT_CACHE.length) {
			return SINGLE_INT_CACHE[i];
		} else {
			return MultiValue.of(DynamicNumber.of(i));
		}
	}

	int length() {
		return values.length;
	}

	@RuntimeApi
	public Dynamic select(int index) {
		return index >= 0 && index < values.length ? values[index] : DynamicNil.nil();
	}

	@RuntimeApi
	public MultiValue selectFrom(int index) {
		return new MultiValue(Arrays.copyOfRange(values, index, values.length));
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(values);
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof MultiValue && Arrays.equals(((MultiValue) obj).values, values);
	}

	@Override
	public String toString() {
		return Arrays.toString(values);
	}
}

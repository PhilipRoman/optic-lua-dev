package optic.lua.runtime;

import java.util.Arrays;

@RuntimeApi
public final class MultiValue {
	private static final MultiValue EMPTY = new MultiValue();
	final Dynamic[] values;

	private MultiValue(Dynamic... values) {
		this.values = values;
	}

	@RuntimeApi
	public static MultiValue of(Dynamic... values) {
		return new MultiValue(values);
	}

	@RuntimeApi
	public static MultiValue of() {
		return EMPTY;
	}

	@RuntimeApi
	public static MultiValue of(MultiValue trailing, Dynamic... values) {
		if (trailing.length() == 0) {
			return new MultiValue(values);
		}
		Dynamic[] array = Arrays.copyOf(values, values.length + trailing.length());
		System.arraycopy(trailing.values, 0, array, values.length + 1, trailing.length());
		return new MultiValue(array);
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
}

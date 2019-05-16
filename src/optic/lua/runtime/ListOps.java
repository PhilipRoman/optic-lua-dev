package optic.lua.runtime;

@RuntimeApi
public final class ListOps {
	private static Object[] EMPTY = {};
	private static Object[] TRUE = {true};
	private static Object[] FALSE = {false};

	@RuntimeApi
	public static Object[] empty() {
		return EMPTY;
	}

	@RuntimeApi
	public static Object[] concat(Object[] toAppend, Object... original) {
		if (toAppend.length == 0) {
			return original;
		}
		if (original.length == 0) {
			return toAppend;
		}
		Object[] array = new Object[original.length + toAppend.length];
		System.arraycopy(original, 0, array, 0, original.length);
		System.arraycopy(toAppend, 0, array, original.length, toAppend.length);
		return array;
	}

	@RuntimeApi
	public static Object[] create() {
		return EMPTY;
	}

	@RuntimeApi
	public static Object[] create(Object... args) {
		return args;
	}

	static Object[] createWithBoolean(boolean value) {
		return value ? TRUE : FALSE;
	}

	@RuntimeApi
	public static Object get(Object[] array, int index) {
		if (index >= 0 && index < array.length) {
			return array[index];
		}
		return null;
	}

	@RuntimeApi
	public static Object[] sublist(Object[] array, int from) {
		if (from == array.length) {
			return EMPTY;
		}
		if (from == 0) {
			return array;
		}
		Object[] sub = new Object[array.length - from];
		System.arraycopy(array, from, sub, 0, sub.length);
		return sub;
	}
}

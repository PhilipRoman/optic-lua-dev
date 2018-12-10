package optic.lua.runtime;

@RuntimeApi
public final class MultiValue {
	@RuntimeApi
	public static MultiValue of(Dynamic... values) {
		return null;
	}

	@RuntimeApi
	public static MultiValue of() {
		return null;
	}

	@RuntimeApi
	public static MultiValue of(MultiValue trailing, Dynamic... values) {
		return null;
	}

	@RuntimeApi
	public Dynamic select(int index) {
		return null;
	}

	@RuntimeApi
	public MultiValue selectFrom(int index) {
		return null;
	}
}

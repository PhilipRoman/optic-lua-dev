package optic.lua.runtime;

@RuntimeApi
public final class UpValue {
	Object value = null;

	@RuntimeApi
	public static UpValue create() {
		return new UpValue();
	}

	public static UpValue create(Object value) {
		UpValue u = new UpValue();
		u.value = value;
		return u;
	}

	@RuntimeApi
	public Object get() {
		return value;
	}

	@RuntimeApi
	public void set(Object value) {
		this.value = value;
	}
}

package optic.lua.runtime;

@RuntimeApi
public final class UpValue {
	Object value = null;
	@RuntimeApi
	public static UpValue create() {
		return new UpValue();
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

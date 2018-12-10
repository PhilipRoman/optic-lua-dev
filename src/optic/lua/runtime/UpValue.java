package optic.lua.runtime;

@RuntimeApi
public final class UpValue {
	@RuntimeApi
	public static UpValue create() {
		return new UpValue();
	}

	@RuntimeApi
	public Dynamic get() {
		return null;
	}

	@RuntimeApi
	public void set(Dynamic value) {
	}
}

package optic.lua.runtime;

@RuntimeApi
public abstract class Dynamic {
	@RuntimeApi
	public static Dynamic of(Object x) {
		return null;
	}

	@RuntimeApi
	public static Dynamic of(double x) {
		return null;
	}

	@RuntimeApi
	public Dynamic get(Dynamic key) {
		return null;
	}

	@RuntimeApi
	public Dynamic get(String key) {
		return null;
	}

	@RuntimeApi
	public void set(Dynamic key, Dynamic value) {

	}

	@RuntimeApi
	public void set(String key, Dynamic value) {

	}

	@RuntimeApi
	public MultiValue call(MultiValue args) {
		return null;
	}
}

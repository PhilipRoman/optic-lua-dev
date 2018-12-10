package optic.lua.runtime;

@RuntimeApi
public abstract class DynamicFunction extends Dynamic {
	@RuntimeApi
	public DynamicFunction() {
		super(Dynamic.FUNCTION);
	}

	@RuntimeApi
	public abstract MultiValue call(MultiValue args);
}

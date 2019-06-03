package optic.lua.runtime.invoke;

public final class SimpleFunctionMetafactory implements FunctionMetafactory {
	public SimpleFunctionMetafactory() {
	}

	@Override
	public FunctionFactory create(int id) {
		return new SimpleFunctionFactory(id);
	}
}

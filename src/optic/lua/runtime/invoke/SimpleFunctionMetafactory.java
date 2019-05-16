package optic.lua.runtime.invoke;

public final class SimpleFunctionMetafactory implements FunctionMetafactory {
	@Override
	public FunctionFactory create(int id) {
		return new SimpleFunctionFactory(id);
	}
}

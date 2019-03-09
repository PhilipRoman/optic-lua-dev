package optic.lua.runtime.invoke;

public class SimpleFunctionFactory implements FunctionFactory {
	private final String name;

	public SimpleFunctionFactory(String name) {
		this.name = name;
	}

	public SimpleFunctionFactory(int id) {
		this("anon_" + id);
	}

	@Override
	public String toString() {
		return name;
	}
}

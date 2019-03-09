package optic.lua.runtime.invoke;

public class SimpleFunctionConstructionSite implements FunctionConstructionSite {
	private final String name;

	public SimpleFunctionConstructionSite(String name) {
		this.name = name;
	}

	public SimpleFunctionConstructionSite(int id) {
		this("anon_" + id);
	}

	@Override
	public String toString() {
		return name;
	}
}

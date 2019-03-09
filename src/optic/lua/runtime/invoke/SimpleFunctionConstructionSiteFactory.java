package optic.lua.runtime.invoke;

public class SimpleFunctionConstructionSiteFactory implements FunctionConstructionSiteFactory {
	@Override
	public FunctionConstructionSite create(int id) {
		return new SimpleFunctionConstructionSite(id);
	}
}

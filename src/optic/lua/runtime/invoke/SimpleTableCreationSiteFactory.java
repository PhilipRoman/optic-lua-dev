package optic.lua.runtime.invoke;

public class SimpleTableCreationSiteFactory implements TableCreationSiteFactory {
	public TableCreationSite create(int id) {
		return new SimpleTableCreationSite(id);
	}
}

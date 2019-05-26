package optic.lua.runtime.invoke;

public final class SimpleCallSiteFactory implements CallSiteFactory {
	@Override
	public CallSite create(int id) {
		return new SimpleCallSite(id);
	}
}

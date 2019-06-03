package optic.lua.runtime.invoke;

public final class SimpleCallSiteFactory implements CallSiteFactory {
	public SimpleCallSiteFactory() {
	}

	@Override
	public CallSite create(int id) {
		return new SimpleCallSite(id);
	}
}

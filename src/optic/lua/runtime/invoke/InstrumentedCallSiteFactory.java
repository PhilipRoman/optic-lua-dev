package optic.lua.runtime.invoke;

public final class InstrumentedCallSiteFactory implements CallSiteFactory {
	@Override
	public CallSite create(int id) {
		return new InstrumentedCallSite(id);
	}
}

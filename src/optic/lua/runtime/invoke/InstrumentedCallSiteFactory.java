package optic.lua.runtime.invoke;

public class InstrumentedCallSiteFactory implements CallSiteFactory {
	@Override
	public CallSite create(int id) {
		return new InstrumentedCallSite(id);
	}
}

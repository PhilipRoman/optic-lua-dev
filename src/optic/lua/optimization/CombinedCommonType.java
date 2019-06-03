package optic.lua.optimization;

import optic.lua.util.Combined;

public final class CombinedCommonType extends Combined<ProvenType> {
	public CombinedCommonType() {
	}

	@Override
	protected ProvenType reduce(ProvenType a, ProvenType b) {
		return a.and(b);
	}

	@Override
	protected boolean isAlreadyMax(ProvenType value) {
		return value == ProvenType.OBJECT;
	}

	@Override
	protected ProvenType emptyValue() {
		return ProvenType.OBJECT;
	}
}

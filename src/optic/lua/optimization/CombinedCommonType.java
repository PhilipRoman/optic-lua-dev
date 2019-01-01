package optic.lua.optimization;

import optic.lua.util.Combined;

public class CombinedCommonType extends Combined<ProvenType> {
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
		return ProvenType.UNKNOWN;
	}
}

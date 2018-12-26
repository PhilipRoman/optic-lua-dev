package optic.lua.optimization;

import optic.lua.util.Combined;

public class CombinedSpeculatedType extends Combined<SpeculatedType> {
	@Override
	protected SpeculatedType reduce(SpeculatedType a, SpeculatedType b) {
		return a.and(b);
	}

	@Override
	protected boolean isAlreadyMax(SpeculatedType value) {
		return value == SpeculatedType.META;
	}

	@Override
	protected SpeculatedType emptyValue() {
		return SpeculatedType.UNKNOWN;
	}
}

package optic.lua.optimization;

import optic.lua.util.Combined;

public final class CombinedCommonType extends Combined<StaticType> {
	public CombinedCommonType() {
	}

	@Override
	protected StaticType reduce(StaticType a, StaticType b) {
		return a.and(b);
	}

	@Override
	protected boolean isAlreadyMax(StaticType value) {
		return value == StaticType.OBJECT;
	}

	@Override
	protected StaticType emptyValue() {
		return StaticType.OBJECT;
	}
}

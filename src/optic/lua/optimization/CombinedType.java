package optic.lua.optimization;

import optic.lua.util.Combined;

public final class CombinedType extends Combined<StaticType> {
	public CombinedType() {
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

package optic.lua.optimization;

import optic.lua.util.Combined;

public class CombinedTypeStatus extends Combined<TypeStatus> {
	@Override
	protected TypeStatus reduce(TypeStatus a, TypeStatus b) {
		return a.and(b);
	}

	@Override
	protected boolean isAlreadyMax(TypeStatus value) {
		return value == TypeStatus.OBJECT;
	}

	@Override
	protected TypeStatus emptyValue() {
		return TypeStatus.UNKNOWN;
	}
}

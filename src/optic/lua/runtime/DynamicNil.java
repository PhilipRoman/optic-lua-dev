package optic.lua.runtime;

final class DynamicNil extends Dynamic {
	private static final DynamicNil NIL = new DynamicNil();

	static Dynamic nil() {
		return NIL;
	}

	private DynamicNil() {
		super(Dynamic.NIL);
	}
}

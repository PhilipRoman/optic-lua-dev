package optic.lua.runtime;

final class DynamicBool extends Dynamic {
	static final DynamicBool TRUE = new DynamicBool();
	static final DynamicBool FALSE = new DynamicBool();

	private DynamicBool() {
		super(Dynamic.BOOL);
	}
}

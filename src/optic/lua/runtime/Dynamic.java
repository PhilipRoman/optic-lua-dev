package optic.lua.runtime;

import java.util.*;

@RuntimeApi
public abstract class Dynamic {
	final int type;

	static final int NIL = 0;
	static final int NUMBER = 1;
	static final int FUNCTION = 2;
	static final int STRING = 3;
	static final int BOOL = 4;
	static final int TABLE = 5;

	protected Dynamic(int type) {
		this.type = type;
	}

	@RuntimeApi
	@SuppressWarnings("unchecked")
	public static Dynamic of(Object x) {
		if(x == null) {
			return DynamicNil.nil();
		}
		if(x instanceof Dynamic) {
			return (Dynamic) x;
		}
		if(x instanceof Number) {
			return DynamicNumber.of(((Number) x).doubleValue());
		}
		if(x instanceof CharSequence) {
			return DynamicString.of(x.toString());
		}
		if(x instanceof List) {
			return DynamicTable.ofArray((List)x);
		}
		if(x instanceof Map) {
			return DynamicTable.ofMap((Map)x);
		}
		return DynamicNil.nil();
	}

	@RuntimeApi
	public static Dynamic of(double x) {
		return new DynamicNumber(x);
	}

	@RuntimeApi
	public Dynamic get(Dynamic key) {
		Errors.forbidden();
		return null;
	}

	@RuntimeApi
	public Dynamic get(String key) {
		Errors.forbidden();
		return null;
	}

	@RuntimeApi
	public void set(Dynamic key, Dynamic value) {
		Errors.forbidden();
	}

	@RuntimeApi
	public void set(String key, Dynamic value) {
		Errors.forbidden();
	}

	@RuntimeApi
	public MultiValue call(MultiValue args) {
		Errors.forbidden();
		return null;
	}
}

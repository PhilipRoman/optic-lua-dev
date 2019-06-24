package optic.lua.optimization;

import java.util.function.Supplier;

public class StaticType implements Supplier<StaticType> {
	public static final StaticType OBJECT = extend(null);
	public static final StaticType NUMBER = extend(OBJECT);
	public static final StaticType INTEGER = extend(NUMBER);

	public static final StaticType BOOLEAN = extend(OBJECT);
	public static final StaticType STRING = extend(OBJECT);
	public static final StaticType TABLE = extend(OBJECT);
	public static final StaticType FUNCTION = extend(OBJECT);

	private final StaticType superType;

	private StaticType(StaticType type) {
		superType = type;
	}

	private static StaticType extend(StaticType superType) {
		return new StaticType(superType);
	}

	public StaticType and(StaticType other) {
		var tmp = this;
		while (!other.subtypeOf(tmp)) {
			tmp = tmp.superType;
		}
		return tmp;
	}

	public boolean subtypeOf(StaticType type) {
		if (this == type)
			return true;
		if (superType == null)
			return false;
		return superType.subtypeOf(type);
	}

	public boolean isNumeric() {
		return this == NUMBER || this == INTEGER;
	}

	@Override
	public StaticType get() {
		return this;
	}
}

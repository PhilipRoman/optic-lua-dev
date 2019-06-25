package optic.lua.optimization;

import java.util.function.Supplier;

public class StaticType implements Supplier<StaticType> {
	public static final StaticType OBJECT = extend(null, "object");
	public static final StaticType NUMBER = extend(OBJECT, "number");
	public static final StaticType INTEGER = extend(NUMBER, "integer");

	public static final StaticType BOOLEAN = extend(OBJECT, "boolean");
	public static final StaticType STRING = extend(OBJECT, "string");
	public static final StaticType TABLE = extend(OBJECT, "table");
	public static final StaticType FUNCTION = extend(OBJECT, "function");

	private final StaticType superType;
	private final String name;

	private StaticType(StaticType type, String name) {
		superType = type;
		this.name = name;
	}

	private static StaticType extend(StaticType superType, String name) {
		return new StaticType(superType, name);
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

	@Override
	public String toString() {
		return name;
	}
}

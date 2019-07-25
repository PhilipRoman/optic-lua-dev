package optic.lua.codegen.java;

import optic.lua.asm.*;
import optic.lua.optimization.StaticType;
import org.codehaus.janino.InternalCompilerException;

import java.util.*;

import static optic.lua.optimization.StaticType.*;

final class JavaUtils {
	private JavaUtils() {
	}

	private static IdentityHashMap<StaticType, String> typeNames = new IdentityHashMap<>(Map.of(
			OBJECT, "Object",
			NUMBER, "double",
			INTEGER, "long",
			BOOLEAN, "boolean",
			STRING, "String",
			FUNCTION, "LuaFunction",
			TABLE, "LuaTable"
	));

	private static IdentityHashMap<StaticType, String> upvalueTypeNames = new IdentityHashMap<>(Map.of(
			OBJECT, "UpValue",
			NUMBER, "UpValue.OfNum",
			INTEGER, "UpValue.OfInt",
			FUNCTION, "UpValue.OfFunction",
			TABLE, "UpValue.OfTable"
	));

	static String typeName(Register r) {
		return typeName(r.typeInfo());
	}

	static String typeName(VariableInfo i) {
		if (i.getMode() == VariableMode.UPVALUE && !i.isFinal()) {
			return upvalueTypeNames.getOrDefault(i.typeInfo(), "UpValue");
		}
		return typeName(i.typeInfo());
	}

	static String typeName(StaticType type) {
		String name = typeNames.get(type);
		if (name == null)
			throw new InternalCompilerException("No type name available for " + type.getClass());
		return name;
	}
}

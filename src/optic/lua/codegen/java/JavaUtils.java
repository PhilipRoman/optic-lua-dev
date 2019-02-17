package optic.lua.codegen.java;

import optic.lua.asm.*;
import optic.lua.optimization.ProvenType;

final class JavaUtils {
	private JavaUtils() {
	}

	static String typeName(Register r) {
		return r.isVararg() ? "Object[]" : typeName(r.typeInfo());
	}

	static String typeName(VariableInfo i) {
		return typeName(i.typeInfo());
	}

	static String typeName(ProvenType type) {
		switch (type) {
			case OBJECT:
				return "Object";
			case NUMBER:
				return "double";
			case INTEGER:
				return "long";
			default:
				throw new AssertionError("should never reach here");
		}
	}
}

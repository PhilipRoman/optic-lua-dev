package optic.lua.codegen.java;

import optic.lua.optimization.ProvenType;

final class JavaTypes {
	private JavaTypes() {
	}

	static String getTypeName(ProvenType type) {
		switch (type) {
			case UNKNOWN:
			case OBJECT:
				return "Object";
			case NUMBER:
				return "double";
			default:
				throw new AssertionError("should never reach here");
		}
	}
}

package optic.lua.asm;

import java.util.EnumSet;

final class VariableInfo {
	private final EnumSet<AccessType> access = EnumSet.noneOf(AccessType.class);

	EnumSet<AccessType> accessInfo() {
		return access;
	}

	enum AccessType {
		READ, WRITE
	}
}

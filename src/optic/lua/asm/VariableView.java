package optic.lua.asm;

import optic.lua.asm.VariableInfo.AccessType;

final class VariableView {
	private final VariableInfo variable;
	private final ViewType type;

	private VariableView(VariableInfo variable, ViewType type) {
		this.variable = variable;
		this.type = type;
	}

	void markAsRead() {
		variable.accessInfo().add(AccessType.READ);
	}

	void markAsWritten() {
		variable.accessInfo().add(AccessType.WRITE);
	}

	public ViewType viewType() {
		return type;
	}

	public VariableInfo variable() {
		return variable;
	}

	enum ViewType {
		LOCAL, UPVALUE;
	}

	static VariableView viewAsUpvalue(VariableInfo variable) {
		return new VariableView(variable, ViewType.UPVALUE);
	}

	static VariableView viewAsLocal(VariableInfo variable) {
		return new VariableView(variable, ViewType.LOCAL);
	}
}

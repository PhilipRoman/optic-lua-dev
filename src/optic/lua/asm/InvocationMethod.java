package optic.lua.asm;

import optic.lua.optimization.*;

public enum InvocationMethod {
	ADD,
	BAND,
	BNOT,
	BOR,
	BXOR,
	CALL(ReturnCount.ANY),
	CONCAT,
	DIV,
	EQ,
	GE,
	GT,
	IDIV,
	INDEX,
	LE,
	LEN,
	LT,
	MOD,
	MUL,
	POW,
	SET_INDEX(ReturnCount.ZERO),
	SHL,
	SHR,
	SUB,
	TO_NUMBER,
	UNM;

	private final ReturnCount returnCount;

	InvocationMethod() {
		this(ReturnCount.ONE);
	}

	InvocationMethod(ReturnCount count) {
		returnCount = count;
	}

	public ReturnCount getReturnCount() {
		return returnCount;
	}

	public ProvenType typeInfo(ExprNode object, ListNode arguments) {
		switch (this) {
			case INDEX:
			case CALL:
			case SET_INDEX:
				return ProvenType.OBJECT;
			case TO_NUMBER:
				return ProvenType.NUMBER;
			default:
				var luaOp = LuaOperator.valueOf(name());
				if (luaOp.arity() == 2) {
					return luaOp.resultType(object.typeInfo(), arguments.childTypeInfo(0));
				} else {
					assert luaOp.arity() == 1;
					return luaOp.resultType(null, object.typeInfo());
				}
		}
	}

	public enum ReturnCount {
		ZERO, ONE, ANY
	}
}

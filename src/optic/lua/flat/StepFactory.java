package optic.lua.flat;

import optic.lua.flat.ops.*;

import java.util.List;

class StepFactory {
	static Step assign(List<String> names, List<Register> values) {
		return new Assign(StepType.ASSIGN, names, values);
	}

	static Step declareLocal(String name) {
		return new Declare(StepType.DECLARE, name);
	}

	static Step constNumber(Register register, double num) {
		return new LoadConstant(StepType.NUMBER, register, num);
	}

	static Step constString(Register register, String string) {
		return new LoadConstant(StepType.STRING, register, string);
	}

	public static Step binaryOperator(Register a, Register b, String op, Register register) {
		return new Operator(StepType.BINARY_OP, a, b, register, op);
	}

	public static Step unaryOperator(Register param, String op, Register register) {
		return new Operator(StepType.UNARY_OP, param, register, op);
	}

	public static Step dereference(Register register, String name) {
		return new Dereference(StepType.LOOKUP, register, name);
	}

	public static Step forRange(String varName, Register from, Register to, List<Step> block) {
		return new ForRangeLoop(StepType.FOR, varName, from, to, block);
	}

	public static Step call(Register function, List<Register> args) {
		return new Call(StepType.CALL, function, args);
	}

	public static Step comment(String text) {
		return new Comment(text);
	}

	public static Step call(Register function, List<Register> registers, Register output) {
		return new Call(StepType.CALL, function, registers, output);
	}
}

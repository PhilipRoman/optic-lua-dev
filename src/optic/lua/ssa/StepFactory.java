package optic.lua.ssa;

import optic.lua.ssa.instructions.*;

import java.util.*;

class StepFactory {
	static Step assign(List<LValue> names, List<Register> values) {
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

	static Step binaryOperator(Register a, Register b, String op, Register register) {
		return new Operator(StepType.BINARY_OP, a, b, register, op);
	}

	static Step unaryOperator(Register param, String op, Register register) {
		return new Operator(StepType.UNARY_OP, param, register, op);
	}

	static Step dereference(Register register, String name) {
		return new Dereference(StepType.LOOKUP, register, name);
	}

	static Step forRange(String varName, Register from, Register to, List<Step> block) {
		return new ForRangeLoop(StepType.FOR, varName, from, to, block);
	}

	static Step call(Register function, List<Register> args) {
		return new Call(StepType.CALL, function, args);
	}

	static Step comment(String text) {
		return new Comment(text);
	}

	static Step call(Register function, List<Register> registers, Register output) {
		return new Call(StepType.CALL, function, registers, output);
	}

	static Step doBlock(List<Step> steps) {
		return new Block(StepType.BLOCK, steps);
	}

	static Step functionLiteral(List<Step> body, Register assignTo, ParameterList params) {
		return new FunctionLiteral(StepType.FUNCTION, body, assignTo, params);
	}

	static Step returnFromFunction(List<Register> registers) {
		return new Return(StepType.RETURN, registers);
	}

	static Step getVarargs(Register to) {
		if(!to.isVararg()) {
			throw new IllegalArgumentException(to + " is not a vararg register!");
		}
		return new GetVarargs(StepType.VARARGS, to);
	}

	static Step createTable(Map<Register, Register> table, Register result) {
		return new MakeTable(StepType.TABLE, table, result);
	}

	static Step constNil(Register register) {
		return new LoadConstant(StepType.NIL, register, null);
	}

	static Step tableIndex(Register table, Register key, Register out) {
		return new TableIndex(StepType.INDEX, table, key, out);
	}

	static Step ifThen(Register condition, List<Step> body) {
		return new Branch(StepType.BRANCH, condition, body);
	}
}

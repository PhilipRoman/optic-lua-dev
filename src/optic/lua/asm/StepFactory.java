package optic.lua.asm;

import optic.lua.asm.instructions.*;

import java.util.*;

class StepFactory {
	static Step assign(List<LValue> names, List<Register> values) {
		return new Assign(names, values);
	}

	static Step declareLocal(String name) {
		return new Declare(name);
	}

	static Step constNumber(Register register, double num) {
		return new LoadConstant(register, num);
	}

	static Step constString(Register register, String string) {
		return new LoadConstant(register, string);
	}

	static Step binaryOperator(Register a, Register b, String op, Register register) {
		return new Operator(a, b, register, op);
	}

	static Step unaryOperator(Register param, String op, Register register) {
		return new Operator(param, register, op);
	}

	static Step dereference(Register register, String name) {
		return new Dereference(register, name);
	}

	static Step forRange(String varName, Register from, Register to, List<Step> block) {
		return new ForRangeLoop(varName, from, to, block);
	}

	static Step call(Register function, List<Register> args) {
		return new Call(function, args, RegisterFactory.unused());
	}

	static Step comment(String text) {
		return new Comment(text);
	}

	static Step call(Register function, List<Register> registers, Register output) {
		return new Call(function, registers, output);
	}

	static Step doBlock(List<Step> steps) {
		return new Block(steps);
	}

	static Step functionLiteral(List<Step> body, Register assignTo, ParameterList params) {
		return new FunctionLiteral(body, assignTo, params);
	}

	static Step returnFromFunction(List<Register> registers) {
		return new Return(registers);
	}

	static Step getVarargs(Register to) {
		if(!to.isVararg()) {
			throw new IllegalArgumentException(to + " is not a vararg register!");
		}
		return new GetVarargs(to);
	}

	static Step createTable(Map<Register, Register> table, Register result) {
		return new MakeTable(table, result);
	}

	static Step constNil(Register register) {
		return new LoadConstant(register, null);
	}

	static Step tableIndex(Register table, Register key, Register out) {
		return new TableIndex(table, key, out);
	}

	static Step ifThen(Register condition, List<Step> body) {
		return new Branch(condition, body);
	}
}

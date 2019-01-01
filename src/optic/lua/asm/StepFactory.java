package optic.lua.asm;

import optic.lua.asm.instructions.*;
import optic.lua.optimization.LuaOperator;

import java.util.*;

class StepFactory {
	static Step tableWrite(LValue.TableField target, Register value) {
		checkVararg(false, value);
		return new TableWrite(target, value);
	}

	static Step declareLocal(VariableInfo info) {
		return new Declare(info);
	}

	static Step constNumber(Register register, double num) {
		return new LoadConstant(register, num);
	}

	static Step constString(Register register, String string) {
		return new LoadConstant(register, string);
	}

	static Step binaryOperator(Register a, Register b, LuaOperator op, Register register) {
		return new Operation(a, b, register, op);
	}

	static Step unaryOperator(Register param, LuaOperator op, Register register) {
		return new Operation(param, register, op);
	}

	static Step forRange(VariableInfo counter, Register from, Register to, AsmBlock block) {
		return new ForRangeLoop(counter, from, to, block);
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

	static Step doBlock(AsmBlock block) {
		return new Block(block);
	}

	static Step functionLiteral(AsmBlock body, Register assignTo, ParameterList params) {
		return new FunctionLiteral(body, assignTo, params);
	}

	static Step returnFromFunction(List<Register> registers) {
		return new Return(registers);
	}

	static Step getVarargs(Register to) {
		checkVararg(true, to);
		return new GetVarargs(to);
	}

	static Step createTable(Map<Register, Register> table, Register result) {
		return new MakeTable(table, result);
	}

	static Step constNil(Register register) {
		return new LoadConstant(register, null);
	}

	static Step tableIndex(Register table, Register key, Register out) {
		return new TableRead(table, key, out);
	}

	static Step select(Register out, Register varargs, int n) {
		checkVararg(false, out);
		return new Select(out, varargs, n);
	}

	private static void checkVararg(boolean expected, Register register) {
		if (register.isVararg() != expected) {
			var msg = register + " is " + (expected ? "not " : "") + "a vararg register!";
			throw new IllegalArgumentException(msg);
		}
	}

	static Step write(VariableInfo target, Register value) {
		return new Write(target, value);
	}

	static Step read(VariableInfo source, Register target) {
		return new Read(target, source);
	}

	static Step toNumber(Register source, Register target) {
		checkVararg(false, source);
		checkVararg(false, target);
		return new ToNumber(source, target);
	}

	public static Step constBool(Register register, boolean value) {
		return new LoadConstant(register, value);
	}

	public static Step ifThenChain(Map<FlatExpr, AsmBlock> clauses) {
		return new IfElseChain(clauses);
	}
}

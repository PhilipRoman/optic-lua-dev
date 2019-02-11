package optic.lua.asm;

import optic.lua.asm.instructions.*;
import optic.lua.optimization.LuaOperator;

import java.util.*;

class StepFactory {
	static Step tableWrite(LValue.TableField target, RValue value) {
		checkVararg(false, value);
		return new Invoke(RegisterFactory.unused(), target.getTable(), InvocationMethod.SET_INDEX, List.of(target.getKey(), value));
	}

	static Step declareLocal(VariableInfo info) {
		return new Declare(info);
	}

	static Step binaryOperator(RValue a, RValue b, LuaOperator op, Register target) {
		return new Invoke(target, a, op.invocationTarget(), List.of(b));
	}

	static Step unaryOperator(RValue argument, LuaOperator op, Register target) {
		return new Invoke(target, argument, op.invocationTarget(), List.of());
	}

	static Step forRange(VariableInfo counter, RValue from, RValue to, AsmBlock block) {
		return new ForRangeLoop(counter, from, to, block);
	}

	static Step call(RValue function, List<RValue> arguments) {
		return new Invoke(RegisterFactory.unused(), function, InvocationMethod.CALL, arguments);
	}

	static Step comment(String text) {
		return new Comment(text);
	}

	static Step call(RValue function, List<RValue> arguments, Register output) {
		return new Invoke(output, function, InvocationMethod.CALL, arguments);
	}

	static Step doBlock(AsmBlock block) {
		return new Block(block);
	}

	static Step returnFromFunction(List<RValue> registers) {
		return new Return(registers);
	}

	static Step getVarargs(Register to) {
		checkVararg(true, to);
		return new GetVarargs(to);
	}

	static Step assign(Register result, RValue value) {
		return new Assign(result, value);
	}

	static Step tableIndex(RValue table, RValue key, Register out) {
		return new Invoke(out, table, InvocationMethod.INDEX, List.of(key));
	}

	static Step select(Register out, Register varargs, int n) {
		checkVararg(false, out);
		return new Select(out, varargs, n);
	}

	private static void checkVararg(boolean expected, RValue register) {
		if (register.isVararg() != expected) {
			var msg = register + " is " + (expected ? "not " : "") + "a vararg register!";
			throw new IllegalArgumentException(msg);
		}
	}

	static Step write(VariableInfo target, RValue value) {
		return new Write(target, value);
	}

	static Step toNumber(RValue source, Register target) {
		checkVararg(false, source);
		checkVararg(false, target);
		return new ToNumber(source, target);
	}

	static Step ifThenChain(Map<FlatExpr, AsmBlock> clauses) {
		return new IfElseChain(clauses);
	}
}

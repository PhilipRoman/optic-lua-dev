package optic.lua.asm;

import optic.lua.asm.RValue.Invocation;

import java.util.*;

class StepFactory {
	static Step tableWrite(LValue.TableField target, RValue value) {
		checkVararg(false, value);
		return discard(RValue.invocation(target.getTable(), InvocationMethod.SET_INDEX, List.of(target.getKey(), value)));
	}

	static Step declareLocal(VariableInfo info) {
		return new Declare(info);
	}

	static Step forRange(VariableInfo counter, RValue from, RValue to, AsmBlock block) {
		return new ForRangeLoop(counter, from, to, block);
	}

	static Step comment(String text) {
		return new Comment(text);
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
		return assign(out, RValue.invocation(table, InvocationMethod.INDEX, List.of(key)));
	}

	static Step select(Register out, RValue varargs, int n) {
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

	static Step discard(Invocation invocation) {
		return new Void(invocation);
	}

	static Step ifThenChain(LinkedHashMap<FlatExpr, AsmBlock> clauses) {
		return new IfElseChain(clauses);
	}
}

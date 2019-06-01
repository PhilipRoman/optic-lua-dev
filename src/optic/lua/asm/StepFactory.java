package optic.lua.asm;

import optic.lua.asm.RValue.Invocation;

import java.util.*;

final class StepFactory {
	static Step tableWrite(LValue.TableField target, RValue value) {
		checkVararg(false, value);
		return discard(RValue.invocation(target.table(), InvocationMethod.SET_INDEX, List.of(target.key(), value)));
	}

	static Step declareLocal(VariableInfo info) {
		return new Step.Declare(info);
	}

	static Step forRange(VariableInfo counter, RValue from, RValue to, AsmBlock block) {
		return new Step.ForRangeLoop(counter, from, to, RValue.number(1), block);
	}

	static Step forRange(VariableInfo counter, RValue from, RValue to, RValue step, AsmBlock block) {
		return new Step.ForRangeLoop(counter, from, to, step, block);
	}

	static Step comment(String text) {
		return new Step.Comment(text);
	}

	static Step doBlock(AsmBlock block) {
		return new Step.Block(block);
	}

	static Step returnFromFunction(List<RValue> registers) {
		return new Step.Return(registers);
	}

	static Step assign(Register result, RValue value) {
		return new Step.Assign(result, value);
	}

	static Step tableIndex(RValue table, RValue key, Register out) {
		return assign(out, RValue.invocation(table, InvocationMethod.INDEX, List.of(key)));
	}

	static Step select(Register out, RValue varargs, int n) {
		checkVararg(false, out);
		return new Step.Select(out, varargs, n);
	}

	private static void checkVararg(boolean expected, RValue register) {
		if (register.isVararg() != expected) {
			var msg = register + " is " + (expected ? "not " : "") + "a vararg register!";
			throw new IllegalArgumentException(msg);
		}
	}

	static Step write(VariableInfo target, RValue value) {
		return new Step.Write(target, value);
	}

	static Step discard(Invocation invocation) {
		return new Step.Void(invocation);
	}

	static Step ifThenChain(LinkedHashMap<FlatExpr, AsmBlock> clauses) {
		return new Step.IfElseChain(clauses);
	}

	static Step breakIf(RValue expression, boolean isTrue) {
		return new Step.BreakIf(expression, isTrue);
	}

	static Step loop(AsmBlock body) {
		return new Step.Loop(body);
	}

	static Step forInLoop(List<VariableInfo> variables, RValue iterator, AsmBlock body) {
		return new Step.ForEachLoop(variables, iterator, body);
	}

	static Step lineNumber(int number) {
		return new Step.LineNumber(number);
	}
}

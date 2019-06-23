package optic.lua.asm;

import java.util.*;

final class StepFactory {
	static VoidNode tableWrite(LValue.TableField target, ExprNode value) {
		return discard(ExprNode.invocation(target.table(), InvocationMethod.SET_INDEX, ListNode.exprList(target.key(), value)));
	}

	static VoidNode declareLocal(VariableInfo info) {
		return new VoidNode.Declare(info);
	}

	static VoidNode forRange(VariableInfo counter, ExprNode from, ExprNode to, AsmBlock block) {
		return new VoidNode.ForRangeLoop(counter, from, to, ExprNode.number(1), block);
	}

	static VoidNode forRange(VariableInfo counter, ExprNode from, ExprNode to, ExprNode step, AsmBlock block) {
		return new VoidNode.ForRangeLoop(counter, from, to, step, block);
	}

	static VoidNode doBlock(AsmBlock block) {
		return new VoidNode.Block(block);
	}

	static VoidNode returnFromFunction(ListNode values) {
		return new VoidNode.Return(values);
	}

	static VoidNode assign(Register result, ExprNode value) {
		return new VoidNode.Assign(result, value);
	}

	static VoidNode write(VariableInfo target, ExprNode value) {
		return new VoidNode.Write(target, value);
	}

	static VoidNode discard(ListNode invocation) {
		return new VoidNode.Void(invocation);
	}

	static VoidNode ifThenChain(LinkedHashMap<FlatExpr, AsmBlock> clauses) {
		return new VoidNode.IfElseChain(clauses);
	}

	static VoidNode breakIf(ExprNode expression, boolean isTrue) {
		return new VoidNode.BreakIf(expression, isTrue);
	}

	static VoidNode loop(AsmBlock body) {
		return new VoidNode.Loop(body);
	}

	static VoidNode forInLoop(List<VariableInfo> variables, ExprNode iterator, AsmBlock body) {
		return new VoidNode.ForEachLoop(variables, iterator, body);
	}

	static VoidNode lineNumber(int number) {
		return new VoidNode.LineNumber(number);
	}

	private StepFactory() {
	}
}

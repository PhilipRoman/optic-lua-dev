package optic.lua.asm;

import java.util.*;

public interface StatementVisitor<T, X extends Throwable> {
	T visitAssignment(Register register, ExprNode value) throws X;

	T visitArrayAssignment(ArrayRegister register, ListNode value) throws X;

	T visitBlock(AsmBlock block) throws X;

	T visitBreakIf(ExprNode condition, boolean isTrue) throws X;

	T visitComment(String comment) throws X;

	T visitDeclaration(VariableInfo variable) throws X;

	T visitForEachLoop(List<VariableInfo> variables, ExprNode iterator, AsmBlock body) throws X;

	T visitForRangeLoop(VariableInfo counter, ExprNode from, ExprNode to, ExprNode step, AsmBlock body) throws X;

	T visitIfElseChain(LinkedHashMap<FlatExpr, AsmBlock> clauses) throws X;

	T visitLineNumber(int number) throws X;

	T visitLoop(AsmBlock body) throws X;

	T visitReturn(ListNode values) throws X;

	T visitVoid(ListNode invocation) throws X;

	T visitWrite(VariableInfo target, ExprNode value) throws X;

	default List<T> visitAll(List<VoidNode> values) throws X {
		var result = new ArrayList<T>(values.size());
		for (var v : values) {
			result.add(v.accept(this));
		}
		return result;
	}
}

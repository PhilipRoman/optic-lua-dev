package optic.lua.asm;

import java.util.*;

public interface StepVisitor<T, X extends Throwable> {
	T visitComment(String comment) throws X;

	T visitAssignment(Register register, RValue value) throws X;

	T visitBlock(AsmBlock block) throws X;

	T visitBreakIf(RValue condition, boolean isTrue) throws X;

	T visitDeclaration(VariableInfo variable) throws X;

	T visitForEachLoop(List<VariableInfo> variables, RValue iterator, AsmBlock body) throws X;

	T visitForRangeLoop(VariableInfo counter, RValue from, RValue to, RValue step, AsmBlock body) throws X;

	T visitIfElseChain(LinkedHashMap<FlatExpr, AsmBlock> clauses) throws X;

	T visitLoop(AsmBlock body) throws X;

	T visitReturn(List<RValue> values) throws X;

	T visitSelect(Register out, int n, RValue varargs) throws X;

	T visitVoid(RValue.Invocation invocation) throws X;

	T visitWrite(VariableInfo target, RValue value) throws X;

	default List<T> visitAll(List<Step> values) throws X {
		var result = new ArrayList<T>(values.size());
		for (var v : values) {
			result.add(v.accept(this));
		}
		return result;
	}
}

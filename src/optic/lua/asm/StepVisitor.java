package optic.lua.asm;

import java.util.*;

public interface StepVisitor<T, X extends Throwable> {
	T visitComment(String comment) throws X;

	T visitAssignment(Register register, RValue value) throws X;

	T visitBlock(AsmBlock block) throws X;

	T visitDeclaration(VariableInfo variable);

	T visitForRangeLoop(VariableInfo counter, RValue from, RValue to, AsmBlock body) throws X;

	T visitGetVarargs(Register register) throws X;

	T visitIfElseChain(LinkedHashMap<FlatExpr, AsmBlock> clauses) throws X;

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

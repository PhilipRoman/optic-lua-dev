package optic.lua.asm;

import optic.lua.asm.RValue.TableLiteral;

public interface RValueVisitor<T, X extends Throwable> {
	T visitNumberConstant(double n) throws X;

	T visitStringConstant(String s) throws X;

	T visitBooleanConstant(boolean b) throws X;

	T visitNilConstant() throws X;

	T visitTableConstructor(TableLiteral t) throws X;

	T visitFunctionLiteral(RValue.FunctionLiteral t) throws X;

	T visitRegister(Register r) throws X;

	T visitLocalName(VariableInfo variable) throws X;

	T visitUpValueName(VariableInfo upvalue) throws X;

	T visitGlobalName(VariableInfo global) throws X;

	T visitInvocation(RValue.Invocation invocation) throws X;

	T visitVarargs() throws X;

	T visitNot(RValue value) throws X;

	T visitAnd(RValue first, RValue second) throws X;

	T visitOr(RValue first, RValue second) throws X;

	T visitSelectNth(RValue source, int n) throws X;
}

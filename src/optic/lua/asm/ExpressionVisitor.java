package optic.lua.asm;

import optic.lua.asm.ExprNode.TableLiteral;

import java.util.*;

/**
 * A function that may be applied to an expression.
 *
 * @param <T> The type of object returned by this function
 * @param <X> The type of exception thrown by this function
 */
public interface ExpressionVisitor<T, X extends Throwable> {
	T visitNumberConstant(double n) throws X;

	T visitStringConstant(String s) throws X;

	T visitBooleanConstant(boolean b) throws X;

	T visitNilConstant() throws X;

	T visitTableConstructor(TableLiteral t) throws X;

	T visitFunctionLiteral(ExprNode.FunctionLiteral f) throws X;

	T visitRegister(Register register) throws X;

	T visitArrayRegister(ArrayRegister register) throws X;

	T visitLocalName(VariableInfo variable) throws X;

	T visitUpValueName(VariableInfo upvalue) throws X;

	T visitGlobalName(VariableInfo global) throws X;

	T visitInvocation(ExprNode.Invocation invocation) throws X;

	T visitVarargs() throws X;

	T visitNot(ExprNode value) throws X;

	T visitAnd(ExprNode first, ExprNode second) throws X;

	T visitOr(ExprNode first, ExprNode second) throws X;

	T visitSelectNth(ListNode source, int n) throws X;

	T visitExprList(List<ExprNode> nodes, Optional<ListNode> trailing) throws X;

	T acceptIntrinsic(String methodName, ExprList args) throws X;
}

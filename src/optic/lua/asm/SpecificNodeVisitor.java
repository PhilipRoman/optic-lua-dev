package optic.lua.asm;

import optic.lua.asm.ExprNode.*;
import optic.lua.asm.ListNode.Invocation;

import java.util.*;

public abstract class SpecificNodeVisitor<T, X extends Throwable> implements StatementVisitor<T, X>, ExpressionVisitor<T, X> {
	@Override
	public T visitNumberConstant(double n) throws X {
		return null;
	}

	@Override
	public T visitStringConstant(String s) throws X {
		return null;
	}

	@Override
	public T visitBooleanConstant(boolean b) throws X {
		return null;
	}

	@Override
	public T visitNilConstant() throws X {
		return null;
	}

	@Override
	public T visitTableConstructor(TableLiteral t) throws X {
		return null;
	}

	@Override
	public T visitFunctionLiteral(FunctionLiteral f) throws X {
		return null;
	}

	@Override
	public T visitRegister(Register register) throws X {
		return null;
	}

	@Override
	public T visitArrayRegister(ArrayRegister register) throws X {
		return null;
	}

	@Override
	public T visitLocalName(VariableInfo variable) throws X {
		return null;
	}

	@Override
	public T visitUpValueName(VariableInfo upvalue) throws X {
		return null;
	}

	@Override
	public T visitGlobalName(VariableInfo global) throws X {
		return null;
	}

	@Override
	public T visitInvocation(Invocation invocation) throws X {
		return null;
	}

	@Override
	public T visitVarargs() throws X {
		return null;
	}

	@Override
	public T visitNot(ExprNode value) throws X {
		return null;
	}

	@Override
	public T visitAnd(ExprNode first, ExprNode second) throws X {
		return null;
	}

	@Override
	public T visitOr(ExprNode first, ExprNode second) throws X {
		return null;
	}

	@Override
	public T visitSelectNth(ListNode source, int n) throws X {
		return null;
	}

	@Override
	public T visitExprList(List<ExprNode> nodes, Optional<ListNode> trailing) throws X {
		return null;
	}

	@Override
	public T acceptIntrinsic(String methodName, ExprList args) throws X {
		return null;
	}

	@Override
	public T visitAssignment(Register register, ExprNode value) throws X {
		return null;
	}

	@Override
	public T visitArrayAssignment(ArrayRegister register, ListNode value) throws X {
		return null;
	}

	@Override
	public T visitBlock(AsmBlock block) throws X {
		return null;
	}

	@Override
	public T visitBreakIf(ExprNode condition, boolean isTrue) throws X {
		return null;
	}

	@Override
	public T visitComment(String comment) throws X {
		return null;
	}

	@Override
	public T visitDeclaration(VariableInfo variable) throws X {
		return null;
	}

	@Override
	public T visitForEachLoop(List<VariableInfo> variables, ExprNode iterator, AsmBlock body) throws X {
		return null;
	}

	@Override
	public T visitForRangeLoop(VariableInfo counter, ExprNode from, ExprNode to, ExprNode step, AsmBlock body) throws X {
		return null;
	}

	@Override
	public T visitIfElseChain(LinkedHashMap<FlatExpr, AsmBlock> clauses) throws X {
		return null;
	}

	@Override
	public T visitLineNumber(int number) throws X {
		return null;
	}

	@Override
	public T visitLoop(AsmBlock body) throws X {
		return null;
	}

	@Override
	public T visitReturn(ExprList values) throws X {
		return null;
	}

	@Override
	public T visitVoid(ListNode invocation) throws X {
		return null;
	}

	@Override
	public T visitWrite(VariableInfo target, ExprNode value) throws X {
		return null;
	}
}

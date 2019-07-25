package optic.lua.asm;

import org.slf4j.*;

import java.util.*;

/**
 * A node which produces zero values (a statement).
 * Obtain instances of this interface using factory methods.
 */
public interface VoidNode extends Node {
	Logger log = LoggerFactory.getLogger(VoidNode.class);

	static VoidNode tableWrite(LValue.TableField target, ExprNode value) {
		return discard(ListNode.invocation(target.table(), InvocationMethod.SET_INDEX, ExprList.exprList(target.key(), value)));
	}

	static VoidNode declareLocal(VariableInfo info) {
		return new Declare(info);
	}

	static VoidNode forRange(VariableInfo counter, ExprNode from, ExprNode to, AsmBlock block) {
		return new ForRangeLoop(counter, from, to, ExprNode.number(1), block);
	}

	static VoidNode forRange(VariableInfo counter, ExprNode from, ExprNode to, ExprNode step, AsmBlock block) {
		return new ForRangeLoop(counter, from, to, step, block);
	}

	static VoidNode doBlock(AsmBlock block) {
		return new Block(block);
	}

	static VoidNode returnFromFunction(ExprList values) {
		return new Return(values);
	}

	static VoidNode assign(Register result, ExprNode value) {
		return new Assign(result, value);
	}

	static VoidNode assignArray(ArrayRegister result, ListNode value) {
		return new AssignArray(result, value);
	}

	static VoidNode write(VariableInfo target, ExprNode value) {
		return new Write(target, value);
	}

	static VoidNode discard(ListNode invocation) {
		return new Void(invocation);
	}

	static VoidNode ifThenChain(LinkedHashMap<FlatExpr, AsmBlock> clauses) {
		return new IfElseChain(clauses);
	}

	static VoidNode breakIf(ExprNode expression, boolean isTrue) {
		return new BreakIf(expression, isTrue);
	}

	static VoidNode loop(AsmBlock body) {
		return new Loop(body);
	}

	static VoidNode forInLoop(List<VariableInfo> variables, ExprNode iterator, AsmBlock body) {
		return new ForEachLoop(variables, iterator, body);
	}

	static VoidNode lineNumber(int number) {
		return new LineNumber(number);
	}

	<T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X;

	final class Assign implements VoidNode {
		private final Register result;
		private final ExprNode value;

		private Assign(Register result, ExprNode value) {
			this.result = result;
			this.value = value;
			if (!value.typeInfo().subtypeOf(result.typeInfo())) {
				throw new IllegalArgumentException(value + " not assignable to " + result);
			}
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitAssignment(result, value);
		}
	}

	final class Block implements VoidNode {
		private final AsmBlock steps;

		private Block(AsmBlock steps) {
			this.steps = steps;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitBlock(steps);
		}
	}

	final class BreakIf implements VoidNode {
		private final ExprNode condition;
		private final boolean isTrue;

		private BreakIf(ExprNode condition, boolean isTrue) {
			this.condition = condition;
			this.isTrue = isTrue;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitBreakIf(condition, isTrue);
		}
	}

	final class Comment implements VoidNode {
		private final String text;

		Comment(String text) {
			this.text = text;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitComment(text);
		}
	}

	final class Declare implements VoidNode {
		private final VariableInfo variable;

		Declare(VariableInfo variable) {
			this.variable = variable;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitDeclaration(variable);
		}
	}

	final class ForEachLoop implements VoidNode {
		private final List<VariableInfo> variables;
		private final ExprNode iterator;
		private final AsmBlock body;

		private ForEachLoop(List<VariableInfo> variables, ExprNode iterator, AsmBlock body) {
			this.variables = variables;
			this.iterator = iterator;
			this.body = body;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitForEachLoop(variables, iterator, body);
		}
	}

	final class ForRangeLoop implements VoidNode {
		private final VariableInfo counter;
		private final ExprNode from;
		private final ExprNode to;
		private final ExprNode step;
		private final AsmBlock block;

		private ForRangeLoop(VariableInfo counter, ExprNode from, ExprNode to, ExprNode step, AsmBlock block) {
			this.counter = counter;
			this.from = from;
			this.to = to;
			this.step = step;
			this.block = block;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitForRangeLoop(counter, from, to, step, block);
		}
	}

	final class IfElseChain implements VoidNode {
		private final LinkedHashMap<FlatExpr, AsmBlock> clauses;

		private IfElseChain(LinkedHashMap<FlatExpr, AsmBlock> clauses) {
			this.clauses = clauses;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitIfElseChain(clauses);
		}
	}

	final class Loop implements VoidNode {
		private final AsmBlock body;

		private Loop(AsmBlock body) {
			this.body = body;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitLoop(body);
		}
	}

	final class Return implements VoidNode {
		private final ExprList values;

		private Return(ExprList values) {
			this.values = values;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitReturn(values);
		}
	}

	final class Void implements VoidNode {
		private final ListNode value;

		private Void(ListNode value) {
			this.value = value;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitVoid(value);
		}
	}

	final class Write implements VoidNode {
		final ExprNode source;
		private final VariableInfo target;

		private Write(VariableInfo target, ExprNode source) {
			this.target = target;
			this.source = source;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitWrite(target, source);
		}
	}

	final class LineNumber implements VoidNode {
		private final int number;

		private LineNumber(int number) {
			if (number <= 0)
				throw new IllegalArgumentException("Line number must be greater than 0");
			this.number = number;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitLineNumber(number);
		}
	}

	final class AssignArray implements VoidNode {
		private final ArrayRegister result;
		private final ListNode value;

		private AssignArray(ArrayRegister result, ListNode value) {
			this.result = result;
			this.value = value;
		}

		@Override
		public <T, X extends Throwable> T accept(StatementVisitor<T, X> visitor) throws X {
			return visitor.visitArrayAssignment(result, value);
		}
	}
}

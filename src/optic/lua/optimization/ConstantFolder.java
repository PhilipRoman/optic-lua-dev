package optic.lua.optimization;

import optic.lua.asm.*;
import optic.lua.asm.ExprNode.*;
import optic.lua.asm.ListNode.*;
import org.jetbrains.annotations.Contract;
import org.slf4j.*;

import java.util.*;

public final class ConstantFolder implements ExpressionVisitor<ListNode, RuntimeException>, StatementVisitor<VoidNode, RuntimeException> {
	private static final Logger log = LoggerFactory.getLogger(ConstantFolder.class);

	public ConstantFolder() {
	}

	@Contract(pure = true)
	private static boolean alwaysTrue(ExprNode node) {
		if (node == ExprNode.bool(true))
			return true;
		StaticType type = node.typeInfo();
		return type != StaticType.OBJECT && type != StaticType.BOOLEAN;
	}

	@Contract(pure = true)
	private static boolean alwaysFalse(ExprNode node) {
		return node == ExprNode.bool(false) || node == ExprNode.nil();
	}

	public AsmBlock fold(AsmBlock asm) {
		return new AsmBlock(visitAll(asm.steps()), asm.locals());
	}

	@Override
	public ExprNode visitNumberConstant(double n) throws RuntimeException {
		return ExprNode.number(n);
	}

	@Override
	public ExprNode visitStringConstant(String s) throws RuntimeException {
		return ExprNode.string(s);
	}

	@Override
	public ExprNode visitBooleanConstant(boolean b) throws RuntimeException {
		return ExprNode.bool(b);
	}

	@Override
	public ExprNode visitNilConstant() throws RuntimeException {
		return ExprNode.nil();
	}

	@Override
	public ExprNode visitTableConstructor(TableLiteral t) throws RuntimeException {
		return t;
	}

	@Override
	public ExprNode visitFunctionLiteral(FunctionLiteral f) throws RuntimeException {
		return f;
	}

	@Override
	public ExprNode visitRegister(Register register) throws RuntimeException {
		return register;
	}

	@Override
	public ListNode visitArrayRegister(ArrayRegister register) throws RuntimeException {
		return register;
	}

	@Override
	public ExprNode visitLocalName(VariableInfo variable) throws RuntimeException {
		return ExprNode.variableName(variable);
	}

	@Override
	public ExprNode visitUpValueName(VariableInfo upvalue) throws RuntimeException {
		return ExprNode.variableName(upvalue);
	}

	@Override
	public ExprNode visitGlobalName(VariableInfo global) throws RuntimeException {
		return ExprNode.variableName(global);
	}

	@Override
	public ListNode visitInvocation(Invocation invocation) throws RuntimeException {
		if (invocation instanceof MonoInvocation) {
			return ExprNode.monoInvocation(
					(ExprNode) invocation.getObject().accept(this),
					invocation.getMethod(),
					invocation.getArguments().accept(this)
			);
		}
		return ListNode.invocation(
				(ExprNode) invocation.getObject().accept(this),
				invocation.getMethod(),
				invocation.getArguments().accept(this)
		);
	}

	@Override
	public ListNode visitVarargs() throws RuntimeException {
		return null;
	}

	@Override
	public ExprNode visitNot(ExprNode value) throws RuntimeException {
		if (alwaysTrue(value)) {
			log.warn("Expression is always false: \"{}\"", ExprNode.logicalNot(value));
			return ExprNode.bool(false);
		}
		if (alwaysFalse(value)) {
			log.warn("Expression is always true: \"{}\"", ExprNode.logicalNot(value));
			return ExprNode.bool(true);
		}
		return ExprNode.logicalNot(value);
	}

	@Override
	public ExprNode visitAnd(ExprNode first, ExprNode second) throws RuntimeException {
		if (alwaysTrue(first)) {
			log.warn("\"{}\" can be simplified to \"{}\"", ExprNode.logicalAnd(first, second), second);
			return second;
		}
		if (alwaysFalse(first)) {
			log.warn("\"{}\" can be simplified to \"{}\"", ExprNode.logicalAnd(first, second), first);
			return first;
		}
		return ExprNode.logicalAnd(first, second);
	}

	@Override
	public ExprNode visitOr(ExprNode first, ExprNode second) throws RuntimeException {
		if (alwaysTrue(first)) {
			log.warn("\"{}\" can be simplified to \"{}\"", ExprNode.logicalOr(first, second), first);
			return first;
		}
		if (alwaysFalse(first)) {
			log.warn("\"{}\" can be simplified to \"{}\"", ExprNode.logicalOr(first, second), second);
			return second;
		}
		return ExprNode.logicalOr(first, second);
	}

	@Override
	public ExprNode visitSelectNth(ListNode source, int n) throws RuntimeException {
		if (source instanceof ExprList) {
			ExprList el = (ExprList) source;
			if (n < el.getLeading().size()) {
				log.warn("\"{}\" can be simplified to \"{}\"",
						ExprNode.selectNth(source, n),
						el.getLeading(n));
				return el.getLeading(n);
			}
			if (el.getTrailing().isPresent()) {
				int offset = n - el.getLeading().size();
				log.warn("\"{}\" can be simplified to \"{}\"",
						ExprNode.selectNth(source, n),
						ExprNode.selectNth(el.getTrailing().get(), offset));
				return ExprNode.selectNth(el.getTrailing().get(), offset);
			}
			log.warn("\"{}\" can be simplified to \"{}\"",
					ExprNode.selectNth(source, n),
					ExprNode.nil());
			return ExprNode.nil();
		}
		return ExprNode.selectNth(source, n);
	}

	@Override
	public ListNode visitExprList(List<ExprNode> nodes, Optional<ListNode> trailing) throws RuntimeException {
		List<ListNode> copy = new ArrayList<>(nodes.size() + 1);
		for (ExprNode node : nodes) {
			copy.add(node.accept(this));
		}
		trailing.ifPresent(node -> copy.add(node.accept(this)));
		return ListNode.exprList(copy);
	}
	// ##########################################################################################
	// Statement visitor implementation

	// ##########################################################################################

	@Override
	public VoidNode visitAssignment(Register register, ExprNode value) throws RuntimeException {
		return VoidNode.assign(register, (ExprNode) value.accept(this));
	}

	@Override
	public VoidNode visitArrayAssignment(ArrayRegister register, ListNode value) throws RuntimeException {
		return VoidNode.assignArray(register, value.accept(this));
	}

	@Override
	public VoidNode visitBlock(AsmBlock block) throws RuntimeException {
		return VoidNode.doBlock(fold(block));
	}

	@Override
	public VoidNode visitBreakIf(ExprNode condition, boolean isTrue) throws RuntimeException {
		if (alwaysFalse(condition)) {
			log.warn("Condition is always false: \"{}\"", condition);
		}
		if (alwaysTrue(condition)) {
			log.warn("Condition is always true: \"{}\"", condition);
		}
		return VoidNode.breakIf((ExprNode) condition.accept(this), isTrue);
	}

	@Override
	public VoidNode visitComment(String comment) throws RuntimeException {
		throw new UnsupportedOperationException();
	}

	@Override
	public VoidNode visitDeclaration(VariableInfo variable) throws RuntimeException {
		return VoidNode.declareLocal(variable);
	}

	@Override
	public VoidNode visitForEachLoop(List<VariableInfo> variables, ExprNode iterator, AsmBlock body) throws RuntimeException {
		return VoidNode.forInLoop(variables, (ExprNode) iterator.accept(this), fold(body));
	}

	@Override
	public VoidNode visitForRangeLoop(VariableInfo counter, ExprNode from, ExprNode to, ExprNode step, AsmBlock body) throws RuntimeException {
		return VoidNode.forRange(counter, (ExprNode) from.accept(this), (ExprNode) to.accept(this), (ExprNode) step.accept(this), fold(body));
	}

	@Override
	public VoidNode visitIfElseChain(LinkedHashMap<FlatExpr, AsmBlock> clauses) throws RuntimeException {
		var copy = new LinkedHashMap<FlatExpr, AsmBlock>(clauses.size());
		clauses.forEach((k, v) -> {
			if (alwaysFalse((ExprNode) k.value())) {
				log.warn("Condition is always false: \"{}\"", k.value());
			}
			copy.put(new FlatExpr(k.block(), k.value().accept(this)), fold(v));
		});
		return VoidNode.ifThenChain(copy);
	}

	@Override
	public VoidNode visitLineNumber(int number) throws RuntimeException {
		return VoidNode.lineNumber(number);
	}

	@Override
	public VoidNode visitLoop(AsmBlock body) throws RuntimeException {
		return VoidNode.loop(fold(body));
	}

	@Override
	public VoidNode visitReturn(ListNode values) throws RuntimeException {
		return VoidNode.returnFromFunction(values.accept(this));
	}

	@Override
	public VoidNode visitVoid(ListNode invocation) throws RuntimeException {
		return VoidNode.discard(invocation.accept(this));
	}

	@Override
	public VoidNode visitWrite(VariableInfo target, ExprNode value) throws RuntimeException {
		return VoidNode.write(target, (ExprNode) value.accept(this));
	}
}

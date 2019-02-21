package optic.lua.asm

class Assign(private val result: Register, private val value: RValue) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitAssignment(result, value)
}

class Block(private val steps: AsmBlock) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitBlock(steps)
}

class Comment(private val text: String) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitComment(text)
}

class Declare(private val variable: VariableInfo) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitDeclaration(variable)
}

class ForRangeLoop(
        private val counter: VariableInfo,
        private val from: RValue,
        private val to: RValue,
        private val block: AsmBlock) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitForRangeLoop(counter, from, to, block)
}

class GetVarargs(private val to: Register) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitGetVarargs(to)
}

class IfElseChain(private val clauses: LinkedHashMap<FlatExpr, AsmBlock>) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitIfElseChain(clauses)
}

class Return(private val values: List<RValue>) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitReturn(values)
}

class Select(val out: Register, private val varargs: RValue, val n: Int) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitSelect(out, n, varargs)
}

class Void(private val invocation: RValue.Invocation) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitVoid(invocation)
}

class Write(private val target: VariableInfo, val source: RValue) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitWrite(target, source)
}
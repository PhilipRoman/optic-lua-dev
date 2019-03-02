package optic.lua.asm

import org.jetbrains.annotations.NotNull

class Assign(private val result: Register, private val value: RValue) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitAssignment(result, value)
}

class Block(private val steps: AsmBlock) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitBlock(steps)
}

class BreakIf(private val condition: @NotNull RValue, private val isTrue: Boolean) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitBreakIf(condition, isTrue)
}

class Comment(private val text: String) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitComment(text)
}

class Declare(private val variable: VariableInfo) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitDeclaration(variable)
}

class ForEachLoop(private val variables: List<VariableInfo>, private val iterator: RValue, private val body: AsmBlock) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitForEachLoop(variables, iterator, body)
}

class ForRangeLoop(
        private val counter: VariableInfo,
        private val from: RValue,
        private val to: RValue,
        private val step: RValue,
        private val block: AsmBlock) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitForRangeLoop(counter, from, to, step, block)
}

class IfElseChain(private val clauses: LinkedHashMap<FlatExpr, AsmBlock>) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitIfElseChain(clauses)
}

class Loop(private val body: AsmBlock) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitLoop(body)
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
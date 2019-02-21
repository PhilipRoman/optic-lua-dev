package optic.lua.asm.instructions

import optic.lua.asm.*
import java.util.function.Consumer

class IfElseChain(val clauses: LinkedHashMap<FlatExpr, AsmBlock>) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitIfElseChain(clauses)

    override fun toString(): String {
        return "if ..."
    }

    override fun children(): List<Step> {
        return clauses.values.flatMap { it.steps() } + clauses.keys.flatMap { it.block() }
    }

    override fun forEachObserved(action: Consumer<RValue>) {
        clauses.keys.map { condition -> condition.value() }.forEach(action)
    }
}

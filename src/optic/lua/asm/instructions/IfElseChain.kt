package optic.lua.asm.instructions

import optic.lua.asm.*
import java.util.function.Consumer

class IfElseChain(val clauses: Map<FlatExpr, AsmBlock>) : Step {
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

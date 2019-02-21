package optic.lua.asm.instructions

import optic.lua.asm.RValue
import optic.lua.asm.Step
import optic.lua.asm.StepVisitor
import java.util.function.Consumer

class Return(val values: List<RValue>) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitReturn(values)

    override fun toString(): String {
        return "return $values"
    }

    override fun forEachObserved(action: Consumer<RValue>) = values.forEach(action)
}

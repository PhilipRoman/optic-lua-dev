package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step
import optic.lua.asm.StepVisitor

class GetVarargs(val to: Register) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitGetVarargs(to)

    override fun toString(): String {
        return "varargs $to = ..."
    }

    override fun modified(): Register? = to
}

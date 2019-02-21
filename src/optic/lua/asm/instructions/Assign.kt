package optic.lua.asm.instructions

import optic.lua.asm.RValue
import optic.lua.asm.Register
import optic.lua.asm.Step
import optic.lua.asm.StepVisitor

class Assign(public val result: Register, public val value: RValue) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitAssignment(result, value)

    override fun modified(): Register? = result
}

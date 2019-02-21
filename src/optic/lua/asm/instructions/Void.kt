package optic.lua.asm.instructions

import optic.lua.asm.RValue
import optic.lua.asm.Step
import optic.lua.asm.StepVisitor

class Void(val invocation: RValue.Invocation) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitVoid(invocation)
}
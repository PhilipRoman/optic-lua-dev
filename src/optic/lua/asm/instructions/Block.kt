package optic.lua.asm.instructions

import optic.lua.asm.AsmBlock
import optic.lua.asm.Step
import optic.lua.asm.StepVisitor

class Block(val steps: AsmBlock) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitBlock(steps)

    override fun children(): List<Step> {
        return steps.steps()
    }

    override fun toString(): String {
        return "do"
    }
}

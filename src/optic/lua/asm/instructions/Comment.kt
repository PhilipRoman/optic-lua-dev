package optic.lua.asm.instructions

import optic.lua.asm.Step
import optic.lua.asm.StepVisitor

class Comment(val text: String) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitComment(text)

    override fun toString(): String {
        return "// $text"
    }
}

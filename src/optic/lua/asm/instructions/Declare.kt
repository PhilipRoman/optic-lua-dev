package optic.lua.asm.instructions

import optic.lua.asm.Step
import optic.lua.asm.StepVisitor
import optic.lua.asm.VariableInfo

class Declare(val variable: VariableInfo) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitDeclaration(variable)

    override fun toString(): String {
        val type = variable.mode.toString().toLowerCase()
        return "local-$type $variable"
    }
}

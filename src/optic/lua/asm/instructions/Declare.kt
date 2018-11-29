package optic.lua.asm.instructions

import optic.lua.asm.Step
import optic.lua.asm.VariableInfo

class Declare(val variable: VariableInfo) : Step {
    override fun toString(): String {
        val type = variable.mode.toString().toLowerCase()
        return "local-$type $variable"
    }

    fun getName(): String {
        return variable.name;
    }
}

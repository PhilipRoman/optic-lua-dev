package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step
import optic.lua.asm.VariableInfo

class Write(val target: VariableInfo, val source: Register) : Step {
    override fun toString(): String {
        val modeName = target.mode.toString().toLowerCase()
        return "set-$modeName $target = $source"
    }
}

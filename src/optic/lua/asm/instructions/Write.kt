package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step

class Write(val target: String, val source: Register, val mode: VariableMode) : Step {
    override fun toString(): String {
        return "set-${mode.toString().toLowerCase()} $target = $source"
    }
}

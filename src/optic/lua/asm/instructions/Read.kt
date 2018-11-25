package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step

class Read(val register: Register, val name: String, val mode: VariableMode) : Step {
    override fun toString(): String {
        return "get-${mode.toString().toLowerCase()} $register = $name"
    }
}

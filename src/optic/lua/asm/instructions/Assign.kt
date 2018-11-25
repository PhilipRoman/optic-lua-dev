package optic.lua.asm.instructions

import optic.lua.asm.LValue
import optic.lua.asm.Register
import optic.lua.asm.Step

class Assign(val target: LValue.Name, val source: Register) : Step {
    override fun toString(): String {
        return "assign $target = $source"
    }
}

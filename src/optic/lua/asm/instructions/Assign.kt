package optic.lua.asm.instructions

import optic.lua.asm.LValue
import optic.lua.asm.Register
import optic.lua.asm.Step

class Assign(val targets: List<LValue>, val sources: List<Register>) : Step {
    override fun toString(): String {
        return "assign $targets = $sources"
    }
}

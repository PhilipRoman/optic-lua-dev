package optic.lua.ssa.instructions

import optic.lua.ssa.LValue
import optic.lua.ssa.Register
import optic.lua.ssa.Step

class Assign(val targets: List<LValue>, val sources: List<Register>) : Step {
    override fun toString(): String {
        return "assign $targets = $sources"
    }
}

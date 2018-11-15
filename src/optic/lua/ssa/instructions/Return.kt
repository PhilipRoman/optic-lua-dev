package optic.lua.ssa.instructions

import optic.lua.ssa.Register
import optic.lua.ssa.Step

class Return(val registers: List<Register>) : Step {
    override fun toString(): String {
        return "return $registers"
    }
}

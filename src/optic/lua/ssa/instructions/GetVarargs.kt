package optic.lua.ssa.instructions

import optic.lua.ssa.Register
import optic.lua.ssa.Step

class GetVarargs(val to: Register) : Step {
    override fun toString(): String {
        return "varargs $to = ..."
    }
}

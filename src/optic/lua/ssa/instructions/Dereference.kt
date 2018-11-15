package optic.lua.ssa.instructions

import optic.lua.ssa.Register
import optic.lua.ssa.Step

class Dereference(val register: Register, val name: String) : Step {
    override fun toString(): String {
        return "lookup $register = $name"
    }
}

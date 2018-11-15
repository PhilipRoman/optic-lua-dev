package optic.lua.ssa.instructions

import optic.lua.ssa.Step

class Declare(val name: String) : Step {
    override fun toString(): String {
        return "local $name"
    }
}

package optic.lua.ssa.instructions

import optic.lua.ssa.Register
import optic.lua.ssa.Step

class Call(val function: Register, val args: List<Register>, val output: Register) : Step {
    override fun toString(): String {
        val resultUnused = output == Register.unused()
        return if (resultUnused)
            "call $function($args)"
        else
            "call $output = $function($args)"
    }
}

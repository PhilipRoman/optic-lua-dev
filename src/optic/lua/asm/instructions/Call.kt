package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.RegisterFactory
import optic.lua.asm.Step

class Call(val function: Register, val args: List<Register>, val output: Register) : Step {
    override fun toString(): String {
        val resultUnused = output == RegisterFactory.unused()
        return if (resultUnused)
            "call $function($args)"
        else
            "call $output = $function($args)"
    }
}

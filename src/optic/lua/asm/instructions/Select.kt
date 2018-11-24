package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step

class Select(val out: Register, val varargs: Register, val n: Int) : Step {
    override fun toString(): String {
        return "$out = select($varargs, $n)"
    }
}

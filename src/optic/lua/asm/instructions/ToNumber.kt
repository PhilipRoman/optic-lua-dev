package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step

class ToNumber(val source: Register, val target: Register) : Step {
    override fun toString(): String {
        return "$target = number($source)"
    }
}
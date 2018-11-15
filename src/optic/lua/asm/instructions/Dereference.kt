package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step

class Dereference(val register: Register, val name: String) : Step {
    override fun toString(): String {
        return "lookup $register = $name"
    }
}

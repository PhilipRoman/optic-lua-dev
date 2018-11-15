package optic.lua.asm.instructions

import optic.lua.asm.Step

class Declare(val name: String) : Step {
    override fun toString(): String {
        return "local $name"
    }
}

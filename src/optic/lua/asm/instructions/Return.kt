package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.Step

class Return(val registers: List<Register>) : Step {
    override fun toString(): String {
        return "return $registers"
    }
}

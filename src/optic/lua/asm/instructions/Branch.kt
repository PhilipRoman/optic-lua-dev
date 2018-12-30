package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.AsmBlock
import optic.lua.asm.Step

class Branch(val condition: Register, val body: AsmBlock) : Step {
    override fun toString(): String {
        return "if $condition"
    }

    override fun children(): List<Step> {
        return body.steps()
    }
}

package optic.lua.asm.instructions

import optic.lua.asm.AsmBlock
import optic.lua.asm.Step

class Block(val steps: AsmBlock) : Step {
    override fun children(): List<Step> {
        return steps.steps()
    }

    override fun toString(): String {
        return "do"
    }
}

package optic.lua.asm.instructions

import optic.lua.asm.AsmBlock
import optic.lua.asm.Step
import java.util.stream.Stream

class Block(val steps: AsmBlock) : Step {
    override fun children(): Stream<Step> {
        return steps.stream()
    }

    override fun toString(): String {
        return "do"
    }
}

package optic.lua.asm.instructions

import optic.lua.asm.Register
import optic.lua.asm.AsmBlock
import optic.lua.asm.Step
import java.util.stream.Stream

class ForRangeLoop(
        val varName: String,
        val from: Register,
        val to: Register,
        val block: AsmBlock) : Step {

    override fun toString(): String {
        return "for $varName = $from, $to"
    }

    override fun children(): Stream<Step> {
        return block.stream()
    }
}

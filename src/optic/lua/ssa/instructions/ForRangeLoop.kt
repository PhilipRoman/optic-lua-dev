package optic.lua.ssa.instructions

import optic.lua.ssa.Register
import optic.lua.ssa.Step
import java.util.stream.Stream

class ForRangeLoop(
        val varName: String,
        val from: Register,
        val to: Register,
        val block: List<Step>) : Step {

    override fun toString(): String {
        return "for $varName = $from, $to"
    }

    override fun children(): Stream<Step> {
        return block.stream()
    }
}

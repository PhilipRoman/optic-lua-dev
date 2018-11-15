package optic.lua.ssa.instructions

import optic.lua.ssa.Step
import java.util.stream.Stream

class Block(val steps: List<Step>) : Step {
    override fun children(): Stream<Step> {
        return steps.stream()
    }

    override fun toString(): String {
        return "do"
    }
}

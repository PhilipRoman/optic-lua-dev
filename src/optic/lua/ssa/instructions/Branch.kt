package optic.lua.ssa.instructions

import optic.lua.ssa.Register
import optic.lua.ssa.Step
import java.util.stream.Stream

class Branch(val condition: Register, val body: List<Step>) : Step {
    override fun toString(): String {
        return "if $condition"
    }

    override fun children(): Stream<Step> {
        return body.stream()
    }
}

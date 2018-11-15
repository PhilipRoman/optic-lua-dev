package optic.lua.ssa.instructions

import optic.lua.ssa.ParameterList
import optic.lua.ssa.Register
import optic.lua.ssa.Step
import java.util.stream.Stream

class FunctionLiteral(val body: List<Step>, val assignTo: Register, val params: ParameterList) : Step {
    override fun toString(): String {
        return "function $assignTo = function($params)"
    }

    override fun children(): Stream<Step> {
        return body.stream()
    }
}

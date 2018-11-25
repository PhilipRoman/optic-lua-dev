package optic.lua.asm.instructions

import optic.lua.asm.ParameterList
import optic.lua.asm.Register
import optic.lua.asm.AsmBlock
import optic.lua.asm.Step
import java.util.stream.Stream

class FunctionLiteral(val body: AsmBlock, val assignTo: Register, val params: ParameterList) : Step {
    override fun toString(): String {
        return "function $assignTo = function($params)"
    }

    override fun children(): Stream<Step> {
        return body.stream()
    }
}

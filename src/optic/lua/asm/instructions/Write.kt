package optic.lua.asm.instructions

import optic.lua.asm.RValue
import optic.lua.asm.Step
import optic.lua.asm.VariableInfo
import java.util.function.Consumer

class Write(val target: VariableInfo, val source: RValue) : Step {
    override fun toString(): String {
        val modeName = target.mode.toString().toLowerCase()
        return "set-$modeName $target = $source"
    }

    override fun forEachObserved(action: Consumer<RValue>) = action.accept(source)
}

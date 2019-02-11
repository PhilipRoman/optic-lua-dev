package optic.lua.asm.instructions

import optic.lua.asm.AsmBlock
import optic.lua.asm.RValue
import optic.lua.asm.Step
import optic.lua.asm.VariableInfo
import java.util.function.Consumer

class ForRangeLoop(
        val counter: VariableInfo,
        val from: RValue,
        val to: RValue,
        val block: AsmBlock) : Step {

    override fun toString(): String {
        return "for $counter = $from, $to"
    }

    override fun children(): List<Step> {
        return block.steps()
    }

    override fun forEachObserved(action: Consumer<RValue>) {
        action.accept(from)
        action.accept(to)
    }
}

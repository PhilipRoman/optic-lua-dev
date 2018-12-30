package optic.lua.asm.instructions

import optic.lua.asm.AsmBlock
import optic.lua.asm.Register
import optic.lua.asm.Step
import optic.lua.asm.VariableInfo
import java.util.function.Consumer

class ForRangeLoop(
        val counter: VariableInfo,
        val from: Register,
        val to: Register,
        val block: AsmBlock) : Step {

    override fun toString(): String {
        return "for $counter = $from, $to"
    }

    override fun children(): List<Step> {
        return block.steps()
    }

    override fun forEachObserved(action: Consumer<Register>) {
        action.accept(from)
        action.accept(to)
    }
}

package optic.lua.asm.instructions

import optic.lua.asm.RValue
import optic.lua.asm.Register
import optic.lua.asm.Step
import java.util.function.Consumer

@Deprecated("Use Invoke in future")
class Call(val function: Register, val args: List<Register>, val output: Register) : Step {
    override fun toString(): String {
        return if (output.isUnused())
            "call $function($args)"
        else
            "call $output = $function($args)"
    }

    override fun modified(): Register? {
        return when {
            output.isUnused() -> null
            else -> output
        }
    }

    override fun forEachObserved(action: Consumer<RValue>) {
        args.forEach(action)
        action.accept(function)
    }
}

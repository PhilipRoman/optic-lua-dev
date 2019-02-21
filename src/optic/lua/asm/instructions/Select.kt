package optic.lua.asm.instructions

import optic.lua.asm.RValue
import optic.lua.asm.Register
import optic.lua.asm.Step
import optic.lua.asm.StepVisitor
import java.util.function.Consumer

class Select(val out: Register, val varargs: RValue, val n: Int) : Step {
    override fun <T : Any, X : Throwable> accept(visitor: StepVisitor<T, X>): T = visitor.visitSelect(out, n, varargs)

    override fun toString(): String {
        return "$out = select($varargs, $n)"
    }

    override fun modified(): Register? = out

    override fun forEachObserved(action: Consumer<RValue>) = action.accept(varargs)
}

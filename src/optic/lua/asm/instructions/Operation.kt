package optic.lua.asm.instructions

import optic.lua.asm.RValue
import optic.lua.asm.Register
import optic.lua.asm.Step
import optic.lua.optimization.LuaOperator
import java.util.function.Consumer

@Deprecated("Use Invoke in future")
class Operation : Step {
    val a: Register?
    val b: Register
    val target: Register
    val operator: LuaOperator

    constructor(a: Register, b: Register, target: Register, operator: LuaOperator) {
        this.a = a
        this.b = b
        this.target = target
        this.operator = operator
    }

    constructor(b: Register, target: Register, operator: LuaOperator) {
        this.a = null
        this.b = b
        this.target = target
        this.operator = operator
    }

    override fun toString(): String {
        return "op $target = ${a ?: ""} $operator $b"
    }

    override fun modified(): Register? = target

    override fun forEachObserved(action: Consumer<RValue>) {
        if (a != null) {
            action.accept(a)
        }
        action.accept(b)
    }
}

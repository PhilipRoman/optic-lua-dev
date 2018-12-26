package optic.lua.asm

import optic.lua.optimization.CombinedTypeStatus
import optic.lua.optimization.TypeStatus
import optic.lua.util.UniqueNames
import java.util.function.Supplier

/**
 * Register is the basic unit of ASM form. Each assignment targets a new, unique register.
 * Some registers have *vararg capabilities*, which means that they may store a list
 * of values rather than a single value. There also exists the *unused register* which
 * can be used if the value of an expression is not needed (like when calling `print("hello")`).
 *
 * Use [RegisterFactory] to obtain instances of this class.
 */
class Register constructor(val name: String, val isVararg: Boolean) {
    constructor(isVararg: Boolean) : this(UniqueNames.next(), isVararg)

    private val statusDependencies: CombinedTypeStatus = CombinedTypeStatus()
    override fun toString(): String {
        return name + if (isVararg) "@" else ""
    }

    fun isUnused(): Boolean {
        return name == "_"
    }

    fun status(): TypeStatus {
        return statusDependencies.get()
    }

    fun updateStatus(type: TypeStatus) {
        statusDependencies.add(type)
    }

    fun addStatusDependency(type: Supplier<TypeStatus>) {
        statusDependencies.add(type)
    }

    fun toDebugString(): String {
        val varargSuffix = if (isVararg) "..." else ""
        return "register(\"$name$varargSuffix\" ${status()})"
    }
}

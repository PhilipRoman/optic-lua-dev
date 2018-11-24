package optic.lua.asm

import java.lang.Integer.toHexString
import java.util.concurrent.atomic.AtomicInteger

/**
 * Register is the basic unit of ASM form. Each assignment targets a new, unique register.
 * Some registers have *vararg capabilities*, which means that they may store a list
 * of values rather than a single value. There also exists the *unused register* which
 * can be used if the value of an expression is not needed (like when calling `print("hello")`).
 *
 * Use [RegisterFactory] to obtain instances of this class.
 */
class Register constructor(val name: String, val isVararg: Boolean) {

    constructor(isVararg: Boolean) : this("v" + randomSuffix(), isVararg)

    override fun toString(): String {
        return name + if (isVararg) "@" else ""
    }

    fun isUnused(): Boolean {
        return name == "_"
    }

    companion object {
        private val counter = AtomicInteger()

        private fun randomSuffix(): String {
            return toHexString(counter.getAndIncrement()).toUpperCase()
        }
    }
}

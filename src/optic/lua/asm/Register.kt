package optic.lua.asm

import optic.lua.optimization.CombinedCommonType
import optic.lua.optimization.ProvenType
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
class Register constructor(val name: String, val vararg: Boolean) : RValue {
    override fun <T : Any, X : Throwable> accept(visitor: RValueVisitor<T, X>): T = visitor.visitRegister(this)

    constructor(isVararg: Boolean) : this(UniqueNames.next(), isVararg)

    private val statusDependencies: CombinedCommonType = CombinedCommonType()
    override fun toString(): String {
        return name + if (vararg) "@" else ""
    }

    fun isUnused(): Boolean {
        return name == "_"
    }

    override fun isVararg(): Boolean = vararg;

    override fun typeInfo(): ProvenType {
        return statusDependencies.get()
    }

    fun updateStatus(provenType: ProvenType) {
        statusDependencies.add(provenType)
    }

    fun addTypeDependency(provenType: Supplier<ProvenType>) {
        statusDependencies.add(provenType)
    }

    fun toDebugString(): String {
        val varargSuffix = if (vararg) "..." else ""
        return "(${typeInfo()}$varargSuffix \"$name\")"
    }

    override fun discardRemaining(): FlatExpr {
        if (!vararg) {
            return FlatExpr(listOf(), this)
        }
        val first = RegisterFactory.create()
        first.updateStatus(ProvenType.OBJECT)
        return FlatExpr(listOf(StepFactory.select(first, this, 0)), first)
    }
}

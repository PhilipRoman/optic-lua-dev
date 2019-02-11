package optic.lua.asm

import optic.lua.asm.instructions.*
import optic.lua.messages.CompilationFailure
import org.jetbrains.annotations.Contract
import java.util.*

abstract class StepVisitor<R> {
    abstract fun defaultValue(x: Step): R

    @Throws(CompilationFailure::class)
    open fun visitBlock(x: Block): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitIfElseChain(x: IfElseChain): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitComment(x: Comment): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitDeclare(x: Declare): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitForRangeLoop(x: ForRangeLoop): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitGetVarargs(x: GetVarargs): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitRead(x: Read): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitReturn(x: Return): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitSelect(x: Select): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitToNumber(x: ToNumber): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitWrite(x: Write): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitInvoke(x: Invoke): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitAssign(x: Assign): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    fun visitAll(steps: List<Step>): List<R> {
        val list = ArrayList<R>(steps.size)
        for (step in steps) {
            list.add(visit(step))
        }
        return list
    }

    @Throws(CompilationFailure::class)
    fun visit(x: Step): R {
        return when (x) {
            is Block -> visitBlock(x)
            is IfElseChain -> visitIfElseChain(x)
            is Comment -> visitComment(x)
            is Declare -> visitDeclare(x)
            is ForRangeLoop -> visitForRangeLoop(x)
            is GetVarargs -> visitGetVarargs(x)
            is Read -> visitRead(x)
            is Return -> visitReturn(x)
            is Select -> visitSelect(x)
            is ToNumber -> visitToNumber(x)
            is Write -> visitWrite(x)
            is Invoke -> visitInvoke(x)
            is Assign -> visitAssign(x)
            else -> throw IllegalArgumentException(x.javaClass.name)
        }
    }

}

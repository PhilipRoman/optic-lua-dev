package optic.lua.util

import optic.lua.asm.Step
import optic.lua.asm.instructions.*
import optic.lua.messages.CompilationFailure
import java.util.*

abstract class StepVisitor<R> {
    abstract fun defaultValue(x: Step): R

    @Throws(CompilationFailure::class)
    open fun visitBlock(x: Block): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitBranch(x: Branch): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitCall(x: Call): R {
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
    open fun visitFunctionLiteral(x: FunctionLiteral): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitGetVarargs(x: GetVarargs): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitLoadConstant(x: LoadConstant): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitMakeTable(x: MakeTable): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitOperator(x: Operator): R {
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
    open fun visitTableRead(x: TableRead): R {
        return defaultValue(x)
    }

    @Throws(CompilationFailure::class)
    open fun visitTableWrite(x: TableWrite): R {
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
            is Branch -> visitBranch(x)
            is Call -> visitCall(x)
            is Comment -> visitComment(x)
            is Declare -> visitDeclare(x)
            is ForRangeLoop -> visitForRangeLoop(x)
            is FunctionLiteral -> visitFunctionLiteral(x)
            is GetVarargs -> visitGetVarargs(x)
            is LoadConstant -> visitLoadConstant(x)
            is MakeTable -> visitMakeTable(x)
            is Operator -> visitOperator(x)
            is Read -> visitRead(x)
            is Return -> visitReturn(x)
            is Select -> visitSelect(x)
            is TableRead -> visitTableRead(x)
            is TableWrite -> visitTableWrite(x)
            is ToNumber -> visitToNumber(x)
            is Write -> visitWrite(x)
            else -> throw IllegalArgumentException(x.javaClass.name)
        }
    }
}

package optic.lua.codegen

import java.io.PrintStream

class TemplateOutput(private val out: PrintStream) {
    private var indent = 0

    fun addIndent() {
        indent++
    }

    fun removeIndent() {
        if (indent <= 0) throw IllegalStateException()
        indent--
    }

    fun printLine() {
        out.println()
    }

    fun printLine(a: Any) {
        printIndent()
        out.println(a)
    }

    fun printLine(a: Any, b: Any) {
        printIndent()
        out.print(a)
        out.println(b)
    }

    fun printLine(a: Any, b: Any, c: Any) {
        printIndent()
        out.print(a)
        out.print(b)
        out.println(c)
    }

    fun printLine(a: Any, b: Any, c: Any, d: Any) {
        printIndent()
        out.print(a)
        out.print(b)
        out.print(c)
        out.println(d)
    }

    fun printLine(a: Any, b: Any, c: Any, d: Any, e: Any) {
        printIndent()
        out.print(a)
        out.print(b)
        out.print(c)
        out.print(d)
        out.println(e)
    }

    fun printLine(a: Any, b: Any, c: Any, d: Any, e: Any, f: Any) {
        printIndent()
        out.print(a)
        out.print(b)
        out.print(c)
        out.print(d)
        out.print(e)
        out.println(f)
    }

    fun printLine(a: Any, b: Any, c: Any, d: Any, e: Any, f: Any, g: Any) {
        printIndent()
        out.print(a)
        out.print(b)
        out.print(c)
        out.print(d)
        out.print(e)
        out.print(f)
        out.println(g)
    }

    fun printLine(a: Any, b: Any, c: Any, d: Any, e: Any, f: Any, g: Any, h: Any) {
        printIndent()
        out.print(a)
        out.print(b)
        out.print(c)
        out.print(d)
        out.print(e)
        out.print(f)
        out.print(g)
        out.println(h)
    }

    fun printLine(a: Any, b: Any, c: Any, d: Any, e: Any, f: Any, g: Any, h: Any, i: Any) {
        printIndent()
        out.print(a)
        out.print(b)
        out.print(c)
        out.print(d)
        out.print(e)
        out.print(f)
        out.print(g)
        out.print(h)
        out.println(i)
    }

    fun printLine(vararg args: Any) {
        printIndent()
        for (arg in args) {
            out.print(arg)
        }
        out.println()
    }

    private fun printIndent() {
        for (i in 0 until indent) {
            out.print('\t')
        }
    }
}

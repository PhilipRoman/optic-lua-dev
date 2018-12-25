package optic.lua.messages

import optic.lua.CodeSource

class Context(private val options: Set<Option>, private val reporter: MessageReporter) {
    fun options(): Set<Option> = options
    fun reporter(): MessageReporter = reporter

    fun withPhase(phase: Phase): Context = Context(options, reporter.withPhase(phase))
    fun withSource(source: CodeSource): Context = Context(options, reporter.withSource(source))
}
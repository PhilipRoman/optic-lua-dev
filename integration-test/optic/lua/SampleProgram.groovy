package optic.lua

import groovy.transform.CompileStatic
import optic.lua.codegen.java.JavaCodeOutput
import optic.lua.files.Compiler
import optic.lua.messages.MessageReporter
import optic.lua.messages.Options
import optic.lua.messages.StandardFlags
import optic.lua.messages.StandardMessageReporter
import optic.lua.runtime.LuaContext

import java.nio.file.Files

@CompileStatic
final class SampleProgram {
    private final CodeSource source
    private static final MessageReporter reporter = new StandardMessageReporter()

    SampleProgram(CodeSource source) {
        this.source = source
    }

    SampleProgram(String fileName) {
        this.source = CodeSource.ofFile(fileName)
    }

    List<String> run() {
        def options = new Options([
                (StandardFlags.VERIFY)    : true,
                (StandardFlags.LOOP_SPLIT): true
        ])
        def pipeline = new Pipeline(
                options, reporter, source
        )
        def temp = Files.createTempFile("optic_lua_", ".java")
        pipeline.registerPlugin(JavaCodeOutput.writingTo(Files.newOutputStream(temp)))
        pipeline.run()
//        Files.copy(temp, System.out)
        def context = LuaContext.create()
        def out = new StringWriter()
        context.out = new PrintWriter(out)
        new Compiler(reporter).run(Files.newInputStream(temp), 1, context, List.of())
        def scanner = new Scanner(out.getBuffer().toString())
        def list = new ArrayList<String>()
        while (scanner.hasNextLine()) {
            list.add(scanner.nextLine())
        }
        return list
    }
}

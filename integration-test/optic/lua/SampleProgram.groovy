package optic.lua

import groovy.transform.CompileStatic
import optic.lua.io.BundleCompiler
import optic.lua.io.Runner
import optic.lua.messages.*
import optic.lua.runtime.LuaContext

import java.nio.file.Paths

@CompileStatic
final class SampleProgram {
    private final String filePath
    private static final MessageReporter reporter = new StandardMessageReporter()

    SampleProgram(String filePath) {
        this.filePath = filePath
    }

    List<String> run() {
        def options = new Options([
                (StandardFlags.VERIFY)        : true,
                (StandardFlags.LOOP_SPLIT)    : true,
                (StandardFlags.SSA_SPLIT)     : true,
                (StandardFlags.DEBUG_COMMENTS): true,
        ])
        def bundle = new BundleCompiler(new Context(options, reporter)).compile([Paths.get(filePath)])
        def luaContext = LuaContext.create(bundle)
        def out = new StringWriter()
        luaContext.out = new PrintWriter(out)
        new Runner().run(bundle.findCompiled(filePath).get(), luaContext, [])
        def scanner = new Scanner(out.getBuffer().toString())
        def list = new ArrayList<String>()
        while (scanner.hasNextLine()) {
            list.add(scanner.nextLine())
        }
        return list
    }
}

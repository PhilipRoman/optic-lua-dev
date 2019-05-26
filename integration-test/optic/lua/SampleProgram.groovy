package optic.lua

import groovy.transform.CompileStatic
import optic.lua.io.BundleCompiler
import optic.lua.io.Runner
import optic.lua.messages.Options
import optic.lua.messages.StandardFlags
import optic.lua.runtime.LuaContext

import java.nio.file.Paths

/**
 * Helper class to run a single Lua file and capture the standard output.
 * Use {@link SampleProgram#run() run()} to execute the program and retrieve standard output.
 */
@CompileStatic
final class SampleProgram {
    private final String filePath

    /**
     * Creates a new program which uses the given file path as source.
     * @param filePath path to the Lua file to execute
     */
    SampleProgram(String filePath) {
        this.filePath = filePath
    }

    /**
     * Executes the referenced program and returns the output as a list of lines.
     * @return List of lines captured from standard output during program execution
     */
    List<String> run() {
        def options = new Options([
                (StandardFlags.VERIFY)        : true,
                (StandardFlags.LOOP_SPLIT)    : true,
                (StandardFlags.SSA_SPLIT)     : true,
                (StandardFlags.DEBUG_COMMENTS): true,
        ])
        def bundle = new BundleCompiler(options).compile([Paths.get(filePath)])
        def luaContext = LuaContext.create(bundle)
        def out = new StringWriter()
        luaContext.out = new PrintWriter(out)
        new Runner(options).run(bundle.findCompiled(filePath).get(), luaContext, [])

        def scanner = new Scanner(out.getBuffer().toString())
        def list = new ArrayList<String>()
        while (scanner.hasNextLine()) {
            list.add(scanner.nextLine())
        }
        return list
    }
}

package optic.lua

import groovy.transform.CompileStatic
import optic.lua.io.BundleCompiler
import optic.lua.io.Runner
import optic.lua.messages.Options
import optic.lua.messages.StandardFlags
import optic.lua.runtime.LuaContext
import optic.lua.runtime.invoke.InstrumentedCallSite

import java.nio.file.Paths

@CompileStatic
final class SampleProgram {
    private final String filePath

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
        def bundle = new BundleCompiler(options).compile([Paths.get(filePath)])
        def luaContext = LuaContext.create(bundle)
        def out = new StringWriter()
        luaContext.out = new PrintWriter(out)
        new Runner(options).run(bundle.findCompiled(filePath).get(), luaContext, [])

        // show information about call sites:
        int skippedSites = 0
        for (site in luaContext.getCallSites()) {
            def numberOfInvocations = (site as InstrumentedCallSite).history().values().sum()
            if (numberOfInvocations != null && (numberOfInvocations as int) > 1) {
                site.printTo(System.out)
                System.out.println()
            } else {
                skippedSites++
            }
        }
        System.out.println(skippedSites + " call sites skipped")
        def scanner = new Scanner(out.getBuffer().toString())
        def list = new ArrayList<String>()
        while (scanner.hasNextLine()) {
            list.add(scanner.nextLine())
        }
        return list
    }
}

package optic.lua

import groovy.transform.CompileStatic

@CompileStatic
class ChainedTablesTest extends GroovyTestCase {
    void testChainedTables() {
        def program = new SampleProgram("samples/chained-tables.lua")
        def result = program.run()
        assert result[0..1] == ["Foo!", "Hello!"]
    }
}

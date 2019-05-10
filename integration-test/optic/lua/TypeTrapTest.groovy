package optic.lua

import groovy.transform.CompileStatic

@CompileStatic
class TypeTrapTest extends GroovyTestCase {
    void testTypeTrap() {
        def program = new SampleProgram("samples/type-trap.lua")
        assert program.run() == ["1", "2", "3", "4", "5"]
    }
}

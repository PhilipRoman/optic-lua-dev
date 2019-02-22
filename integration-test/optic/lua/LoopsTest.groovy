package optic.lua

import groovy.transform.CompileStatic

@CompileStatic
class LoopsTest extends GroovyTestCase {
    void testLoops() {
        def program = new SampleProgram("samples/loops.lua")
        assert program.run() == ["true", "true", "true", "true", "true"]
    }
}

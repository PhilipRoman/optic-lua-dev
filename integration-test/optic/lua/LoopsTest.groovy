package optic.lua

import groovy.transform.CompileStatic

@CompileStatic
class LoopsTest extends GroovyTestCase {
    void testLoops() {
        def program = new SampleProgram("samples/loops.lua")
        def result = program.run()
        assert result[0..6] == ["true", "true", "true", "true", "true", "true", "true"]
        assert result[7].toDouble() == 1d
        assert result[8].toDouble() == 1d
        assert result[9].toDouble() == 1d
    }
}

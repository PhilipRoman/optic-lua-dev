package optic.lua

import groovy.transform.CompileStatic

@CompileStatic
class SumLoopTest extends GroovyTestCase {
    void testSumLoop() {
        def program = new SampleProgram("samples/sum-loop.lua")
        def result = program.run()
        assert result[0].toDouble() == 2500500025000000d
    }
}

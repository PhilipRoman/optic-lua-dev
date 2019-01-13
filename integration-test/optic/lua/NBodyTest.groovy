package optic.lua

import groovy.transform.CompileStatic

@CompileStatic
class NBodyTest extends GroovyTestCase {
    void testNBody() {
        def program = new SampleProgram("samples/n-body.lua")
        def result = program.run()[1].toDouble()
        assertEquals(-0.169096566d, result, 1.0e-8d)
    }
}
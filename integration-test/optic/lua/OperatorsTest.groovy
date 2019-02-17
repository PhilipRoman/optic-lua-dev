package optic.lua

import groovy.transform.CompileStatic

@CompileStatic
class OperatorsTest extends GroovyTestCase {
    void testOperators() {
        def program = new SampleProgram("samples/operators.lua")
        def result = program.run()
        assert result == []
    }
}

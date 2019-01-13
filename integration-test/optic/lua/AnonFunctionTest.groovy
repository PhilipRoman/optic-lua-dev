package optic.lua

import groovy.transform.CompileStatic

import java.text.DecimalFormat

@CompileStatic
class AnonFunctionTest extends GroovyTestCase {
    void testAnonFunctions() {
        def program = new SampleProgram("samples/anon-functions.lua")
        def result = program.run()
        assert result[0].toInteger() == 163840
        def format = new DecimalFormat("#.000000")
        assert format.format(result[1].toDouble()) == "1181050.634327"
    }
}

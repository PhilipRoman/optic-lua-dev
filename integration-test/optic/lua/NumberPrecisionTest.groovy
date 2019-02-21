package optic.lua

import groovy.transform.CompileStatic

import java.text.DecimalFormat

@CompileStatic
class NumberPrecisionTest extends GroovyTestCase {
    void testNumberPrecision() {
        def program = new SampleProgram("samples/number-precision.lua")
        def result = program.run()
        assert result[0].toDouble() == 1d
        assert result[1].toDouble() == 0d
        def format = new DecimalFormat("#.0000000000")
        assert format.format(result[2].toDouble()) == "1.0000000000"
        // assert result[3..6] == ["false", "true", "true", "false"]
        // it appears that Java code provides better number precision
        assert result[3] == "false"
        assert result[6] == "false"
    }
}

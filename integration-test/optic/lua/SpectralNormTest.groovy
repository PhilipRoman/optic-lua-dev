package optic.lua

import groovy.transform.CompileStatic

import java.text.DecimalFormat

@CompileStatic
class SpectralNormTest extends GroovyTestCase {
    void testSpectralNorm() {
        def program = new SampleProgram("samples/spectral-norm.lua")
        def format = new DecimalFormat("#.000000000")
        def result = program.run().first().toDouble()
        assert format.format(result) == "1.274224116"
    }
}

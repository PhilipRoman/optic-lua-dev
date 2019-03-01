package optic.lua

import groovy.transform.CompileStatic

@CompileStatic
class StandardLibraryTest extends GroovyTestCase {
    void testStandardLibrary() {
        def program = new SampleProgram("samples/standard-library.lua")
        assert program.run() == ["1", "2", "3", "4", "5", "6"]
    }
}

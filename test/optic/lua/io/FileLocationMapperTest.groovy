package optic.lua.io

import groovy.transform.CompileStatic

import java.nio.file.Paths

@CompileStatic
class FileLocationMapperTest extends GroovyTestCase {
    void testMap() {
        def m1 = new FileLocationMapper(
                Paths.get("/foo/src"),
                Paths.get("/foo/bin")
        )
        assert Paths.get("/foo/bin/x") == m1.map(Paths.get("/foo/src/x"))
        assert Paths.get("/foo/bin/x/y") == m1.map(Paths.get("/foo/src/x/y"))
    }
}

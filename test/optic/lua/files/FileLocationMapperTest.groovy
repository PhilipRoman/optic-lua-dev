package optic.lua.files

import java.nio.file.Paths

class FileLocationMapperTest extends GroovyTestCase {
    void testMap() {
        def m1 = new FileLocationMapper(
                Paths.get("/foo/src"),
                Paths.get("/foo/bin")
        )
        assertEquals(Paths.get("/foo/bin/x"), m1.map(Paths.get("/foo/src/x")))
        assertEquals(Paths.get("/foo/bin/x/y"), m1.map(Paths.get("/foo/src/x/y")))
    }
}

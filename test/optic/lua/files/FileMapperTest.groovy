package optic.lua.files

import java.nio.file.Path
import java.nio.file.Paths

class FileMapperTest extends GroovyTestCase {
    void testAllOf() {
        def mapper = FileMapper.allOf(
                new FileMapper() {
                    Path map(Path source) {
                        return source.getParent()
                    }
                },
                new FileMapper() {
                    Path map(Path source) {
                        return source.getParent()
                    }
                }
        )
        assertEquals(Paths.get("/foo"), mapper.map(Paths.get("/foo/bar/baz")))
    }
}

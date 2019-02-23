package optic.lua.io

import groovy.transform.CompileStatic

import java.nio.file.Path
import java.nio.file.Paths

@CompileStatic
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
        assert Paths.get("/foo") == mapper.map(Paths.get("/foo/bar/baz"))
    }
}

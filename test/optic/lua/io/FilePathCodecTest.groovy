package optic.lua.io

import groovy.transform.CompileStatic

import static optic.lua.io.FilePathCodec.decode
import static optic.lua.io.FilePathCodec.encode

@CompileStatic
class FilePathCodecTest extends GroovyTestCase {
    void testEncode() {
        final strings = [
                "optic-lua.lua",
                "test.lua",
                "path/ to/123",
                "1",
                "_foo_bar_123_",
                '$$$',
                "...",
                '!@#$%^&*()',
                "~`_-+={[}]:;<,>.?/",
                "☺☻♥♦♣♠♂♀♪♫☼►"
        ]
        for (s in strings) {
            def decoded = decode(encode(s))
            assert s == decoded
        }
    }
}

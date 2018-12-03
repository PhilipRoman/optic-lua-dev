package optic.lua.util

import groovy.transform.CompileStatic

@CompileStatic
class UniqueNamesTest extends GroovyTestCase {
    void testNext() {
        assert UniqueNames.next() != UniqueNames.next()
    }
}

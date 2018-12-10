package optic.lua.runtime

import groovy.transform.CompileStatic

@CompileStatic
class DynamicNumberTest extends GroovyTestCase {
    void test() {
        assert DynamicNumber.of(3.14d).value() == 3.14d
        assert DynamicNumber.of(0).value() == 0
        assert DynamicNumber.of(1).value() == 1
        assert DynamicNumber.of(64.5d).value() == 64.5d
        assert DynamicNumber.of(65.5d).value() == 65.5d
        assert DynamicNumber.of(12345).value() == 12345
    }
}

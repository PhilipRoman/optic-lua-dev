package optic.lua.runtime

import groovy.transform.CompileStatic

@CompileStatic
class StandardLibraryTest extends GroovyTestCase {
    void testTostring() {
        assert StandardLibrary.tostring(MultiValue.of(Dynamic.of(true))) == MultiValue.of(Dynamic.of("true"))
        assert StandardLibrary.tostring(MultiValue.of(Dynamic.of("abc"))) == MultiValue.of(Dynamic.of("abc"))
    }

    void testType() {
        assert StandardLibrary.type(MultiValue.of(Dynamic.of(true))) == MultiValue.of(Dynamic.of("bool"))
        assert StandardLibrary.type(MultiValue.of(DynamicNumber.of(3.14))) == MultiValue.of(Dynamic.of("number"))
        assert StandardLibrary.type(MultiValue.of(DynamicTable.ofArray([]))) == MultiValue.of(Dynamic.of("table"))
        assert StandardLibrary.type(MultiValue.of(new DynamicFunction() {
            MultiValue call(MultiValue args) {
                args
            }
        })) == MultiValue.of(Dynamic.of("function"))
        assert StandardLibrary.type(MultiValue.of(DynamicNil.nil())) == MultiValue.of(Dynamic.of("nil"))
        assert StandardLibrary.type(MultiValue.of(DynamicString.of("hello"))) == MultiValue.of(Dynamic.of("string"))

    }

    void testSelect() {
        final TRUE = Dynamic.of(true)
        final FALSE = Dynamic.of(false)
        assert StandardLibrary.select(MultiValue.of(Dynamic.of("#"), TRUE)) == MultiValue.of(Dynamic.of(1))
        assert StandardLibrary.select(MultiValue.of(Dynamic.of(2), TRUE, FALSE)) == MultiValue.of(FALSE)
        assert StandardLibrary.select(MultiValue.of(Dynamic.of(1), TRUE, FALSE)) == MultiValue.of(TRUE, FALSE)
    }

    void testTable_concat() {
        assert StandardLibrary.table_concat(MultiValue.of(DynamicTable.ofArray(["abc", " ", "def"]))) == MultiValue.of(Dynamic.of("abc def"))
        assert StandardLibrary.table_concat(MultiValue.of(DynamicTable.ofArray([]))) == MultiValue.of(Dynamic.of(""))
        assert StandardLibrary.table_concat(MultiValue.of(DynamicTable.ofMap([1: "string", 3: "foo"]))) == MultiValue.of(Dynamic.of("string"))
    }
}

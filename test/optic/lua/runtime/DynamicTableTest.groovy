package optic.lua.runtime

import groovy.transform.CompileStatic

@CompileStatic
class DynamicTableTest extends GroovyTestCase {
    void testObject() {
        DynamicTable table = DynamicTable.ofMap(["key1": "value1",
                                                 "key2": "value2",
                                                 "key3": "value3"])
        assert table.get("key1") == DynamicString.of("value1")
        assert table.get("key2") == DynamicString.of("value2")
        assert table.get("key3") == DynamicString.of("value3")
        assert table.get("key4") == DynamicNil.nil()
    }

    void testMixed() {
        DynamicTable table = DynamicTable.ofMap(["key1": "value1",
                                                 "key2": "value2",
                                                 "key3": "value3",
                                                 1     : "i1",
                                                 2     : "i2",
                                                 3     : "i3"])
        assert table.get("key1") == DynamicString.of("value1")
        assert table.get("key2") == DynamicString.of("value2")
        assert table.get("key3") == DynamicString.of("value3")
        assert table.get("key4") == DynamicNil.nil()
        assert table.get(1) == DynamicString.of("i1")
        assert table.get(2) == DynamicString.of("i2")
        assert table.get(3) == DynamicString.of("i3")
        assert table.get(4) == DynamicNil.nil()
        assert table.get(0) == DynamicNil.nil()
        assert table.get(-1) == DynamicNil.nil()
    }

    void testBigArray() {
        DynamicTable table = DynamicTable.ofArray([]);
        for (int i = -50; i < 50; i++) {
            table.set(DynamicNumber.of(i), DynamicString.of(String.valueOf(i)));
        }
        assert table.get(Dynamic.of(0)) == DynamicString.of("0")
        assert table.get(Dynamic.of(-50)) == DynamicString.of("-50")
        assert table.get(-50) == DynamicString.of("-50")
        assert table.get(50) == DynamicNil.nil()
    }

    void testLength() {
        DynamicTable table = DynamicTable.ofArray([]);
        for (int i = -50; i < 50; i++) {
            table.set(DynamicNumber.of(i), DynamicString.of(String.valueOf(i)));
        }
        assert table.length() == 49
        table.set(DynamicNumber.of(11), DynamicNil.nil())
        assert table.length() == 10
    }
}

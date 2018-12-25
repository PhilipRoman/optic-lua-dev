package optic.lua.util

class SetsTest extends GroovyTestCase {
    void testMerge() {
        assert Sets.merge(Set.of("a", "b"), Set.of("b", "c")) == Set.of("a", "b", "c")
        assert Sets.merge(Set.of(), Set.of()) == Set.of()
        assert Sets.merge(Set.of("a"), Set.of()) == Set.of("a")
    }
}

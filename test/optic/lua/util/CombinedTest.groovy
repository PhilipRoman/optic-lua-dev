package optic.lua.util

import groovy.transform.CompileStatic

import java.util.function.Supplier

@CompileStatic
class CombinedTest extends GroovyTestCase {
    static class CombinedSets extends Combined<Set<String>> {
        @Override
        protected Set<String> reduce(Set<String> a, Set<String> b) {
            return a + b
        }

        @Override
        protected boolean isAlreadyMax(Set<String> value) {
            return false
        }

        @Override
        protected Set<String> emptyValue() {
            return Set.of()
        }
    }

    void testGet() {
        def combined = new CombinedSets()
        combined.add(Set.of("a"))
        combined.add(Set.of("b"))
        Set<String> set = new HashSet<String>()
        set.add("c")
        combined.add(set)
        assert combined.get() == Set.of("a", "b", "c")
        set.add("d")
        assert combined.get() == Set.of("a", "b", "c", "d")
        Supplier<Set<String>> supplier = new Supplier<Set<String>>() {
            boolean state = false
            @Override
            Set<String> get() {
                return state ? Set.of("true") : Set.of("false")
            }
        }
        combined.add(supplier)
        assert combined.get() == Set.of("a", "b", "c", "d", "false")
        supplier.state = true
        assert combined.get() == Set.of("a", "b", "c", "d", "true")
    }

    static String max(Set<String> set) {
        return set.stream().max({x, y -> x <=> y}).orElseThrow()
    }
}

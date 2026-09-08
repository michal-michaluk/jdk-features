package dev.bottega.jdkfeatures.jdk25.jep506_scoped_values;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ScopedValuesDemoTest {

    @Test
    void describeMatchesConvention() {
        String d = ScopedValuesDemo.describe();
        assertTrue(d.startsWith("JEP 506 — "));
        assertTrue(d.contains("Scoped Values"));
        assertTrue(d.contains("(JDK 25)"));
    }

    @Test
    void runContainsResolvedAndFallbackResults() {
        String out = ScopedValuesDemo.run();
        assertTrue(out.contains("single:      alice"));
        assertTrue(out.contains("chained:     alice/42"));
        assertTrue(out.contains("nested:      alice->bob"));
        assertTrue(out.contains("fallback:    guest"));
        assertTrue(out.contains("boundInside: isBound=true"));
        assertTrue(out.contains("boundOutside:isBound=false"));
        assertTrue(out.contains("nestedTask:  nested sees: alice"));
    }

    @Test
    void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() -> ScopedValuesDemo.main(new String[0]));
    }

    @Test
    void singleBindResolves() {
        assertTrue(ScopedValuesDemo.readSingle().equals("alice"));
    }

    @Test
    void chainedBindResolvesBothValues() {
        assertTrue(ScopedValuesDemo.readChained().equals("alice/42"));
    }

    @Test
    void nestedRebindOverridesInnerScope() {
        assertTrue(ScopedValuesDemo.readNestedRebind().equals("alice->bob"));
    }

    @Test
    void fallbackUsedWhenUnbound() {
        assertTrue(ScopedValuesDemo.readFallback().equals("guest"));
    }

    @Test
    void isBoundReflectsScope() {
        assertTrue(ScopedValuesDemo.boundInside().equals("isBound=true"));
        assertTrue(ScopedValuesDemo.boundOutside().equals("isBound=false"));
    }

    @Test
    void nestedTaskSeesBoundValue() {
        assertTrue(ScopedValuesDemo.readFromNestedTask().equals("nested sees: alice"));
    }

    @Test
    void getResolvesValueInsideScope() {
        assertTrue(ScopedValue.where(ScopedValuesDemo.USER, "carol").call(() -> ScopedValuesDemo.USER.get())
                .equals("carol"));
    }

    @Test
    void orElseReturnsValueWhenBound() {
        assertTrue(ScopedValue.where(ScopedValuesDemo.USER, "dave").call(() -> ScopedValuesDemo.USER.orElse("guest"))
                .equals("dave"));
    }

    @Test
    void unboundGetThrows() {
        assertThrows(java.util.NoSuchElementException.class, () -> {
            ScopedValuesDemo.USER.get();
        });
    }

    @Test
    void valueIsUnboundOutsideAnyScope() {
        assertFalse(ScopedValuesDemo.USER.isBound());
        assertTrue(ScopedValuesDemo.USER.orElse("guest").equals("guest"));
    }

    @Test
    void plainNewThreadDoesNotInheritBinding() {
        // Documented JEP-506 behavior: without StructuredTaskScope (preview) a plain
        // child thread does NOT inherit the scoped-value binding.
        java.util.concurrent.atomic.AtomicReference<String> childSeen = new java.util.concurrent.atomic.AtomicReference<>();
        ScopedValue.where(ScopedValuesDemo.USER, "erin").run(() -> {
            Thread child = new Thread(() -> childSeen.set(ScopedValuesDemo.USER.orElse("UNBOUND")));
            child.start();
            try {
                child.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });
        assertTrue(childSeen.get().equals("UNBOUND"));
    }
}

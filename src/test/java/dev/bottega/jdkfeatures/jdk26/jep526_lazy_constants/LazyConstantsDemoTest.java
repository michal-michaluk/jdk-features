package dev.bottega.jdkfeatures.jdk26.jep526_lazy_constants;

import org.junit.jupiter.api.Test;

import java.lang.LazyConstant;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LazyConstantsDemoTest {

    @Test
    void computesSupplierOnlyOnce() {
        AtomicInteger calls = new AtomicInteger();
        LazyConstant<String> lazy = LazyConstant.of(() -> "v" + calls.incrementAndGet());
        assertEquals("v1", lazy.get());
        assertEquals("v1", lazy.get());
        assertEquals(1, calls.get(), "supplier must run exactly once");
        assertTrue(lazy.isInitialized());
    }

    @Test
    void orElseReturnsFallbackBeforeInitialization() {
        AtomicInteger calls = new AtomicInteger();
        LazyConstant<String> lazy = LazyConstant.of(() -> "x" + calls.incrementAndGet());
        assertFalse(lazy.isInitialized());
        assertEquals("fallback", lazy.orElse("fallback"));
    }

    @Test
    void runReportsLazyBehaviour() {
        String result = LazyConstantsDemo.run();
        assertTrue(result.contains("supplierCalls=1"), "value must be computed exactly once");
        assertTrue(result.contains("afterIsInitialized=true"));
    }

    @Test
    void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() -> LazyConstantsDemo.main(new String[0]));
    }
}

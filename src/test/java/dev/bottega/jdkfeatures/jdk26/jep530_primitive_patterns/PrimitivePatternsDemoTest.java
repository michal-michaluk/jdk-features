package dev.bottega.jdkfeatures.jdk26.jep530_primitive_patterns;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimitivePatternsDemoTest {

    @Test
    void matchesExactPrimitiveTypes() {
        assertEquals("int:5", PrimitivePatternsDemo.classify(5));
        assertEquals("long:7", PrimitivePatternsDemo.classify(7L));
        assertEquals("double:3.5", PrimitivePatternsDemo.classify(3.5));
    }

    @Test
    void fallsBackToDefaultAndHandlesNull() {
        assertEquals("other:String", PrimitivePatternsDemo.classify("str"));
        assertEquals("null", PrimitivePatternsDemo.classify(null));
    }

    @Test
    void runReportsAllBranches() {
        String result = PrimitivePatternsDemo.run();
        assertTrue(result.contains("int:5"));
        assertTrue(result.contains("long:7"));
        assertTrue(result.contains("other:String"));
        assertTrue(result.contains("null"));
    }

    @Test
    void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() -> PrimitivePatternsDemo.main(new String[0]));
    }
}

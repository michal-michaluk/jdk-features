package dev.bottega.jdkfeatures.jdk21.jep441_pattern_matching_switch;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PatternMatchingSwitchDemoTest {

    @Test
    void runContainsGuardAndNullEvidence() {
        String out = PatternMatchingSwitchDemo.run();
        assertTrue(out.contains("classify(short): short string: hi"));
        assertTrue(out.contains("classify(long): long string: hello"));
        assertTrue(out.contains("classify(42): non-negative int: 42"));
        assertTrue(out.contains("classify(-7): negative int: -7"));
        assertTrue(out.contains("classify(null): null"));
        assertTrue(out.contains("shapeType(circle): circle radius=5"));
        assertTrue(out.contains("shapeType(rect): rect 3x4"));
        assertTrue(out.contains("shapeArea(circle): 78.53981633974483"));
        assertTrue(out.contains("shapeArea(rect): 12.0"));
    }

    @Test
    void describeMatchesJep() {
        assertEquals("JEP 441 — Pattern Matching for switch (JDK 21)", PatternMatchingSwitchDemo.describe());
    }

    @Test
    void mainDoesNotThrow() {
        assertDoesNotThrow(() -> PatternMatchingSwitchDemo.main(new String[0]));
    }

    @Test
    void classifyUsesWhenGuard() {
        assertEquals("short string: hi", PatternMatchingSwitchDemo.classify("hi"));
        assertEquals("long string: hello", PatternMatchingSwitchDemo.classify("hello"));
    }

    @Test
    void classifyHandlesNull() {
        assertEquals("null", PatternMatchingSwitchDemo.classify(null));
    }

    @Test
    void classifyHandlesIntegerGuard() {
        assertEquals("negative int: -7", PatternMatchingSwitchDemo.classify(-7));
        assertEquals("non-negative int: 42", PatternMatchingSwitchDemo.classify(42));
    }

    @Test
    void classifyDefaultsToTypeName() {
        assertEquals("other: Double", PatternMatchingSwitchDemo.classify(3.14));
    }

    @Test
    void shapeAreaIsExhaustiveSwitch() {
        assertEquals(Math.PI * 25, PatternMatchingSwitchDemo.shapeArea(new PatternMatchingSwitchDemo.Circle(5)), 1e-9);
        assertEquals(12.0, PatternMatchingSwitchDemo.shapeArea(new PatternMatchingSwitchDemo.Rectangle(3, 4)), 1e-9);
    }

    @Test
    void shapeTypeMatchesSealedTypes() {
        assertEquals("circle radius=5", PatternMatchingSwitchDemo.shapeType(new PatternMatchingSwitchDemo.Circle(5)));
        assertEquals("rect 3x4", PatternMatchingSwitchDemo.shapeType(new PatternMatchingSwitchDemo.Rectangle(3, 4)));
    }
}

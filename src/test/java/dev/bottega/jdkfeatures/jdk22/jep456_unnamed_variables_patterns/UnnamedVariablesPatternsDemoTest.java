package dev.bottega.jdkfeatures.jdk22.jep456_unnamed_variables_patterns;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UnnamedVariablesPatternsDemoTest {

    @Test
    void describeMatchesJep() {
        assertEquals(
            "JEP 456 — Unnamed Variables & Patterns (JDK 22)",
            UnnamedVariablesPatternsDemo.describe());
    }

    @Test
    void runContainsEveryCase() {
        String out = UnnamedVariablesPatternsDemo.run();
        assertTrue(out.contains("Case 1"));
        assertTrue(out.contains("Case 2"));
        assertTrue(out.contains("Case 3"));
        assertTrue(out.contains("Case 4"));
        assertTrue(out.contains("Case 5"));
        assertTrue(out.contains("Case 6"));
    }

    @Test
    void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() -> UnnamedVariablesPatternsDemo.main(new String[] {}));
    }

    @Test
    void recordPatternWithUnnamedComponent() {
        String result = UnnamedVariablesPatternsDemo.recordPatternWithUnnamedComponent();
        assertTrue(result.contains("x=3"));
        assertTrue(result.contains("y ignored"));
    }

    @Test
    void instanceofPatternWithUnderscore() {
        assertTrue(UnnamedVariablesPatternsDemo.instanceofPatternWithUnderscore("hi").contains("is a String"));
        assertTrue(UnnamedVariablesPatternsDemo.instanceofPatternWithUnderscore(42).contains("not a String"));
    }

    @Test
    void lambdaWithUnnamedParams() {
        String result = UnnamedVariablesPatternsDemo.lambdaWithUnnamedParams(21, 2);
        assertTrue(result.contains("both params ignored"));
        assertTrue(result.contains("x=21"));
    }

    @Test
    void forLoopIgnoringElement() {
        assertTrue(UnnamedVariablesPatternsDemo.forLoopIgnoringElement().contains("4 elements"));
    }

    @Test
    void catchIgnoringException() {
        assertTrue(UnnamedVariablesPatternsDemo.catchIgnoringException().contains("caught RuntimeException"));
    }

    @Test
    void tryWithUnnamedResource() {
        String result = UnnamedVariablesPatternsDemo.tryWithUnnamedResource();
        assertTrue(result.contains("closed"));
        assertTrue(result.contains("true"));
    }
}

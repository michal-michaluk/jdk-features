package dev.bottega.jdkfeatures.jdk26.jep525_structured_concurrency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructuredConcurrencyDemoTest {

    @Test
    void joinsForkedTasksAndCombinesResults() throws Exception {
        String result = StructuredConcurrencyDemo.run();
        assertTrue(result.contains("result=hello world"));
        assertTrue(result.contains("success=true"));
    }

    @Test
    void runsSubtasksInScope() throws Exception {
        try (var scope = java.util.concurrent.StructuredTaskScope.open()) {
            var task = scope.fork(() -> 21 * 2);
            scope.join();
            assertEquals(42, task.get());
            assertEquals(java.util.concurrent.StructuredTaskScope.Subtask.State.SUCCESS, task.state());
        }
    }

    @Test
    void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() -> StructuredConcurrencyDemo.main(new String[0]));
    }
}

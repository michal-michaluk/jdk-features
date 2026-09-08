package dev.bottega.jdkfeatures.jdk24.jep485_stream_gatherers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for the JEP 485 Stream Gatherers demo. */
class StreamGatherersDemoTest {

    private static final List<Integer> NUMBERS = List.of(1, 2, 3, 4, 5, 6, 7, 8);

    @Test
    void describeMatchesConvention() {
        assertEquals("JEP 485 — Stream Gatherers (JDK 24)", StreamGatherersDemo.describe());
    }

    @Test
    void windowFixedChunksIntoSizeThree() {
        assertEquals(
                List.of(List.of(1, 2, 3), List.of(4, 5, 6), List.of(7, 8)),
                StreamGatherersDemo.windowFixed(3, NUMBERS));
    }

    @Test
    void windowFixedWithExactMultipleLeavesNoPartialWindow() {
        assertEquals(
                List.of(List.of(1, 2), List.of(3, 4)),
                StreamGatherersDemo.windowFixed(2, List.of(1, 2, 3, 4)));
    }

    @Test
    void windowFixedSizeOneMakesSingletons() {
        assertEquals(
                List.of(List.of(42), List.of(7)),
                StreamGatherersDemo.windowFixed(1, List.of(42, 7)));
    }

    @Test
    void windowSlidingProducesOverlappingWindows() {
        assertEquals(
                List.of(List.of(1, 2), List.of(2, 3), List.of(3, 4), List.of(4, 5),
                        List.of(5, 6), List.of(6, 7), List.of(7, 8)),
                StreamGatherersDemo.windowSliding(2, NUMBERS));
    }

    @Test
    void windowSlidingSizeEqualsLengthYieldsSingleWindow() {
        assertEquals(
                List.of(NUMBERS),
                StreamGatherersDemo.windowSliding(8, NUMBERS));
    }

    @Test
    void foldReducesToSingleValue() {
        assertEquals(36, StreamGatherersDemo.foldSum(NUMBERS));
    }

    @Test
    void scanProducesRunningTotal() {
        assertEquals(
                List.of(1, 3, 6, 10, 15, 21, 28, 36),
                StreamGatherersDemo.runningTotal(NUMBERS));
    }

    @Test
    void emptyStreamIsSafelyHandled() {
        assertEquals(List.of(), StreamGatherersDemo.windowFixed(3, List.of()));
        assertEquals(List.of(), StreamGatherersDemo.windowSliding(3, List.of()));
        assertEquals(0, StreamGatherersDemo.foldSum(List.of()));
        assertEquals(List.of(), StreamGatherersDemo.runningTotal(List.of()));
    }

    @Test
    void rangeProducesDeterministicNumbers() {
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), StreamGatherersDemo.range(1, 9));
    }

    @Test
    void runContainsExpectedSections() {
        String out = StreamGatherersDemo.run();
        assertTrue(out.contains("JEP 485 — Stream Gatherers (JDK 24)"));
        assertTrue(out.contains("numbers = [1, 2, 3, 4, 5, 6, 7, 8]"));
        assertTrue(out.contains("windowFixed(3) = [[1, 2, 3], [4, 5, 6], [7, 8]]"));
        assertTrue(out.contains(
                "windowSliding(2) = [[1, 2], [2, 3], [3, 4], [4, 5], [5, 6], [6, 7], [7, 8]]"));
        assertTrue(out.contains("fold(sum) = 36"));
        assertTrue(out.contains("runningTotal = [1, 3, 6, 10, 15, 21, 28, 36]"));
    }

    @Test
    void mainDoesNotThrow() {
        assertDoesNotThrow(() -> StreamGatherersDemo.main(new String[0]));
    }
}

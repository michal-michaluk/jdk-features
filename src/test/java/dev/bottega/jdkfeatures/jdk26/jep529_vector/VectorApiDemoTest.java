package dev.bottega.jdkfeatures.jdk26.jep529_vector;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VectorApiDemoTest {

    @Test
    void sumsBlockWithVectorReduction() {
        assertEquals(36, VectorApiDemo.sumBlock(new int[]{1, 2, 3, 4, 5, 6, 7, 8}));
        assertEquals(-4, VectorApiDemo.sumBlock(new int[]{10, 20, -12, 30, -40, 2, 1, -15}));
    }

    @Test
    void rejectsWrongLaneCount() {
        assertThrows(IllegalArgumentException.class, () -> VectorApiDemo.sumBlock(new int[]{1, 2, 3}));
    }

    @Test
    void runReportsSum() {
        assertTrue(VectorApiDemo.run().contains("=36"), "run() must report the reduced sum");
    }

    @Test
    void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() -> VectorApiDemo.main(new String[0]));
    }
}

package dev.bottega.jdkfeatures.jdk22.jep454_ffm;

import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForeignMemoryApiDemoTest {

    @Test
    void nativeDowncallReturnsNonZeroVersion() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            long version = ForeignMemoryApiDemo.avcodecVersion(arena);
            assertNotEquals(0L, version, "ffmpeg avcodec_version should be a real, non-zero version");
        }
    }

    @Test
    void nativeSegmentRoundTrip() {
        assertEquals(42, ForeignMemoryApiDemo.segmentRoundTrip());
    }

    @Test
    void nativeBufferSum() {
        assertEquals(30, ForeignMemoryApiDemo.sumNativeBuffer(new int[]{5, 10, 15}));
        assertEquals(0, ForeignMemoryApiDemo.sumNativeBuffer(new int[0]));
    }

    @Test
    void runReportsVersionAndSums() {
        String result = ForeignMemoryApiDemo.run();
        assertTrue(result.contains("avcodec_version="), "should print the ffmpeg version");
        assertTrue(result.contains("native buffer sum=10"), "1+2+3+4 == 10");
    }

    @Test
    void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() -> ForeignMemoryApiDemo.main(new String[0]));
    }
}

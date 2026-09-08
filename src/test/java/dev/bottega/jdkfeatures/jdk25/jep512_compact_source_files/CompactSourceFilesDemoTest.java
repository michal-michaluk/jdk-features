package dev.bottega.jdkfeatures.jdk25.jep512_compact_source_files;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompactSourceFilesDemoTest {

    @Test
    void launchesTopLevelMainSourceFile() throws Exception {
        String output = CompactSourceFilesDemo.launch();
        assertTrue(output.contains("compact=7"), "Math.max(3,7)==7, evidence the compact source ran");
    }

    @Test
    void runDescribesThenLaunches() throws Exception {
        assertTrue(CompactSourceFilesDemo.run().contains("JEP 512"));
    }

    @Test
    void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() -> CompactSourceFilesDemo.main(new String[0]));
    }
}

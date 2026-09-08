package dev.bottega.jdkfeatures.jdk22.jep458_launch_multi_file;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaunchMultiFileDemoTest {

    @Test
    void launchesTwoSourceFilesTogether() throws Exception {
        String output = LaunchMultiFileDemo.launch();
        assertTrue(output.contains("MultiFile sum=15"), "7 + 8 == 15, evidence both files compiled together");
    }

    @Test
    void runDescribesThenLaunches() throws Exception {
        assertTrue(LaunchMultiFileDemo.run().contains("JEP 458"));
    }

    @Test
    void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() -> LaunchMultiFileDemo.main(new String[0]));
    }
}

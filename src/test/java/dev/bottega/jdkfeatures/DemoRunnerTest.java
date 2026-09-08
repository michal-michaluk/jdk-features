package dev.bottega.jdkfeatures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DemoRunnerTest {

    @Test
    void runsEveryFeatureDemoInSequence() {
        assertDoesNotThrow(() -> DemoRunner.main(new String[0]));
    }
}

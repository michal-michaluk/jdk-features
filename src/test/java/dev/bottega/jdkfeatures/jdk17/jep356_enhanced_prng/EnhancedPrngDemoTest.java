package dev.bottega.jdkfeatures.jdk17.jep356_enhanced_prng;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.random.RandomGenerator;

import org.junit.jupiter.api.Test;

class EnhancedPrngDemoTest {

    @Test
    void describeMatchesConvention() {
        assertEquals("JEP 356 — Enhanced Pseudo-Random Number Generators (JDK 17)",
                EnhancedPrngDemo.describe());
    }

    @Test
    void demoConstantsAreFixed() {
        assertTrue(EnhancedPrngDemo.ALGORITHM.equals("L64X128MixRandom"));
        assertTrue(EnhancedPrngDemo.SEED == 42L);
    }

    @Test
    void allAlgorithmsContainsKnownNames() {
        List<String> names = EnhancedPrngDemo.allAlgorithms();
        assertTrue(names.contains("L64X128MixRandom"));
        assertTrue(names.contains("Random"));
    }

    @Test
    void ofNameCreatesAWorkingGenerator() {
        RandomGenerator rng = EnhancedPrngDemo.ofName("L64X128MixRandom");
        assertNotNull(rng);
    }

    @Test
    void seededCreatesAWorkingGenerator() {
        RandomGenerator rng = EnhancedPrngDemo.seeded("Random", EnhancedPrngDemo.SEED);
        assertNotNull(rng);
    }

    @Test
    void oneOfEachDrawsIntLongAndDouble() {
        String out = EnhancedPrngDemo.oneOfEach(EnhancedPrngDemo.seeded("Random", EnhancedPrngDemo.SEED));
        assertTrue(out.contains("int="));
        assertTrue(out.contains("long="));
        assertTrue(out.contains("double="));
    }

    @Test
    void sameSeedSameSequenceIsDeterministic() {
        assertTrue(EnhancedPrngDemo.sameSeedSameSequence("Random", EnhancedPrngDemo.SEED, 5));
        assertTrue(EnhancedPrngDemo.sameSeedSameSequence("L64X128MixRandom", EnhancedPrngDemo.SEED, 5));
    }

    @Test
    void runShowsFactoriesSeedingAndDeterminism() {
        String out = EnhancedPrngDemo.run();
        assertTrue(out.contains("L64X128MixRandom"));
        assertTrue(out.contains("Random (seed 42)"));
        assertTrue(out.contains("Determinism (same seed -> same sequence): true"));
    }

    @Test
    void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() -> EnhancedPrngDemo.main(new String[0]));
    }
}

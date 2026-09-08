package dev.bottega.jdkfeatures.jdk17.jep356_enhanced_prng;

import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;

/**
 * JEP 356 — Enhanced Pseudo-Random Number Generators (JDK 17).
 *
 * <p>The JEP adds a common, SPI-based interface {@link RandomGenerator} (covering
 * {@code Random}, {@code SplittableRandom} and the JDK's new LXM/PRNG families)
 * plus {@link RandomGeneratorFactory}, which lets you select an algorithm by name
 * and create generators from a stream of available algorithms. Algorithms chosen
 * up-front, factory-created generators and seed-derived generators all share the
 * same {@code int/long/double} drawing API.
 */
public final class EnhancedPrngDemo {

    /** The algorithm name used to show factory selection and seeded generators. */
    public static final String ALGORITHM = "L64X128MixRandom";
    /** A fixed seed so the demo and its tests are deterministic. */
    public static final long SEED = 42L;

    private EnhancedPrngDemo() {
    }

    /** Returns the JEP descriptor string used by {@link #main}. */
    public static String describe() {
        return "JEP 356 — Enhanced Pseudo-Random Number Generators (JDK 17)";
    }

    /** Lists every algorithm name exposed by {@link RandomGeneratorFactory#all()}, sorted. */
    public static List<String> allAlgorithms() {
        return RandomGeneratorFactory.all()
                .map(RandomGeneratorFactory::name)
                .sorted()
                .toList();
    }

    /** Creates a {@link RandomGenerator} from an algorithm name. */
    public static RandomGenerator ofName(String algorithm) {
        return RandomGeneratorFactory.of(algorithm).create();
    }

    /** Creates a deterministic {@link RandomGenerator} from an algorithm name and seed. */
    public static RandomGenerator seeded(String algorithm, long seed) {
        return RandomGeneratorFactory.of(algorithm).create(seed);
    }

    /** Draws one int, one long and one double from {@code rng}. */
    public static String oneOfEach(RandomGenerator rng) {
        return "int=" + rng.nextInt() + ", long=" + rng.nextLong() + ", double=" + rng.nextDouble();
    }

    /** Returns {@code true} if two generators of the same algorithm/seed yield the same sequence. */
    public static boolean sameSeedSameSequence(String algorithm, long seed, int draws) {
        RandomGenerator first = seeded(algorithm, seed);
        RandomGenerator second = seeded(algorithm, seed);
        for (int i = 0; i < draws; i++) {
            if (first.nextLong() != second.nextLong()) {
                return false;
            }
        }
        return true;
    }

    /** Runs the illustrative cases and returns a multi-line summary. */
    public static String run() {
        StringBuilder sb = new StringBuilder();

        List<String> algorithms = allAlgorithms();
        sb.append("Available PRNG algorithms (").append(algorithms.size()).append("):\n");
        sb.append(algorithms.stream().limit(8).collect(Collectors.joining(", "))).append("...\n");

        RandomGenerator factoryMade = ofName(ALGORITHM);
        sb.append(ALGORITHM).append(" (factory) -> ").append(oneOfEach(factoryMade)).append('\n');

        RandomGenerator seedMade = seeded("Random", SEED);
        sb.append("Random (seed ").append(SEED).append(") -> ").append(oneOfEach(seedMade)).append('\n');

        boolean deterministic = sameSeedSameSequence("Random", SEED, 5);
        sb.append("Determinism (same seed -> same sequence): ").append(deterministic).append('\n');

        return sb.toString();
    }

    /** Entry point: prints {@link #describe()} then {@link #run()}. */
    public static void main(String[] args) {
        System.out.println(describe());
        System.out.println(run());
    }
}

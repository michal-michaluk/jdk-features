package dev.bottega.jdkfeatures.jdk26.jep529_vector;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;

/**
 * JEP 529 — Vector API (incubator in JDK 26).
 *
 * <p>SIMD-style bulk arithmetic over {@link IntVector}: a whole lane vector is
 * loaded from an int array and reduced with a single vector operator call —
 * the JIT lowers this to vector instructions.</p>
 *
 * <p><b>Note:</b> this is an <em>incubating</em> module, so it needs
 * {@code --add-modules jdk.incubator.vector} (and, because it shares a module that
 * uses preview elsewhere, {@code --enable-preview} in this build).</p>
 */
public final class VectorApiDemo {

    public static final int BLOCK = 8;

    private VectorApiDemo() {
    }

    public static String describe() {
        return "JEP 529 \u2014 Vector API (JDK 26, incubator)";
    }

    /** Sums exactly {@value #BLOCK} ints in a single vector reduction. */
    public static int sumBlock(int[] values) {
        if (values.length != BLOCK) {
            throw new IllegalArgumentException("expected " + BLOCK + " values");
        }
        IntVector vector = IntVector.fromArray(IntVector.SPECIES_256, values, 0);
        return vector.reduceLanes(VectorOperators.ADD);
    }

    public static String run() {
        int[] values = {1, 2, 3, 4, 5, 6, 7, 8};
        return describe() + "\nsum(" + java.util.Arrays.toString(values) + ")=" + sumBlock(values);
    }

    public static void main(String[] args) {
        System.out.println(run());
    }
}

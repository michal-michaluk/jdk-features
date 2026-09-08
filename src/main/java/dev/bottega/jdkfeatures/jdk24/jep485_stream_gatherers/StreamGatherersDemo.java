package dev.bottega.jdkfeatures.jdk24.jep485_stream_gatherers;

import java.util.List;
import java.util.stream.Gatherers;
import java.util.stream.IntStream;

/**
 * Demonstrates JEP 485 — Stream Gatherers (JDK 24, final).
 *
 * <p>The JEP adds {@link java.util.stream.Stream#gather} together with a set of
 * built-in {@link java.util.stream.Gatherers} that make it easy to transform a
 * stream with stateful intermediate operations: fixed/sliding windows, folds and
 * scans.
 */
public final class StreamGatherersDemo {

    private StreamGatherersDemo() {
    }

    /** Returns the JEP descriptor string used on the first line of {@link #run()}. */
    public static String describe() {
        return "JEP 485 — Stream Gatherers (JDK 24)";
    }

    /** Boxes an int range into a deterministic list (fromInclusive inclusive, toExclusive exclusive). */
    public static List<Integer> range(int fromInclusive, int toExclusive) {
        return IntStream.range(fromInclusive, toExclusive).boxed().toList();
    }

    /**
     * Chunks {@code source} into non-overlapping windows of exactly {@code windowSize}
     * elements; the trailing window may be smaller when the size does not divide evenly.
     */
    public static List<List<Integer>> windowFixed(int windowSize, List<Integer> source) {
        return source.stream()
                .gather(Gatherers.windowFixed(windowSize))
                .toList();
    }

    /**
     * Produces overlapping windows of {@code windowSize} elements, stepping one element
     * at a time, mirroring "sliding" windows.
     */
    public static List<List<Integer>> windowSliding(int windowSize, List<Integer> source) {
        return source.stream()
                .gather(Gatherers.windowSliding(windowSize))
                .toList();
    }

    /** Reduces {@code source} to a single value with an additive fold seeded at {@code 0}. */
    public static int foldSum(List<Integer> source) {
        return source.stream()
                .gather(Gatherers.fold(() -> 0, (acc, e) -> acc + e))
                .findFirst()
                .orElse(0);
    }

    /** Emits the running total, i.e. the cumulative sum after each element. */
    public static List<Integer> runningTotal(List<Integer> source) {
        return source.stream()
                .gather(Gatherers.scan(() -> 0, (acc, e) -> acc + e))
                .toList();
    }

    /** Runs the illustrative cases and returns a multi-line summary. */
    public static String run() {
        List<Integer> numbers = range(1, 9); // 1..8
        StringBuilder sb = new StringBuilder();
        sb.append(describe()).append('\n');
        sb.append("numbers = ").append(numbers).append('\n');
        sb.append("windowFixed(3) = ").append(windowFixed(3, numbers)).append('\n');
        sb.append("windowSliding(2) = ").append(windowSliding(2, numbers)).append('\n');
        sb.append("fold(sum) = ").append(foldSum(numbers)).append('\n');
        sb.append("runningTotal = ").append(runningTotal(numbers)).append('\n');
        return sb.toString();
    }

    /** Entry point: prints {@link #run()} to stdout. */
    public static void main(String[] args) {
        System.out.println(run());
    }
}

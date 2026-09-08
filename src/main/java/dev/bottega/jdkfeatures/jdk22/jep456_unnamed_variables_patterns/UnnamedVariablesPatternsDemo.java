package dev.bottega.jdkfeatures.jdk22.jep456_unnamed_variables_patterns;

import java.io.Closeable;
import java.util.List;
import java.util.function.BiFunction;

/**
 * JEP 456 — Unnamed Variables &amp; Patterns (JDK 22).
 *
 * <p>Since JDK 22 the bare underscore {@code _} is a <em>reserved keyword</em> and can no longer
 * be used as a variable name. Instead it denotes an <em>unnamed variable</em>: "I need to be
 * here syntactically, but I do not care about the value bound here."
 *
 * <p>Before JDK 22 these were legal identifiers — today they are <strong>COMPILE ERRORS</strong>:
 * <pre>
 *   int _ = 5;        // ERROR: '_' is a keyword since JDK 22
 *   String _ = "x";   // ERROR
 *   int _x = 5;       // still legal: only the bare '_' is reserved
 * </pre>
 */
public final class UnnamedVariablesPatternsDemo {

    private UnnamedVariablesPatternsDemo() {
    }

    /** Simple record used to demonstrate an unnamed component in a record pattern. */
    record Point(int x, int y) {
    }

    /** Closeable that tracks whether it has actually been closed by try-with-resources. */
    private static final class TrackedResource implements Closeable {

        private boolean closed = false;

        @Override
        public void close() {
            closed = true;
        }

        public boolean isClosed() {
            return closed;
        }
    }

    public static String describe() {
        return "JEP 456 — Unnamed Variables & Patterns (JDK 22)";
    }

    public static void main(String[] args) {
        System.out.println(describe());
        System.out.println(run());
    }

    public static String run() {
        return String.join(System.lineSeparator(),
            "Case 1 — record pattern with unnamed component: " + recordPatternWithUnnamedComponent(),
            "Case 2 — instanceof pattern with `_`: " + instanceofPatternWithUnderscore("hello"),
            "Case 3 — lambda with unnamed parameter(s): " + lambdaWithUnnamedParams(21, 2),
            "Case 4 — for-loop ignoring each element: " + forLoopIgnoringElement(),
            "Case 5 — catch ignoring the exception: " + catchIgnoringException(),
            "Case 6 — try-with-resources unnamed variable: " + tryWithUnnamedResource());
    }

    // ---- helpers exposed for the demo and its tests ----

    /** Case 1: record pattern with an unnamed component — use x, ignore y. */
    public static String recordPatternWithUnnamedComponent() {
        Object obj = new Point(3, 9);
        if (obj instanceof Point(int x, _)) {
            return "x=" + x + ", y ignored (unnamed)";
        }
        return "not a Point";
    }

    /** Case 2: {@code instanceof} type pattern with {@code _} — match type, ignore value. */
    public static String instanceofPatternWithUnderscore(Object obj) {
        if (obj instanceof String _) {
            return "object is a String (value ignored)";
        }
        return "object is not a String";
    }

    /** Case 3: lambda with unnamed parameters — both unused, and one-used-one-unnamed. */
    public static String lambdaWithUnnamedParams(int a, int b) {
        BiFunction<Integer, Integer, String> bothIgnored = (_, _) -> "both params ignored";
        BiFunction<Integer, Integer, String> partialIgnored = (x, _) -> "x=" + x + ", second ignored";
        return bothIgnored.apply(a, b) + " | " + partialIgnored.apply(a, b);
    }

    /** Case 4: enhanced-for loop that ignores each element but still iterates. */
    public static String forLoopIgnoringElement() {
        List<String> items = List.of("a", "b", "c", "d");
        int count = 0;
        for (var _ : items) { // only the iteration matters; the element is unused
            count++;
        }
        return "iterated over " + count + " elements";
    }

    /** Case 5: {@code catch} that ignores the thrown exception. */
    public static String catchIgnoringException() {
        try {
            throw new RuntimeException("boom");
        } catch (RuntimeException _) { // the exception value is irrelevant here
            return "caught RuntimeException (ignored)";
        }
    }

    /** Case 6: try-with-resources with an unnamed resource variable (still auto-closed). */
    public static String tryWithUnnamedResource() {
        TrackedResource resource = new TrackedResource();
        try (TrackedResource _ = resource) { // resource variable unnamed, but still closed at the end
            // body intentionally empty
        }
        return "resource closed after try-with-resources: " + resource.isClosed();
    }
}

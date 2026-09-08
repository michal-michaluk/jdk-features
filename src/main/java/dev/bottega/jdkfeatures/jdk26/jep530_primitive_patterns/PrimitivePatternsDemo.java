package dev.bottega.jdkfeatures.jdk26.jep530_primitive_patterns;

/**
 * JEP 530 — Primitive Types in Patterns, instanceof, and switch (preview in JDK 26).
 *
 * <p>Type patterns can now name a <b>primitive</b> type: {@code case int i},
 * {@code case long l}, etc. When the selector is a primitive (or its boxed type),
 * the pattern unboxes it and binds the value to a primitive variable. This removes
 * the old boxing/instanceof dance for exact numeric types.</p>
 */
public final class PrimitivePatternsDemo {

    private PrimitivePatternsDemo() {
    }

    public static String describe() {
        return "JEP 530 \u2014 Primitive Types in Patterns, instanceof, and switch (JDK 26, preview)";
    }

    /** Classifies an object by its exact primitive type via pattern matching. */
    public static String classify(Object value) {
        return switch (value) {
            case null -> "null";
            case int i -> "int:" + i;
            case long l -> "long:" + l;
            case double d -> "double:" + d;
            default -> "other:" + value.getClass().getSimpleName();
        };
    }

    public static String run() {
        return describe()
                + "\n" + classify(5)
                + "\n" + classify(7L)
                + "\n" + classify(3.5)
                + "\n" + classify("str")
                + "\n" + classify(null);
    }

    public static void main(String[] args) {
        System.out.println(run());
    }
}

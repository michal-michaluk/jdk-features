package dev.bottega.jdkfeatures.jdk17.jep409_sealed;

import java.util.List;
import java.util.Locale;

/**
 * JEP 409 — Sealed Classes (JDK 17).
 *
 * <p>A sealed type restricts its direct subtypes to the explicit {@code permits}
 * list. Because the compiler knows the complete set of permitted subtypes, a
 * {@code switch} over the sealed type can be exhaustively enumerated with no
 * {@code default} clause — the compiler proves the switch covers everything.
 */
public final class SealedClassesDemo {

    private SealedClassesDemo() {
    }

    /** Returns the JEP descriptor string used by {@link #main}. */
    public static String describe() {
        return "JEP 409 — Sealed Classes (JDK 17)";
    }

    /**
     * Sealed hierarchy: only {@code Circle}, {@code Square}, {@code Triangle} and
     * {@code FreeShape} may implement {@code Shape}. Any other direct subclass is a
     * compile-time error.
     */
    public sealed interface Shape permits Circle, Square, Triangle, FreeShape {

        /** Area of the shape. */
        double area();
    }

    /** A permitted, immutable record subtype. */
    public record Circle(double radius) implements Shape {
        @Override
        public double area() {
            return Math.PI * radius * radius;
        }
    }

    /** A permitted, immutable record subtype. */
    public record Square(double side) implements Shape {
        @Override
        public double area() {
            return side * side;
        }
    }

    /** A permitted, immutable record subtype; area from Heron's formula. */
    public record Triangle(double a, double b, double c) implements Shape {
        @Override
        public double area() {
            double s = (a + b + c) / 2;
            return Math.sqrt(s * (s - a) * (s - b) * (s - c));
        }
    }

    /**
     * The {@code non-sealed} escape hatch. {@code non-sealed} marks a permitted
     * subtype as intentionally OPEN for further extension, so it is NOT {@code final}
     * (combining {@code final} with {@code non-sealed} is illegal). This lets a sealed
     * hierarchy stay extensible by code that cannot be listed in {@code permits}.
     */
    public static non-sealed class FreeShape implements Shape {
        private final String description;

        public FreeShape(String description) {
            this.description = description;
        }

        @Override
        public double area() {
            return 0;
        }

        public String description() {
            return description;
        }
    }

    /**
     * Exhaustive pattern-matching switch over the sealed hierarchy with NO
     * {@code default}. Listing every permitted subtype makes it exhaustive, which the
     * compiler enforces: dropping a case would be a compile error.
     */
    public static String describeShape(Shape shape) {
        return switch (shape) {
            case Circle c ->
                    "Circle(r=" + c.radius() + ", area=" + round(c.area()) + ")";
            case Square s ->
                    "Square(side=" + s.side() + ", area=" + round(s.area()) + ")";
            case Triangle t ->
                    "Triangle(a=" + t.a() + ", b=" + t.b() + ", c=" + t.c()
                            + ", area=" + round(t.area()) + ")";
            case FreeShape f ->
                    "FreeShape(non-sealed: " + f.description() + ", area=" + round(f.area()) + ")";
        };
    }

    /** Older {@code instanceof}-based classification, to contrast with the pattern switch. */
    public static String kindOf(Shape shape) {
        if (shape instanceof Circle) {
            return "circle";
        } else if (shape instanceof Square) {
            return "square";
        } else if (shape instanceof Triangle) {
            return "triangle";
        }
        return "free";
    }

    /** Sums the areas of a list of shapes over the sealed type. */
    public static double totalArea(List<Shape> shapes) {
        return shapes.stream().mapToDouble(Shape::area).sum();
    }

    /** Runs the illustrative cases and returns a multi-line summary. */
    public static String run() {
        List<Shape> shapes = List.of(
                new Circle(2),
                new Square(3),
                new Triangle(3, 4, 5),
                new FreeShape("custom logo"));
        StringBuilder sb = new StringBuilder();
        for (Shape shape : shapes) {
            sb.append(describeShape(shape)).append('\n');
            sb.append("  kind (instanceof): ").append(kindOf(shape)).append('\n');
        }
        sb.append("totalArea = ").append(round(totalArea(shapes))).append('\n');
        return sb.toString();
    }

    /** Entry point: prints {@link #describe()} then {@link #run()}. */
    public static void main(String[] args) {
        System.out.println(describe());
        System.out.println(run());
    }

    private static String round(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}

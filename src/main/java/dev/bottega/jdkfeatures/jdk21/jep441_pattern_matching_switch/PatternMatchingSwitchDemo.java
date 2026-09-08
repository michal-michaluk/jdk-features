package dev.bottega.jdkfeatures.jdk21.jep441_pattern_matching_switch;

/**
 * JEP 441 — Pattern Matching for switch (JDK 21).
 *
 * Type patterns with when guards, null handling, and an exhaustive switch over
 * a sealed hierarchy.
 */
public final class PatternMatchingSwitchDemo {

    private PatternMatchingSwitchDemo() {
    }

    public sealed interface Shape permits Circle, Rectangle {
    }

    public static final class Circle implements Shape {
        private final int radius;

        public Circle(int radius) {
            this.radius = radius;
        }

        public int radius() {
            return radius;
        }
    }

    public static final class Rectangle implements Shape {
        private final int width;
        private final int height;

        public Rectangle(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }
    }

    public static String describe() {
        return "JEP 441 — Pattern Matching for switch (JDK 21)";
    }

    public static void main(String[] args) {
        System.out.println(describe());
        System.out.println(run());
    }

    public static String run() {
        StringBuilder sb = new StringBuilder();

        sb.append("classify(short): ").append(classify("hi")).append('\n');
        sb.append("classify(long): ").append(classify("hello")).append('\n');
        sb.append("classify(42): ").append(classify(42)).append('\n');
        sb.append("classify(-7): ").append(classify(-7)).append('\n');
        sb.append("classify(null): ").append(classify(null)).append('\n');
        sb.append("shapeType(circle): ").append(shapeType(new Circle(5))).append('\n');
        sb.append("shapeType(rect): ").append(shapeType(new Rectangle(3, 4))).append('\n');
        sb.append("shapeArea(circle): ").append(shapeArea(new Circle(5))).append('\n');
        sb.append("shapeArea(rect): ").append(shapeArea(new Rectangle(3, 4))).append('\n');

        return sb.toString();
    }

    public static String classify(Object obj) {
        return switch (obj) {
            case null -> "null";
            case String s when s.length() > 3 -> "long string: " + s;
            case String s -> "short string: " + s;
            case Integer i when i < 0 -> "negative int: " + i;
            case Integer i -> "non-negative int: " + i;
            default -> "other: " + obj.getClass().getSimpleName();
        };
    }

    public static double shapeArea(Shape shape) {
        return switch (shape) {
            case Circle c -> Math.PI * c.radius() * c.radius();
            case Rectangle r -> r.width() * r.height();
        };
    }

    public static String shapeType(Shape shape) {
        return switch (shape) {
            case Circle c -> "circle radius=" + c.radius();
            case Rectangle r -> "rect " + r.width() + "x" + r.height();
        };
    }
}

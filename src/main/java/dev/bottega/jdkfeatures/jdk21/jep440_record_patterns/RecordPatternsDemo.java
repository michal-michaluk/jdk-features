package dev.bottega.jdkfeatures.jdk21.jep440_record_patterns;

import java.util.List;
import java.util.Optional;

/**
 * JEP 440 — Record Patterns (JDK 21).
 *
 * Nested record destructuring in instanceof and switch, and pattern matching
 * over a sealed hierarchy of shapes.
 */
public final class RecordPatternsDemo {

    private RecordPatternsDemo() {
    }

    public record Point(int x, int y) {
    }

    public record Box(Point center, int size) {
    }

    public sealed interface Shape permits Circle, Rectangle {
    }

    public record Circle(Point center, int radius) implements Shape {
    }

    public record Rectangle(Point corner, int width, int height) implements Shape {
    }

    public static String describe() {
        return "JEP 440 — Record Patterns (JDK 21)";
    }

    public static void main(String[] args) {
        System.out.println(describe());
        System.out.println(run());
    }

    public static String run() {
        StringBuilder sb = new StringBuilder();

        Point p = new Point(3, 4);
        Box box = new Box(p, 10);

        sb.append("box sum: ").append(sumBox(box)).append('\n');
        sb.append("boxLabel: ").append(boxLabel(box)).append('\n');
        sb.append("shape total area: ")
                .append(totalArea(List.of(
                        new Circle(new Point(0, 0), 2),
                        new Rectangle(new Point(0, 0), 3, 4))))
                .append('\n');
        sb.append("classify(Point): ").append(classify(new Point(5, 6))).append('\n');
        sb.append("classify(Box): ").append(classify(box)).append('\n');
        sb.append("classify(null): ").append(classify(null)).append('\n');
        sb.append("classify(\"abc\"): ").append(classify("abc")).append('\n');

        return sb.toString();
    }

    public static int sumBox(Object obj) {
        // Nested record pattern: Box -> (Point(int x, int y), int size).
        if (obj instanceof Box(Point(int x, int y), int size)) {
            return x + y + size;
        }
        return -1;
    }

    public static String boxLabel(Box box) {
        return switch (box) {
            case Box(Point(int x, int y), int size) -> "box(" + x + "," + y + ") size=" + size;
        };
    }

    public static double totalArea(List<? extends Shape> shapes) {
        double sum = 0;
        for (Shape s : shapes) {
            sum += switch (s) {
                case Circle(Point(int x, int y), int radius) when radius < 0 -> 0;
                case Circle(Point(int x, int y), int radius) -> Math.PI * radius * radius;
                case Rectangle(Point(int x, int y), int width, int height) -> width * height;
            };
        }
        return sum;
    }

    public static String classify(Object obj) {
        return switch (obj) {
            case Point(int x, int y) -> "point(" + x + "," + y + ")";
            case Box(Point(int x, int y), int size) -> "box(" + x + "," + y + ") size=" + size;
            case null -> "null";
            default -> obj.getClass().getSimpleName();
        };
    }
}

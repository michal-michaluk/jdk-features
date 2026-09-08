package dev.bottega.jdkfeatures.jdk23.jep467_markdown_doc_comments;

/**
 * JEP 467 — Markdown Documentation Comments (final in JDK 23).
 *
 * <p>This class uses <b>plain {@code ///} markdown doc comments</b> instead of the
 * legacy {@code /**} + HTML tags. This Javadoc block itself is rendered markdown.</p>
 */
public final class MarkdownDocCommentsDemo {

    private MarkdownDocCommentsDemo() {
    }

    /// Returns the rectangle area for a width/height.
    ///
    /// - `area(2, 3)` → **6**
    /// - *emphasis*, `inline code`, `[links](https://openjdk.org/jeps/467)` all supported.
    ///
    /// @param width  the rectangle width
    /// @param height the rectangle height
    /// @return width × height
    static double area(double width, double height) {
        return width * height;
    }

    /// Human-readable feature tag.
    static String tag() {
        return "JEP 467 \u2014 Markdown Documentation Comments (JDK 23)";
    }

    public static void main(String[] args) {
        System.out.println(tag());
        System.out.println("area=" + area(2, 3));
    }
}

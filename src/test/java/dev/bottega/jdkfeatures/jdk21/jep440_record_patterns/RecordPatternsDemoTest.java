package dev.bottega.jdkfeatures.jdk21.jep440_record_patterns;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RecordPatternsDemoTest {

    private static final RecordPatternsDemo.Point POINT = new RecordPatternsDemo.Point(3, 4);
    private static final RecordPatternsDemo.Box BOX = new RecordPatternsDemo.Box(POINT, 10);

    @Test
    void runContainsDestructuringEvidence() {
        String out = RecordPatternsDemo.run();
        assertTrue(out.contains("box sum: 17"));
        assertTrue(out.contains("boxLabel: box(3,4) size=10"));
        assertTrue(out.contains("shape total area: 24.566370614359172"));
        assertTrue(out.contains("classify(Point): point(5,6)"));
        assertTrue(out.contains("classify(Box): box(3,4) size=10"));
        assertTrue(out.contains("classify(null): null"));
        assertTrue(out.contains("classify(\"abc\"): String"));
    }

    @Test
    void describeMatchesJep() {
        assertEquals("JEP 440 — Record Patterns (JDK 21)", RecordPatternsDemo.describe());
    }

    @Test
    void mainDoesNotThrow() {
        assertDoesNotThrow(() -> RecordPatternsDemo.main(new String[0]));
    }

    @Test
    void sumBoxDestructuresNestedRecord() {
        assertEquals(17, RecordPatternsDemo.sumBox(BOX));
        assertEquals(-1, RecordPatternsDemo.sumBox("not a box"));
    }

    @Test
    void boxLabelIsExhaustive() {
        assertEquals("box(3,4) size=10", RecordPatternsDemo.boxLabel(BOX));
    }

    @Test
    void totalAreaSumsShapes() {
        double area = RecordPatternsDemo.totalArea(List.of(
                new RecordPatternsDemo.Circle(new RecordPatternsDemo.Point(0, 0), 2),
                new RecordPatternsDemo.Rectangle(new RecordPatternsDemo.Point(0, 0), 3, 4)));
        assertEquals(Math.PI * 4 + 12, area, 1e-9);
    }

    @Test
    void classifyUsesRecordPatternInSwitch() {
        assertEquals("point(5,6)", RecordPatternsDemo.classify(new RecordPatternsDemo.Point(5, 6)));
        assertEquals("null", RecordPatternsDemo.classify(null));
        assertEquals("String", RecordPatternsDemo.classify("abc"));
    }
}

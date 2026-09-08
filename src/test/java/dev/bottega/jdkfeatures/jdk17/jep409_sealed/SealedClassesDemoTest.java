package dev.bottega.jdkfeatures.jdk17.jep409_sealed;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class SealedClassesDemoTest {

    @Test
    void describeMatchesConvention() {
        assertEquals("JEP 409 — Sealed Classes (JDK 17)", SealedClassesDemo.describe());
    }

    @Test
    void runShowsEveryPermittedSubtypeIncludingTheNonSealedOne() {
        String out = SealedClassesDemo.run();
        assertTrue(out.contains("Circle"));
        assertTrue(out.contains("Square"));
        assertTrue(out.contains("Triangle"));
        assertTrue(out.contains("FreeShape"));
        assertTrue(out.contains("totalArea"));
    }

    @Test
    void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() -> SealedClassesDemo.main(new String[0]));
    }

    @Test
    void exhaustivePatternSwitchDescribesEachShape() {
        var circle = new SealedClassesDemo.Circle(2);
        var square = new SealedClassesDemo.Square(3);
        var triangle = new SealedClassesDemo.Triangle(3, 4, 5);
        var free = new SealedClassesDemo.FreeShape("custom logo");

        assertTrue(SealedClassesDemo.describeShape(circle).contains("Circle(r=2.0"));
        assertTrue(SealedClassesDemo.describeShape(square).contains("Square(side=3.0"));
        assertTrue(SealedClassesDemo.describeShape(triangle).contains("Triangle(a=3.0, b=4.0, c=5.0"));
        assertTrue(SealedClassesDemo.describeShape(free).contains("FreeShape(non-sealed: custom logo"));
    }

    @Test
    void instanceofHelperClassifiesEveryKind() {
        assertEquals("circle", SealedClassesDemo.kindOf(new SealedClassesDemo.Circle(1)));
        assertEquals("square", SealedClassesDemo.kindOf(new SealedClassesDemo.Square(1)));
        assertEquals("triangle", SealedClassesDemo.kindOf(new SealedClassesDemo.Triangle(3, 4, 5)));
        assertEquals("free", SealedClassesDemo.kindOf(new SealedClassesDemo.FreeShape("x")));
    }

    @Test
    void totalAreaSumsAllShapes() {
        double total = SealedClassesDemo.totalArea(List.of(
                new SealedClassesDemo.Circle(2),
                new SealedClassesDemo.Square(3),
                new SealedClassesDemo.Triangle(3, 4, 5),
                new SealedClassesDemo.FreeShape("logo")));
        assertTrue(total > 25);
    }

    @Test
    void freeShapeExposesItsDescription() {
        SealedClassesDemo.FreeShape free = new SealedClassesDemo.FreeShape("banner");
        assertTrue(free.description().equals("banner"));
        assertTrue(free.area() == 0);
    }
}

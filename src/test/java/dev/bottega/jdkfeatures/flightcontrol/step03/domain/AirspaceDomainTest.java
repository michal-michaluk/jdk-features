package dev.bottega.jdkfeatures.flightcontrol.step03.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirspaceDomainTest {

    private final Airspace airspace = Airspace.sample();

    @Test
    void sampleBuildsFoxAndEchoPlusControlAndTerminal() {
        assertEquals(2, airspace.aircraft().size());
        assertEquals(2, airspace.areas().size());
        assertEquals("FOX", airspace.aircraft().get(0).label());
        assertEquals("ECHO", airspace.aircraft().get(1).label());
        assertEquals("CTR", airspace.areas().get(0).label());
        assertEquals("TMA", airspace.areas().get(1).label());
    }

    @Test
    void modelIsImmutableRecords() {
        assertEquals(new Point(10, 0), airspace.aircraft().get(0).pos());
        assertEquals(1.0, airspace.aircraft().get(0).vel().dx());
        assertEquals(0.0, airspace.aircraft().get(0).vel().dy());
        assertEquals("CTR", airspace.areas().get(0).label());
    }

    @Test
    void stepIsDeterministicFromSeed() {
        Airspace a = Airspace.sample();
        Airspace b = Airspace.sample();
        for (int i = 0; i < 6; i++) {
            a = a.step();
            b = b.step();
        }
        assertEquals(a.aircraft(), b.aircraft());
    }

    @Test
    void stepBoundsCourseTurnAndSpeedDelta() {
        Airspace next = airspace.step();
        Aircraft before = airspace.aircraft().get(0);
        Aircraft after = next.aircraft().get(0);
        double maxTurn = airspace.movement().maxTurnDeg();
        double maxDelta = airspace.movement().maxSpeedDelta();

        double delta = Math.hypot(after.vel().dx(), after.vel().dy())
                - Math.hypot(before.vel().dx(), before.vel().dy());
        assertTrue(Math.abs(delta) <= maxDelta, "speed delta " + delta + " > " + maxDelta);

        double h0 = Math.atan2(before.vel().dy(), before.vel().dx());
        double h1 = Math.atan2(after.vel().dy(), after.vel().dx());
        double turn = Math.toDegrees(Math.abs(h1 - h0));
        assertTrue(turn <= maxTurn, "turn " + turn + " > " + maxTurn);
    }

    @Test
    void stepMovesAircraftAndLeavesOriginalUnchanged() {
        Airspace next = airspace.step();
        assertNotEquals(airspace.aircraft().get(0).pos(), next.aircraft().get(0).pos());
        assertNotEquals(airspace.aircraft().get(1).pos(), next.aircraft().get(1).pos());
        assertEquals(new Point(10, 0), airspace.aircraft().get(0).pos());
        assertEquals(new Point(0, 5), airspace.aircraft().get(1).pos());
    }

    @Test
    void pointMoveAndAircraftMove() {
        assertEquals(new Point(11, 0), new Point(10, 0).move(new Velocity(1, 0)));
        Aircraft a = new Aircraft("a1", "FOX", "FOX123", new Point(10, 0), new Velocity(1, 0));
        assertEquals(new Point(11, 0), a.move().pos());
        assertEquals(new Point(10, 0), a.pos());
    }

    @Test
    void randomMovementIsAValueObject() {
        RandomMovement m1 = RandomMovementFactory.of(5, 2);
        RandomMovement m2 = RandomMovementFactory.of(5, 2);
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
        assertEquals(5, m1.maxTurnDeg());
        assertEquals(2, m1.maxSpeedDelta());
        assertTrue(m1.equals(m1));
        assertFalse(m1.equals(null));
        assertFalse(m1.equals("x"));
        assertFalse(m1.equals(RandomMovementFactory.of(6, 2)));
        assertTrue(m1.toString().contains("RandomMovement"));
        assertTrue(m1.toString().contains("maxTurnDeg=5"));
    }

    @Test
    void describeIsExhaustiveOverSealedArea() {
        String circle = airspace.describe(airspace.areas().get(0));
        String polygon = airspace.describe(airspace.areas().get(1));
        assertTrue(circle.startsWith("circle:CTR"));
        assertTrue(polygon.startsWith("polygon:TMA"));
        assertTrue(airspace.areas().get(0) instanceof Circle);
        assertTrue(airspace.areas().get(1) instanceof Polygon);
        assertInstanceOf(Circle.class, airspace.areas().get(0));
        assertInstanceOf(Polygon.class, airspace.areas().get(1));
    }

    @Test
    void describeReportsCircleRadiusAndVertexCount() {
        assertEquals("circle:CTR r=3.0", airspace.describe(
                new Circle(new Point(0, 0), 3.0, "CTR", Map.of("type", "control"))));
        assertEquals("polygon:TMA v=3", airspace.describe(
                new Polygon(List.of(new Point(0, 0), new Point(4, 0), new Point(4, 4)),
                        "TMA", Map.of("kind", "terminal"))));
    }

    @Test
    void airspaceAcceptsBothAreaSubtypes() {
        List<Area> areas = airspace.areas();
        assertEquals(2, areas.size());
        assertInstanceOf(Circle.class, areas.get(0));
        assertInstanceOf(Polygon.class, areas.get(1));
    }

    @Test
    void copyListsAreDefensive() {
        Airspace as = new Airspace(List.of(), List.of());
        assertTrue(as.aircraft().isEmpty());
        assertTrue(as.areas().isEmpty());
    }

    @Test
    void constructorCopiesInputListsSoMutationDoesNotLeak() {
        List<Aircraft> ac = new ArrayList<>(List.of(
                new Aircraft("a1", "FOX", "FOX123", new Point(10, 0), new Velocity(1, 0))));
        List<Area> ar = new ArrayList<>(List.of(new Circle(new Point(0, 0), 3.0, "CTR", Map.of())));
        Airspace as = new Airspace(ac, ar);

        ac.clear();
        ar.clear();

        assertEquals(1, as.aircraft().size());
        assertEquals(1, as.areas().size());
        // the returned lists are unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> as.aircraft().clear());
        assertThrows(UnsupportedOperationException.class, () -> as.areas().clear());
    }

    @Test
    void aircraftAccessorsAndEquality() {
        Aircraft a = new Aircraft("a1", "FOX", "FOX123", new Point(10, 0), new Velocity(1, 0));
        assertEquals("a1", a.id());
        assertEquals("FOX", a.label());
        assertEquals("FOX123", a.callsign());
        assertEquals(new Point(10, 0), a.pos());
        assertEquals(new Velocity(1, 0), a.vel());
        assertEquals(a, new Aircraft("a1", "FOX", "FOX123", new Point(10, 0), new Velocity(1, 0)));
        assertEquals(a.hashCode(), new Aircraft("a1", "FOX", "FOX123", new Point(10, 0), new Velocity(1, 0)).hashCode());
        assertTrue(a.toString().contains("FOX"));
    }

    @Test
    void withPositionReturnsNewAircraftLeavingOriginalUntouched() {
        Aircraft a = new Aircraft("a1", "FOX", "FOX123", new Point(10, 0), new Velocity(1, 0));
        Aircraft moved = a.withPosition(new Point(11, 0));
        assertEquals(new Point(11, 0), moved.pos());
        assertEquals(new Point(10, 0), a.pos());
        assertEquals("a1", moved.id());
        assertEquals(new Velocity(1, 0), moved.vel());
    }

    @Test
    void pointAccessorsEqualityAndToString() {
        Point p = new Point(2.5, -1.0);
        assertEquals(2.5, p.x());
        assertEquals(-1.0, p.y());
        assertEquals(new Point(2.5, -1.0), p);
        assertEquals(new Point(2.5, -1.0).hashCode(), p.hashCode());
        assertTrue(p.toString().contains("2.5"));
    }

    @Test
    void velocityAccessorsEqualityAndToString() {
        Velocity v = new Velocity(1.5, -0.5);
        assertEquals(1.5, v.dx());
        assertEquals(-0.5, v.dy());
        assertEquals(new Velocity(1.5, -0.5), v);
        assertEquals(new Velocity(1.5, -0.5).hashCode(), v.hashCode());
        assertTrue(v.toString().contains("1.5"));
    }

    @Test
    void circleAccessorsEqualityAndToString() {
        Circle c = new Circle(new Point(0, 0), 3.0, "CTR", Map.of("type", "control"));
        assertEquals(new Point(0, 0), c.center());
        assertEquals(3.0, c.radius());
        assertEquals("CTR", c.label());
        assertEquals(Map.of("type", "control"), c.props());
        assertEquals(c, new Circle(new Point(0, 0), 3.0, "CTR", Map.of("type", "control")));
        assertEquals(c.hashCode(), new Circle(new Point(0, 0), 3.0, "CTR", Map.of("type", "control")).hashCode());
        assertTrue(c.toString().contains("CTR"));
    }

    @Test
    void polygonAccessorsEqualityAndToString() {
        Polygon p = new Polygon(List.of(new Point(0, 0), new Point(4, 0)), "TMA", Map.of("kind", "terminal"));
        assertEquals(List.of(new Point(0, 0), new Point(4, 0)), p.vertices());
        assertEquals("TMA", p.label());
        assertEquals(Map.of("kind", "terminal"), p.props());
        assertEquals(p, new Polygon(List.of(new Point(0, 0), new Point(4, 0)), "TMA", Map.of("kind", "terminal")));
        assertEquals(p.hashCode(), new Polygon(List.of(new Point(0, 0), new Point(4, 0)), "TMA", Map.of("kind", "terminal")).hashCode());
        assertTrue(p.toString().contains("TMA"));
    }

    @Test
    void eachAreaIsACircleOrPolygon() {
        for (Area area : airspace.areas()) {
            String described = airspace.describe(area);
            assertTrue(described.startsWith("circle:") || described.startsWith("polygon:"));
        }
    }
}

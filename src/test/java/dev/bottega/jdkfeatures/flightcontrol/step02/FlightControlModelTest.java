package dev.bottega.jdkfeatures.flightcontrol.step02;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightControlModelTest {

    private final Airspace airspace = Airspace.sample();

    @Test
    void modelIsImmutableRecords() {
        assertEquals(new Point(10, 0), airspace.aircraft().get(0).pos());
        assertEquals(1.0, airspace.aircraft().get(0).vel().dx());
        assertEquals(0.0, airspace.aircraft().get(0).vel().dy());
        assertEquals("CTR", airspace.areas().get(0).label());
    }

    @Test
    void pointMoveDisplacesByVelocity() {
        assertEquals(new Point(11, 0), new Point(10, 0).move(new Velocity(1, 0)));
        assertEquals(new Point(10, 6), new Point(10, 5).move(new Velocity(0, 1)));
    }

    @Test
    void aircraftMoveUsesCurrentVelocityAndLeavesOriginal() {
        Aircraft a = new Aircraft("a1", "FOX", "FOX123", new Point(10, 0), new Velocity(1, 0));
        Aircraft moved = a.move();
        assertEquals(new Point(11, 0), moved.pos());
        assertEquals(new Velocity(1, 0), moved.vel());
        assertEquals(new Point(10, 0), a.pos());
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
    void randomMovementIsAValueObject() {
        RandomMovement m1 = RandomMovementFactory.of(5, 2);
        RandomMovement m2 = RandomMovementFactory.of(5, 2);
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
        assertEquals(5, m1.maxTurnDeg());
        assertEquals(2, m1.maxSpeedDelta());
        // deterministic generator: same seed -> same first draw
        assertEquals(m1.generator().nextDouble(), m2.generator().nextDouble());
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
}

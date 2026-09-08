package dev.bottega.jdkfeatures.flightcontrol.step05.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.SequencedSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    void stepAdvancesEveryAircraftByItsVelocity() {
        Airspace next = airspace.step();
        assertEquals(new Point(11, 0), next.aircraft().get(0).pos());
        assertEquals(new Point(0, 6), next.aircraft().get(1).pos());
        // original is unchanged (immutability)
        assertEquals(new Point(10, 0), airspace.aircraft().get(0).pos());
        assertEquals(new Point(0, 5), airspace.aircraft().get(1).pos());
    }

    @Test
    void describeReportsDestructuredComponents() {
        assertEquals("circle:CTR center=(0.0,0.0) r=3.0 props={type=control}", airspace.describe(
                new Circle(new Point(0, 0), 3.0, "CTR", Map.of("type", "control"))));
        assertEquals("polygon:TMA v=3 props={kind=terminal}", airspace.describe(
                new Polygon(List.of(new Point(0, 0), new Point(4, 0), new Point(4, 4)),
                        "TMA", Map.of("kind", "terminal"))));
    }

    @Test
    void describeUsesNestedRecordPatternForCenter() {
        String c = airspace.describe(new Circle(new Point(2.5, -1.0), 4.0, "CTR", Map.of()));
        assertTrue(c.contains("center=(2.5,-1.0)"));
        assertTrue(c.contains("r=4.0"));
        assertTrue(c.startsWith("circle:CTR"));
    }

    @Test
    void describeDoesNotMutateArea() {
        Circle c = new Circle(new Point(0, 0), 3.0, "CTR", Map.of("type", "control"));
        Polygon p = new Polygon(List.of(new Point(0, 0), new Point(4, 0), new Point(4, 4)),
                "TMA", Map.of("kind", "terminal"));
        airspace.describe(c);
        airspace.describe(p);
        assertEquals(new Point(0, 0), c.center());
        assertEquals(3.0, c.radius());
        assertEquals(List.of(new Point(0, 0), new Point(4, 0), new Point(4, 4)), p.vertices());
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

    // ---- JEP 431: sequenced collections ----

    @Test
    void aircraftSortedByDistanceAscending() {
        List<Aircraft> sorted = airspace.aircraftSortedByDistance(new Point(0, 0), true);
        assertEquals(List.of("ECHO", "FOX"), sorted.stream().map(Aircraft::label).toList());
    }

    @Test
    void aircraftSortedByDistanceDescending() {
        List<Aircraft> sorted = airspace.aircraftSortedByDistance(new Point(0, 0), false);
        assertEquals(List.of("FOX", "ECHO"), sorted.stream().map(Aircraft::label).toList());
    }

    @Test
    void aircraftSortedByDistanceDoesNotMutateInternalState() {
        assertEquals(List.of("FOX", "ECHO"), airspace.aircraft().stream().map(Aircraft::label).toList());
        airspace.aircraftSortedByDistance(new Point(0, 0), true);
        airspace.aircraftSortedByDistance(new Point(0, 0), false);
        assertEquals(List.of("FOX", "ECHO"), airspace.aircraft().stream().map(Aircraft::label).toList());
    }

    @Test
    void aircraftSortedByDistanceIsDeterministic() {
        List<Aircraft> first = airspace.aircraftSortedByDistance(new Point(0, 0), true);
        List<Aircraft> second = airspace.aircraftSortedByDistance(new Point(0, 0), true);
        assertEquals(first, second);
    }

    @Test
    void areaLabelsIsSequencedSetInInsertionOrder() {
        SequencedSet<String> labels = airspace.areaLabels();
        assertInstanceOf(SequencedSet.class, labels);
        assertEquals(List.of("CTR", "TMA"), labels.stream().toList());
        assertEquals("CTR", labels.getFirst());
        assertEquals("TMA", labels.getLast());
    }

    @Test
    void areaLabelsDeduplicatesKeepingFirstOccurrenceOrder() {
        Airspace as = new Airspace(List.of(), List.of(
                new Circle(new Point(0, 0), 1, "CTR", Map.of()),
                new Polygon(List.of(new Point(0, 0)), "CTR", Map.of()),
                new Circle(new Point(1, 1), 2, "TMA", Map.of())));
        SequencedSet<String> labels = as.areaLabels();
        assertEquals(List.of("CTR", "TMA"), labels.stream().toList());
    }

    @Test
    void aircraftByIdIsSequencedMapInInsertionOrder() {
        SequencedMap<String, Aircraft> byId = airspace.aircraftById();
        assertInstanceOf(SequencedMap.class, byId);
        assertEquals(List.of("a1", "a2"), new ArrayList<>(byId.keySet()));
        assertEquals("FOX", byId.get("a1").label());
        assertEquals("ECHO", byId.get("a2").label());
    }

    @Test
    void aircraftByIdFirstAndLastEntry() {
        SequencedMap<String, Aircraft> byId = airspace.aircraftById();
        assertEquals("a1", byId.firstEntry().getKey());
        assertEquals("FOX", byId.firstEntry().getValue().label());
        assertEquals("a2", byId.lastEntry().getKey());
        assertEquals("ECHO", byId.lastEntry().getValue().label());
    }

    @Test
    void aircraftByIdPutFirstInsertsAtFrontWithoutTouchingState() {
        SequencedMap<String, Aircraft> byId = airspace.aircraftById();
        byId.putFirst("z", new Aircraft("z", "ZULU", "ZULU9", new Point(1, 1), new Velocity(1, 0)));
        assertEquals("z", byId.firstEntry().getKey());
        assertEquals(List.of("z", "a1", "a2"), new ArrayList<>(byId.keySet()));
        // internal state untouched
        assertEquals(List.of("a1", "a2"), airspace.aircraft().stream().map(Aircraft::id).toList());
    }

    @Test
    void aircraftByIdReversedIsViewOverReverseOrder() {
        SequencedMap<String, Aircraft> byId = airspace.aircraftById();
        SequencedMap<String, Aircraft> reversed = byId.reversed();
        assertEquals("a2", reversed.firstEntry().getKey());
        assertEquals("a1", reversed.lastEntry().getKey());
        assertEquals(List.of("a2", "a1"), new ArrayList<>(reversed.keySet()));
        // original map order unchanged
        assertEquals(List.of("a1", "a2"), new ArrayList<>(byId.keySet()));
    }

    @Test
    void orderedViewsBuildFreshFromSample() {
        assertEquals(List.of("ECHO", "FOX"),
                airspace.aircraftSortedByDistance(new Point(0, 0), true).stream().map(Aircraft::label).toList());
        assertEquals(List.of("CTR", "TMA"), new ArrayList<>(airspace.areaLabels()));
        assertEquals(2, airspace.aircraftById().size());
    }
}

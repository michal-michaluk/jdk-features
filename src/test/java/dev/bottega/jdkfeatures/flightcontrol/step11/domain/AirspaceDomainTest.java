package dev.bottega.jdkfeatures.flightcontrol.step11.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.SequencedSet;

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
        assertEquals(m1.generator().nextDouble(), m2.generator().nextDouble());
        assertTrue(m1.equals(m1));
        assertFalse(m1.equals(null));
        assertFalse(m1.equals("x"));
        assertFalse(m1.equals(RandomMovementFactory.of(6, 2)));
        assertTrue(m1.toString().contains("RandomMovement"));
        assertTrue(m1.toString().contains("maxTurnDeg=5"));
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

    // ---- JEP 441: pattern matching for switch (classification) ----

    private static final Circle CTR = new Circle(new Point(0, 0), 3.0, "CTR", Map.of("type", "control"));
    private static final ThreatClassifier classifier = new ThreatClassifier(10.0, CTR);

    @Test
    void classifierExposesConfiguration() {
        assertEquals(10.0, classifier.alarmMinSpeed());
        assertEquals(CTR, classifier.sector());
    }

    @Test
    void nullAircraftIsUnknown() {
        assertEquals(Category.UNKNOWN, classifier.classify(null));
    }

    @Test
    void fastAndInsideIsAlarm() {
        Aircraft hot = new Aircraft("a1", "HOT", "HOT1", new Point(1, 0), new Velocity(20, 0));
        assertEquals(Category.ALARM, classifier.classify(hot));
    }

    @Test
    void insideButNotFastIsSector() {
        Aircraft cool = new Aircraft("a2", "COOL", "COOL2", new Point(1, 0), new Velocity(5, 0));
        assertEquals(Category.SECTOR, classifier.classify(cool));
    }

    @Test
    void speedEqualToMinimumIsNotAlarm() {
        // speed == minSpeed is NOT > minSpeed, so it stays SECTOR (guard is strict).
        Aircraft boundary = new Aircraft("a3", "EDGE", "EDGE3", new Point(1, 0), new Velocity(10, 0));
        assertEquals(10.0, ThreatClassifier.speed(boundary));
        assertEquals(Category.SECTOR, classifier.classify(boundary));
    }

    @Test
    void outsideIsNormal() {
        Aircraft away = new Aircraft("a4", "AWAY", "AWAY4", new Point(9, 9), new Velocity(1, 1));
        assertEquals(Category.NORMAL, classifier.classify(away));
    }

    @Test
    void speedIsEuclideanLengthOfVelocity() {
        assertEquals(5.0, ThreatClassifier.speed(new Aircraft("x", "X", "X", new Point(0, 0), new Velocity(3, 4))));
        assertEquals(0.0, ThreatClassifier.speed(new Aircraft("x", "X", "X", new Point(0, 0), new Velocity(0, 0))));
    }

    @Test
    void insideCircleIsPointWithinRadius() {
        assertTrue(ThreatClassifier.inside(new Point(3, 0), CTR));       // on boundary
        assertTrue(ThreatClassifier.inside(new Point(0, 0), CTR));       // at center
        assertTrue(ThreatClassifier.inside(new Point(-2.9, 0.5), CTR)); // inside
        assertFalse(ThreatClassifier.inside(new Point(9, 9), CTR));      // outside
    }

    @Test
    void insideNonCircleAreaIsFalse() {
        Polygon tma = new Polygon(List.of(new Point(0, 0), new Point(4, 0), new Point(4, 4)),
                "TMA", Map.of("kind", "terminal"));
        assertFalse(ThreatClassifier.inside(new Point(1, 1), tma));
        assertFalse(ThreatClassifier.inside(new Point(1, 1), tma)); // non-circle is never a containment sector
    }

    @Test
    void allCategoriesInTheSwitchAreReachable() {
        assertEquals(4, Category.values().length);
        assertTrue(java.util.Arrays.asList(Category.values()).containsAll(
                java.util.List.of(Category.ALARM, Category.SECTOR, Category.NORMAL, Category.UNKNOWN)));
    }

    // ---- JEP 444: virtual threads (parallel radar) ----

    private static Radar radar() {
        Circle sector = new Circle(new Point(0, 0), 3.0, "CTR", Map.of("type", "control"));
        Airspace space = new Airspace(
                List.of(
                        new Aircraft("a1", "HOT", "HOT1", new Point(1, 0), new Velocity(20, 0)),
                        new Aircraft("a2", "HOT", "HOT2", new Point(1, 0), new Velocity(30, 0)),
                        new Aircraft("a3", "COOL", "COOL3", new Point(1, 0), new Velocity(5, 0)),
                        new Aircraft("a4", "AWAY", "AWAY4", new Point(9, 9), new Velocity(1, 1))),
                List.of(sector));
        return new Radar(space, new ThreatClassifier(10.0, sector));
    }

    @Test
    void radarAggregatesCountsPerCategory() throws Exception {
        RadarReport report = radar().report();
        assertEquals(4, report.processed());
        assertEquals(Integer.valueOf(2), report.counts().get(Category.ALARM));
        assertEquals(Integer.valueOf(1), report.counts().get(Category.SECTOR));
        assertEquals(Integer.valueOf(1), report.counts().get(Category.NORMAL));
        assertEquals(Integer.valueOf(0), report.counts().get(Category.UNKNOWN));
    }

    @Test
    void radarWorkersAreAllVirtualAndJoined() throws Exception {
        RadarReport report = radar().report();
        assertTrue(report.allVirtual());
        assertEquals(4, report.processed());
    }

    @Test
    void radarIsDeterministicOnSameInput() throws Exception {
        RadarReport first = radar().report();
        RadarReport second = radar().report();
        assertEquals(first.counts(), second.counts());
        assertEquals(first.processed(), second.processed());
        assertEquals(first.allVirtual(), second.allVirtual());
    }

    @Test
    void radarReportExposesImmutableSnapshot() throws Exception {
        RadarReport report = radar().report();
        assertTrue(report.counts() instanceof java.util.Map);
        assertThrows(UnsupportedOperationException.class, () -> report.counts().put(Category.ALARM, 99));
        assertTrue(report.toString().contains(Integer.toString(report.processed())));
    }

    @Test
    void radarReportAccessorsAndEquality() {
        RadarReport r1 = new RadarReport(Map.of(Category.ALARM, 2), 4, true);
        RadarReport r2 = new RadarReport(Map.of(Category.ALARM, 2), 4, true);
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertEquals(Integer.valueOf(2), r1.counts().get(Category.ALARM));
        assertEquals(4, r1.processed());
        assertTrue(r1.allVirtual());
    }

    // ---- JEP 485: stream gatherers (telemetry) ----

    private static final Telemetry telemetry = Telemetry.sample();
    private static final List<Integer> SAMPLE = List.of(10, 20, 30, 40, 50, 60, 70, 80);

    @Test
    void sampleBuildsTheEightValueYamlReadings() {
        assertEquals(SAMPLE, telemetry.readings());
        assertEquals(8, telemetry.readings().size());
    }

    @Test
    void telemetryConstructorCopiesTheInputList() {
        List<Integer> input = new ArrayList<>(List.of(10, 20, 30));
        Telemetry t = new Telemetry(input);
        input.clear();
        assertEquals(List.of(10, 20, 30), t.readings());
        assertThrows(UnsupportedOperationException.class, () -> t.readings().clear());
    }

    @Test
    void windowFixedChunksNonOverlappingIncludingTrailingShortWindow() {
        assertEquals(
                List.of(List.of(10, 20, 30), List.of(40, 50, 60), List.of(70, 80)),
                telemetry.windowFixed(3));
    }

    @Test
    void windowFixedWithWindowLargerThanStreamIsSingleShorterWindow() {
        assertEquals(List.of(SAMPLE), telemetry.windowFixed(10));
    }

    @Test
    void windowSlidingOverlapsByOneElement() {
        assertEquals(
                List.of(
                        List.of(10, 20), List.of(20, 30), List.of(30, 40), List.of(40, 50),
                        List.of(50, 60), List.of(60, 70), List.of(70, 80)),
                telemetry.windowSliding(2));
    }

    @Test
    void windowSlidingOfWindowSizeLargerThanStreamIsSingleWindow() {
        assertEquals(List.of(SAMPLE), telemetry.windowSliding(10));
    }

    @Test
    void emptyTelemetryProducesEmptyWindowsAndZeroSum() {
        Telemetry empty = new Telemetry(List.of());
        assertTrue(empty.windowFixed(3).isEmpty());
        assertTrue(empty.windowSliding(2).isEmpty());
        assertEquals(0, empty.foldSum());
        assertEquals(new Telemetry.Bounds(0, 0), empty.foldBounds());
    }

    @Test
    void foldSumTotalsAllReadings() {
        assertEquals(360, telemetry.foldSum());
    }

    @Test
    void foldBoundsReturnsMinAndMax() {
        assertEquals(new Telemetry.Bounds(10, 80), telemetry.foldBounds());
    }

    @Test
    void boundsRecordAccessorsEqualityAndToString() {
        Telemetry.Bounds b = new Telemetry.Bounds(10, 80);
        assertEquals(10, b.min());
        assertEquals(80, b.max());
        assertEquals(b, new Telemetry.Bounds(10, 80));
        assertEquals(b.hashCode(), new Telemetry.Bounds(10, 80).hashCode());
        assertTrue(b.toString().contains("10"));
    }

    // ---- JEP 506: scoped values (simulation context) ----

    @Test
    void runWithContextBindsBothValues() {
        SimulationContext.runWithContext("TMA", 42L, () -> {
            assertEquals("TMA", SimulationContext.currentSector());
            assertEquals(42L, SimulationContext.simulationId());
            assertTrue(SimulationContext.isSectorBound());
        });
    }

    @Test
    void forkInTaskInheritsScopedValuesOnVirtualThread() throws Exception {
        SimulationContext.runWithContext("TMA", 42L, () -> {
            try {
                String sector = SimulationContext.forkInTask(SimulationContext::currentSector);
                long id = SimulationContext.forkInTask(SimulationContext::simulationId);
                assertEquals("TMA", sector);
                assertEquals(42L, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void nestedOverrideWinsThenRestoresOuter() {
        SimulationContext.runWithContext("TMA", 1L, () -> {
            assertEquals("TMA", SimulationContext.currentSector());
            assertEquals(1L, SimulationContext.simulationId());
            SimulationContext.runWithContext("CTR", 2L, () -> {
                assertEquals("CTR", SimulationContext.currentSector());
                assertEquals(2L, SimulationContext.simulationId());
            });
            // outer values restored after the inner scope closes
            assertEquals("TMA", SimulationContext.currentSector());
            assertEquals(1L, SimulationContext.simulationId());
        });
    }

    @Test
    void currentSectorUnboundThrowsButOrElseFallsBack() {
        assertThrows(java.util.NoSuchElementException.class, SimulationContext::currentSector);
        assertEquals("UNKNOWN", SimulationContext.currentSectorOr("UNKNOWN"));
        assertEquals(SimulationContext.UNKNOWN_ID, SimulationContext.simulationId());
        assertFalse(SimulationContext.isSectorBound());
    }

    @Test
    void afterRunValuesAreOutOfScope() {
        SimulationContext.runWithContext("TMA", 42L, () -> {
            assertEquals("TMA", SimulationContext.currentSectorOr("UNKNOWN"));
        });
        // after run() completes the binding is gone, so orElse returns the fallback
        assertEquals("UNKNOWN", SimulationContext.currentSectorOr("UNKNOWN"));
        assertFalse(SimulationContext.isSectorBound());
    }
}

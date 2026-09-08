package dev.bottega.jdkfeatures.flightcontrol.step10.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.SequencedSet;

/**
 * Holds aircraft + areas, advances every aircraft by its velocity (headless, no UI),
 * and exposes **ordered views** over them via the sequenced-collections API (JEP 431).
 *
 * ## Immutability contract
 *
 * The aggregate stays fully immutable:
 *
 * - the stored lists are defensive `List.copyOf` copies and are never mutated;
 * - the ordered views (`areaLabels`, `aircraftById`, `aircraftSortedByDistance`) are built
 *   fresh from those lists on each call, so the caller never touches (or mutates) the
 *   internal state;
 * - `step()` returns a **new** `Airspace`; the receiver is never changed.
 *
 * There is no simulation loop inside the domain: no scheduler, no bounds, no bouncing.
 * The only operation is the pure `step()` transition.
 */
public final class Airspace {

    private final List<Aircraft> aircraft;
    private final List<Area> areas;

    public Airspace(List<Aircraft> aircraft, List<Area> areas) {
        this.aircraft = List.copyOf(aircraft);
        this.areas = List.copyOf(areas);
    }

    public List<Aircraft> aircraft() {
        return aircraft;
    }

    public List<Area> areas() {
        return areas;
    }

    /**
     * Advances every aircraft by its velocity and returns a **new** `Airspace`; the receiver
     * is unchanged (immutability).
     *
     * ```text
     * (x, y) -> (x + dx, y + dy)  applied to each aircraft's pos, keeping its vel
     * ```
     *
     * Aircraft are moved independently; the static `areas` list is carried over unchanged.
     */
    public Airspace step() {
        List<Aircraft> moved = aircraft.stream()
                .map(ac -> ac.withPosition(new Point(ac.pos().x() + ac.vel().dx(),
                        ac.pos().y() + ac.vel().dy())))
                .toList();
        return new Airspace(moved, areas);
    }

    /**
     * Exhaustive switch over the sealed {@link Area} hierarchy (no `default` branch), using
     * **record patterns** (JEP 440): components are destructured directly — including a
     * **nested** pattern for the circle's center `Point` — instead of casting and calling
     * accessors.
     *
     * ```text
     * circle:CTR center=(0.0,0.0) r=3.0 props={type=control}
     * polygon:TMA v=3 props={kind=terminal}
     * ```
     *
     * The switch is exhaustive over the sealed hierarchy, so adding a new `Area` subtype at
     * compile time forces `describe` to handle it.
     */
    public String describe(Area area) {
        return switch (area) {
            case Circle(Point(double cx, double cy), double radius, String label, Map<String, String> props) ->
                    "circle:" + label + " center=(" + cx + "," + cy + ") r=" + radius + " props=" + props;
            case Polygon(List<Point> vertices, String label, Map<String, String> props) ->
                    "polygon:" + label + " v=" + vertices.size() + " props=" + props;
        };
    }

    /**
     * Aircraft sorted by Euclidean distance to `center` (ascending when `true`,
     * descending otherwise). The returned list is a fresh copy — the internal state is untouched.
     */
    public List<Aircraft> aircraftSortedByDistance(Point center, boolean ascending) {
        Comparator<Aircraft> byDistance = Comparator.comparingDouble(ac -> distance(ac.pos(), center));
        List<Aircraft> sorted = new ArrayList<>(aircraft);
        sorted.sort(ascending ? byDistance : byDistance.reversed());
        return sorted;
    }

    private static double distance(Point a, Point b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Area labels in insertion order, de-duplicated. Returns a `SequencedSet`: no duplicates,
     * and the order = as the areas appear in the internal list (JEP 431).
     */
    public SequencedSet<String> areaLabels() {
        SequencedSet<String> labels = new LinkedHashSet<>();
        for (Area area : areas) {
            labels.add(area.label());
        }
        return labels;
    }

    /**
     * Map of `id -> Aircraft` in insertion order (a `SequencedMap`), built fresh from the
     * stored aircraft list — the internal state is never mutated (JEP 431).
     */
    public SequencedMap<String, Aircraft> aircraftById() {
        SequencedMap<String, Aircraft> byId = new LinkedHashMap<>();
        for (Aircraft ac : aircraft) {
            byId.put(ac.id(), ac);
        }
        return byId;
    }

    public static Airspace sample() {
        return new Airspace(
                List.of(new Aircraft("a1", "FOX", "FOX123", new Point(10, 0), new Velocity(1, 0)),
                        new Aircraft("a2", "ECHO", "ECHO456", new Point(0, 5), new Velocity(0, 1))),
                List.of(new Circle(new Point(0, 0), 3.0, "CTR", Map.of("type", "control")),
                        new Polygon(List.of(new Point(0, 0), new Point(4, 0), new Point(4, 4)),
                                "TMA", Map.of("kind", "terminal"))));
    }
}

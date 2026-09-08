package dev.bottega.jdkfeatures.flightcontrol.step04.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.random.RandomGenerator;

/**
 * Holds aircraft + areas, advances every aircraft by its velocity (headless, no UI),
 * and exposes <b>ordered views</b> over them via the sequenced-collections API (JEP 431).
 *
 * <p>The aggregate stays immutable: the stored lists are defensive {@code List.copyOf}
 * copies and are never mutated. The ordered views are built fresh from those lists on each
 * call, so the caller never touches (or mutates) the internal state.</p>
 */
public final class Airspace {

    private final List<Aircraft> aircraft;
    private final List<Area> areas;
    private final RandomMovement movement;
    private final RandomGenerator rng;

    public Airspace(List<Aircraft> aircraft, List<Area> areas) {
        this(aircraft, areas, RandomMovementFactory.defaults());
    }

    public Airspace(List<Aircraft> aircraft, List<Area> areas, RandomMovement movement) {
        this(aircraft, areas, movement, movement.generator());
    }

    private Airspace(List<Aircraft> aircraft, List<Area> areas, RandomMovement movement, RandomGenerator rng) {
        this.aircraft = List.copyOf(aircraft);
        this.areas = List.copyOf(areas);
        this.movement = Objects.requireNonNull(movement);
        this.rng = Objects.requireNonNull(rng);
    }

    public List<Aircraft> aircraft() {
        return aircraft;
    }

    public List<Area> areas() {
        return areas;
    }

    public RandomMovement movement() {
        return movement;
    }

    public Airspace step() {
        List<Aircraft> moved = aircraft.stream()
                .map(ac -> ac.withVelocity(movement.apply(ac.vel(), rng)).move())
                .toList();
        return new Airspace(moved, areas, movement, rng);
    }

    /** Exhaustive switch over the sealed {@link Area} hierarchy (no default branch). */
    public String describe(Area area) {
        return switch (area) {
            case Circle c -> "circle:" + c.label() + " r=" + c.radius();
            case Polygon p -> "polygon:" + p.label() + " v=" + p.vertices().size();
        };
    }

    /**
     * Aircraft sorted by Euclidean distance to {@code center} (ascending when {@code true},
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
     * Area labels in insertion order, de-duplicated (a {@code SequencedSet}: no duplicates,
     * order = as they appear in the area list).
     */
    public SequencedSet<String> areaLabels() {
        SequencedSet<String> labels = new LinkedHashSet<>();
        for (Area area : areas) {
            labels.add(area.label());
        }
        return labels;
    }

    /**
     * Map of {@code id -> Aircraft} in insertion order (a {@code SequencedMap}), built fresh
     * from the stored aircraft list — the internal state is never mutated.
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

package dev.bottega.jdkfeatures.flightcontrol.step02;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.random.RandomGenerator;

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

    public String describe(Area area) {
        return switch (area) {
            case Circle c -> "circle:" + c.label() + " r=" + c.radius();
            case Polygon p -> "polygon:" + p.label() + " v=" + p.vertices().size();
        };
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

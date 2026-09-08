package dev.bottega.jdkfeatures.flightcontrol.step07.domain;

import java.util.Map;

/**
 * Classifies aircraft threat levels using <b>pattern matching for switch</b> (JEP 441):
 * a switch <i>expression</i> over a type pattern + {@code when} guards + an explicit
 * {@code case null}. Pure domain logic — no networking, no I/O.
 *
 * <p>The rules are parameterized: {@code alarmMinSpeed} plus the sector (a {@link Circle},
 * i.e. the control zone). An aircraft is {@code ALARM} when it is fast <i>and</i> inside the
 * sector; {@code SECTOR} when inside but not over the alarm speed; {@code NORMAL} otherwise;
 * {@code UNKNOWN} for {@code null}.</p>
 */
public final class ThreatClassifier {

    private final double alarmMinSpeed;
    private final Circle sector;

    public ThreatClassifier(double alarmMinSpeed, Circle sector) {
        this.alarmMinSpeed = alarmMinSpeed;
        this.sector = sector;
    }

    public double alarmMinSpeed() {
        return alarmMinSpeed;
    }

    public Circle sector() {
        return sector;
    }

    /**
     * Exhaustive switch expression over {@link Aircraft} (a final record): the type pattern
     * {@code case Aircraft a} covers every non-null value, {@code case null} the rest — so no
     * {@code default} is needed. Guards narrow the match: fast+inside → ALARM, inside → SECTOR,
     * otherwise → NORMAL.
     */
    public Category classify(Aircraft aircraft) {
        return switch (aircraft) {
            case null -> Category.UNKNOWN;
            case Aircraft a when speed(a) > alarmMinSpeed && inside(a.pos(), sector) -> Category.ALARM;
            case Aircraft a when inside(a.pos(), sector) -> Category.SECTOR;
            case Aircraft a -> Category.NORMAL;
        };
    }

    /** Euclidean length of the velocity vector (a scalar speed). */
    public static double speed(Aircraft a) {
        return Math.hypot(a.vel().dx(), a.vel().dy());
    }

    /**
     * Whether {@code p} lies inside {@code area} (a circle: distance from center <= radius).
     * Uses a record pattern (JEP 440) to destructure the circle; a non-circle area is never
     * a containment sector, so it returns {@code false}.
     */
    public static boolean inside(Point p, Area area) {
        if (area instanceof Circle(Point c, double r, String label, Map<String, String> props)) {
            double dx = p.x() - c.x();
            double dy = p.y() - c.y();
            return Math.hypot(dx, dy) <= r;
        }
        return false;
    }
}

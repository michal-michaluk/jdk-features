package dev.bottega.jdkfeatures.flightcontrol.step13.domain;

import java.util.Map;

/**
 * Classifies aircraft threat levels using **pattern matching for switch** (JEP 441):
 * a switch *expression* over a type pattern + `when` guards + an explicit `case null`.
 * Pure domain logic — no networking, no I/O.
 *
 * ## The rules
 *
 * The rules are parameterized by `alarmMinSpeed` plus the sector (a `Circle`, i.e. the
 * control zone). An aircraft is:
 *
 * - `ALARM` when it is fast **and** inside the sector;
 * - `SECTOR` when inside but not over the alarm speed;
 * - `NORMAL` otherwise;
 * - `UNKNOWN` for `null`.
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
     * `case Aircraft a` covers every non-null value, `case null` the rest — so no `default` is
     * needed. Guards narrow the match:
     *
     * ```text
     * fast   && inside -> ALARM
     * inside           -> SECTOR
     * otherwise        -> NORMAL
     * null             -> UNKNOWN
     * ```
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
     * Whether `p` lies inside `area` (a circle: distance from center <= radius). Uses a record
     * pattern (JEP 440) to destructure the circle; a non-circle area is never a containment
     * sector, so it returns `false`.
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

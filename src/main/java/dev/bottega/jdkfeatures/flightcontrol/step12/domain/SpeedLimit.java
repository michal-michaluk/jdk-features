package dev.bottega.jdkfeatures.flightcontrol.step12.domain;

import java.util.Objects;

/**
 * JEP 513 — Flexible Constructor Bodies (final in JDK 25/26).
 *
 * <p>A <b>domain</b> value object: the maximum allowed speed for a controlled sector.
 * Its constructor demonstrates JEP 513 — statements (argument validation, derived-value
 * computation and final-field assignment) run <b>before</b> the explicit
 * {@code super(...)} invocation. Historically a constructor body had to begin with
 * {@code super(...)} or {@code this(...)}, so such work could only happen after the
 * superclass was constructed.</p>
 *
 * <p>Constructor contract (all enforced <b>before</b> {@code super(...)}):</p>
 * <ul>
 *   <li>{@code label} must be non-{@code null} → {@link NullPointerException};</li>
 *   <li>{@code label} must be non-blank → {@link IllegalArgumentException};</li>
 *   <li>the requested max speed is <b>clamped</b> into {@code [0, ABSOLUTE_MAX]}
 *       (negative → {@code 0}, above {@code ABSOLUTE_MAX} → {@code ABSOLUTE_MAX});</li>
 *   <li>the clamped value and the normalised (trimmed / upper-cased) label are derived
 *       and only then forwarded to {@code super(...)}.</li>
 * </ul>
 *
 * @see <a href="https://openjdk.org/jeps/513">JEP 513</a>
 */
public final class SpeedLimit extends Limit {

    /** Hard upper bound of any sector speed limit (knots). */
    public static final double ABSOLUTE_MAX = 1000.0;

    private final String label;
    private final double effectiveMax;

    /**
     * Validates and derives values <b>before</b> {@code super(...)} (JEP 513).
     *
     * @throws NullPointerException     if {@code label} is {@code null}
     * @throws IllegalArgumentException if {@code label} is blank
     */
    public SpeedLimit(String label, double requestedMaxSpeed) {
        // (a) validate arguments BEFORE super(...) — JEP 513 makes this legal
        Objects.requireNonNull(label, "label must not be null");            // -> NullPointerException
        if (label.isBlank()) {
            throw new IllegalArgumentException("label must not be blank");  // -> IllegalArgumentException
        }
        // (b) derive a value before super(...): clamp the requested max into [0, ABSOLUTE_MAX]
        double derived = clampMax(requestedMaxSpeed);
        // (c) assign final fields before super(...)
        this.label = label.trim().toUpperCase();
        this.effectiveMax = derived;
        // (d) finally the explicit constructor invocation
        super("speed");
    }

    /** Delegating constructor — forwards to the validated two-argument one. */
    public SpeedLimit(String label) {
        this(label, 0.0);
    }

    /** Clamps {@code requested} into {@code [0, ABSOLUTE_MAX]}. */
    public static double clampMax(double requested) {
        return Math.max(0.0, Math.min(ABSOLUTE_MAX, requested));
    }

    public String label() {
        return label;
    }

    /** The clamped, effective maximum speed applied by this limit. */
    public double effectiveMax() {
        return effectiveMax;
    }

    /** Whether a given scalar speed exceeds this limit. */
    public boolean exceededBy(double speed) {
        return speed > effectiveMax;
    }
}

package dev.bottega.jdkfeatures.flightcontrol.step16.domain;

/**
 * An immutable 2D velocity vector.
 *
 * `dx` and `dy` are the per-tick displacement on x and on y. The Euclidean length
 * `Math.hypot(dx, dy)` is the scalar speed used by {@link ThreatClassifier}.
 */
public record Velocity(double dx, double dy) {
}

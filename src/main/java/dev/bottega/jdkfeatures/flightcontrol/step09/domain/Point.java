package dev.bottega.jdkfeatures.flightcontrol.step09.domain;

/**
 * An immutable 2D Cartesian point.
 *
 * `x` and `y` are the coordinates. Used for aircraft positions, circle centers and area vertices.
 */
public record Point(double x, double y) {
}

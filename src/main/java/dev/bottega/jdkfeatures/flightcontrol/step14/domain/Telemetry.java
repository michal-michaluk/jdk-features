package dev.bottega.jdkfeatures.flightcontrol.step14.domain;

import java.util.List;
import java.util.stream.Gatherers;

/**
 * Aggregates a numeric telemetry stream with **Stream Gatherers** (JEP 485).
 *
 * <p>The readings are stored as an immutable list ({@code List.copyOf}). Each method drives the
 * stream through `Stream.gather(...)` with a built-in {@link Gatherers} gatherer —
 * no hand-written reducer or mutable accumulator is needed. Pure domain logic (no I/O, no
 * networking).</p>
 *
 * ## What each operation does
 *
 * - `windowFixed(n)` — non-overlapping chunks of size `n`; the last chunk may be shorter.
 * - `windowSliding(n)` — overlapping windows that slide by one element (length `n`, except the
 *   tail if the stream is shorter than `n`).
 * - `foldSum()` — folds the whole stream into a single value (the sum) via `Gatherers.fold`.
 * - `foldBounds()` — folds the whole stream into a single {@link Bounds} (min/max / bounding box)
 *   via `Gatherers.fold`, carrying the running min and max as the accumulator. An empty stream
 *   yields `Bounds(0, 0)`.
 */
public final class Telemetry {

    private final List<Integer> readings;

    public Telemetry(List<Integer> readings) {
        this.readings = List.copyOf(readings);
    }

    public List<Integer> readings() {
        return readings;
    }

    /** Chunks of `n` consecutive readings; the last chunk may be shorter than `n`. */
    public List<List<Integer>> windowFixed(int n) {
        return readings.stream()
                .gather(Gatherers.windowFixed(n))
                .toList();
    }

    /** Overlapping windows of length `n`, sliding by one reading each step. */
    public List<List<Integer>> windowSliding(int n) {
        return readings.stream()
                .gather(Gatherers.windowSliding(n))
                .toList();
    }

    /** Sum of all readings, folded with an initial value of 0. */
    public int foldSum() {
        return readings.stream()
                .gather(Gatherers.fold(() -> 0, Integer::sum))
                .findFirst()
                .orElse(0);
    }

    /**
     * Minimum and maximum of all readings, folded into a single {@link Bounds}. The accumulator
     * carries the running min/max (initialised to {@link Integer#MAX_VALUE}/{@link Integer#MIN_VALUE}
     * so the first reading wins). An empty stream returns `Bounds(0, 0)` — otherwise `Gatherers.fold`
     * would emit the initial sentinel, which is a surprising "bounds of nothing".
     */
    public Bounds foldBounds() {
        if (readings.isEmpty()) {
            return new Bounds(0, 0);
        }
        return readings.stream()
                .gather(Gatherers.fold(
                        () -> new Bounds(Integer.MAX_VALUE, Integer.MIN_VALUE),
                        (acc, x) -> new Bounds(Math.min(acc.min(), x), Math.max(acc.max(), x))))
                .findFirst()
                .orElse(new Bounds(0, 0));
    }

    /** The 8-value telemetry sample from the step README: {@code [10, 20, 30, 40, 50, 60, 70, 80]}. */
    public static Telemetry sample() {
        return new Telemetry(List.of(10, 20, 30, 40, 50, 60, 70, 80));
    }

    /** An immutable min/max pair (the bounding box of a telemetry window or stream). */
    public record Bounds(int min, int max) {
    }
}

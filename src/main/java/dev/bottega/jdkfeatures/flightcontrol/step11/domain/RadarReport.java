package dev.bottega.jdkfeatures.flightcontrol.step11.domain;

import java.util.Map;

/**
 * Immutable result of a parallel radar sweep.
 *
 * ## Fields
 *
 * - `counts` — a per-{@link Category} count snapshot, keyed by `Category` (all values present).
 * - `processed` — the total number of aircraft processed.
 * - `allVirtual` — `true` if every worker thread was a virtual thread.
 */
public record RadarReport(Map<Category, Integer> counts, int processed, boolean allVirtual) {
}

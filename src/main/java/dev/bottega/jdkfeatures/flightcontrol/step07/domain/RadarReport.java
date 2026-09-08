package dev.bottega.jdkfeatures.flightcontrol.step07.domain;

import java.util.Map;

/**
 * Immutable result of a parallel radar sweep: the number of aircraft processed and a
 * per-{@link Category} count snapshot, plus whether every worker thread was a virtual thread.
 */
public record RadarReport(Map<Category, Integer> counts, int processed, boolean allVirtual) {
}

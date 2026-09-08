package dev.bottega.jdkfeatures.flightcontrol.step15.domain;

/**
 * Result of scanning a single aircraft inside a structured-concurrency pass
 * (see {@link SectorScanner}): which {@link Category} it was classified into and its speed.
 *
 * <p>Immutable value object; a list of these is what {@link SectorScanner#scanAll()} aggregates
 * from the forked subtasks.</p>
 */
public record ScanReport(String aircraftId, Category category, double speed) {
}

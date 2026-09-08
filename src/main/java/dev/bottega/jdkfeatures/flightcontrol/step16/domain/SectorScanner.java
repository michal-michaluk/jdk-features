package dev.bottega.jdkfeatures.flightcontrol.step16.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;

/**
 * JEP 525 — Structured Task Scope (preview).
 *
 * <p>A <b>domain</b> service that scans every aircraft of the sector <b>concurrently</b>,
 * one structured task ({@link Subtask}) per aircraft, and aggregates the individual
 * {@link ScanReport}s. It is the <em>structured</em> counterpart of the hand-rolled
 * {@link Radar} (JEP 444 virtual threads): instead of {@code Thread.start()} + {@code join()},
 * every task is forked into an <b>owned</b> {@link StructuredTaskScope} and joined back before
 * the scope exits — so no task can outlive the method and failures are handled in one place.</p>
 *
 * <p>Two supported strategies:</p>
 *
 * <ul>
 *   <li>{@link #scanAll()} — fails <b>fast</b> on the first failing subtask and aggregates the
 *       successful {@link ScanReport}s via {@link Subtask#get()}.</li>
 *   <li>{@link #scanFailFast()} — the same fail-fast join, but turns a failed subtask into a
 *       domain {@link SectorScanException} instead of leaking the concurrency exception.</li>
 * </ul>
 *
 * <p>Pure domain logic: it uses only {@code java.util.concurrent.*} — no I/O, no networking.</p>
 */
public final class SectorScanner {

    private final List<Aircraft> aircraft;
    private final ThreatClassifier classifier;

    public SectorScanner(List<Aircraft> aircraft, ThreatClassifier classifier) {
        this.aircraft = List.copyOf(aircraft);
        this.classifier = classifier;
    }

    /**
     * Forks one task per aircraft into a {@code awaitAllSuccessfulOrThrow} scope, joins, and
     * aggregates the successful {@link ScanReport}s from the finished subtasks.
     *
     * <p>All subtasks have {@link Subtask.State#SUCCESS} on the happy path; only results from
     * such subtasks are collected.</p>
     *
     * @return the per-aircraft scan reports, in the order the aircraft were supplied
     * @throws InterruptedException if the calling thread is interrupted while joining
     */
    public List<ScanReport> scanAll() throws InterruptedException {
        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<ScanReport>awaitAllSuccessfulOrThrow())) {
            List<Subtask<ScanReport>> subtasks = new ArrayList<>();
            for (Aircraft a : aircraft) {
                subtasks.add(scope.fork(() -> scan(a)));
            }
            scope.join();
            List<ScanReport> reports = new ArrayList<>();
            for (Subtask<ScanReport> subtask : subtasks) {
                if (subtask.state() == Subtask.State.SUCCESS) {
                    reports.add(subtask.get());
                }
            }
            return List.copyOf(reports);
        }
    }

    /**
     * Runs the same fail-fast scan, but a failing subtask is surfaced as a domain
     * {@link SectorScanException} rather than a raw
     * {@link StructuredTaskScope.FailedException}.
     *
     * @throws SectorScanException if any forked scan task fails
     * @throws InterruptedException if the calling thread is interrupted while joining
     */
    public void scanFailFast() throws InterruptedException {
        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<ScanReport>awaitAllSuccessfulOrThrow())) {
            for (Aircraft a : aircraft) {
                scope.fork(() -> scan(a));
            }
            scope.join();
        } catch (StructuredTaskScope.FailedException e) {
            throw new SectorScanException("sector scan failed: " + e.getCause(), e);
        }
    }

    private ScanReport scan(Aircraft a) {
        Category category = classifier.classify(a);
        double speed = ThreatClassifier.speed(a);
        return new ScanReport(a.id(), category, speed);
    }
}

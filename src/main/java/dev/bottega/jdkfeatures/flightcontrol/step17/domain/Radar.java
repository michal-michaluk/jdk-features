package dev.bottega.jdkfeatures.flightcontrol.step17.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A parallel radar sweep: classifies every aircraft **concurrently** on a **virtual thread**
 * per task (JEP 444) and safely aggregates the per-{@link Category} counts.
 *
 * ## Pure domain logic
 *
 * No I/O, no networking. Aggregation is collision-free: each worker atomically increments
 * an `AtomicInteger` under a `ConcurrentHashMap` keyed by {@link Category}, so no update is
 * ever lost. The caller `join()`s every worker before the snapshot is taken.
 *
 * ## Determinism
 *
 * The report is immutable and deterministic on the same input (the counts are keyed by
 * `Category`, not by finish order).
 */
public final class Radar {

    private final Airspace airspace;
    private final ThreatClassifier classifier;

    public Radar(Airspace airspace, ThreatClassifier classifier) {
        this.airspace = airspace;
        this.classifier = classifier;
    }

    /**
     * Runs one virtual thread per aircraft, classifies it, aggregates into a thread-safe
     * accumulator, then `join()`s all workers and returns an immutable snapshot.
     *
     * ## What it produces
     *
     * ```text
     * processed : total number of aircraft
     * counts    : per-Category counts (all Categories present, missing = 0)
     * allVirtual: true if every worker thread was a virtual thread
     * ```
     *
     * @throws InterruptedException if any worker thread is interrupted while joining
     */
    public RadarReport report() throws InterruptedException {
        Map<Category, AtomicInteger> counts = new ConcurrentHashMap<>();
        List<Thread> workers = new ArrayList<>();

        for (Aircraft aircraft : airspace.aircraft()) {
            Thread worker = Thread.ofVirtual()
                    .name("radar-" + aircraft.id())
                    .start(() -> {
                        Category category = classifier.classify(aircraft);
                        counts.computeIfAbsent(category, k -> new AtomicInteger()).incrementAndGet();
                    });
            workers.add(worker);
        }

        boolean allVirtual = true;
        for (Thread worker : workers) {
            worker.join();
            allVirtual &= worker.isVirtual();
        }

        Map<Category, Integer> snapshot = new LinkedHashMap<>();
        for (Category category : Category.values()) {
            AtomicInteger value = counts.get(category);
            snapshot.put(category, value == null ? 0 : value.get());
        }
        return new RadarReport(Map.copyOf(snapshot), airspace.aircraft().size(), allVirtual);
    }
}

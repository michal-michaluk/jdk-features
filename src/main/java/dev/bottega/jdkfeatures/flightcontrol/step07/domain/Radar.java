package dev.bottega.jdkfeatures.flightcontrol.step07.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A parallel radar sweep: classifies every aircraft <b>concurrently</b> on a <b>virtual
 * thread</b> per task (JEP 444) and safely aggregates the per-{@link Category} counts.
 *
 * <p>Pure domain logic (no I/O, no networking). Aggregation is collision-free: each worker
 * atomically increments an {@link AtomicInteger} under a {@link ConcurrentHashMap} keyed by
 * {@link Category}, so no update is ever lost. The caller {@code join()}s every worker before
 * the snapshot is taken. The report is immutable and deterministic on the same input.</p>
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
     * accumulator, then {@code join()}s all workers and returns an immutable snapshot.
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

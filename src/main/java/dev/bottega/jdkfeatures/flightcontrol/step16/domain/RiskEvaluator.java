package dev.bottega.jdkfeatures.flightcontrol.step16.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * JEP 526 — Lazy Constants (preview).
 *
 * <p>A <b>domain</b> evaluator that holds an <b>expensive</b> per-aircraft risk projection
 * inside a {@link LazyConstant}: the projection is a map of {@code aircraftId -> risk score}.
 * The value is only computed <b>once</b>, on the first {@link #projection()} call, and cached
 * for every subsequent call — the supplier runs exactly that one time.</p>
 *
 * <p>Useful for the expensive projection that Flight Control should not build until someone
 * actually asks for it (e.g. an infrequently-rendered risk overlay).</p>
 *
 * <p>Pure domain logic: it uses only {@code java.lang.LazyConstant} + {@code java.util.*}
 * (no I/O, no networking).</p>
 */
public final class RiskEvaluator {

    private final List<Aircraft> aircraft;
    private final LazyConstant<Map<String, Double>> projection;

    private RiskEvaluator(List<Aircraft> aircraft, LazyConstant<Map<String, Double>> projection) {
        this.aircraft = aircraft;
        this.projection = projection;
    }

    /** Builds the evaluator with the default risk projection (speed-based). */
    public RiskEvaluator(List<Aircraft> aircraft) {
        List<Aircraft> snapshot = List.copyOf(aircraft);
        this(snapshot, LazyConstant.of(() -> computeRisk(snapshot)));
    }

    /**
     * Builds the evaluator with an explicit supplier — used to verify that the expensive value
     * is computed exactly once (e.g. a counting supplier in a test).
     */
    public RiskEvaluator(List<Aircraft> aircraft, Supplier<Map<String, Double>> supplier) {
        this(List.copyOf(aircraft), LazyConstant.of(Objects.requireNonNull(supplier, "supplier must not be null")));
    }

    /** The cached projection; the supplier runs once on the first call. */
    public Map<String, Double> projection() {
        return projection.get();
    }

    /** Returns the projection if already computed, otherwise the fallback (no compute triggered). */
    public Map<String, Double> projectionOr(Map<String, Double> fallback) {
        return projection.orElse(fallback);
    }

    /** Whether the expensive projection has been computed yet (false -> true after first {@link #projection()}). */
    public boolean isInitialized() {
        return projection.isInitialized();
    }

    /** The risk score of a given aircraft id (0 if the id is unknown). */
    public double riskFor(String id) {
        return projection().getOrDefault(id, 0.0);
    }

    public List<Aircraft> aircraft() {
        return aircraft;
    }

    /** Default risk = the aircraft's scalar speed. Higher speed -> higher risk. */
    private static Map<String, Double> computeRisk(List<Aircraft> aircraft) {
        Map<String, Double> risk = new LinkedHashMap<>();
        for (Aircraft a : aircraft) {
            risk.put(a.id(), ThreatClassifier.speed(a));
        }
        return Map.copyOf(risk);
    }
}

package dev.bottega.jdkfeatures.flightcontrol.step17.domain;

/**
 * JEP 530 — Primitive Type Patterns in switch (preview).
 *
 * <p>A <b>domain</b> classifier that buckets a numeric speed into a {@link SpeedBucket} using a
 * switch <em>expression</em> over the primitive type patterns {@code case int}, {@code case long}
 * and {@code case double}, combined with {@code when} guards, plus {@code case null} and a
 * {@code default} branch.</p>
 *
 * <p>Because the selector is a boxed {@link Number}, each primitive type pattern matches the
 * <em>exact</em> runtime boxed type — an {@code Integer} falls onto {@code case int}, a
 * {@code Long} onto {@code case long}, a {@code Double} onto {@code case double} — while any
 * other {@code Number} subtype (e.g. {@code Float}) is caught by {@code default}.</p>
 *
 * <p>The buckets apply the same thresholds regardless of the primitive width:</p>
 *
 * <pre>{@code
 * SLOW    : speed < 150
 * CRUISE  : 150 <= speed < 300
 * FAST    : speed >= 300
 * UNKNOWN : null or a Number subtype we do not recognise
 * }</pre>
 *
 * <p>Pure domain logic: no I/O, no networking.</p>
 */
public final class SpeedClassifier {

    private SpeedClassifier() {
    }

    /** Buckets a raw numeric speed into {@link SpeedBucket} using primitive type patterns. */
    public static SpeedBucket classifyBySpeed(Number speed) {
        return switch (speed) {
            case null -> SpeedBucket.UNKNOWN;
            case int i when i < 150 -> SpeedBucket.SLOW;
            case int i when i < 300 -> SpeedBucket.CRUISE;
            case int i -> SpeedBucket.FAST;
            case long l when l < 150 -> SpeedBucket.SLOW;
            case long l when l < 300 -> SpeedBucket.CRUISE;
            case long l -> SpeedBucket.FAST;
            case double d when d < 150 -> SpeedBucket.SLOW;
            case double d when d < 300 -> SpeedBucket.CRUISE;
            case double d -> SpeedBucket.FAST;
            default -> SpeedBucket.UNKNOWN;
        };
    }

    /** Buckets the scalar speed of an aircraft (see {@link ThreatClassifier#speed}). */
    public static SpeedBucket classify(Aircraft aircraft) {
        return classifyBySpeed(ThreatClassifier.speed(aircraft));
    }
}

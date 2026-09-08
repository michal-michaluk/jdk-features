package dev.bottega.jdkfeatures.flightcontrol.step17.domain;

/**
 * Speed bucket produced by {@link SpeedClassifier} for a raw (numeric) speed value.
 *
 * | Bucket | Meaning |
 * |--------|---------|
 * | `SLOW`    | speed &lt; 150 |
 * | `CRUISE`  | 150 &le; speed &lt; 300 |
 * | `FAST`    | speed &ge; 300 |
 * | `UNKNOWN` | non-numeric / <code>null</code> speed |
 */
public enum SpeedBucket {
    SLOW, CRUISE, FAST, UNKNOWN
}

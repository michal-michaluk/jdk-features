package dev.bottega.jdkfeatures.flightcontrol.step13.domain;

/**
 * Sealed hierarchy of static airspace areas: exactly a circle or a polygon.
 *
 * Sealing means the compiler knows the closed set of subtypes, which powers the
 * exhaustive record-pattern switch in {@link Airspace#describe(Area)} (no `default` needed).
 *
 * ## Common fields
 *
 * - `label` — area name shown on the radar (e.g. `CTR`, `TMA`).
 * - `props` — metadata map; `type`/`sector` describe the area kind
 *   (e.g. `control` = CTR, `terminal` = TMA).
 */
public sealed interface Area permits Circle, Polygon {

    String label();

    java.util.Map<String, String> props();
}

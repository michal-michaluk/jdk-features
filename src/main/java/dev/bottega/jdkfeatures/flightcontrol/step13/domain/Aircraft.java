package dev.bottega.jdkfeatures.flightcontrol.step13.domain;

/**
 * Immutable moving aircraft: point + velocity vector + label/callsign.
 *
 * ## What an aircraft is
 *
 * - `id` — unique, stable key (used by `Airspace.aircraftById()`).
 * - `label` — human-readable tag shown on the radar (e.g. `FOX`, `ECHO`).
 * - `callsign` — alphanumeric callsign (e.g. `FOX123`).
 * - `pos` — current position as a {@link Point}.
 * - `vel` — current velocity as a {@link Velocity} (per simulation tick).
 *
 * The record is immutable by construction: `withPosition()` returns a **new** aircraft and
 * leaves the original untouched.
 */
public record Aircraft(String id, String label, String callsign, Point pos, Velocity vel) {

    /**
     * Returns a copy of this aircraft with a new position (same id, label, callsign and
     * velocity). The receiver is unchanged — the domain never mutates an aircraft in place.
     */
    public Aircraft withPosition(Point newPosition) {
        return new Aircraft(id, label, callsign, newPosition, vel);
    }
}

package dev.bottega.jdkfeatures.flightcontrol.step06.domain;

/** Immutable moving aircraft: point + velocity vector + label/callsign. */
public record Aircraft(String id, String label, String callsign, Point pos, Velocity vel) {

    public Aircraft withPosition(Point newPosition) {
        return new Aircraft(id, label, callsign, newPosition, vel);
    }
}

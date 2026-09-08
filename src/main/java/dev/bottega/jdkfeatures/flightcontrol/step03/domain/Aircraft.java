package dev.bottega.jdkfeatures.flightcontrol.step03.domain;

public record Aircraft(String id, String label, String callsign, Point pos, Velocity vel) {

    public Aircraft withPosition(Point newPosition) {
        return new Aircraft(id, label, callsign, newPosition, vel);
    }

    public Aircraft withVelocity(Velocity newVelocity) {
        return new Aircraft(id, label, callsign, pos, newVelocity);
    }

    public Aircraft move() {
        return withPosition(pos.move(vel));
    }
}

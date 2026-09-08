package dev.bottega.jdkfeatures.flightcontrol.step11.domain;

public record Point(double x, double y) {

    public Point move(Velocity vel) {
        return new Point(x + vel.dx(), y + vel.dy());
    }
}

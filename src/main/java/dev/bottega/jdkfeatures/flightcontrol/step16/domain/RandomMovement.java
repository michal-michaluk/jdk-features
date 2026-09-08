package dev.bottega.jdkfeatures.flightcontrol.step16.domain;

import java.util.Objects;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public final class RandomMovement {

    private final double maxTurnDeg;
    private final double maxSpeedDelta;
    private final String algorithm;
    private final long seed;

    RandomMovement(double maxTurnDeg, double maxSpeedDelta, String algorithm, long seed) {
        this.maxTurnDeg = maxTurnDeg;
        this.maxSpeedDelta = maxSpeedDelta;
        this.algorithm = algorithm;
        this.seed = seed;
    }

    public double maxTurnDeg() {
        return maxTurnDeg;
    }

    public double maxSpeedDelta() {
        return maxSpeedDelta;
    }

    public RandomGenerator generator() {
        return RandomGeneratorFactory.of(algorithm).create(seed);
    }

    public Velocity apply(Velocity current, RandomGenerator rng) {
        double speed = Math.hypot(current.dx(), current.dy());
        double heading = Math.atan2(current.dy(), current.dx());
        double turnDeg = (rng.nextDouble() - 0.5) * 2 * maxTurnDeg;
        double delta = (rng.nextDouble() - 0.5) * 2 * maxSpeedDelta;
        double newSpeed = Math.max(0, speed + delta);
        double newHeading = heading + Math.toRadians(turnDeg);
        return new Velocity(Math.cos(newHeading) * newSpeed, Math.sin(newHeading) * newSpeed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RandomMovement r)) {
            return false;
        }
        return Double.compare(r.maxTurnDeg, maxTurnDeg) == 0
                && Double.compare(r.maxSpeedDelta, maxSpeedDelta) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxTurnDeg, maxSpeedDelta);
    }

    @Override
    public String toString() {
        return "RandomMovement{maxTurnDeg=" + maxTurnDeg + ", maxSpeedDelta=" + maxSpeedDelta
                + ", algorithm=" + algorithm + ", seed=" + seed + "}";
    }
}

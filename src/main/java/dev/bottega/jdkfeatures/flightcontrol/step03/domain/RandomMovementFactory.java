package dev.bottega.jdkfeatures.flightcontrol.step03.domain;

public final class RandomMovementFactory {

    private static final String ALGORITHM = "L64X128MixRandom";
    private static final long SEED = 42;
    private static final double DEFAULT_MAX_TURN_DEG = 5;
    private static final double DEFAULT_MAX_SPEED_DELTA = 2;

    private RandomMovementFactory() {
    }

    public static RandomMovement defaults() {
        return of(DEFAULT_MAX_TURN_DEG, DEFAULT_MAX_SPEED_DELTA);
    }

    public static RandomMovement of(double maxTurnDeg, double maxSpeedDelta) {
        return new RandomMovement(maxTurnDeg, maxSpeedDelta, ALGORITHM, SEED);
    }
}

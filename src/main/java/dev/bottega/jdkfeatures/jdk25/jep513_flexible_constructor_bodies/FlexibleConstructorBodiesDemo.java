package dev.bottega.jdkfeatures.jdk25.jep513_flexible_constructor_bodies;

/**
 * JEP 513 — Flexible Constructor Bodies (JDK 25).
 *
 * <p>Historically a constructor body had to start with either {@code super(...)} or
 * {@code this(...)}. JEP 513 allows arbitrary statements <em>before</em> the explicit
 * constructor invocation, as long as they do not use {@code this}/{@code super} in a way
 * that reads the not-yet-initialised instance. This enables argument validation and
 * final-field initialisation to happen before delegating to the superclass.
 */
public final class FlexibleConstructorBodiesDemo {

    private FlexibleConstructorBodiesDemo() {
        // no instances
    }

    /** @return the canonical feature description. */
    public static String describe() {
        return "JEP 513 — Flexible Constructor Bodies (JDK 25)";
    }

    /** Runs every illustrative case and returns a multi-line result. */
    public static String run() {
        Car car = new Car("  bmw  ", 300);
        Car defaultCar = new Car("Audi");
        return String.join("\n",
                describe(),
                "car:        " + car.describe(),
                "defaultCar: " + defaultCar.describe(),
                "clamped:    " + clampSpeed(-50));
    }

    /** Entry point that prints {@link #run()}. */
    public static void main(String[] args) {
        System.out.println(run());
    }

    /**
     * Validates and normalises a brand name. JEP 513 lets a constructor call this
     * style of work before {@code super(...)}.
     *
     * @throws IllegalArgumentException if {@code raw} is {@code null} or blank
     */
    public static String normalizeBrand(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("brand must not be null or blank");
        }
        return raw.trim().toUpperCase();
    }

    /** Clamps the requested speed to a valid positive lower bound (derived value). */
    public static int clampSpeed(int requested) {
        return Math.max(1, requested);
    }

    /** Base class whose constructor accepts the {@code type} that a subclass computes. */
    static class Vehicle {
        private final String type;

        Vehicle(String type) {
            this.type = type;
        }

        String type() {
            return type;
        }

        String describe() {
            return "Vehicle(type=" + type + ")";
        }
    }

    /**
     * Demonstrates the legal form of JEP 513: statements before {@code super(...)}
     * that only touch locals/parameters and static calls (never read {@code this} or its
     * fields), including assigning FINAL fields and computing a derived value.
     */
    static final class Car extends Vehicle {
        private final String brand;
        private final int topSpeed;

        /**
         * Statements before {@code super(...)} — all legal in JDK 25:
         * argument validation, final-field assignment, derived-value computation.
         */
        Car(String brand, int requestedTopSpeed) {
            // 1) validate an argument (would otherwise need to happen after the super call)
            if (brand == null || brand.isBlank()) {
                throw new IllegalArgumentException("brand must not be null or blank");
            }
            // 2) compute a derived value from parameters and static helpers only (no `this` read)
            String normalizedBrand = normalizeBrand(brand);
            int speed = clampSpeed(requestedTopSpeed);
            // 3) assign FINAL fields before the superclass constructor runs
            this.brand = normalizedBrand;
            this.topSpeed = speed;
            // 4) finally the explicit constructor invocation
            super("Car");
        }

        /**
         * Explicit {@code this(...)} constructor invocation choice — again preceded by a
         * legal statement (a validated parameter guard) because of JEP 513.
         */
        Car(String brand) {
            String safeBrand = brand == null || brand.isBlank() ? "Toyota" : brand;
            this(safeBrand, 180);
        }

        String brand() {
            return brand;
        }

        int topSpeed() {
            return topSpeed;
        }

        @Override
        String describe() {
            return "Car(brand=" + brand + ", topSpeed=" + topSpeed + ")";
        }
    }

    /*
     * What is STILL illegal before super(...)/this(...) in JDK 25? Reading `this` or an
     * instance field/method is forbidden, because the instance is not yet initialised.
     *
     * // Illegal — reads the `this` receiver / an instance field before the super call:
     * Car(String brand, int topSpeed) {
     *     this.topSpeed = clampSpeed(topSpeed);                  // this.brand is not yet set
     *     super(this.topSpeed > 0 ? "Car" : this.type());        // calling this.type() reads instance state
     * }
     *
     * // Illegal — forwarding to this(...) based on an instance field:
     * Car(String brand) {
     *     this(brand, this.clampSpeed(200));                     // instance-method read before this(...)
     * }
     *
     * The above simply do not compile; JEP 513 only relaxes the "must be first statement"
     * rule, it does not let a constructor observe the half-built instance.
     */
}

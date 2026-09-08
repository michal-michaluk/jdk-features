package dev.bottega.jdkfeatures.jdk25.jep513_flexible_constructor_bodies;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FlexibleConstructorBodiesDemoTest {

    @Test
    void describeMatchesConvention() {
        String d = FlexibleConstructorBodiesDemo.describe();
        assertTrue(d.startsWith("JEP 513 — "));
        assertTrue(d.contains("Flexible Constructor Bodies"));
        assertTrue(d.contains("(JDK 25)"));
    }

    @Test
    void runContainsConstructedAndDerivedValues() {
        String out = FlexibleConstructorBodiesDemo.run();
        assertTrue(out.contains("car:        Car(brand=BMW, topSpeed=300)"));
        assertTrue(out.contains("defaultCar: Car(brand=AUDI, topSpeed=180)"));
        assertTrue(out.contains("clamped:    " + 1));
    }

    @Test
    void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() -> FlexibleConstructorBodiesDemo.main(new String[0]));
    }

    @Test
    void normalizeBrandTrimsAndUpperCases() {
        assertEquals("BMW", FlexibleConstructorBodiesDemo.normalizeBrand("  bmw  "));
        assertEquals("AUDI", FlexibleConstructorBodiesDemo.normalizeBrand("audi"));
    }

    @Test
    void normalizeBrandRejectsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> FlexibleConstructorBodiesDemo.normalizeBrand(null));
    }

    @Test
    void normalizeBrandRejectsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> FlexibleConstructorBodiesDemo.normalizeBrand("   "));
    }

    @Test
    void clampSpeedForcesPositiveLowerBound() {
        assertEquals(1, FlexibleConstructorBodiesDemo.clampSpeed(-50));
        assertEquals(42, FlexibleConstructorBodiesDemo.clampSpeed(42));
    }

    @Test
    void constructorValidatesBeforeSuper() {
        assertThrows(IllegalArgumentException.class,
                () -> new FlexibleConstructorBodiesDemo.Car("  ", 200));
        assertThrows(IllegalArgumentException.class,
                () -> new FlexibleConstructorBodiesDemo.Car(null, 200));
    }

    @Test
    void constructorAssignsFinalFieldsBeforeSuper() {
        FlexibleConstructorBodiesDemo.Car car = new FlexibleConstructorBodiesDemo.Car("bmw", 250);
        assertEquals("BMW", car.brand());
        assertEquals(250, car.topSpeed());
        assertTrue(car.describe().equals("Car(brand=BMW, topSpeed=250)"));
    }

    @Test
    void constructorClampsDerivedValueBeforeSuper() {
        FlexibleConstructorBodiesDemo.Car car = new FlexibleConstructorBodiesDemo.Car("toyota", 0);
        assertEquals("TOYOTA", car.brand());
        assertEquals(1, car.topSpeed());
    }

    @Test
    void thisInvocationChoiceProvidesDefault() {
        FlexibleConstructorBodiesDemo.Car car = new FlexibleConstructorBodiesDemo.Car("volvo");
        assertEquals("VOLVO", car.brand());
        assertEquals(180, car.topSpeed());
    }

    @Test
    void thisInvocationChoiceFallsBackWhenBlank() {
        FlexibleConstructorBodiesDemo.Car car = new FlexibleConstructorBodiesDemo.Car(null);
        assertEquals("TOYOTA", car.brand());
        assertEquals(180, car.topSpeed());
    }

    @Test
    void thisInvocationChoiceNormalizesInput() {
        FlexibleConstructorBodiesDemo.Car car = new FlexibleConstructorBodiesDemo.Car("audi");
        assertEquals("AUDI", car.brand());
        assertEquals(180, car.topSpeed());
    }

    @Test
    void vehicleDescribeReflectsType() {
        FlexibleConstructorBodiesDemo.Vehicle v = new FlexibleConstructorBodiesDemo.Vehicle("plane");
        assertEquals("plane", v.type());
        assertTrue(v.describe().equals("Vehicle(type=plane)"));
    }
}

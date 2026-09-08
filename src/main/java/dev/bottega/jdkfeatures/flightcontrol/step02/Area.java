package dev.bottega.jdkfeatures.flightcontrol.step02;

/** Sealed hierarchy of static airspace areas: exactly a circle or a polygon. */
public sealed interface Area permits Circle, Polygon {

    String label();

    java.util.Map<String, String> props();
}

package dev.bottega.jdkfeatures.flightcontrol.step04.domain;

import java.util.Map;

public record Circle(Point center, double radius, String label, Map<String, String> props) implements Area {
}

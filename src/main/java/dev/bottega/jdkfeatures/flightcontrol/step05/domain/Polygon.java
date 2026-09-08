package dev.bottega.jdkfeatures.flightcontrol.step05.domain;

import java.util.List;
import java.util.Map;

public record Polygon(List<Point> vertices, String label, Map<String, String> props) implements Area {
}

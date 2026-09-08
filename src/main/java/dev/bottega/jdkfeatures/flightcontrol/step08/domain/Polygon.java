package dev.bottega.jdkfeatures.flightcontrol.step08.domain;

import java.util.List;
import java.util.Map;

/**
 * A polygonal area: an ordered list of vertices, a label and metadata.
 *
 * The `vertices` form the boundary of the zone; `label` and `props` carry the naming/metadata
 * shared by every {@link Area}.
 */
public record Polygon(List<Point> vertices, String label, Map<String, String> props) implements Area {
}

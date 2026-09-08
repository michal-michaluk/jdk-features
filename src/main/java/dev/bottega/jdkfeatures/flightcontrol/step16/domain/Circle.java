package dev.bottega.jdkfeatures.flightcontrol.step16.domain;

import java.util.Map;

/**
 * A circular control zone: a center {@link Point}, a radius, a label and metadata.
 *
 * `center` + `radius` define a disc; `label` and `props` carry the naming/metadata shared
 * by every {@link Area}. Used as the containment sector in {@link ThreatClassifier}.
 */
public record Circle(Point center, double radius, String label, Map<String, String> props) implements Area {
}

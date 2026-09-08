package dev.bottega.jdkfeatures.flightcontrol.step07.server;

import dev.bottega.jdkfeatures.flightcontrol.step07.domain.Airspace;
import dev.bottega.jdkfeatures.flightcontrol.step07.domain.Area;
import dev.bottega.jdkfeatures.flightcontrol.step07.domain.Circle;
import dev.bottega.jdkfeatures.flightcontrol.step07.domain.Polygon;

import java.util.stream.Collectors;

/**
 * Pure <b>serialization</b> of the domain to JSON, matching the viewer/HTTP contract.
 *
 * <p>Infrastructure-only: knows nothing about simulation, routing or HTTP. Builds the JSON
 * with readable text-block "templates" ({@code formatted()}) and escapes string values.</p>
 */
public final class JsonSerde {

    private JsonSerde() {
    }

    /** Escape a value for embedding inside a JSON string literal. */
    private static String jstr(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static String aircraftJson(Airspace as) {
        return as.aircraft().stream()
                .map(a -> """
                        { "id": "%s", "label": "%s", "callsign": "%s",
                          "pos": { "x": %s, "y": %s },
                          "vel": { "dx": %s, "dy": %s } }
                        """.formatted(jstr(a.id()), jstr(a.label()), jstr(a.callsign()),
                        a.pos().x(), a.pos().y(), a.vel().dx(), a.vel().dy()).trim())
                .collect(Collectors.joining(",", "[", "]"));
    }

    public static String areasJson(Airspace as) {
        return as.areas().stream()
                .map(JsonSerde::areaJson)
                .collect(Collectors.joining(",", "[", "]"));
    }

    public static String areaJson(Area area) {
        return switch (area) {
            case Circle c -> """
                    { "kind": "circle", "label": "%s",
                      "center": { "x": %s, "y": %s },
                      "radius": %s, "props": {} }
                    """.formatted(jstr(c.label()), c.center().x(), c.center().y(), c.radius()).trim();
            case Polygon p -> """
                    { "kind": "polygon", "label": "%s",
                      "vertices": [ %s ], "props": {} }
                    """.formatted(jstr(p.label()),
                    p.vertices().stream()
                            .map(v -> """
                                    { "x": %s, "y": %s }
                                    """.formatted(v.x(), v.y()).trim())
                            .collect(Collectors.joining(","))).trim();
        };
    }
}

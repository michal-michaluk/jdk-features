package dev.bottega.jdkfeatures.flightcontrol.step03.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.bottega.jdkfeatures.flightcontrol.step03.domain.Airspace;
import dev.bottega.jdkfeatures.flightcontrol.step03.domain.Area;
import dev.bottega.jdkfeatures.flightcontrol.step03.domain.Circle;
import dev.bottega.jdkfeatures.flightcontrol.step03.domain.Polygon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Thin HTTP <b>infrastructure</b> for the step-03 exercise (JEP 408 Simple Web Server).
 *
 * <p>Exposes the domain {@link Airspace} (from {@link Airspace#sample()}) over HTTP and
 * renders the SVG viewer. It carries <b>no simulation logic</b>: no scheduler, no bouncing,
 * no bounds. The only domain operation it performs is {@code airspace.step()} from the
 * {@code POST /tick} handler; everything else just serialises getters to JSON.</p>
 *
 * <p>Routes: {@code GET /aircraft}, {@code GET /areas}, {@code GET /} (static SVG view),
 * {@code POST /tick} (advances the model and returns the new /aircraft JSON).</p>
 */
public final class AirspaceServer {

    private final HttpServer server;
    private volatile Airspace airspace;

    private AirspaceServer(int port) throws IOException {
        this.airspace = Airspace.sample();

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/aircraft", ex -> respond(ex, aircraftJson(airspace), "application/json"));
        server.createContext("/areas", ex -> respond(ex, areasJson(airspace), "application/json"));
        server.createContext("/tick", this::tick);
        server.createContext("/", ex -> respond(ex, indexHtml(), "text/html"));
        server.start();
    }

    public static AirspaceServer start(int port) throws IOException {
        return new AirspaceServer(port);
    }

    public int port() {
        return server.getAddress().getPort();
    }

    public void stop() {
        server.stop(0);
    }

    /** Advances the model one step and returns the fresh {@code /aircraft} JSON. */
    private void tick(HttpExchange ex) throws IOException {
        airspace = airspace.step();
        respond(ex, aircraftJson(airspace), "application/json");
    }

    // ---- JSON building: text-block "templates" (readable) + formatted() + escaping. ----
    /** Escape a value for embedding inside a JSON string literal. */
    private static String jstr(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String aircraftJson(Airspace as) {
        return as.aircraft().stream()
                .map(a -> """
                        { "id": "%s", "label": "%s", "callsign": "%s",
                          "pos": { "x": %s, "y": %s },
                          "vel": { "dx": %s, "dy": %s } }
                        """.formatted(jstr(a.id()), jstr(a.label()), jstr(a.callsign()),
                        a.pos().x(), a.pos().y(), a.vel().dx(), a.vel().dy()).trim())
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String areasJson(Airspace as) {
        return as.areas().stream()
                .map(AirspaceServer::areaJson)
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String areaJson(Area area) {
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

    private static String indexHtml() throws IOException {
        try (InputStream in = AirspaceServer.class.getResourceAsStream("/flightcontrol/view/index.html")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void respond(HttpExchange ex, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8090;
        AirspaceServer server = start(port);
        System.out.println("Flight Control step-03 server: http://localhost:" + server.port() + "/");
    }
}

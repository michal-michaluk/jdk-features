package dev.bottega.jdkfeatures.flightcontrol.demo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.bottega.jdkfeatures.flightcontrol.step02.Aircraft;
import dev.bottega.jdkfeatures.flightcontrol.step02.Airspace;
import dev.bottega.jdkfeatures.flightcontrol.step02.Area;
import dev.bottega.jdkfeatures.flightcontrol.step02.Circle;
import dev.bottega.jdkfeatures.flightcontrol.step02.Point;
import dev.bottega.jdkfeatures.flightcontrol.step02.Polygon;
import dev.bottega.jdkfeatures.flightcontrol.step02.Velocity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Tiny demo server that exposes the step-02 model (<b>Airspace.sample()</b>) exactly per the
 * JSON contract expected by {@code src/main/resources/flightcontrol/view/index.html}.
 *
 * <p>NOT part of the exercise (participants build their own REST with JEP 408 in step 03) —
 * this just lets the SVG viewer render the first-step model instantly.</p>
 *
 * <p>Routes: {@code GET /aircraft}, {@code GET /areas}, {@code GET /} (static SVG view).</p>
 */
public final class FlightControlDemoServer {

    private final HttpServer server;
    private volatile Airspace airspace;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private FlightControlDemoServer(int port) throws IOException {
        this.airspace = Airspace.sample();
        // Bounce region: areas + initial aircraft + margin. Aircraft stay inside this fixed
        // window, so with a stable view the areas never move and only points move.
        Bounds b = bounds();
        this.minX = b.minX - 2; this.maxX = b.maxX + 2;
        this.minY = b.minY - 2; this.maxY = b.maxY + 2;
        // Advance the simulation so auto-refresh shows the aircraft moving (bouncing in-region).
        scheduler.scheduleAtFixedRate(() -> airspace = advance(airspace), 0, 1, TimeUnit.SECONDS);

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/aircraft", ex -> json(ex, aircraftJson(airspace)));
        server.createContext("/areas", ex -> json(ex, areasJson(airspace)));
        server.createContext("/", ex -> html(ex));
        server.start();
    }

    public static FlightControlDemoServer start(int port) throws IOException {
        return new FlightControlDemoServer(port);
    }

    public int port() {
        return server.getAddress().getPort();
    }

    public void stop() {
        scheduler.shutdownNow();
        server.stop(0);
    }

    // ---- simulation advance with in-region bounce (demo only) ----
    private double minX, maxX, minY, maxY;

    private record Bounds(double minX, double maxX, double minY, double maxY) {}

    private Bounds bounds() {
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (Area a : airspace.areas()) {
            switch (a) {
                case Circle c -> {
                    minX = Math.min(minX, c.center().x() - c.radius()); maxX = Math.max(maxX, c.center().x() + c.radius());
                    minY = Math.min(minY, c.center().y() - c.radius()); maxY = Math.max(maxY, c.center().y() + c.radius());
                }
                case Polygon p -> {
                    for (Point v : p.vertices()) {
                        minX = Math.min(minX, v.x()); maxX = Math.max(maxX, v.x());
                        minY = Math.min(minY, v.y()); maxY = Math.max(maxY, v.y());
                    }
                }
            }
        }
        for (Aircraft ac : airspace.aircraft()) {
            minX = Math.min(minX, ac.pos().x()); maxX = Math.max(maxX, ac.pos().x());
            minY = Math.min(minY, ac.pos().y()); maxY = Math.max(maxY, ac.pos().y());
        }
        return new Bounds(minX, maxX, minY, maxY);
    }

    private Airspace advance(Airspace as) {
        Airspace next = as.step();
        var bounced = next.aircraft().stream().map(this::bounce).toList();
        return new Airspace(bounced, next.areas());
    }

    private Aircraft bounce(Aircraft a) {
        double x = a.pos().x(), y = a.pos().y();
        double dx = a.vel().dx(), dy = a.vel().dy();
        if (x < minX || x > maxX) dx = -dx;
        if (y < minY || y > maxY) dy = -dy;
        return new Aircraft(a.id(), a.label(), a.callsign(), new Point(x, y), new Velocity(dx, dy));
    }

    // ---- JSON building (matches the viewer contract): text-block "templates". ----
    private static String jstr(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    static String aircraftJson(Airspace as) {
        return as.aircraft().stream()
                .map(a -> """
                        { "id": "%s", "label": "%s", "callsign": "%s",
                          "pos": { "x": %s, "y": %s },
                          "vel": { "dx": %s, "dy": %s } }
                        """.formatted(jstr(a.id()), jstr(a.label()), jstr(a.callsign()),
                        a.pos().x(), a.pos().y(), a.vel().dx(), a.vel().dy()).trim())
                .collect(Collectors.joining(",", "[", "]"));
    }

    static String areasJson(Airspace as) {
        return as.areas().stream()
                .map(FlightControlDemoServer::areaJson)
                .collect(Collectors.joining(",", "[", "]"));
    }

    static String areaJson(Area area) {
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

    // ---- HTTP helpers ----
    private static void json(HttpExchange ex, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static void html(HttpExchange ex) throws IOException {
        byte[] bytes = indexHtml().getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    static String indexHtml() throws IOException {
        try (InputStream in = FlightControlDemoServer.class.getResourceAsStream("/flightcontrol/view/index.html")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8090;
        FlightControlDemoServer server = start(port);
        System.out.println("Flight Control demo server: http://localhost:" + server.port() + "/");
    }
}

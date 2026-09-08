package dev.bottega.jdkfeatures.flightcontrol.demo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.bottega.jdkfeatures.flightcontrol.step02.Airspace;
import dev.bottega.jdkfeatures.flightcontrol.step02.Area;
import dev.bottega.jdkfeatures.flightcontrol.step02.Circle;
import dev.bottega.jdkfeatures.flightcontrol.step02.Polygon;

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
        // Advance the simulation so auto-refresh shows the aircraft moving.
        // (Simple linear move; the "sensible" course/speed change is the participant's step-02 task.)
        scheduler.scheduleAtFixedRate(() -> airspace = airspace.step(), 0, 1, TimeUnit.SECONDS);

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

    // ---- JSON building (matches the viewer contract) ----
    static String aircraftJson(Airspace as) {
        return as.aircraft().stream()
                .map(a -> "{\"id\":\"" + a.id() + "\",\"label\":\"" + a.label()
                        + "\",\"callsign\":\"" + a.callsign() + "\",\"pos\":{\"x\":" + a.pos().x()
                        + ",\"y\":" + a.pos().y() + "},\"vel\":{\"dx\":" + a.vel().dx()
                        + ",\"dy\":" + a.vel().dy() + "}}")
                .collect(Collectors.joining(",", "[", "]"));
    }

    static String areasJson(Airspace as) {
        return as.areas().stream()
                .map(FlightControlDemoServer::areaJson)
                .collect(Collectors.joining(",", "[", "]"));
    }

    static String areaJson(Area area) {
        return switch (area) {
            case Circle c -> "{\"kind\":\"circle\",\"label\":\"" + c.label()
                    + "\",\"center\":{\"x\":" + c.center().x() + ",\"y\":" + c.center().y()
                    + "},\"radius\":" + c.radius() + ",\"props\":{}}";
            case Polygon p -> "{\"kind\":\"polygon\",\"label\":\"" + p.label() + "\",\"vertices\":["
                    + p.vertices().stream()
                            .map(v -> "{\"x\":" + v.x() + ",\"y\":" + v.y() + "}")
                            .collect(Collectors.joining(","))
                    + "],\"props\":{}}";
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

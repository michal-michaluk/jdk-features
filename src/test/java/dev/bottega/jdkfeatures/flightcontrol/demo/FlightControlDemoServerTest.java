package dev.bottega.jdkfeatures.flightcontrol.demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightControlDemoServerTest {

    private FlightControlDemoServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void servesAircraftPerContract() throws Exception {
        server = FlightControlDemoServer.start(0);
        String body = get("http://localhost:" + server.port() + "/aircraft");
        assertTrue(body.contains("\"label\":\"FOX\""));
        assertTrue(body.contains("\"callsign\":\"FOX123\""));
        assertTrue(body.contains("\"pos\":{\"x\":"));
    }

    @Test
    void servesAreasPerContract() throws Exception {
        server = FlightControlDemoServer.start(0);
        String body = get("http://localhost:" + server.port() + "/areas");
        assertTrue(body.contains("\"kind\":\"circle\""));
        assertTrue(body.contains("\"kind\":\"polygon\""));
        assertTrue(body.contains("\"radius\":3.0"));
    }

    @Test
    void servesSvgViewer() throws Exception {
        server = FlightControlDemoServer.start(0);
        String body = get("http://localhost:" + server.port() + "/");
        assertTrue(body.contains("Flight Control"));
        assertTrue(body.contains("<svg"));
    }

    @Test
    void exposesBoundPort() throws Exception {
        server = FlightControlDemoServer.start(0);
        assertNotNull(server.port());
        assertTrue(server.port() > 0);
    }

    private String get(String url) throws Exception {
        HttpResponse<String> resp = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }
}

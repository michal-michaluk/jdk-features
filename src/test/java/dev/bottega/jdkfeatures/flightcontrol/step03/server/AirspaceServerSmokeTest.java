package dev.bottega.jdkfeatures.flightcontrol.step03.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirspaceServerSmokeTest {

    private AirspaceServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void servesAircraftPerContract() throws Exception {
        server = AirspaceServer.start(0);
        HttpResponse<String> resp = get("/aircraft");
        assertEquals(200, resp.statusCode());
        String body = resp.body();
        assertTrue(body.contains("\"label\":\"FOX\""));
        assertTrue(body.contains("\"callsign\":\"FOX123\""));
        assertTrue(body.contains("\"pos\":{\"x\":"));
    }

    @Test
    void servesAreasPerContract() throws Exception {
        server = AirspaceServer.start(0);
        HttpResponse<String> resp = get("/areas");
        assertEquals(200, resp.statusCode());
        String body = resp.body();
        assertTrue(body.contains("\"kind\":\"circle\""));
        assertTrue(body.contains("\"kind\":\"polygon\""));
        assertTrue(body.contains("\"radius\":3.0"));
    }

    @Test
    void servesSvgViewer() throws Exception {
        server = AirspaceServer.start(0);
        HttpResponse<String> resp = get("/");
        assertEquals(200, resp.statusCode());
        String body = resp.body();
        assertTrue(body.contains("Flight Control"));
        assertTrue(body.contains("<svg"));
    }

    @Test
    void tickAdvancesAircraftPosition() throws Exception {
        server = AirspaceServer.start(0);
        String base = "http://localhost:" + server.port();
        String before = get("/aircraft").body();
        HttpResponse<String> resp = post(base + "/tick");
        assertEquals(200, resp.statusCode());
        String after = resp.body();
        assertTrue(after.contains("\"pos\":{\"x\":11"));
        assertNotEquals(before, after);
        // model keeps both aircraft but with new positions
        assertTrue(after.contains("\"label\":\"ECHO\""));
    }

    @Test
    void exposesBoundPortAndMainDefaults() throws Exception {
        server = AirspaceServer.start(0);
        assertNotNull(server.port());
        assertTrue(server.port() > 0);
    }

    private HttpResponse<String> get(String path) throws Exception {
        return HttpClient.newHttpClient().send(
                HttpRequest.newBuilder().uri(URI.create("http://localhost:" + server.port() + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String url) throws Exception {
        return HttpClient.newHttpClient().send(
                HttpRequest.newBuilder().uri(URI.create(url)).POST(HttpRequest.BodyPublishers.noBody()).build(),
                HttpResponse.BodyHandlers.ofString());
    }
}

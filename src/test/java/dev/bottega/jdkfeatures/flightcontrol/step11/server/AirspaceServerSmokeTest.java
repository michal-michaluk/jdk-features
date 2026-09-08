package dev.bottega.jdkfeatures.flightcontrol.step11.server;

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
        assertTrue(hasField(body, "label", "FOX"));
        assertTrue(hasField(body, "callsign", "FOX123"));
        assertTrue(body.contains("\"pos\""));
        assertTrue(body.contains("\"vel\""));
    }

    @Test
    void servesAreasPerContract() throws Exception {
        server = AirspaceServer.start(0);
        HttpResponse<String> resp = get("/areas");
        assertEquals(200, resp.statusCode());
        String body = resp.body();
        assertTrue(hasField(body, "kind", "circle"));
        assertTrue(hasField(body, "kind", "polygon"));
        assertTrue(hasNumber(body, "radius", 3.0));
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
        assertTrue(after.contains("\"x\""));
        assertNotEquals(before, after);
        assertTrue(hasField(after, "label", "ECHO"));
    }

    @Test
    void exposesBoundPort() throws Exception {
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

    // whitespace-tolerant JSON checks (the pretty text-block JSON has spaces after ':')
    private static boolean hasField(String body, String field, String value) {
        return body.matches("(?s).*\\\"" + field + "\\\"\\s*:\\s*\\\"" + value + "\\\".*");
    }

    private static boolean hasNumber(String body, String field, double value) {
        return body.matches("(?s).*\\\"" + field + "\\\"\\s*:\\s*" + value + "\\s*[,}].*");
    }
}

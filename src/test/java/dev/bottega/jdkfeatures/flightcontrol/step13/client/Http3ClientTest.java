package dev.bottega.jdkfeatures.flightcontrol.step13.client;

import dev.bottega.jdkfeatures.flightcontrol.step13.server.AirspaceServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Http3ClientTest {

    private static AirspaceServer server;
    private static String baseUrl;

    @BeforeAll
    static void startServer() throws Exception {
        server = AirspaceServer.start(0);
        baseUrl = "http://localhost:" + server.port();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void clientDeclaresHttp3() {
        assertEquals(HttpClient.Version.HTTP_3, Http3Client.client().version());
    }

    @Test
    void fetchesAircraftContract() throws Exception {
        HttpResponse<String> resp = Http3Client.fetch(baseUrl, "/aircraft");
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("label"));
        assertTrue(resp.body().contains("FOX"));
    }

    @Test
    void requestVersionIsPresentAndReportsDeclaredVersion() throws Exception {
        Optional<HttpClient.Version> version = Http3Client.requestVersion(Http3Client.client(), baseUrl + "/aircraft");
        assertTrue(version.isPresent());
        // Honest report: HttpRequest.version() reflects the version pinned on the request
        // (HTTP_3 in requestVersion), NOT the wire protocol. The JEP 408 server is
        // HTTP/1.1-only, so the real transport falls back to HTTP/1.1 even though the
        // client and request declare HTTP_3. We report the declared version and do not
        // hard-fail on the fallback.
        System.out.println("declared request version = " + version.get());
    }
}

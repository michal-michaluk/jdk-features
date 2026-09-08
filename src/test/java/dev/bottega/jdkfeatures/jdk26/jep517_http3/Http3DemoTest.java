package dev.bottega.jdkfeatures.jdk26.jep517_http3;

import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Http3DemoTest {

    @Test
    void clientPrefersHttp3() {
        HttpClient client = Http3Demo.http3Client();
        assertEquals(HttpClient.Version.HTTP_3, client.version());
    }

    @Test
    void requestIsPinnedToHttp3() {
        HttpRequest request = Http3Demo.http3Request("https://example.com/");
        assertEquals(HttpClient.Version.HTTP_3, request.version().orElseThrow());
        assertEquals("https://example.com/", request.uri().toString());
    }

    @Test
    void runReportsHttp3Configuration() {
        String result = Http3Demo.run();
        assertTrue(result.contains("client.version=HTTP_3"));
        assertTrue(result.contains("request.version=HTTP_3"));
    }

    @Test
    void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() -> Http3Demo.main(new String[0]));
    }
}

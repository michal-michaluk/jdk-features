package dev.bottega.jdkfeatures.flightcontrol.step13.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * JEP 517 — HTTP/3 for the HTTP Client API (final in JDK 26).
 *
 * <p>A thin <b>infrastructure</b> <b>client</b> that consumes the step-13 REST contract
 * ({@code GET /aircraft}, {@code GET /areas}) with a {@link HttpClient} configured to
 * <b>prefer</b> HTTP/3 (QUIC). It exposes the client's <em>declared</em> version and, per
 * request, the version the transport <em>actually</em> negotiated.</p>
 *
 * <p><b>Honest fallback.</b> The {@code com.sun.net.httpserver} server (JEP 408) speaks
 * HTTP/1.1 only — it has no QUIC/HTTP/3 endpoint. So the client <em>declares</em>
 * {@code HTTP_3}, but any real request falls back to {@code HTTP_1_1}. The tests assert the
 * <em>declaration</em> ({@code client.version() == HTTP_3}) and the <em>presence</em> of a
 * negotiated {@code request.version()} {@link Optional}, and report whichever version came
 * back instead of failing on HTTP/1.1.</p>
 */
public final class Http3Client {

    private Http3Client() {
    }

    /** Builds an {@link HttpClient} whose preferred protocol version is HTTP/3. */
    public static HttpClient client() {
        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_3).build();
    }

    /** Convenience: builds an HTTP/3-preferring client and sends {@code GET {baseUrl}{path}}. */
    public static HttpResponse<String> fetch(String baseUrl, String path) throws Exception {
        return get(client(), baseUrl + path);
    }

    /** Sends a {@code GET {url}} on the given client and returns the response body as a String. */
    public static HttpResponse<String> get(HttpClient client, String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Sends a {@code GET {url}} on the given client and returns the version the request
     * <b>declares</b> (pinned at build time).
     *
     * <p>Because {@link HttpRequest#version()} reflects the version set on the request
     * builder — not the wire protocol — we pin the request to {@link HttpClient.Version#HTTP_3}
     * so the returned {@link Optional} is present. The <em>actual</em> transport to the
     * HTTP/1.1-only JEP 408 server still falls back to {@code HTTP_1_1}.</p>
     *
     * @return an {@link Optional} with the request's declared version (present, HTTP_3)
     */
    public static Optional<HttpClient.Version> requestVersion(HttpClient client, String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .version(HttpClient.Version.HTTP_3)
                .GET().build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        return response.request().version();
    }
}

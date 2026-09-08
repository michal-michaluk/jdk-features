package dev.bottega.jdkfeatures.jdk26.jep517_http3;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

/**
 * JEP 517 — HTTP/3 for the HTTP Client API (final in JDK 26).
 *
 * <p>Shows the client-side API surface: building an {@link HttpClient} that prefers
 * {@link HttpClient.Version#HTTP_3} and an {@link HttpRequest} pinned to HTTP/3.
 *
 * <p><b>Honest limitation:</b> a live HTTP/3 (QUIC) exchange needs an HTTP/3-capable
 * server; this module demonstrates and asserts the configuration/API, not the transport.</p>
 */
public final class Http3Demo {

    private Http3Demo() {
    }

    public static String describe() {
        return "JEP 517 \u2014 HTTP/3 for the HTTP Client API (JDK 26)";
    }

    /** Builds an {@link HttpClient} whose preferred version is HTTP/3. */
    public static HttpClient http3Client() {
        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_3).build();
    }

    /** Builds an {@link HttpRequest} pinned to HTTP/3 for the given URI. */
    public static HttpRequest http3Request(String uri) {
        return HttpRequest.newBuilder().uri(URI.create(uri)).version(HttpClient.Version.HTTP_3).GET().build();
    }

    public static String run() {
        HttpClient client = http3Client();
        HttpRequest request = http3Request("https://example.com/");
        return describe()
                + "\nclient.version=" + client.version()
                + "\nrequest.version=" + request.version().map(Enum::name).orElse("not-set")
                + "\nrequest.uri=" + request.uri();
    }

    public static void main(String[] args) {
        System.out.println(run());
    }
}

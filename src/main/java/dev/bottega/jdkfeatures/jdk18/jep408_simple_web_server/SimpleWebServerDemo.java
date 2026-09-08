package dev.bottega.jdkfeatures.jdk18.jep408_simple_web_server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * JEP 408 — Simple Web Server.
 *
 * <p>Shows the single-route HTTP server foundation this JEP built on:
 * a {@link com.sun.net.httpserver.HttpServer} that serves a fixed
 * {@code /hello} endpoint on localhost, plus a real client round-trip.</p>
 */
public final class SimpleWebServerDemo {

    static final String HELLO_BODY = "Hello from JEP 408 simple web server";

    private SimpleWebServerDemo() {
    }

    public static String describe() {
        return "JEP 408 \u2014 Simple Web Server (JDK 18)";
    }

    /** Creates and starts a one-route {@link HttpServer} on an ephemeral localhost port. */
    public static HttpServer startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/hello", SimpleWebServerDemo::handle);
        server.start();
        return server;
    }

    /** Serves {@code /hello}; anything other than GET answers 405. Exposed for direct testing. */
    public static void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }
        byte[] body = HELLO_BODY.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    /** Performs a {@code GET} of {@code /hello} and returns the response status. */
    public static int fetchStatus(String baseUrl) throws Exception {
        HttpResponse<Void> resp = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder().uri(URI.create(baseUrl + "/hello")).GET().build(),
                HttpResponse.BodyHandlers.discarding());
        return resp.statusCode();
    }

    /** Performs a {@code POST} of {@code /hello} and returns the response status (expect 405). */
    public static int fetchStatusPost(String baseUrl) throws Exception {
        HttpResponse<Void> resp = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder().uri(URI.create(baseUrl + "/hello")).POST(HttpRequest.BodyPublishers.noBody()).build(),
                HttpResponse.BodyHandlers.discarding());
        return resp.statusCode();
    }

    /** Performs a {@code GET} of {@code /hello} and returns the response body. */
    public static String fetchBody(String baseUrl) throws Exception {
        HttpResponse<String> resp = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder().uri(URI.create(baseUrl + "/hello")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }

    public static String run() throws Exception {
        HttpServer server = startServer();
        try {
            int port = server.getAddress().getPort();
            int status = fetchStatus("http://127.0.0.1:" + port);
            String body = fetchBody("http://127.0.0.1:" + port);
            return describe() + "\nserver :" + port + "\nGET /hello -> " + status + " " + body
                    + "\nPOST /hello -> " + fetchStatusPost("http://127.0.0.1:" + port);
        } finally {
            server.stop(0);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(run());
    }
}

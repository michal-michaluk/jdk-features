package dev.bottega.jdkfeatures.jdk18.jep408_simple_web_server;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleWebServerDemoTest {

    @Test
    void runServesHelloAndRejectsPost() throws Exception {
        String result = SimpleWebServerDemo.run();
        assertTrue(result.contains(SimpleWebServerDemo.HELLO_BODY), "GET should return the hello body");
        assertTrue(result.contains("405"), "POST should be rejected with 405");
    }

    @Test
    void serverServesGetAndRejectsPostOverTheWire() throws Exception {
        HttpServer server = SimpleWebServerDemo.startServer();
        try {
            int port = server.getAddress().getPort();
            String base = "http://127.0.0.1:" + port;
            assertEquals(200, SimpleWebServerDemo.fetchStatus(base));
            assertEquals(405, SimpleWebServerDemo.fetchStatusPost(base));
            assertEquals(SimpleWebServerDemo.HELLO_BODY, SimpleWebServerDemo.fetchBody(base));
            assertNotNull(server.getAddress().getAddress());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() -> SimpleWebServerDemo.main(new String[0]));
    }
}

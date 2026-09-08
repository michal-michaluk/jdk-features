package dev.bottega.jdkfeatures.jdk26.jep524_pem;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PemEncodingDemoTest {

    @Test
    void encodeProducesPemHeader() throws Exception {
        PublicKey pub = KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic();
        String pem = PemEncodingDemo.encode(pub);
        assertTrue(pem.startsWith("-----BEGIN PUBLIC KEY-----"), "PEM must start with the BEGIN marker");
    }

    @Test
    void decodeRoundTripsToSameKey() throws Exception {
        PublicKey pub = KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic();
        PublicKey back = PemEncodingDemo.decode(PemEncodingDemo.encode(pub));
        assertTrue(java.util.Arrays.equals(pub.getEncoded(), back.getEncoded()), "decoded key must be identical");
    }

    @Test
    void runReportsRoundTrip() throws Exception {
        String result = PemEncodingDemo.run();
        assertTrue(result.contains("BEGIN PUBLIC KEY"));
        assertTrue(result.contains("roundtripEncodedEqual=true"));
    }

    @Test
    void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() -> PemEncodingDemo.main(new String[0]));
    }
}

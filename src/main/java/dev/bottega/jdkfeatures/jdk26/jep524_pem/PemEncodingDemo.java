package dev.bottega.jdkfeatures.jdk26.jep524_pem;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PEMDecoder;
import java.security.PEMEncoder;
import java.security.PublicKey;

/**
 * JEP 524 — PEM Encodings of Cryptographic Objects (preview in JDK 26).
 *
 * <p>Encodes a {@link PublicKey} (any {@link java.security.DEREncodable}) to the
 * text {@code PEM} form and decodes it back to a key object via
 * {@link java.security.PEMEncoder}/{@link java.security.PEMDecoder}.</p>
 */
public final class PemEncodingDemo {

    private PemEncodingDemo() {
    }

    public static String describe() {
        return "JEP 524 \u2014 PEM Encodings of Cryptographic Objects (JDK 26, preview)";
    }

    /** Encodes a DER-encodable object to its PEM text form. */
    public static String encode(java.security.DEREncodable object) {
        return PEMEncoder.of().encodeToString(object);
    }

    /** Decodes a PEM string back to a {@link PublicKey}. */
    public static PublicKey decode(String pem) {
        return PEMDecoder.of().decode(pem, PublicKey.class);
    }

    /** Generates an RSA key pair and round-trips the public key through PEM. */
    public static String run() throws Exception {
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        PublicKey pub = keyPair.getPublic();
        String pem = encode(pub);
        PublicKey back = decode(pem);
        boolean same = java.util.Arrays.equals(pub.getEncoded(), back.getEncoded());
        return describe()
                + "\npemHead=" + pem.lines().findFirst().orElse("")
                + "\nalgorithm=" + pub.getAlgorithm()
                + "\nroundtripAlgorithm=" + back.getAlgorithm()
                + "\nroundtripEncodedEqual=" + same;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(run());
    }
}

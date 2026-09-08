package dev.bottega.jdkfeatures.flightcontrol.step14.domain;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PEMDecoder;
import java.security.PEMEncoder;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;

/**
 * JEP 524 — PEM Encodings (preview).
 *
 * <p>A <b>domain</b> value object: the key material that protects a sector's command stream.
 * It wraps a generated RSA {@link KeyPair} and can be serialised to a PEM string
 * ({@code -----BEGIN PRIVATE KEY-----} / {@code -----BEGIN PUBLIC KEY-----}) and parsed back
 * with {@link PEMEncoder}/{@link PEMDecoder}, proving the encode → decode round-trip
 * reproduces the exact same keys.</p>
 *
 * <p>Pure domain logic: it touches only {@code java.security.*} (key generation + PEM
 * encode/decode) — no network, no I/O, no HTTP.</p>
 */
public final class KeyMaterial {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public KeyMaterial(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = Objects.requireNonNull(privateKey, "privateKey must not be null");
        this.publicKey = Objects.requireNonNull(publicKey, "publicKey must not be null");
    }

    /** Generates a fresh RSA key pair of the given modulus size. */
    public static KeyMaterial generate(int keySize) throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(keySize);
        KeyPair pair = generator.generateKeyPair();
        return new KeyMaterial(pair.getPrivate(), pair.getPublic());
    }

    public PrivateKey privateKey() {
        return privateKey;
    }

    public PublicKey publicKey() {
        return publicKey;
    }

    /** PEM text of the private key (`-----BEGIN PRIVATE KEY-----`). */
    public String privateKeyPem() {
        return PEMEncoder.of().encodeToString(privateKey);
    }

    /** PEM text of the public key (`-----BEGIN PUBLIC KEY-----`). */
    public String publicKeyPem() {
        return PEMEncoder.of().encodeToString(publicKey);
    }

    /** Parses a PEM string back into a {@link PrivateKey}. */
    public static PrivateKey decodePrivateKey(String pem) {
        return PEMDecoder.of().decode(pem, PrivateKey.class);
    }

    /** Parses a PEM string back into a {@link PublicKey}. */
    public static PublicKey decodePublicKey(String pem) {
        return PEMDecoder.of().decode(pem, PublicKey.class);
    }

    /** Re-builds the material from its own PEM encodings (encode → decode). */
    public KeyMaterial roundTrip() {
        return new KeyMaterial(decodePrivateKey(privateKeyPem()), decodePublicKey(publicKeyPem()));
    }

    /** Whether encode → decode reproduces the exact same key material. */
    public boolean roundTrips() {
        return roundTrip().equals(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KeyMaterial k)) {
            return false;
        }
        return privateKey.equals(k.privateKey) && publicKey.equals(k.publicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(privateKey, publicKey);
    }
}

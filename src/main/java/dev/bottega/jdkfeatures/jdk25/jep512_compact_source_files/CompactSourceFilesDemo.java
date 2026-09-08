package dev.bottega.jdkfeatures.jdk25.jep512_compact_source_files;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JEP 512 — Compact Source Files and Instance Main Methods (final in JDK 25).
 *
 * <p>A source file without a class, with a top-level {@code void main()}
 * (an <em>instance</em> main method in the implicit class), is runnable directly:
 * {@code java Main.java}. No {@code javac} step, no public-static-void-main ceremony.</p>
 */
public final class CompactSourceFilesDemo {

    private CompactSourceFilesDemo() {
    }

    public static String describe() {
        return "JEP 512 \u2014 Compact Source Files and Instance Main Methods (JDK 25)";
    }

    /** Copies the compact source to a temp dir and runs {@code java Main.java}. */
    public static String launch() throws IOException, InterruptedException {
        Path dir = Files.createTempDirectory("jep512");
        Path main = dir.resolve("Main.java");
        Files.copy(CompactSourceFilesDemo.class.getResourceAsStream("/jep512/Main.java"), main);

        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        Process process = new ProcessBuilder(java, main.toString())
                .redirectErrorStream(true)
                .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exit = process.waitFor();
        return output.strip() + (exit == 0 ? "" : " [exit=" + exit + "]");
    }

    public static String run() throws IOException, InterruptedException {
        return describe() + "\n" + launch();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(run());
    }
}

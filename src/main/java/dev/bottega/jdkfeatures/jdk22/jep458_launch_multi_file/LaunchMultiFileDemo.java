package dev.bottega.jdkfeatures.jdk22.jep458_launch_multi_file;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JEP 458 — Launch Multi-File Source-Code Programs (final in JDK 22).
 *
 * <p>The {@code java} launcher compiles and runs several {@code .java} source files
 * (here {@code Main.java} + {@code Helper.java}) directly, without a separate
 * {@code javac} step or a module/classpath setup: {@code java Main.java Helper.java}.</p>
 */
public final class LaunchMultiFileDemo {

    private LaunchMultiFileDemo() {
    }

    public static String describe() {
        return "JEP 458 \u2014 Launch Multi-File Source-Code Programs (JDK 22)";
    }

    /** Copies the bundled demo sources to a temp dir and runs {@code java Main.java Helper.java}. */
    public static String launch() throws IOException, InterruptedException {
        Path dir = Files.createTempDirectory("jep458");
        Path main = dir.resolve("Main.java");
        Path helper = dir.resolve("Helper.java");
        Files.copy(LaunchMultiFileDemo.class.getResourceAsStream("/jep458/Main.java"), main);
        Files.copy(LaunchMultiFileDemo.class.getResourceAsStream("/jep458/Helper.java"), helper);

        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        Process process = new ProcessBuilder(java, main.toString(), helper.toString())
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

import java.math.BigDecimal

plugins {
    java
    jacoco
}

group = "dev.bottega"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    // jdk.httpserver is a standard module not in java.se; add it so com.sun.net.httpserver (JEP 408) resolves.
    // -Xlint:all minus restricted: the FFM demo intentionally uses restricted APIs, enabled at runtime.
    // -restricted: FFM demo uses restricted APIs (enabled at runtime). -preview: preview features are intentional.
    options.compilerArgs.addAll(listOf("-Xlint:all,-restricted,-preview", "-parameters", "--add-modules=jdk.httpserver"))
    // Preview features (JDK 26: JEP 524/525/526/530) + incubating Vector API (JEP 529).
    options.release.set(26)
    options.compilerArgs.addAll(listOf("--enable-preview", "--add-modules=jdk.incubator.vector"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    // FFM (JEP 454) runs native code; silence the native-access warning for the demo module.
    jvmArgs("--enable-native-access=ALL-UNNAMED", "--add-modules=jdk.httpserver")
    // Preview + incubating Vector module must also be enabled on the test JVM.
    jvmArgs("--enable-preview", "--add-modules=jdk.incubator.vector")
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = false
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Quality gate: enforce the requested 90%+ coverage on meaningfully-coverable counters.
// (Branch is deliberately out of the gate: demo/pattern-matching code is branch-heavy.)
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            element = "BUNDLE"
            limit { counter = "LINE"; minimum = BigDecimal.valueOf(0.90) }
            limit { counter = "INSTRUCTION"; minimum = BigDecimal.valueOf(0.90) }
            limit { counter = "METHOD"; minimum = BigDecimal.valueOf(0.90) }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

// Reliable way to run ALL demos with the preview/incubator/native-access flags the JVM needs.
// (IntelliJ's generated '<Class>.main()' Gradle run does NOT carry these jvmArgs.)
tasks.register<JavaExec>("runDemos") {
    group = "application"
    description = "Runs every feature demo (incl. preview + incubator) with the required JVM flags."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("dev.bottega.jdkfeatures.DemoRunner")
    jvmArgs("--enable-preview", "--add-modules=jdk.incubator.vector,jdk.httpserver", "--enable-native-access=ALL-UNNAMED")
}

// Demo of the Flow-Control first step: serves the step-02 model to the SVG viewer.
tasks.register<JavaExec>("runFlightControlDemo") {
    group = "application"
    description = "Starts a local server exposing the step-02 Flight Control model to the SVG viewer."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("dev.bottega.jdkfeatures.flightcontrol.demo.FlightControlDemoServer")
    args("8090")
    jvmArgs("--add-modules=jdk.httpserver")
}

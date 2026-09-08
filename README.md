# jdk-features

Hands-on, single-module Gradle project demonstrating the **final**, code-visible
Java features introduced in **JDK 17 → JDK 26**. One package per JDK version,
one sub-package per JEP, one demo + one test per feature.

- JDK toolchain: **26** (`openjdk@26.0.1` via jabba; resolved locally via `gradle.properties`)
- JUnit Jupiter 5, JaCoCo coverage report
- **Final** features compile normally; **preview** (JDK 26) features use `--enable-preview`;
  the Vector API uses the incubating `--add-modules jdk.incubator.vector` (all wired into `build.gradle.kts`).


## IntelliJ preview features

The Gradle build adds `--enable-preview` (and `--add-modules jdk.incubator.vector`) to
**compile** and **test**. But IntelliJ's generated `DemoRunner.main()` **Gradle** run does
**not inherit test jvmArgs** — so to run `DemoRunner` from IntelliJ, set the run
configuration's **VM options**:

```
--enable-preview --add-modules=jdk.incubator.vector,jdk.httpserver --enable-native-access=ALL-UNNAMED
```

Without them you hit `UnsupportedClassVersionError: Preview features are not enabled` plus an
FFM *restricted method* warning — the exact symptom of a missing `--enable-preview`.

Also, if IntelliJ compiles outside Gradle delegation, enable preview in
**Settings → Build, Execution, Deployment → Compiler → Java Compiler → Enable preview features**
(language level 26).

## Layout

```
dev.bottega.jdkfeatures
├── jdk17/  jep409_sealed · jep356_enhanced_prng
├── jdk18/  jep408_simple_web_server
├── jdk21/  jep444_virtual_threads · jep431_sequenced_collections
│           jep440_record_patterns · jep441_pattern_matching_switch
├── jdk22/  jep454_ffm · jep456_unnamed_variables_patterns
│           jep458_launch_multi_file
├── jdk23/  jep467_markdown_doc_comments
├── jdk24/  jep485_stream_gatherers · jep484_class_file_api
├── jdk25/  jep506_scoped_values · jep511_module_imports
│           jep512_compact_source_files · jep513_flexible_constructor_bodies
└── jdk26/  jep517_http3(final) · jep524_pem(preview) · jep525_structured_concurrency(preview)
           jep526_lazy_constants(preview) · jep530_primitive_patterns(preview) · jep529_vector(incubator)
```

## Demo & test convention

Every library-style feature ships a `<Name>Demo` with:

- `public static String run()` — runs several illustrative cases and returns a
  multi-line result string (the *what does this JEP give me* content).
- `public static void main(String[] args)` — prints `run()`.

and a `<Name>Test` that:

- asserts on substrings of `run()` (proving the feature behaves as documented),
- exercises `main(...)` under `assertDoesNotThrow`,
- covers the public API; targets **90–100% line coverage**.

Launcher/native/network features (Simple Web Server, FFM, Multi-File Launcher,
Compact Source Files, Module Imports, HTTP/3, Markdown docs) use their own
demo/test style (subprocess / native downcall / API config).

## Build

```bash
./gradlew test              # runs all tests (preview + incubator flags already wired)
./gradlew check             # tests + 90% coverage gate
./gradlew jacocoTestReport  # coverage report in build/reports/jacoco/test/html
```

## Run all demos

```bash
./gradlew runDemos   # Gradle task that runs every demo with the required preview/incubator/native-access flags
```

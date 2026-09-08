# Flight Control — exercise plan (JEP-by-JEP)

A single progressive exercise: build a **Flight Control** app that monitors an airspace —
storing *moving* aircraft (point + velocity vector + label) and *static* areas (circles /
polygons with label + properties). Each JEP adds one aspect. Participants implement code
**and tests** for each stage (~20–30 min per stage). State lives **in memory** (`No UI`)
except stage S16, which exposes it over REST + a static SVG page.

> Companion to `jdk-features`. Skipped entirely: JDK 22 (JEP 454, 456, 458),
> JEP 484 (Class-File API), JEP 512 (Compact Source Files), JEP 529 (Vector API).

---

## Domain model

```java
record Point(double x, double y) {}
record Velocity(double dx, double dy) {}
record Aircraft(Point pos, Velocity vel, String label, String callsign) {}

sealed interface Area permits Circle, Polygon {}
record Circle(Point center, double radius, String label, Map<String,String> props) implements Area {}
record Polygon(List<Point> vertices, String label, Map<String,String> props) implements Area {}

final class Airspace {
    // holds aircraft + areas, exposes step() that advances every aircraft by its velocity
}
```

Invariants: immutable model, `step()` advances `pos += vel`, no rendering engine
(except the final SVG view).

---

## S0 · Starter (provided)

- `Point`, `Velocity`, `Aircraft`, `Circle`, `Polygon` (records), `Airspace` with
  `List<Aircraft>` / `List<Area>` and a basic `step()`.
- Base tests + a small "state snapshot" helper.

---

## Phase A — Model & data (in-memory)

### S1 · JEP 409 — Sealed Classes
- **Task:** model the static world with `sealed interface Area permits Circle, Polygon`
  (optionally a parent `sealed interface Element permits Aircraft, Area`); everything
  immutable via records.
- **Tests:** exhaustive `switch`/`describe` over `Area` **without `default`**; no mutation;
  `Airspace` accepts both subtypes.

### S2 · JEP 440 — Record Patterns
- **Task:** destructure nested records with patterns — `describe(Area)`, `instanceof`,
  extract `label`/`props`.
- **Tests:** `Circle(Point(...), ...)`, `Polygon(List<Point> ...)`, sum fields via patterns.

### S3 · JEP 356 — Enhanced PRNG
- **Task:** *sensible* airborne randomness: each `step()` applies a small bounded change to
  heading/speed; use `RandomGeneratorFactory.of("L64X128MixRandom").create()` with a **seed**.
- **Tests:** deterministic output for a fixed seed; change stays bounded; velocity actually changes.

### S4 · JEP 431 — Sequenced Collections
- **Task:** ordered views — `SequencedCollection` (e.g. ordered by proximity), `SequencedSet`
  of labels, `SequencedMap` `id→aircraft` with `putFirst/firstEntry/reversed`.
- **Tests:** `addFirst/getFirst/reversed`, `firstEntry`, reversed view correctness.

---

## Phase B — Logic & concurrency

### S5 · JEP 441 — Pattern Matching for switch
- **Task:** classification via `switch` type patterns + `when` guards (e.g. alarm sector if
  `speed > X && heading in sector`), handle `case null`.
- **Tests:** each guard branch, `null`, exhaustive sealed switch.

### S6 · JEP 444 — Virtual Threads
- **Task:** parallel "radars": advance N aircraft / query sectors via
  `Thread.ofVirtual()`/`newVirtualThreadPerTaskExecutor`; safe accumulator.
- **Tests:** consistent state after `join`, `isVirtual` true, atomic counter of updated.

### S7 · JEP 506 — Scoped Values
- **Task:** context carried into tasks: `ScopedValue` (`currentSector`, `simulationId`) read
  inside virtual threads/forks.
- **Tests:** bound/unbound, nested rebinding, propagation to `fork`.

### S8 · JEP 525 — Structured Concurrency *(preview)*
- **Task:** `StructuredTaskScope.open()` to run "sector queries" — `fork`/`join`, aggregate,
  fail-fast.
- **Tests:** combined results, `Subtask.state()==SUCCESS`, `close()`.

### S9 · JEP 526 — Lazy Constants *(preview)*
- **Task:** `LazyConstant` for heavy global values (map projection / risk assessment computed
  once).
- **Tests:** `get()` computes once, `orElse` before init, `isInitialized`.

### S10 · JEP 513 — Flexible Constructor Bodies
- **Task:** constructors with validation / derived computation **before** `super()`/`this()`
  (e.g. derived top speed, `requireNonNull` before super).
- **Tests:** validation before super, derived-value clamping.

---

## Phase C — Exposure (REST + browser)

### S11 · JEP 511 — Module Imports
- **Task:** tidy code with `import module java.base;` — fewer explicit imports.
- **Tests:** behavior + compilation.

### S12 · JEP 530 — Primitive Patterns *(preview)*
- **Task:** classify primitives in `switch` (`case int i`, `case long`, `case double`) —
  e.g. bucket speed/heading severity.
- **Tests:** each bucket, `null`, `default`.

### S13 · JEP 524 — PEM Encodings *(preview)*
- **Task:** export/import config: a key (or area metadata) as **PEM** (encode/decode).
- **Tests:** RSA PEM round-trip, `BEGIN …` header.

### S14 · JEP 467 — Markdown Documentation Comments
- **Task:** document the public API with `///` markdown.
- **Acceptance** (no unit test): readable generated Javadoc.

### S15 · JEP 485 — Stream Gatherers
- **Task:** telemetry aggregation via `Stream.gather(Gatherers.windowFixed/sliding/fold)` —
  chunk sectors, sliding positions over time, bounding-box/fold.
- **Tests:** `windowFixed(3)`, `fold(sum)`, sliding windows.

### S16 · JEP 408 — Simple Web Server
- **Task:** expose state with `com.sun.net.httpserver`: **REST** (`/aircraft`, `/areas`,
  `/tick`) + a **static HTML** that draws the scene as **SVG** in the browser.
- **Tests:** `GET /aircraft` → state, `POST /tick` → advances, responds HTML/SVG.

### S17 · JEP 517 — HTTP/3 *(final)*
- **Task:** client: `HttpClient` with `version(HTTP_3)` consuming the REST API
  (falls back over HTTP/1.1).
- **Tests:** API contract — `client.version()`, `request.version()`.
- **Honest limit (F1):** real QUIC needs an HTTP/3 server; the exercise demonstrates the
  client API/configuration.

---

## Notes

- **Single domain, no engine** (beyond S16/SVG); state is in memory.
- **Preview flag** required for S8, S9, S12, S13 (`--enable-preview`).
- Exercise format: starter with `TODO` + description → participant implements + writes
  tests; `./gradlew test`/`check` is the gate (target ≥90% coverage).
- Every stage is additive — the same `FlightControl` app grows through all 17 stages.

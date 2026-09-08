# Flight Control — JEP-ordered exercise plan

One progressive exercise, **exercise order = JEP order**. Participants build the whole
thing themselves: the immutable **model (records + sealed)** from a description and
test data only — **no copy-paste Java code is handed out**. Data and structure are
described in **YAML-style / markdown**, not code.

Rules:
- Single domain, **in-memory**, no rendering engine.
- The static **SVG view appears right after the HTTP server** (E3); at that stage a
  browser refresh showing the state is enough. Later exercises add logic that the view
  reflects after a refresh.
- Participants write **code AND tests**; `./gradlew check` (≥90% coverage) is the gate.
- Preview (`--enable-preview`) exercises: E14 (524), E15 (525), E16 (526), E17 (530).

---
## 0 · Domain — the model you build in E1 (described, not coded)

```
entities:
  aircraft:              # moving point + vector + label
    id:       uuid
    label:    string
    callsign: string
    pos:      { x: float, y: float }
    vel:      { dx: float, dy: float }
  area:                  # sealed: exactly one of these
    circle:
      center: { x: float, y: float }
      radius: float
      label:  string
      props:  { k: v }
    polygon:
      vertices: [ { x: float, y: float }, ... ]
      label:   string
      props:   { k: v }

airspace:
  aircraft: [ aircraft, ... ]
  areas:    [ area, ... ]
  step():   pos += vel            # movement; heading/speed change arrives in E2
```

Semantics to implement (as immutable `record` + `sealed interface`):
- `Aircraft`, `Circle`, `Polygon` are immutable records; `Area` is a sealed type with
  exactly `Circle` and `Polygon` as permitted subtypes.
- `Airspace` holds both lists and exposes `step()` that advances every aircraft by its
  velocity. Public API should be documented (see E8).

---
## E1 · JEP 409 — Sealed Classes
- **Opis:** zbuduj niezmienny model domeny jako `record` + `sealed interface Area`.
- **Dane testowe (YAML):**
  ```
  aircraft: [{id:a1,label:"FOX",pos:{x:10,y:0},vel:{dx:1,dy:0}},
             {id:a2,label:"ECHO",pos:{x:0,y:5},vel:{dx:0,dy:1}}]
  areas:    [{circle:{center:{x:0,y:0},radius:3,label:"CTR",props:{type:"control"}}},
             {polygon:{vertices:[{x:0,y:0},{x:4,y:0},{x:4,y:4}],label:"TMA",props:{kind:"terminal"}}}]
  after step(): a1->(11,0), a2->(0,6)
  ```
- **Akceptacja:** exhaustive `describe`/kind nad `Area` bez `default`; brak mutacji;
  `Airspace` przyjmuje oba podtypy.

## E2 · JEP 356 — Enhanced PRNG
- **Opis:** ruch „jak samolot": w każdym `step()` mała, ograniczona zmiana kursu i
  prędkości; deterministyczny z seedu (np. `L64X128MixRandom`).
- **Parametry (YAML):** `maxTurnDeg`, `maxSpeedDelta`, `seed`.
- **Dane testowe (YAML):** seed `42` → oczekiwane pozycje po 5 krokach (deterministyczne);
  granice zmiany wektora.
- **Akceptacja:** ten sam seed → ta sama trajektoria; zmiana wektora w granicach.

## E3 · JEP 408 — Simple Web Server  (widok pojawia się TUŻ POTEM)
- **Opis:** wystaw stan w pamięci przez HTTP + **statyczny HTML rysujący scenę jako SVG**
  (zmiany po odświeżeniu są OK na tym etapie).
- **API (YAML):**
  ```
  GET  /aircraft -> [ {label, pos, vel} ... ]
  GET  /areas    -> [ {kind, label, geo...} ... ]
  POST /tick     -> wywołuje step(), zwraca nowy stan
  GET  /         -> statyczny HTML + SVG (czerwone punkty=gór, szare=obszary)
  ```
- **Dane testowe (YAML):** po `POST /tick` pozycje się przesunęły; `GET /aircraft` zwraca
  label-e i współrzędne; `GET /` zwraca dokument zawierający `<svg` oraz nagłówki elementów.
- **Akceptacja:** REST zwraca stan; strona SVG renderuje się w przeglądarce.

## E4 · JEP 431 — Sequenced Collections
- **Opis:** uporządkowane widoki — lista samolotów (np. wg odległości), `SequencedSet`
  labeli, `SequencedMap` id→aircraft z `putFirst/firstEntry/reversed`.
- **Dane testowe (YAML):** kolejność dodania → `first`/`last`/`reversed` oczekiwane;
  mapa `firstEntry`/`lastEntry`.
- **Akceptacja:** `addFirst/getFirst/reversed`; widok `reversed` poprawny.

## E5 · JEP 440 — Record Patterns
- **Opis:** destrukturyzacja modelu — `describe(Area)` przez record pattern
  (`Circle(center, radius, label, props)`, `Polygon(vertices, label, props)`).
- **Dane testowe (YAML):** dla obszaru → wyciągnięty `label`, `radius`/`vertices`.
- **Akceptacja:** poprawna destrukturyzacja i wyciągnięcie `props`.

## E6 · JEP 441 — Pattern Matching for switch
- **Opis:** klasyfikacja przez `switch` z type patterns + `when` (np. „alarm" gdy
  `speed > X` i kierunek w sektorze), obsługa `case null`.
- **Reguła (YAML):** `alarm: {minSpeed: 300, sectorDeg: [..]}`.
- **Dane testowe (YAML):** obiekty → oczekiwana kategoria (alarm/sektor/normal/null).
- **Akceptacja:** każdy guard, `null`, wyczerpujący sealed switch.

## E7 · JEP 444 — Virtual Threads
- **Opis:** równoległe „radary" — aktualizacja sektorów przez `Thread.ofVirtual()` /
  `newVirtualThreadPerTaskExecutor`; bezpieczny akumulator.
- **Dane testowe (YAML):** N samolotów w sektorach → po `join` wszystkie zaktualizowane;
  `isVirtual` prawdziwe.
- **Akceptacja:** spójny stan po `join`; atomowy licznik zaktualizowanych.

## E8 · JEP 467 — Markdown Documentation Comments
- **Opis:** udokumentuj publiczne API przez `///` markdown (np. metadane „sektor",
  zachowanie `step`).
- **Akceptacja** (bez testu): czytelne wygenerowane Javadoc.

## E9 · JEP 485 — Stream Gatherers
- **Opis:** agregacja telemetrii — `windowFixed(3)` po sektorach, okno przesuwne pozycji
  w czasie, `fold` do bbox.
- **Dane testowe (YAML):** lista 8 zadań → `windowFixed(3)` => `[[..],[..],[..],[2]`,
  `fold` => suma, `windowSliding(2)`.
- **Akceptacja:** zgrupowane foldy/okna zgodne z oczekiwaniami.

## E10 · JEP 506 — Scoped Values
- **Opis:** kontekst przekazywany do zadań — `ScopedValue` (`currentSector`,
  `simulationId`) czytany wewnątrz wątków wirtualnych/forków.
- **Dane testowe (YAML):** bound/unbound, zagnieżdżone nadpisanie, propagacja do `fork`.
- **Akceptacja:** wartość dostępna w zadaniu; `orElse` gdy niezbindowana.

## E11 · JEP 511 — Module Import Declarations
- **Opis:** zredukuj importy w klasie przez jedno `import module java.base;` (deklaracja
  importu modułu — sprawdź w dokumentacji składnię; nie dostajemy gotowca).
- **Akceptacja:** zachowanie bez zmian, mniej importów.

## E12 · JEP 513 — Flexible Constructor Bodies
- **Opis:** konstruktory z walidacją/obliczeniem przed `super(...)`/`this(...)` (np.
  pochodna top-speed, `requireNonNull` argumentów przed super).
- **Dane testowe (YAML):** niepoprawne dane → wyjątek **przed** super; wartości pochodne.
- **Akceptacja:** walidacja przed super; clamp pochodnej.

## E13 · JEP 517 — HTTP/3 (final)
- **Opis:** klient `HttpClient` z `version(HTTP_3)` konsumujący REST z E3 (fallback
  HTTP/1.1).
- **Dane testowe (YAML):** `client.version == HTTP_3`, request `version` => `HTTP_3`.
- **Akceptacja:** API/konfiguracja klienta (realny QUIC wymaga serwera HTTP/3 — uczciwie
  oznacz).

## E14 · JEP 524 — PEM Encodings *(preview)*
- **Opis:** eksport/import konfiguracji — klucz (lub metadane obszaru) jako **PEM**
  (encode/decode).
- **Dane testowe (YAML):** klucz RSA → PEM zaczyna się `-----BEGIN …-----`, po dekodzie
  ten sam klucz.
- **Akceptacja:** PEM round-trip.

## E15 · JEP 525 — Structured Concurrency *(preview)*
- **Opis:** `StructuredTaskScope.open()` — odpytywanie sektorów, `fork`/`join`, agregacja,
  fail-fast.
- **Dane testowe (YAML):** wyniki z fork-ów złożone; `Subtask.state == SUCCESS`; `close`.
- **Akceptacja:** cykl życia zamknięty, agregacja poprawna.

## E16 · JEP 526 — Lazy Constants *(preview)*
- **Opis:** `LazyConstant` dla ciężkich wartości globalnych (projekcja mapy / ocena
  ryzyka liczone raz).
- **Dane testowe (YAML):** `get()` liczy raz; `orElse` przed inicjalizacją; `isInitialized`.
- **Akceptacja:** jednokrotne policzenie.

## E17 · JEP 530 — Primitive Patterns *(preview)*
- **Opis:** klasyfikacja prymitywów w `switch` (`case int`, `case double`…) — np.
  „bucketing" prędkości.
- **Dane testowe (YAML):** wartości → odpowiednie `case`; `null`; `default`.
- **Akceptacja:** poprawne buckety i `default`.

---
## Notes
- **Order = JEP order** (409, 356, 408, 431, 440, 441, 444, 467, 485, 506, 511, 513,
  517, 524, 525, 526, 530). Skipped: JDK 22 (454/456/458), 484, 512, 529.
- Wszystkie dane/structure podane jako **YAML/markdown** — uczestnik mapuje je na kod.
- Widok (SVG) dochodzi w E3 i jest odświeżany ręcznie; późniejsza logika tylko go
  wzbogaca.

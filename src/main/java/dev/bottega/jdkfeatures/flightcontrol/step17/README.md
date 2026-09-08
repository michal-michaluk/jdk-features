# Step 17 — JEP 530 — Primitive Type Patterns in `switch` (preview)

> **JDK 26 (preview)** · Klasyfikacja prędkości `switch`em po **wzorach prymitywnych**
> (`case int`/`case long`/`case double`) z `when`.

## Architektura: DOMENA vs INFRASTRUKTURA

Podział pozostaje; ten krok **dodaje logikę do domeny**, serwer pozostaje cienki:

- **Domena** (`...step17.domain`) — model + operacje skopiowane bez zmian z kroku 16
  (w tym `import module java.base;` w `Airspace` (jako jedyny plik), a także `KeyMaterial`
  (JEP 524), `SectorScanner` (JEP 525), `RiskEvaluator` (JEP 526), `SimulationContext`,
  `Radar`, `ThreatClassifier`, `Telemetry`, `SpeedLimit`) plus **nowe klasy** `SpeedBucket`
  (enum) i `SpeedClassifier`.
  `SpeedClassifier.classifyBySpeed(Number)` to `switch`-wyrażenie po **wzorach prymitywnych**
  (`case int`, `case long`, `case double`) z **guardami** `when`, `case null` i `default` —
  bucketuje liczbę do `SLOW`/`CRUISE`/`FAST`/`UNKNOWN`. Używa tylko `java.util.*` —
  żadnego I/O, żadnej sieci.
- **Infrastruktura** (`...step17.server`) — `AirspaceServer` + `JsonSerde` skopiowane
  bez zmian z kroku 16 (tylko pakiet `...step17.server` i string `step-17` w Javadoc/bannerze).
  Serwer nie zna `SpeedClassifier` — to wątek domeny.

## Cel ćwiczenia
Poznasz **Primitive Types in Patterns** (JEP 530): dopasowujesz **prymitywne** typy w
`switch`/`instanceof`. W Flight Control klasyfikujesz **prędkość** samolotu
(`SLOW`/`CRUISE`/`FAST`) — a że selektorem jest `Number`, każdy wzorzec prymitywny łapie
dokładnie swój „boxed" typ (`Integer` → `case int`, `Long` → `case long`, `Double` →
`case double`), a obcy `Number` (np. `Float`) i `null` idą do `default`/`case null`.

## Co zrobić
1. Napisz `switch (Number speed)` z wzorcami `case int`, `case long`, `case double`
   + **guard** `when` (progi `SLOW`/`CRUISE`/`FAST`).
2. Dodaj `case null` i `default` (→ `UNKNOWN`).
3. Przetestuj **wszystkie** gałęzie: int/long/double, granice (`150`, `300`), `null`,
   oraz `Number` innego typu (`Float` → `default`).
4. Opcjonalnie wywołaj z domeny: `classify(aircraft)` liczy prędkość i bucketuje.

## Dane testowe (YAML)
```yaml
speed:
  slow:   [50, 50L, 50.0]           #  < 150
  cruise: [150, 150L, 150.0]        #  150 .. < 300
  fast:   [300, 300, 400L, 400.0]   #  >= 300
unknown: [null, 50f, BigDecimal("50")]  # case null / default
```

## Napisz testy (akceptacja)
- `case int`, `case long`, `case double` — każda gałąź osiągnięta (SLOW/CRUISE/FAST).
- Granice: `150` → `CRUISE`, `300` → `FAST`.
- `null` → `UNKNOWN` (`case null`); inny `Number` (np. `Float`) → `UNKNOWN` (`default`).
- `classify(aircraft)` liczy prędkość i zwraca bucket.
- (ciągłość) testy domeny i kontrakt serwera z kroku 16 nadal przechodzą; pakiet
  `step17.domain` ma **100% pokrycia linii**.

## Wskazówki
- JEP 530 jest **preview** (JDK 26) → `--enable-preview` przy kompilacji/run.
- `switch` nie może mieć selektora `double` — **dlatego** wzorzec prymitywny działa na
  `Number` (boxed) i dopasowuje dokładny typ (`case double` ↔ `Double`).
- Ustaw `case null`/`default` na końcu; wzorce prymitywne dopasowują **dokładny** typ
  boxed, więc `default` łapie `Float`/`BigDecimal`/oboje.
- **Kolejność ma znaczenie** przy konwersjach rozszerzających — tu trzymamy wzorce
  `int` → `long` → `double` i osobne progi `when`, żeby wyniki były jednoznaczne.
- **Do przemyślenia:** co dają wzorce prymitywne zamiast ręcznego `if-else` na
  `instanceof Number` + rzutowania? (deklaratywność, wyczerpywalność, brak `cast`).

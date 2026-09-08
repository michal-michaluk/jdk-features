# Step 15 — JEP 525 — Structured Task Scope (preview)

> **JDK 26 (preview)** · Skany sektorów jako **strukturalna współbieżność**: `fork`/`join`
> w `StructuredTaskScope` z `Joiner`.

## Architektura: DOMENA vs INFRASTRUKTURA

Podział pozostaje; ten krok **dodaje logikę do domeny**, serwer pozostaje cienki:

- **Domena** (`...step15.domain`) — model + operacje skopiowane bez zmian z kroku 14
  (w tym `import module java.base;` w `Airspace` (jako jedyny plik), a także `KeyMaterial`
  (JEP 524), `SimulationContext`, `Radar`, `ThreatClassifier`, `Telemetry`, `SpeedLimit`)
  plus **nowe klasy** `SectorScanner`,
  `ScanReport` i `SectorScanException`. `SectorScanner` jest **strukturalnym** odpowiednikiem
  ręcznie zakręconego `Radar` (JEP 444): zamiast `Thread.start()` + `join()` każde zadanie
  jest `fork`owane do **własnego** `StructuredTaskScope` i `join`owane z powrotem zanim scope
  wyjdzie — nic nie może go „przeżyć" i błędy łapane są w jednym miejscu. Używa tylko
  `java.util.concurrent.*` — żadnego I/O, żadnej sieci.
- **Infrastruktura** (`...step15.server`) — `AirspaceServer` + `JsonSerde` skopiowane
  bez zmian z kroku 14 (tylko pakiet `...step15.server` i string `step-15` w Javadoc/bannerze).
  Serwer nie zna `SectorScanner` — to wątek domeny.

## Cel ćwiczenia
Poznasz **Structured Task Scope** (JEP 525): zbierasz zadania w *scope*, które **jesteś
zobowiązany zamknąć**, a wynik (`Subtask.get()`) odbierasz dopiero po `join()`. Jak w `Radar`
z kroku 10, ale zamiast ręcznego `Thread` — `scope.fork(...)`, `scope.join()`,
`subtask.state()`/`subtask.get()`, oraz **fail-fast** (`awaitAllSuccessfulOrThrow()` → rzuca,
gdy każde zadanie ma się powieść, a któreś padnie).

## Co zrobić
1. Otwórz `StructuredTaskScope` z `Joiner.awaitAllSuccessfulOrThrow()` (albo
   `allSuccessfulOrThrow()` dla agregacji listy wyników).
2. `fork()` jedno zadanie na sektor/statek — każde zadanie liczy coś dla domeny
   (np. `ScanReport` z kategorią i prędkością).
3. `join()` zbierz wyniki; odczytaj `subtask.state()` (SUCCESS) i `subtask.get()`.
4. Obsłuż **fail-fast**: padające zadanie → `FailedException` → zamknij scope i zgłoś
   błąd domeny (`SectorScanException`).

## Dane testowe (YAML)
```yaml
aircraft:
  - {id: a1, pos: [1,0], vel: [20,0]}   # ALARM
  - {id: a2, pos: [1,0], vel: [30,0]}   # ALARM
  - {id: a3, pos: [1,0], vel: [5,0]}    # SECTOR
  - {id: a4, pos: [9,9], vel: [1,1]}    # NORMAL
scope:
  - all fork succeed -> subtask.state() == SUCCESS, get() zwraca ScanReport
  - failing fork -> FailedException -> SectorScanException (fail-fast)
```

## Napisz testy (akceptacja)
- `scanAll()` zwraca **wszystkie** raporty w kolejności wejścia (pojedyncze zadanie na
  statek, współbieżnie).
- Każdy `Subtask` kończy się `SUCCESS`; `subtask.get()` zwraca dane (przy agregacji).
- **Fail-fast:** padające zadanie → `scope.join()` rzuca `FailedException` →
  `SectorScanException` (z `cause` do `FailedException`), a `isCancelled()` = `true`.
- Scope jest **zamknięty** (try-with-resources) po zakończeniu.
- (ciągłość) testy domeny i kontrakt serwera z kroku 14 nadal przechodzą; pakiet
  `step15.domain` ma **100% pokrycia linii**.

## Wskazówki
- JEP 525 jest **preview** (JDK 26) → `--enable-preview` przy kompilacji/run.
- W JDK 26 `StructuredTaskScope` to **interfejs** z `Joiner` — `ShutdownOnFailure` już nie
  istnieje. Fail-fast daje `Joiner.awaitAllSuccessfulOrThrow()` (join → `Void`, rzuca na
  pierwszym błędzie) / `allSuccessfulOrThrow()` (join → `List<T>`).
- `FailedException` (nested `StructuredTaskScope.FailedException`) niesie `cause` = wyjątek
  padającego zadania; `subtask.state()` to `SUCCESS`/`FAILED`/`UNAVAILABLE`.
- **Do przemyślenia:** po co `StructuredTaskScope` zamiast gołych wątków? (gwarancja
  zamknięcia wszystkich zadań, jedno miejsce obsługi błędów, brak „uciekających" wątków).

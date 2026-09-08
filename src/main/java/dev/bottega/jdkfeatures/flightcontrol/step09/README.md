# Step 09 — JEP 485 — Stream Gatherers

> **JDK 24 (final)** · Agregujesz telemetrię — okna i foldy na strumieniu `Stream.gather(...)`.

## Cel ćwiczenia
Poznasz **Stream Gatherers** (JEP 485): nową fazę `Stream.gather(Gatherer)` obok
`map`/`filter`/`reduce`. Gatherer pozwala na **stanowe** i **okienne** przekształcenia, które
dawniej wymagały własnych akumulatorów. W Flight Control agregujesz telemetrię: **okna** pozycji
statku w czasie (`windowFixed`/`windowSliding`) i **fold** do sumy oraz do **bounding box**
(min/max) przez `Gatherers.fold`.

## Co zrobić
1. `Telemetry(windowFixed(n))` → nierozłączne chunki o `n` elementach; **ostatni chunk może być
   krótszy**.
2. `Telemetry(windowSliding(n))` → przesuwne, zachodzące okna (przesunięcie o 1 element).
3. `Telemetry(foldSum())` → `Stream.gather(Gatherers.fold(initial, accumulator))` — suma.
4. `Telemetry(foldBounds())` → `Gatherers.fold` z akumulatorem przenoszącym bieżące min/max —
   wynik `Bounds(min, max)`.
5. `Telemetry.sample()` — 8 wartości z YAML poniżej.

> Uwaga: w JDK 26 publiczne `Gatherers.fold(initial, accumulator)` przyjmuje **sumator
> `BiFunction`**, a nie osobny finalizator — wynik jest emitowany na końcu strumienia (fold
> zawsze emituje stan początkowy nawet dla pustego strumienia). Dlatego `foldBounds()` jawnie
> obsługuje pusty strumień i zwraca `Bounds(0, 0)`.

## Dane testowe (YAML)
```yaml
telemetry: [10, 20, 30, 40, 50, 60, 70, 80]   # 8 wartości
combinations:
  windowFixed(3): [[10,20,30],[40,50,60],[70,80]]   # ostatnie niepełne okno
  windowFixed(10): [[10,20,30,40,50,60,70,80]]      # jedno niepełne okno (cały strumień)
  windowSliding(2): [[10,20],[20,30],[30,40],[40,50],[50,60],[60,70],[70,80]]
  fold(sum): 360
  fold(bbox): {min: 10, max: 80}
```

## Napisz testy (akceptacja)
- `windowFixed(3)` daje dokładnie chunki z tabeli (ostatnie niepełne okno `[70,80]`).
- `windowSliding(2)` daje przesuwne, zachodzące okna (7 okien).
- `foldSum()` zwraca **360**, `foldBounds()` zwraca `Bounds(10, 80)` (min/max).
- Pusty strumień: `foldSum()` → `0`, `foldBounds()` → `Bounds(0, 0)`, okna puste.
- `Telemetry` jest **niezmienna** (defensywna kopia listy, `readings()` niemutowalna).
- (ciągłość) testy domeny i kontrakt serwera z kroku 08 nadal przechodzą; pakiet
  `step09.domain` ma **100% pokrycia linii**.

## Wskazówki
- `Gatherers` jest w `java.util.stream.Gatherers`; używasz `readings().stream().gather(...)`.
- `windowFixed`/`windowSliding`: ostatnie/„wystające" okno może być **krótsze** — uwzględnij to
  w teście.
- `fold` przyjmuje `initial` + akumulator (`BiFunction`) i emituje **jeden** wynik.
- **Do przemyślenia:** czym `windowSliding` różni się od `windowFixed` w kontekście „śledzenia
  toru samolotu" (gdzie chcesz widzieć trend, nie rozłączne kawałki)?

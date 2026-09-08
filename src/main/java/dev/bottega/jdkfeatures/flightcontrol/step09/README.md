# Step 09 — JEP 485 — Stream Gatherers

> **JDK 24 (final)** · Agregujesz telemetrię — okna i foldy na strumieniu.

## Cel ćwiczenia
Poznasz **Stream Gatherers** (JEP 485): nową fazę `Stream.gather(Gatherer)` obok
`map`/`filter`/`reduce`. Gatherer pozwala na **stanowe** i **okienne** przekształcenia,
które dawniej wymagały własnych akumulatorów. W Flight Control agregujesz telemetrię:
**okna** pozycji samolotu w czasie, **fold** do bounding box, zgrupowanie po sektorach.

## Co zrobić
1. Z listy pozycji/telemetrii zbuduj strumień i użyj `Stream.gather(...)`.
2. `Gatherers.windowFixed(n)` → nierozłączne okna o `n` elementach.
3. `Gatherers.windowSliding(n)` → przesuwne okna.
4. `Gatherers.fold(...)` → redukcja z akumulatorem (np. suma, bbox).
5. (opcjonalnie) zgrupowanie telemetrii po sektorze — okna per obszar.

## Dane testowe (YAML)
```yaml
telemetry: [10, 20, 30, 40, 50, 60, 70, 80]   # 8 wartości
combinations:
  windowFixed(3): [[10,20,30],[40,50,60],[70,80]]   # ostatnie niepełne okno
  windowSliding(2): [[10,20],[20,30],[30,40],[40,50],[50,60],[60,70],[70,80]]
  fold(sum): 360
  fold(bbox): {min: 10, max: 80}
```

## Napisz testy (akceptacja)
- `windowFixed(3)` daje dokładnie chunki z tabeli (ostatnie niepełne okno `[70,80]`).
- `windowSliding(2)` daje przesuwne, zachodzące okna.
- `fold` zwraca **sumę** i **bbox** (min/max) zgodnie z YAML.

## Wskazówki
- `Gatherers` jest w `java.util.stream.Gatherers`. Własny `Gatherer` implementujesz, gdy
  wbudowane nie wystarczą (to zaawansowane — zacznij od wbudowanych).
- `windowFixed/k-last, sink` — ostatnie okno może być **krótsze**; uwzględnij to w teście.
- `fold` przyjmuje `initial` + akumulator + finalizator.
- **Do przemyślenia:** czym `windowSliding` różni się od `windowFixed` w kontekście
  „śledzenia toru samolotu" (gdzie chcesz widzieć trend, nie rozłączne kawałki)?

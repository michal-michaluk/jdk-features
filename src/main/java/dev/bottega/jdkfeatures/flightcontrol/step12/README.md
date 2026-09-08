# Step 12 — JEP 513 — Flexible Constructor Bodies

> **JDK 25 (final)** · Walidacja i obliczenia **przed** `super(...)`/`this(...)`.

## Cel ćwiczenia
Poznasz **Flexible Constructor Bodies** (JEP 513): w konstruktorze możesz **wykonać
instrukcje i weryfikację argumentów przed** jawnym wywołaniem `super(...)`/`this(...)`.
Wcześniej taka logika musiała być po `super`. W Flight Control to naturalne miejsce na:
`requireNonNull` argumentów, **wyliczenie wartości pochodnych** (np. limit top-speed z
typu samolotu) i **clamp** — zanim obiekt powstanie, a w `record` — zanim składowe są
ustalone.

## Co zrobić
1. W konstruktorze (klasy lub `record`) **zweryfikuj dane przed** `super(...)`: `requireNonNull`,
   zakresy, spójność (np. `radius > 0`, `vertices >= 3`, prędkość > 0).
2. **Wylicz** pochodną wartość przed super i przekaż ją (np. `topSpeed` z `category`,
   `bbox` z `vertices`).
3. **Clamp** wartość do dozwolonego zakresu (np. `maxSpeedDelta` nieujemne, kąt w [0,360)).
4. Sprawdź, że wyjątki rzucane są **zanim** obiekt/`super` wystartuje.

## Dane testowe (YAML)
```yaml
valid:   {radius: 3, vertices: 4, speed: 100, label: TMA}
derived: {topSpeedBound, bbox}
invalid:
  - {vertices: 2}        # -> IllegalArgumentException PRZED super()
  - {speed: -5}          # -> clamp do 0 / wyjątek przed super()
  - {label: null}        # -> NullPointerException (requireNonNull)
```

## Napisz testy (akceptacja)
- Niepoprawne dane (**`vertices: 2`**, **`speed: -5`**, **`null`**) rzucają wyjątek **przed**
  `super`/`this` (sprawdź, że konstruktor nie wszedł w stan).
- Wartości pochodne (top-speed, bbox) liczone poprawnie z danych wejściowych.
- Poprawne dane tworzą obiekt; clamp (ujemna prędkość → 0) działa.

## Wskazówki
- W `record` możesz wywołać **`this(...)`** (compact constructor) lub wykonać logikę przed
  `super`? — sprawdź, co JEP 513 realnie pozwala w `record`; w klasie `this`/`super`
  musi być pierwszym... nie, teraz możesz mieć instrukcje przed nim.
- **Zasada:** waliduj argumenty, zanim zbudujesz obiekt — rzucaj `NullPointerException`/
  `IllegalArgumentException` z czytelnym komunikatem.
- **Wartości pochodne:** nie trzymaj ich jako pól jeśli są wyliczalne; albo `record`
  z dodatkowym komponentem, albo `derive` w konstruktorze.
- **Do przemyślenia:** dlaczego bezpieczniej jest odrzucić `vertices: 2` od razu, niż
  wpuścić obiekt „prawie-pusty" i łapać gdzieś później?

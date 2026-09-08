# Step 12 — JEP 513 — Flexible Constructor Bodies

> **JDK 26 (final)** · Walidacja i obliczenia **przed** `super(...)`/`this(...)`.

## Architektura: DOMENA vs INFRASTRUKTURA

Podział pozostaje; ten krok **dodaje logikę do domeny**, serwer pozostaje cienki:

- **Domena** (`...step12.domain`) — model z kroku 11 (w tym `import module java.base;`
  w `Airspace`) plus **nowe klasy** `Limit` (abstrakcyjna baza) i `SpeedLimit` (wartość
  domenowa). Konstruktor `SpeedLimit` demonstruje **JEP 513**: waliduje argumenty
  (`Objects.requireNonNull` → `NullPointerException`, blank → `IllegalArgumentException`),
  **wylicza wartość pochodną** (clamp żądanej prędkości do `[0, ABSOLUTE_MAX]`) i przypisuje
  pola finalne — wszystko **przed** jawnym `super(...)`. Domeny nie interesuje sieć.
- **Infrastruktura** (`...step12.server`) — `AirspaceServer` + `JsonSerde` skopiowane
  bez zmian z kroku 11 (tylko pakiet `...step12.server` i string `step-12` w Javadoc/bannerze).
  Serwer nie zna `SpeedLimit` — to wątek domeny.

## Cel ćwiczenia
Poznasz **Flexible Constructor Bodies** (JEP 513): w konstruktorze możesz **wykonać
instrukcje i weryfikację argumentów przed** jawnym wywołaniem `super(...)`/`this(...)`.
Wcześniej taka logika musiała być po `super`. W Flight Control to naturalne miejsce na:
`requireNonNull` argumentów, **wyliczenie wartości pochodnych** (np. limit top-speed z
typu samolotu) i **clamp** — zanim obiekt powstanie.

## Co zrobić
1. W konstruktorze (`SpeedLimit`, a nie `record`) **zweryfikuj dane przed** `super(...)`:
   `requireNonNull` labela, zakres (label nie może być blank), limit prędkości >= 0.
2. **Wylicz** pochodną wartość przed `super`: skwantowany/klamowany limit
   (`effectiveMax`) oraz znormalizowany label (`trim().toUpperCase()`).
3. **Clamp** wartość do dozwolonego zakresu: ujemna wartość → `0`, powyżej
   `ABSOLUTE_MAX` → `ABSOLUTE_MAX`.
4. Sprawdź, że wyjątki rzucane są **zanim** obiekt/`super` wystartuje.

## Dane testowe (YAML)
```yaml
valid:     {label: "  tma ", requestedMaxSpeed: 250}   # -> label "TMA", effectiveMax 250, kind "speed"
clamp:     {requestedMaxSpeed: -5}                     # -> effectiveMax 0 (ujemne -> 0)
clampMax:  {requestedMaxSpeed: 2000}                   # -> effectiveMax ABSOLUTE_MAX (1000)
invalid:
  - {label: null}         # -> NullPointerException PRZED super()
  - {label: "   "}        # -> IllegalArgumentException (blank) PRZED super()
delegating: {label: "CTR"}                              # -> this(label, 0.0) -> effectiveMax 0
```

## Napisz testy (akceptacja)
- Niepoprawne dane (**`label: null`**, **blank**) rzucają wyjątek **przed** `super`/`this`
  (sprawdź, że konstruktor nie wszedł w stan).
- Wartości pochodne (znormalizowany label, `effectiveMax`) liczone poprawnie z danych.
- Clamp działa: ujemna prędkość → `0`, przekroczenie `ABSOLUTE_MAX` → `ABSOLUTE_MAX`.
- `kind()`, `label()`, `effectiveMax()`, `exceededBy(...)` dostępne; delegujący konstruktor
  `SpeedLimit(label)` przechodzi.
- (ciągłość) testy domeny i kontrakt serwera z kroku 11 nadal przechodzą; pakiet
  `step12.domain` ma **100% pokrycia linii**.

## Wskazówki
- **Zasada:** waliduj argumenty, zanim zbudujesz obiekt — rzucaj `NullPointerException`/
  `IllegalArgumentException` z czytelnym komunikatem.
- **Wartości pochodne:** nie trzymaj ich jako pól jeśli są wyliczalne; jeśli są — wylicz je
  w konstruktorze (jak `effectiveMax`) zamiast w `record`.
- JEP 513 nadal **nie pozwala** czytać `this` (pól/metod instancji) przed `super`/`this` —
  tylko operacje na lokalnych/parametrach i wywołania statyczne są legalne.
- **Do przemyślenia:** dlaczego bezpieczniej jest odrzucić `label: null` od razu, niż
  wpuścić obiekt „prawie-pusty" i łapać gdzieś później?

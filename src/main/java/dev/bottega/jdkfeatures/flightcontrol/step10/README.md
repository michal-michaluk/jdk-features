# Step 10 — JEP 506 — Scoped Values

> **JDK 25 (final)** · Kontekst przekazywany do zadań bez argumentów.

## Cel ćwiczenia
Poznasz **Scoped Values** (JEP 506): `ScopedValue` — zmienne **kontekstu** przekazywane
przez wywołania/wątki bez przeciągania ich w argumentach. W Flight Control to np.
`currentSector` i `simulationId`, które mają być widoczne w zadaniach (wirtualnych
wątkach/forkach) bez przekazywania ich parametrem. Zastępują `ThreadLocal` (bez wycieków
i z bezpiecznym, kontrolowanym cyklem życia `runWhere`).

## Co zrobić
1. Zdefiniuj `ScopedValue<String> CURRENT_SECTOR` i `ScopedValue<Long> SIMULATION_ID`.
2. Wewnątrz `ScopedValue.where(...).run(...)` ustaw wartość i wykonaj zadanie.
3. Czytaj **wewnątrz** zadań (wątków wirtualnych z kroku 07 / forków z kroku 15) —
   wartość dostępna jest dziedziczona.
4. Obsłuż przypadek **niezbindowany** (`orElse(...)`).
5. Przetestuj **zagnieżdżone** nadpisanie (wewnętrzny `where` nadpisuje zewnętrzny).

## Dane testowe (YAML)
```yaml
context:
  outerSector: TMA
  innerSector: CTR
  fallback: "UNKNOWN"
scenarios:
  - read in task          -> outerSector  # widoczne w zadaniu
  - nested override       -> innerSector  # wewnętrzne nadpisuje
  - unbound + orElse      -> "UNKNOWN"
  - after run()           -> orElse       # poza run() niezbindowane (out of scope)
```

## Napisz testy (akceptacja)
- Wartość **dostępna** wewnątrz zadania (dziedziczona do wątku/forku).
- **Zagnieżdżone** nadpisanie: wewnętrzny `where` wygrywa, po wyjściu wraca zewnętrzny.
- **Niezbindowane:** `orElse` zwraca fallback; odczyt poza `run()` nie rzuca błędu (zwraca
  `orElse`).

## Wskazówki
- `ScopedValue` == **immutable po `run()`**; ustawisz `where(...).run(...)`, a nie `set()`.
- **Bezpieczeństwo:** w przeciwieństwie do `ThreadLocal` wartość nie „wycieka" między
  zadaniami po zakończeniu `run()`.
- **Dziedziczenie** do wątków wirtualnych/forków działa — sprawdź, że zadanie widzi kontekst.
- `orElse` jest na odczycie (`get()`/`orElse`), gdy wartość nie jest związana w tym zakresie.
- **Do przemyślenia:** dlaczego `ScopedValue` jest lepszy od przekazywania parametru, gdy
  kontekst dotyczy wielu głęboko zagnieżdżonych wywołań?

# Step 10 — JEP 506 — Scoped Values

> **JDK 25 (final)** · Kontekst przekazywany do zadań bez argumentów.

## Cel ćwiczenia
Poznasz **Scoped Values** (JEP 506): `ScopedValue` — zmienne **kontekstu** przekazywane przez
wywołania/wątki bez przeciągania ich w argumentach. W Flight Control to `currentSector` i
`simulationId`, które mają być widoczne w zadaniach (forkach wątków wirtualnych) bez przekazywania
ich parametrem. Zastępują `ThreadLocal` (bez wycieków i z bezpiecznym, kontrolowanym cyklem życia
`where(...).run(...)`).

## Co zrobić
1. Zdefiniuj `ScopedValue<String> CURRENT_SECTOR` i `ScopedValue<Long> SIMULATION_ID`.
2. `runWithContext(sector, id, task)` → `ScopedValue.where(...).where(...).run(...)` wiąże oba
   na czas `task` i odwiązuje po jego zakończeniu.
3. **Czytaj wewnątrz** zadania (`get()`/`orElse`). Do przekazania kontekstu do wątku wirtualnego
   użyj **`StructuredTaskScope`** (preview, JEP 453/525): scope utworzony wewnątrz aktywnego
   zakresu **dziedziczy** wiązania do zadań forkowanych (`fork`). Zwykły `Thread.ofVirtual().start()`
   dziedziczenia NIE zapewnia.
4. Obsłuż przypadek **niezbindowany** (`orElse`/fallback; `get()` poza zakresem rzuca
   `NoSuchElementException`).
5. Przetestuj **zagnieżdżone** nadpisanie (wewnętrzny `where` wygrywa, potem wraca zewnętrzny).

## Dane testowe (YAML)
```yaml
context:
  outerSector: TMA
  innerSector: CTR
  simulationId: 42
  fallback: "UNKNOWN"
scenarios:
  - read in fork/task      -> outerSector   # widoczne w zadaniu (StructuredTaskScope)
  - nested override        -> innerSector   # wewnętrzne nadpisuje
  - unbound + orElse       -> "UNKNOWN"     # orElse daje fallback
  - after run()            -> orElse        # poza run() niezbindowane (out of scope)
```

## Napisz testy (akceptacja)
- Wartość **dostępna** wewnątrz zadania (`forkInTask`) — dziedziczona do wątku wirtualnego.
- **Zagnieżdżone** nadpisanie: wewnętrzny `where` wygrywa, po wyjściu wraca zewnętrzny.
- **Niezbindowane:** `currentSectorOr("UNKNOWN")` zwraca fallback; `get()` poza `run()` rzuca
  `NoSuchElementException`.
- **Out of scope:** po zakończeniu `run()` wiązanie znika (`orElse` → fallback, `isSectorBound()==false`).
- (ciągłość) testy domeny i kontrakt serwera z kroku 09 nadal przechodzą; pakiet
  `step10.domain` ma **100% pokrycia linii**.

## Wskazówki
- `ScopedValue` jest **immutable po `run()`** — ustawiasz `where(...).run(...)`, a nie `set()`.
- **Dziedziczenie** idzie przez `StructuredTaskScope` (fork) lub `try` aktywnego zakresu, nie przez
  dowolny wątek; sprawdź, że zadanie forkowane widzi kontekst.
- `orElse` jest bezpieczne przy odczycie (`get()`/`orElse`), gdy wartość nie jest związana.
- **Do przemyślenia:** dlaczego `ScopedValue` jest lepszy od przekazywania parametru, gdy kontekst
  dotyczy wielu głęboko zagnieżdżonych wywołań?

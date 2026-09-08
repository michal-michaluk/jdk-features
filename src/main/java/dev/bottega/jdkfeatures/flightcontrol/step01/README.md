# Step 01 — JEP 409 — Sealed Classes & Records

> **JDK 17 (final)** · Zaczynasz domenę Flight Control. Kolejność = kolejność JEP; ten krok
> buduje model, na którym oprzesz wszystkie dalsze.

## Cel ćwiczenia
Poznasz **sealed types** (JEP 409) i **records**. Obszary przestrzeni powietrznej (`Area`)
mają dokładnie dwie postacie — koło i wielokąt — więc `sealed interface` z `permits`
idealnie modeluje zamknięty zbiór typów, a `record` daje zwięzłe, niezmienne wartości
(pozycja, wektor, samolot, obszar).

## Co zbudować
Mapuj opis poniżej na kod — **nie kopiuj** (struktura i dane, nie gotowe klasy):
1. **Wartości:** `Point(x, y)` i `Velocity(dx, dy)`.
2. **Samolot:** `Aircraft(id, label, callsign, pos, vel)` — ruchomy punkt + etykieta.
3. **Obszary:** `Circle(center, radius, label, props)` i `Polygon(vertices, label, props)`.
4. Wspólne cechy (`label`, `props`) wynieś do wspólnego typu `Area`.
5. `Area` = **sealed interface** z dokładnie dwoma `permits` (`Circle`, `Polygon`).
6. **`Airspace`** — trzyma listy `aircraft` i `areas`, oraz `step()` przesuwający każdy
   samolot o jego `vel` (zwraca **nowy**, niezmienny stan).
7. **`describe(Area)`** — funkcja klasyfikująca obszar po typie (wyczerpujący `switch`).

## Dane testowe (YAML) — zamapuj na własny kod
```yaml
aircraft:
  - {id: a1, label: FOX,  pos: {x: 10, y: 0}, vel: {dx: 1, dy: 0}}
  - {id: a2, label: ECHO, pos: {x: 0,  y: 5}, vel: {dx: 0, dy: 1}}
areas:
  - {circle: {center: {x: 0, y: 0}, radius: 3, label: CTR, props: {type: control}}}
  - {polygon: {vertices: [{x: 0, y: 0}, {x: 4, y: 0}, {x: 4, y: 4}], label: TMA, props: {kind: terminal}}}
after step(): a1 -> (11,0), a2 -> (0,6)
```

## Napisz testy (akceptacja)
- `describe(Area)` wyczerpujący **bez** `default` (sealed ⇒ kompilator wymusza kompletność).
- `step()` przesuwa o `vel` zgodnie z tabelką; **oryginał bez zmian** (immutability).
- `Airspace` przyjmuje oba podtypy `Area`.

## Wskazówki
- `record` generuje konstruktor, akcesory, `equals`/`hashCode`/`toString`.
- `sealed` wymusza wyczerpujący `switch` w `describe` — nie zapomnisz o żadnym podtypie.
- **Immutability:** nie zwracaj wewnętrznych list/pól przez referencję — zwróć kopię
  (np. `List.copyOf(...)`) lub niezmienny widok.
- **Do przemyślenia:** co się stanie, gdy dodasz trzeci podtyp `Area`? Czy `describe` nadal
  się kompiluje?

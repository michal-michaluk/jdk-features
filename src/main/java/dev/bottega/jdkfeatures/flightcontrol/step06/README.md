# Step 06 — JEP 441 — Pattern Matching for switch

> **JDK 21 (final)** · Klasyfikujesz samoloty „alarm / sektor / normal" przez `switch`.

## Cel ćwiczenia
Poznasz **Pattern Matching for switch** (JEP 441): `switch` jako wyrażenie z **type
patterns** i strażnikami `when` + obsługa `case null`. W Flight Control to klasyfikacja
zagrożenia: samolot dostaje kategorię na podstawie prędkości, kierunku i tego, czy leci w
sektorze. Logika „co z tym zrobić" jest czytelna i **wyczerpująca** (sealed + `null`
wymuszone przez kompilator).

## Co zrobić
1. Zdefiniuj **kategorie** (np. `ALARM`, `SECTOR`, `NORMAL`, `UNKNOWN`).
2. `classify(Aircraft)` przez `switch` na typie (`Aircraft`, `null`) z `when`:
   - `case Aircraft a when a.speed() > alarmMinSpeed && inSector(a) -> ALARM`,
   - `case Aircraft a when inSector(a) -> SECTOR`,
   - `case Aircraft a -> NORMAL`,
   - `case null -> UNKNOWN`.
3. Dodaj pomocnicze `speed()` (długość wektora) i `inSector(...)` (czy w obszarze).
4. Reguły (`minSpeed`, zakres sektora) parametryzuj — z YAML poniżej.

## Dane testowe (YAML) — reguła + obiekty
```yaml
alarm:
  minSpeed: 300
  sectorDeg: [0, 90]     # kierunek (azimut) = sektor
aircraft:
  - {label: HOT,   pos: {x: 9, y: 1}, vel: {dx: 10, dy: 0}, speed: 100}   # w sektorze, >minSpeed -> ALARM
  - {label: COOL,  pos: {x: 1, y: 0}, vel: {dx: 2,  dy: 0}, speed: 20}    # w sektorze -> SECTOR
  - {label: AWAY,  pos: {x: 9, y: 9}, vel: {dx: 1,  dy: 1}, speed: 15}    # poza sektorem -> NORMAL
  - null                                                                  # -> UNKNOWN
```

## Napisz testy (akceptacja)
- Każdy `case`/guard: ALARM (prędkość > minSpeed **i** w sektorze), SECTOR (tylko sektor),
  NORMAL (poza), UNKNOWN (`null`).
- Wyczerpujący `switch` po sealed (brak `default` jeśli wszystkie typy pokryte) — albo
  świadomy `default`.
- `switch` zwraca wartość (expression) — przypisujesz wynik, nie `return`.

## Wskazówki
- Strażnik `when` pozwala warunkować dopasowanie bez twardego zagnieżdżania `if`.
- `case null` **wymusza** jawną obsługę `null` w przełączaniu (JEP 441) — nie musisz pisać
  osobnego `if (x == null)`.
- „W sektorze": azimut z `atan2(dy, dx)` (od 0–360°), porównaj z `sectorDeg`.
- Kolejność `case` ma znaczenie — szerszy wzorzec (`Aircraft`) umieść **po** węższych
  z `when`, inaczej wcześniejszy złapie wszystko.
- Wartość `switch` po **sealed** + bez `default` jest wyczerpująca; kompilator to sprawdzi.

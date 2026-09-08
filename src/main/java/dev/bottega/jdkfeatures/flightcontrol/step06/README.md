# Step 06 — JEP 441 — Pattern Matching for switch

> **JDK 21 (final)** · Klasyfikujesz samoloty „alarm / sektor / normal" przez `switch`.

## Cel ćwiczenia
Poznasz **Pattern Matching for switch** (JEP 441): `switch` jako **wyrażenie** z **type
patterns** i strażnikami `when` + obsługa `case null`. W Flight Control to klasyfikacja
zagrożenia: samolot dostaje kategorię na podstawie prędkości i tego, czy leci w sektorze.
Logika „co z tym zrobić" jest czytelna i **wyczerpująca** (finalny typ `Aircraft` + `null`).

## Co zrobić
1. Zdefiniuj **kategorie** (`Category`): `ALARM`, `SECTOR`, `NORMAL`, `UNKNOWN`.
2. `classify(Aircraft)` — `switch` na typie (`Aircraft`, `null`) z `when`:
   - `case Aircraft a when speed(a) > alarmMinSpeed && inside(a.pos(), sector) -> ALARM`,
   - `case Aircraft a when inside(a.pos(), sector) -> SECTOR`,
   - `case Aircraft a -> NORMAL`,
   - `case null -> UNKNOWN`.
3. Dodaj pomocnicze `speed(Aircraft)` (długość wektora prędkości) i
   `inside(Point, Area)` (czy punkt w obszarze — tu: koło sektora).
4. Parametryzuj reguły: `alarmMinSpeed` i sektor (koło CTR) przekazuj do `ThreatClassifier`.

## Dane testowe (YAML) — reguła + obiekty
```yaml
alarm:
  minSpeed: 10
  sector: {kind: circle, center: {x: 0, y: 0}, radius: 3, label: CTR}
aircraft:
  - {label: HOT,   pos: {x: 1, y: 0}, vel: {dx: 20, dy: 0}}   # w sektorze, speed>min -> ALARM
  - {label: COOL,  pos: {x: 1, y: 0}, vel: {dx: 5,  dy: 0}}   # w sektorze, speed<=min -> SECTOR
  - {label: AWAY,  pos: {x: 9, y: 9}, vel: {dx: 1,  dy: 1}}   # poza sektorem -> NORMAL
  - null                                                        # -> UNKNOWN
expect: speed(HOT)=20, speed(COOL)=5, speed(AWAY)=~1.41
```

## Napisz testy (akceptacja)
- Każdy `case`/guard: ALARM (prędkość `> minSpeed` **i** w sektorze), SECTOR (w sektorze,
  `speed <= min`), NORMAL (poza), UNKNOWN (`null`).
- Granica: `speed == minSpeed` **nie** jest ALARM (strażnik jest ostry `>`).
- `speed()` = długość Euklidesowa wektora; `inside()` na granicy/wewnątrz/poza kołem.
- `inside()` dla nie-koła (wielokąt) zwraca `false`.
- Wyczerpujący `switch`: `null` + finalny typ `Aircraft` pokrywają wszystko — bez `default`.
- (ciągłość) testy domeny i kontrakt serwera z kroku 05 nadal przechodzą; pakiet
  `step06.domain` ma **100% pokrycia linii**.

## Wskazówki
- Strażnik `when` pozwala warunkować dopasowanie bez twardego zagnieżdżania `if`.
- `case null` **wymusza** jawną obsługę `null` w przełączaniu (JEP 441) — nie musisz pisać
  osobnego `if (aircraft == null)`.
- „W sektorze": odległość od środka koła `<= radius` — policz `Math.hypot(dx, dy)`.
- Kolejność `case` ma znaczenie — szerszy wzorzec (`Aircraft`) umieść **po** węższych
  z `when`, inaczej wcześniejszy złapie wszystko.
- `switch` jako **wyrażenie** zwraca wartość (przypisujesz wynik, nie `return`).
- **Do przemyślenia:** dlaczego `case null` jest potrzebny skoro `Aircraft` jest finalny?

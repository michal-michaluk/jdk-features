# Step 17 — JEP 530 — Primitive Patterns (preview)

> **JDK 26 (preview)** · Przełączasz po **typach prymitywnych** w `switch`.

## Cel ćwiczenia
Poznasz **Primitive Patterns** (JEP 530): `switch` dopasowuje **typy prymitywne** —
`case int`, `case long`, `case double`, `case byte`… z konwersją i `default`. Wcześniej
`switch` po `Object` nie mógł odróżnić `Integer` od `Long` od `Double` w jednym wyrażeniu.
W Flight Control to wygodne **bucketing** prędkości/wysokości: wartość `double` wpada do
przedziału (case `double` + strażnik) albo typowanej kategorii.

## Co zrobić
1. Zbuduj `bucketing(speed)` przez `switch` na `double`/`Number`:
   - `case double v when v < 50 -> SLOW`,
   - `case double v when v < 300 -> CRUISE`,
   - `case double v -> FAST`.
2. (opcjonalnie) rozróżnij typ: `case Integer i` vs `case Long l` vs `case Double d` w
   jednym `switch` — tego wcześniej nie dało się zrobić.
3. Obsłuż `null`/`default`.
4. Zwróć kategorię jako wartość switch (expression).

## Dane testowe (YAML)
```yaml
speedBuckets:
  {min: 0,   max: 50,  bucket: SLOW}
  {min: 50,  max: 300, bucket: CRUISE}
  {min: 300, max: +inf, bucket: FAST}
values:
  10.0 -> SLOW
  120.0 -> CRUISE
  400.0 -> FAST
  null -> UNKNOWN (default/null)
```

## Napisz testy (akceptacja)
- Wartości wpadają do właściwych `case` (`10.0→SLOW`, `120→CRUISE`, `400→FAST`).
- `null` → `default` (lub `case null`).
- (opcjonalnie) różne typy `Integer`/`Long`/`Double` rozróżnione w jednym `switch`.

## Wskazówki
- JEP 530 jest **preview** (JDK 26) → `--enable-preview`.
- Dopasowanie prymitywu: `case int` dopasowuje `int`/`Integer` (po dereferencji boxingu), a
  `case double` po konwersji — **sprawdź konwersje** w dokumentacji (nie zgaduj).
- Strażnik `when` pozwala na **zakresy** (`when v < 300`).
- Kolejność `case` ma znaczenie: węższy zakres (`< 50`) przed szerszym (`else`-owe).
- **Do przemyślenia:** jakie zalety ma tu `switch` z `when` vs. `if/else if`? (czytelność,
  wyczerpywalność, brak pomyłek w zakresach)

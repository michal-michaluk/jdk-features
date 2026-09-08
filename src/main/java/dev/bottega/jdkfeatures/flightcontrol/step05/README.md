# Step 05 — JEP 440 — Record Patterns

> **JDK 21 (final)** · Destrukturyzujesz model w `describe(Area)` z kroku 01.

## Cel ćwiczenia
Poznasz **Record Patterns** (JEP 440): dopasowanie do **komponentów rekordu** w
`instanceof`/`switch`. Zamiast rzutować i wołać akcesory, rozpakowujesz od razu
`Circle(center, radius, label, props)` i `Polygon(vertices, label, props)`. To czystsza
i bezpieczniejsza (pusty `null`/zły typ ⇒ brak dopasowania) destrukturyzacja modelu.

## Co zrobić
1. Przepisz `describe(Area)` z kroku 01 na **record pattern** w `switch`:
   - `case Circle(Point c, double r, String label, Props p) -> ...`,
   - `case Polygon(List<Point> v, String label, Props p) -> ...`.
2. Wyciągaj `label`, `radius`/`vertices`, `props` bezpośrednio z dopasowania.
3. Zagnieżdżony pattern: rozpakuj `Point c` wewnątrz `Circle` (np. `Point(double x, double y)`).
4. (opcjonalnie) dodaj inną destrukturyzację — np. wyznaczanie środka/średnicy obszaru.

## Dane testowe (YAML) — obiekty do destrukturyzacji
```yaml
areas:
  - {circle: {center: {x: 0, y: 0}, radius: 3, label: CTR, props: {type: control}}}
  - {polygon: {vertices: [{x: 0, y: 0}, {x: 4, y: 0}, {x: 4, y: 4}], label: TMA, props: {kind: terminal}}}
expect (dla CTR):  label=CTR, center=(0,0), radius=3, props={type:control}
expect (dla TMA):  label=TMA, vertices=3 punkty, props={kind:terminal}
```

## Napisz testy (akceptacja)
- Dla `Circle` i `Polygon` wydobywasz **wszystkie** komponenty zgodnie z powyższym.
- Zagnieżdżony pattern (`Point(x, y)`) poprawnie rozpakowuje współrzędne.
- Brak dopasowania dla `null` lub nieznanego typu nie rzuca `ClassCastException`
  (to jest właśnie korzyść pattern matching).

## Wskazówki
- Składnia `instanceof Circle(Point c, double r, ... )` — typy komponentów muszą zgadzać się
  z deklaracją rekordu.
- Kompilator potrafi **wywnioskować** typy w zagnieżdżonym patternie (`var` lub typ).
- W `switch` po sealed `Area` wciąż wymagane są **wszystkie** podtypy (bez `default`) —
  record pattern i sealed typ współpracują.
- **Do przemyślenia:** jak zmieni się `describe`, gdy użyjesz `case Polygon(..., String label, ...)`
  zamiast odwoływać się do `label()` po dopasowaniu?

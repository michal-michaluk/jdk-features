# Step 05 — JEP 440 — Record Patterns

> **JDK 21 (final)** · Destrukturyzujesz model w `describe(Area)` z kroku 04.

## Architektura: DOMENA vs INFRASTRUKTURA

Podział z kroku 04 pozostaje: model i operacje na nim tworzą **domenę**, a serwer to tylko
cienka **infrastruktura** wystawiająca dane. Ten krok zmienia **wyłącznie** implementację
domenowej metody `describe(Area)` — kontrakt serwera zostaje nietknięty:

- **Domena** (`...step05.domain`) — `describe(Area)` przepisany na **record patterns**
  (JEP 440): komponenty obszaru są wydestrukturyzowane od razu w dopasowaniu, łącznie z
  **zagnieżdżonym** patternem na punkt środka. Pakiet domeny nadal **nie importuje** klas
  sieciowych.
- **Infrastruktura** (`...step05.server`) — skopiowana bez zmian z kroku 04: `GET /aircraft`,
  `GET /areas`, `GET /`, `POST /tick`. Serwer nie wie, jak wygląda destrukturyzacja obszaru.

## Cel ćwiczenia
Poznasz **Record Patterns** (JEP 440): dopasowanie do **komponentów rekordu** w
`instanceof`/`switch`. Zamiast rzutować i wołać akcesory, rozpakowujesz od razu
`Circle(center, radius, label, props)` i `Polygon(vertices, label, props)`. To czystsza
i bezpieczniejsza (pusty `null`/zły typ ⇒ brak dopasowania) destrukturyzacja modelu.

## Co zrobić
1. Przepisz `describe(Area)` z kroku 04 na **record pattern** w `switch`:
   - `case Circle(Point center, double radius, String label, Map props) -> ...`,
   - `case Polygon(List<Point> vertices, String label, Map props) -> ...`.
2. Wyciągaj `label`, `radius`/`vertices` oraz `props` bezpośrednio z dopasowania — bez
   odwoływania się do `label()` itd. po dopasowaniu.
3. Zagnieżdżony pattern: rozpakuj `center` wewnątrz `Circle` jako `Point(double x, double y)`
   i zbuduj z niego tekst środka (np. `center=(x,y)`).
4. Zbuduj tekst opisu z **wydestrukturyzowanych komponentów**: `label`, `radius`/`vertices`,
   współrzędne środka.
5. Zachowaj **wyczerpywalność**: `switch` po sealed `Area` bez `default` (oba podtypy pokryte).
6. Sprawdź, że serwer z kroku 04 działa bez zmian (ten krok nie zmienia kontraktu).

## Dane testowe (YAML) — obiekty do destrukturyzacji
```yaml
areas:
  - {circle: {center: {x: 0, y: 0}, radius: 3, label: CTR, props: {type: control}}}
  - {polygon: {vertices: [{x: 0, y: 0}, {x: 4, y: 0}, {x: 4, y: 4}], label: TMA, props: {kind: terminal}}}
expect (dla CTR):  label=CTR, center=(0,0), radius=3, props={type:control}
expect (dla TMA):  label=TMA, vertices=3, props={kind:terminal}
```

## Napisz testy (akceptacja)
- Dla `Circle` i `Polygon` wydobywasz **wszystkie** komponenty zgodnie z powyższym.
- Zagnieżdżony pattern (`Point(double x, double y)`) poprawnie rozpakowuje współrzędne środka.
- `describe` **nie modyfikuje** obszaru (immutability).
- `describe` jest wyczerpujący po sealed `Area` — kompilator wymusza kompletność (brak `default`).
- (ciągłość) testy domeny i kontrakt serwera z kroku 04 nadal przechodzą; pakiet
  `step05.domain` ma **100% pokrycia linii**.

## Wskazówki
- Składnia `case Circle(Point(double x, double y), double radius, String label, Map props)` —
  typy komponentów muszą zgadzać się z deklaracją rekordu.
- Kompilator potrafi **wywnioskować** typy w zagnieżdżonym patternie (`var` lub typ).
- W `switch` po sealed `Area` wciąż wymagane są **wszystkie** podtypy (bez `default`) —
  record pattern i sealed typ współpracują.
- **Do przemyślenia:** jak zmieni się `describe`, gdy dodasz trzeci podtyp `Area`? Czy
  `switch` nadal się kompiluje?

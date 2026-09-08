# Step 16 — JEP 526 — Lazy Constants (preview)

> **JDK 26 (preview)** · Ciężkie wartości liczone **raz**, leniwie.

## Cel ćwiczenia
Poznasz **Lazy Constants** (JEP 526): `java.util.LazyConstant.of(supplier)` — wartości,
które są **wyliczane raz**, dopiero w momencie pierwszego odczytu, i potem współdzielone
(wątkowo-bezpiecznie). W Flight Control to ciężkie dane globalne: projekcja mapy obszarów,
ocena ryzyka, statystyki telemetrii — policzone raz, używane wszędzie.

## Co zrobić
1. Zdefiniuj `LazyConstant` dla **ciężkiej** wartości (np. `Map<label,Area>` lub obiekt
   oceny ryzyka liczonej z całego `Airspace`).
2. Odczytuj przez `get()` — policzone przy pierwszym `get`, potem cachowane.
3. Obsłuż `orElse`/`isInitialized` przed/po inicjalizacji.
4. Porównaj z wariantem „policz od razu" — zobacz różnicę (efektywne jednokrotne liczenie).

## Dane testowe (YAML)
```yaml
heavy:
  projection: "label -> Area"
  cost: 1000000        # tyle jednostek „kosztu" — licz raz, nie N razy
expect:
  get() -> policzone raz  (drugi get zwraca tę samą wartość/instancję)
  isInitialized: false -> true (po pierwszym get)
  orElse: dostarczony przed inicjalizacją
```

## Napisz testy (akceptacja)
- **Jednokrotne** policzenie: dostawca wywołany **raz** mimo wielu `get()` (licznik w
  supplierze).
- `get()` po inicjalizacji zwraca stabilną wartość; `isInitialized()` rośnie do `true`.
- `orElse` zwraca wartość zastępczą, gdy niezainicjalizowane (zależnie od API).

## Wskazówki
- `LazyConstant.of(Supplier)` — sprawdź realną sygnaturę/metody (`get()`, `isInitialized()`,
  `orElse(...)`) w dokumentacji JDK 26 (nie zgaduj).
- **Wątkowo-bezpieczne:** inicjalizacja raz — nie musisz `synchronized`.
- Używaj do **naprawdę ciężkich/rzadkich** wartości; dla tanich `final` jest prostsze.
- **Do przemyślenia:** kiedy `LazyConstant` zamiast zwykłego polimorficznego `final`?
  (gdy koszt inicjalizacji odraczamy do pierwszego faktycznego użycia)

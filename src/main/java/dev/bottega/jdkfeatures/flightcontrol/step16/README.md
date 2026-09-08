# Step 16 — JEP 526 — Lazy Constants (preview)

> **JDK 26 (preview)** · Drogie wyliczenie (projekcja ryzyka) liczone **raz**, dopiero gdy
> potrzebne — przez `java.lang.LazyConstant`.

## Cel ćwiczenia
Poznasz **Lazy Constants** (JEP 526): `LazyConstant.of(Supplier<T>)` pozwala opóźnić
**kosztowne** wyliczenie do pierwszego `get()` i zapamiętać wynik. W Flight Control to
projekcja ryzyka/overlay, której nie chcesz liczyć przy starcie — dopiero gdy ktoś jej
zażąda. Sprawdzasz, że supplier odpala się **raz**, `isInitialized()` przechodzi
false → true, a `orElse` podaje fallback zanim projekcja istnieje.

## Co zrobić
1. Stwórz `LazyConstant.of(supplier)` dla drogiej wartości (mapa `id -> risk`).
2. Czytaj przez `get()` — supplier ma się wywołać **dokładnie raz**.
3. Obserwuj `isInitialized()` (false przed, true po pierwszym `get()`).
4. Dodaj fallback `orElse(...)` — gdy projekcja jeszcze nie policzona, zwróć wartość
   zastępczą **bez** liczenia.

## Dane testowe (YAML)
```yaml
aircraft:
  - {id: a1, vel: [20,0]}   # risk 20.0
  - {id: a2, vel: [5,0]}    # risk 5.0
  - {id: a3, vel: [1,1]}    # risk ~1.414
lazy:
  supplier_calls: 1          # niezależnie ile razy wywołasz get()
  isInitialized: false -> true
  orElse: fallback gdy jeszcze nie policzone (bez liczenia)
```

## Napisz testy (akceptacja)
- **Dokładnie raz:** supplier wykonuje się **1 raz** mimo wielu `get()` (licznik w teście).
- `isInitialized()` przechodzi `false` → `true` po pierwszym `get()`.
- `orElse` zwraca fallback, gdy projekcja nie jest jeszcze policzona (i **nie** wymusza
  liczenia); gdy policzona — zwraca projekcję.
- Wartości projekcji są poprawne (risk = prędkość, `riskFor(unknown)` = 0).
- (ciągłość) testy domeny i kontrakt serwera z kroku 15 nadal przechodzą; pakiet
  `step16.domain` ma **100% pokrycia linii**.

## Wskazówki
- JEP 526 jest **preview** (JDK 26) → `--enable-preview` przy kompilacji/run.
- `LazyConstant` jest w pakiecie **`java.lang`** (nie `java.util`): `LazyConstant.of(...)`,
  `get()`, `isInitialized()`, `orElse(T)`.
- `orElse` **nie inicjalizuje** — zwraca fallback, jeśli wartość jeszcze nie policzona.
- **Do przemyślenia:** kiedy opłaca się LazyConstant zamiast zwykłego pola wyliczonego w
  konstruktorze? (gdy budowa jest droga i nie zawsze potrzebna, np. rzadko renderowany overlay)

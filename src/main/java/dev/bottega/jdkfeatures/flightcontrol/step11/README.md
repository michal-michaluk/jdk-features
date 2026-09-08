# Step 11 — JEP 511 — Module Import Declarations

> **JDK 26 (final)** · Redukujesz importy przez jedno `import module`.

## Architektura: DOMENA vs INFRASTRUKTURA

Podział z kroku 10 (i wcześniejszych) pozostaje bez zmian; ten krok **nie dodaje
logiki**, a jedynie upraszcza importy w warstwie domeny:

- **Domena** (`...step11.domain`) — model + operacje skopiowane z kroku 10. W klasie
  `Airspace` grupę importów `java.util.*` zastępuje **jeden** `import module java.base;`.
  Pakiet domeny nadal nie importuje klas sieciowych; używa wyłącznie typów z modułu
  `java.base` (`java.util`, `java.util.concurrent`, `java.lang`, `java.util.stream`).
- **Infrastruktura** (`...step11.server`) — `AirspaceServer` + `JsonSerde` skopiowane bez
  zmian z kroku 10 (tylko pakiet `...step11.server` i string `step-11` w Javadoc/bannerze).
  Serwer celowo zachowuje **jawne** importy `com.sun.net.httpserver.*` — to moduł
  `jdk.httpserver`, a nie `java.base`, więc `import module java.base;` nie obejmuje go.

## Cel ćwiczenia
Poznasz **Module Import Declarations** (JEP 511): `import module java.base;` importuje
wszystkie eksportowane pakiety modułu **jedną linią** zamiast listy `import ...`. W
Flight Control to uproszczenie importów w modelu — kod czyta się krócej, a zachowanie
jest identyczne.

## Co zrobić
1. W klasie `Airspace` (domena) zastąp grupę importów z `java.base`
   (**`java.util.*`**) jednym `import module java.base;`.
2. Upewnij się, że klasa działa w **unnamed module** (domyślnie) — to jest dozwolone.
3. Zachowaj te importy, które **nie** pochodzą z `java.base` (np. `com.sun.net.httpserver`
   w serwerze, albo własny pakiet `dev.bottega...`).
4. Sprawdź, że **zachowanie i testy się nie zmieniły** (ten sam model, te same testy).

## Dane (markdown)
```
Before:  ~8 linii import java.util.*
After:   1 linia  import module java.base;
```

## Napisz testy (akceptacja)
- Kod kompiluje się i **wszystkie dotychczasowe testy przechodzą** bez zmian zachowania.
- Liczba linii `import` znacząco spadła (policz `import` przed/po).
- Importy spoza `java.base` (np. `com.sun.net.httpserver` w serwerze) nadal jawnie obecne.
- `import module java.base;` kompiluje się **bez** `--enable-preview` — to JDK 26 (final),
  a nie feature preview (jedyny „preview" w projekcie to `StructuredTaskScope` w
  `SimulationContext`, skopiowany z kroku 10).

## Wskazówki
- Składnia: `import module java.base;` (nie `import java.base.*`).
- `java.base` to **podstawowy** moduł (zawiera `java.lang`, `java.util`, `java.net`,
  `java.time`, `java.io`, `java.util.concurrent`, `java.util.stream`…). Moduły API
  (np. `java.sql`, `java.httpclient`, `jdk.httpserver`) trzeba importować osobno.
- **Uważaj na kolizje nazw:** `import module` może przynieść więcej symboli — jeśli coś
  się nie kompiluje, sprawdź konflikt z własnym kodem.
- **Do przemyślenia:** kiedy wiele `import` jest czytelniejszych od jednego `import module`?
  (niewielka liczba użyć vs. cały moduł)

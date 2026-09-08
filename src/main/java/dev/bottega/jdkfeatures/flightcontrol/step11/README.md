# Step 11 — JEP 511 — Module Import Declarations

> **JDK 25 (final)** · Redukujesz importy przez jedno `import module`.

## Cel ćwiczenia
Poznasz **Module Import Declarations** (JEP 511): `import module java.base;` importuje
wszystkie eksportowane pakiety modułu **jedną linią** zamiast listy `import ...`. W
Flight Control to uproszczenie importów w modelu/serverze — kod czyta się krócej, a
zachowanie jest identyczne.

## Co zrobić
1. W klasie (np. `Airspace` / serwer z kroku 03) zastąp grupę importów z `java.base`
   (**`java.util.*`, `java.net.*`, `java.time.*`, …**) jednym `import module java.base;`.
2. Upewnij się, że klasa działa w **unnamed module** (domyślnie) — to jest dozwolone.
3. Zachowaj te importy, które **nie** pochodzą z `java.base` (np. `com.sun.net.httpserver`,
   albo własny pakiet `dev.bottega...`).
4. Sprawdź, że **zachowanie i testy się nie zmieniły**.

## Dane (markdown)
```
Before:  ~10 linii import java.util.* / java.net.* / java.time.*
After:   1 linia  import module java.base;   + tylko importy spoza java.base
```

## Napisz testy (akceptacja)
- Kod kompiluje się i **wszystkie dotychczasowe testy przechodzą** bez zmian zachowania.
- Liczba linii `import` znacząco spadła (elementarne sprawdzenie — policz `import` przed/po).
- Importy spoza `java.base` nadal jawnie obecne.

## Wskazówki
- Składnia: `import module java.base;` (nie `import java.base.*`). Upewnij się w dokumentacji.
- `java.base` to **podstawowy** moduł (zawiera `java.lang`, `java.util`, `java.net`,
  `java.time`, `java.io`…). Moduły API (np. `java.sql`, `java.httpclient`) trzeba
  importować osobno (`import module java.sql;`).
- **Uważaj na kolizje nazw:** `import module` może przynieść więcej symboli — jeśli coś
  się nie kompiluje, sprawdź konflikt z własnym kodem.
- **Do przemyślenia:** kiedy wiele `import` jest czytelniejszych od jednego `import module`?
  (niewielka liczba użyć vs. cały moduł)

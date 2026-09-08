# Step 03 — JEP 408 — Simple Web Server (serwer jest ćwiczeniem)

> **JDK 18 (final)** · Wystawiasz stan domeny przez HTTP i statyczny widok SVG.
> **Serwer to część zadania** — zaczynasz od **statycznego** udostępniania danych;
> kolejne JEP-y dodadzą mu aspekty (ruchu, porządku, współbieżności, klienta…).

## Architektura: DOMENA vs INFRASTRUKTURA

Ćwiczenie wymaga **ścisłego rozdzielenia** dwóch warstw — to najważniejsza zasada tego kroku:

- **Domena** = model + operacje na nim. Czysta, bez I/O, bez sieci. W pakiecie
  `...step03.domain` (rekordy `Aircraft`, `Point`, `Velocity`, `Circle`, `Polygon`, obszar
  `Area`, agregat `Airspace`). Ten pakiet **nie może** importować nic z `java.net`,
  `com.sun.net.httpserver`, `javax`/HTTP — jedynie `java.util.*`.
- **Infrastruktura** = cienka warstwa, która **tylko wystawia** dane domeny. W pakiecie
  `...step03.server` klasa `AirspaceServer`. **Zero logiki symulacji**: żadnego schedulera,
  auto-odświeżania, odbijania, ramek. Jedyny wywołany kod domeny to `airspace.step()`
  w handlerze `POST /tick` (oraz gettery przy `GET`).

Model to **niezmienne rekordy**, a `step()` zwraca nowy stan (nowy `Airspace`) — serwer
nie modyfikuje stanu, tylko go odczytuje i (w `/tick`) przesuwa o jeden krok.

## Cel ćwiczenia
Poznasz **Simple Web Server** (JEP 408): `com.sun.net.httpserver.HttpServer` do szybkiego
wystawienia REST bez zewnętrznego frameworka. W Flight Control to etap „radarowa tablica" —
masz stan w pamięci i chcesz go zobaczyć. **Statyczny widok SVG** (czerwone punkty =
samoloty, szare = obszary) renderuje się w przeglądarce; na tym etapie wystarczy
**odświeżenie strony** pokazujące aktualny stan.

## Co zrobić
1. Otwórz `HttpServer` na (`127.0.0.1`, port z argumentu; `port 0` = dynamiczny).
2. Zarejestruj **handlery** (sprawdź samodzielnie API):
   - `GET /aircraft` → JSON listy samolotów (`id`, `label`, `callsign`, `pos {x,y}`, `vel {dx,dy}`),
   - `GET /areas` → JSON listy obszarów (`kind`, `label`, geometria, `props`),
   - `GET /` → statyczny HTML + SVG,
   - `POST /tick` → wywołuje `step()` i zwraca nowy stan `/aircraft`.
3. Serwuj widok SVG (plik `src/main/resources/flightcontrol/view/index.html` — **dany**) —
   czytasz go z classpathu i zwracasz jako ciało odpowiedzi (`text/html`).
4. Mapuj swój model → JSON (rekordy z kroku 01/02) — to też ćwiczenie (serializacja).
   Trzymaj się **dokładnie** kontraktu podanego w `Dane testowe` (nazwy pól są sztywne).
5. Klasa `AirspaceServer` ma: `static AirspaceServer start(int port)`, `int port()`,
   `void stop()`, `static void main(String[] args)`. Trzyma **jedno** pole
   `Airspace airspace` (z `Airspace.sample()`) — nic więcej.

## Dane testowe (YAML) — kontrakt API, który masz spełnić
```
GET  /aircraft -> [ {id, label, callsign, pos: {x, y}, vel: {dx, dy}}, ... ]
GET  /areas    -> [ {kind, label, ...geometria, props}, ... ]
POST /tick     -> wywołuje step(), zwraca nowy stan
GET  /         -> dokument zawierający <svg> oraz etykiety (CTR, TMA, FOX, ECHO)
```

Uwaga do kontraktu: pole `props` obszarów występuje w kontrakcie, ale na tym etapie
**serializowane jest jako `{}`** — widok SVG go nie czyta, a serwer nie musi go mapować.

## Napisz testy (akceptacja)
- `GET /aircraft` zwraca 200 i JSON z `label`-ami, `callsign`-ami i współrzędnymi
  (przetestuj uruchomiony serwer przez `HttpClient` z `java.net.http`).
- `POST /tick` → pozycje się **przesunęły** (zgodnie z `step()`), status 200.
- `GET /` → odpowiedź zawiera `<svg` oraz `Flight Control`.
- Serwer startuje z portu `0` (dynamiczny) i `port()` zwraca faktyczny port > 0.
- **Testy domeny** (osobno, bez serwera): `describe` dla koła i wielokąta, `step`
  (ruch + niezmienność), `sample`, defensywne kopie list, `withPosition`, accessory, `equals`.

## Wskazówki
- **Domena czyściutka:** nie da się „przypadkiem" podpiąć HTTP do modelu, bo pakiet domeny
  nie może importować klas sieciowych. Trzymaj się tego — to celowo daje czytelny podział.
- **Nie blokuj wątku:** `HttpServer.start()` jest asynchroniczny; trzymaj referencję do
  `server`, by móc go zatrzymać (`stop(0)`).
- **JSON:** ręcznie lub popularną biblioteką — wybierz najprostsze, co daje poprawny format
  wg kontraktu (najprościej: prosta konkatenacja stringów).
- **Kontrakt ma być stabilny:** to jest API, na które później napiszesz klienta HTTP/3
  (krok 13) — nie zmieniaj nazw pól.
- **Widok SVG jest dany** — nie musisz go pisać; twoje jest serwowanie i zadbanie, by
  kontrakt zgadzał się z tym, co czyta `index.html` (label→nazwa, pos→współrzędne).
- **Zadanie do przemyślenia:** dlaczego `POST /tick` jest lepsze na ten etap niż serwer
  sam przesuwający stan w pętli? (Bo server ma być na tym etapie „statyczny" — ruch to wątek
  domeny, nie infrastruktury.)

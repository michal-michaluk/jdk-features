# Step 03 — JEP 408 — Simple Web Server (serwer jest ćwiczeniem)

> **JDK 18 (final)** · Wystawiasz stan domeny przez HTTP i statyczny widok SVG.
> **Serwer to część zadania** — zaczynasz od **statycznego** udostępniania danych;
> kolejne JEP-y dodadzą mu aspekty (ruchu, porządku, współbieżności, klienta…).

## Cel ćwiczenia
Poznasz **Simple Web Server** (JEP 408): `com.sun.net.httpserver.HttpServer` do szybkiego
wystawienia REST bez zewnętrznego frameworka. W Flight Control to etap „radarowa tablica" —
masz stan w pamięci i chcesz go zobaczyć. **Statyczny widok SVG** (czerwone punkty =
samoloty, szare = obszary) renderuje się w przeglądarce; na tym etapie wystarczy
**odświeżenie strony** pokazujące aktualny stan.

## Co zrobić
1. Otwórz `HttpServer` na (`localhost`, port z argumentu).
2. Zarejestruj **handlery** (sprawdź samodzielnie API):
   - `GET /aircraft` → JSON listy samolotów (label, callsign, pos, vel),
   - `GET /areas` → JSON listy obszarów (kind, label, geometria…),
   - `GET /` → statyczny HTML + SVG.
3. Serwuj widok SVG (plik `src/main/resources/flightcontrol/view/index.html` — **dany**) —
   czytasz go i zwracasz jako ciało odpowiedzi.
4. Mapuj swój model → JSON (rekordy z kroku 01/02) — to też jest ćwiczenie (serializacja).
5. Dodaj `POST /tick` → wywołuje `step()` i zwraca nowy stan (na razie wystarczy, że
   model ma ruch; serwer sam nie „gra" — to przyjdzie w późniejszych krokach).

## Dane testowe (YAML) — kontrakt API, który masz spełnić
```
GET  /aircraft -> [ {label, callsign, pos: {x, y}, vel: {dx, dy}}, ... ]
GET  /areas    -> [ {kind, label, ...geometria, props}, ... ]
POST /tick     -> wywołuje step(), zwraca nowy stan
GET  /         -> dokument zawierający <svg> oraz etykiety (CTR, TMA, FOX, ECHO)
```

## Napisz testy (akceptacja)
- `GET /aircraft` zwraca JSON z label-ami i współrzędnymi (przetestuj uruchomiony serwer,
  jak w teście kroku 03 w innych modułach — przez `HttpClient`).
- `POST /tick` → pozycje się **przesunęły** (zgodnie z `step()`).
- `GET /` → odpowiedź zawiera `<svg` i nazwy obszarów/samolotów.
- Serwer startuje z portu `0` (dynamiczny) i `port()` zwraca faktyczny port.

## Wskazówki
- **Nie blokuj wątku:** `HttpServer` uruchamiasz na osobnym wątku (`start()` jest
  asynchroniczny); trzymaj referencję do `server`, by móc go zatrzymać (`stop(0)`).
- **JSON:** ręcznie lub popularną biblioteką — wybierz najprostsze, co daje poprawny format
  wg kontraktu.
- **Kontrakt ma być stabilny:** to jest API, na które później napiszesz klienta HTTP/3
  (krok 13) — nie zmieniaj nazw pól.
- **Widok SVG jest dany** — nie musisz go pisać; twoje jest serwowanie i zadbanie, by
  kontrakt zgadzał się z tym, co czyta `index.html` (label→nazwa, pos→współrzędne).
- **Zadanie do przemyślenia:** dlaczego `POST /tick` jest lepsze na ten etap niż serwer
  sam przesuwający stan w pętli? (Bo serwer ma być najpierw „statyczny".)

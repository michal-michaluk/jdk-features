# Step 13 — JEP 517 — HTTP/3 Client

> **JDK 26 (final)** · Klient `HttpClient` konsumujący REST z kroku 03.

## Cel ćwiczenia
Poznasz **HTTP/3 (QUIC)** w `java.net.http.HttpClient` (JEP 517): `version(HTTP_3)`
i odczyt `request.version()`. W Flight Control budujesz **klienta**, który zapytuje Twój
serwer z kroku 03 o `/aircraft`/`/areas` — i deklaruje wersję HTTP/3 (z uczciwym
fallbackiem, bo realny QUIC wymaga serwera HTTP/3).

## Co zrobić
1. Stwórz `HttpClient` z `version(HttpClient.Version.HTTP_3)`.
2. Wyślij `GET /aircraft` i `GET /areas` do serwera (uruchom go osobno, port z kroku 03).
3. Odczytaj odpowiedź (`BodyHandlers.ofString()`), sparsuj JSON (lub zostaw jako string).
4. Sprawdź `request.version()` — zwraca `Optional<Version>`; uczciwie potraktuj, że realny
   transport może być `HTTP/1.1` bez serwera QUIC.
5. Obsłuż `Connection refused`/błędy sieci w teście (serwer może nie działać).

## Dane testowe (YAML)
```yaml
client:
  version: HTTP_3
  request:
    - GET /aircraft   -> 200, JSON z label/pos
    - GET /areas      -> 200, JSON z kind/label
expect:
  request.version() -> Optional[HTTP_3] (lub fallback HTTP/1.1 przy braku QUIC)
  fallback:         # gdy serwer nie wspiera QUIC — spadki do HTTP/1.1, bez błędu
```

## Napisz testy (akceptacja)
- `client.version() == HTTP_3` (deklaracja klienta).
- `request.version()` to `Optional` — potrafisz odczytać i porównać (z fallbackiem).
- Klient konsumuje z serwera (jeśli serwer działa) — przynajmniej 200 i poprawny kontrakt.
- Port `0`/uruchomienie serwera na znanym porcie (jak w kroku 03) — sekwencja start→request→stop.

## Wskazówki
- `HttpClient.newBuilder().version(HTTP_3).build()`; `HttpRequest.version()` zwraca
  `Optional<Version>` (a nie `Version` — sprawdź, nie zgaduj).
- **Realny QUIC** wymaga serwera z HTTP/3; bez niego klient spada do `HTTP/1.1`. Nie
  udawaj, że zawsze jest QUIC — przetestuj deklarację (wersja klienta) + fallback.
- `HttpClient` jest reużywalny i wątkowo-bezpieczny — stwórz raz.
- Uruchom serwer w `@BeforeAll` na porcie `0`, odczytaj `port()`, potem testuj klienta.
- **Do przemyślenia:** dlaczego `request.version()` zwraca `Optional`? (bo wersja
  odpowiedzi jest znana dopiero po nawiązaniu połączenia / może się różnić.)

# Step 13 — JEP 517 — HTTP/3 Client

> **JDK 26 (final)** · Klient `HttpClient` konsumujący REST (deklaracja HTTP/3 + uczciwy fallback).

## Cel ćwiczenia
Poznasz **HTTP/3 (QUIC)** w `java.net.http.HttpClient` (JEP 517): `version(HTTP_3)`
i odczyt `request.version()`. W Flight Control budujesz **klienta**, który zapytuje serwer
z tego kroku o `/aircraft` — i deklaruje wersję HTTP/3 (z uczciwym fallbackiem, bo realny
QUIC wymaga serwera HTTP/3).

## Co zrobić
1. Stwórz `HttpClient` z `version(HttpClient.Version.HTTP_3)` (`client()`); `client.version()`
   zwraca `HTTP_3` (deklaracja klienta).
2. Wyślij `GET /aircraft` do serwera (`get`/`fetch`) — serwer startujesz na porcie `0`,
   odczytujesz `port()`, potem testujesz klienta.
3. Odczytaj odpowiedź (`BodyHandlers.ofString()`), sprawdź `200` i obecność `label`/`FOX`.
4. `requestVersion(...)` buduje żądanie z `.version(HTTP_3)` i zwraca `request.version()`
   (typ `Optional<HttpClient.Version>` — **obecny**).
5. Obsłuż **uczciwy fallback**: serwer JEP 408 to HTTP/1.1 (brak punktu QUIC), więc realny
   transport to `HTTP/1.1`; deklaracja klienta/żądania pozostaje `HTTP_3` — w teście nie
   zgłaszaj twardego błędu przy `HTTP/1.1`.

## Dane testowe (YAML)
```yaml
client:
  version: HTTP_3            # client.version() == HTTP_3 (deklaracja)
  request:
    - GET /aircraft          # -> 200, JSON z label/pos; body zawiera "label" i "FOX"
expect:
  request.version() -> Optional[HTTP_3]   # obecne (żądanie przypięte do HTTP_3)
  fallback:          # realny transport do serwera JEP 408 = HTTP/1.1, bez błędu (200 OK)
```

## Napisz testy (akceptacja)
- `client.version() == HTTP_3` (deklaracja klienta).
- `requestVersion(...)` zwraca **obecny** `Optional<HttpClient.Version>`.
- Klient konsumuje serwer: `fetch(base, "/aircraft")` → `200` + body zawierające `label`
  i `FOX`.
- Sekwencja start→request→stop (`@BeforeAll`/`@AfterAll`, port 0).

## Wskazówki
- `HttpClient.newBuilder().version(HTTP_3).build()`; `request.version()` zwraca
  `Optional<Version>` (a nie `Version`) — i jest to wersja **zadeklarowana na żądaniu**
  (ustawiana w `HttpRequest.newBuilder().version(...)`).
- **Realny QUIC** wymaga serwera z HTTP/3; nasz serwer (JEP 408) to HTTP/1.1. Klient
  deklaruje `HTTP_3`, ale transport faktycznie spada do `HTTP/1.1` — nie udawaj, że zawsze
  jest QUIC. Przetestuj deklarację + fallback (wszystko działa, 200 OK).
- `HttpClient` jest reużywalny i wątkowo-bezpieczny — stwórz raz (`client()`).
- **Do przemyślenia:** dlaczego `request.version()` zwraca `Optional`? (bo wersja może
  nie być znana/ustawiona w momencie budowy żądania — stąd wymóg przypięcia wersji).

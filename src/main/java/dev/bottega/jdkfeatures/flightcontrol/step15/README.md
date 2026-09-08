# Step 15 — JEP 525 — Structured Concurrency (preview)

> **JDK 26 (preview)** · Grupujesz zadania: odpytywanie sektorów, `fork`/`join`.

## Cel ćwiczenia
Poznasz **Structured Concurrency** (JEP 525): `StructuredTaskScope.open()` — zadania jako
**dzieci** jednego zakresu, z dostępem do `fork`, `join`, `Subtask.state()` i `close()`.
Kontrast z „dzikimi" wątkami z kroku 07: tu wszystkie podzadania są **przypisane do
jednego właściciela**, który nimi zarządza (jak `try-with-resources`). W Flight Control
odpytujesz kilka sektorów naraz (radar), składasz wyniki i reagujesz na błąd (fail-fast).

## Co zrobić
1. `try (var scope = new StructuredTaskScope.ShutdownOnFailure()) { ... }` — zakres.
2. **`fork`** po jednym zadaniu na sektor/samolot (każde zwraca wynik).
3. **`join()`** — poczekaj na wszystkie; `joinUntil(...)` opcjonalnie ogranicza czas.
4. Odczytaj `Subtask.state()` (`SUCCESS`/`FAILED`/`CANCELLED`).
5. **Agreguj** wyniki; obsłuż **fail-fast** (`scope.throwIfFailed()` / `close()`).

## Dane testowe (YAML)
```yaml
tasks:
  - {id: t1, sector: TMA,   result: 11}
  - {id: t2, sector: CTR,   result: 22}
  - {id: t3, sector: AWY,   result: 33}
expect:
  joined: [11, 22, 33]         # wyniki z fork-ów złożone
  state: SUCCESS               # Subtask.state() po join
  closed: true                 # close() zamknął wszystkie (fail-fast lub normalnie)
failFast:
  t2 -> throw                 # jedno zadanie rzuca -> throwIfFailed/close przerwie resztę
```

## Napisz testy (akceptacja)
- Wszystkie `fork` zwracają wyniki; po `join` `state == SUCCESS`; agregacja poprawna.
- **Fail-fast:** gdy jedno zadanie rzuci, `scope.throwIfFailed()` / `close()` kończy
  pozostałe (wystarczy, że `join()`/`close` nie „wiszą" i rzucają).
- Zakres zamknięty (`close`) — wszystkie dzieci zakończone, bez wycieków.

## Wskazówki
- **`open()` (statyczna fabryka)** — nie `new`. Użyj `try-with-resources` dla `close`.
- Każde `fork` to `Future`-podobny `Subtask<T>`; wynik przez `subtask.get()`.
- `ShutdownOnFailure` przerywa, gdy jedno zadanie zawiedzie; `ShutdownOnSuccess` —
  gdy pierwsze się powiedzie (wybierz wg potrzeby).
- `join()` wraca, gdy **wszystkie** dzieci zakończone (albo limit czasu); po nim dostępne
  `get()`/`state()`.
- **Do przemyślenia:** czym różni się `ShutdownOnFailure` od `ShutdownOnSuccess` i kiedy
  którego użyć w radarze (chcę komplet vs. najszybszy obiekt)?

# Step 07 — JEP 444 — Virtual Threads

> **JDK 21 (final)** · Równoległe „radary" — wiele lekkich scenariuszy naraz.

## Architektura: DOMENA vs INFRASTRUKTURA

Podział z kroku 06 pozostaje: model i operacje na nim tworzą **domenę**, a serwer to tylko
cienka **infrastruktura** wystawiająca dane. Ten krok dodaje **równoległy radar** wyłącznie po
stronie domeny:

- **Domena** (`...step07.domain`) — nowa klasa `Radar` + niezmienny `RadarReport`. Każdy samolot
  jest klasyfikowany na osobnym **wątku wirtualnym**, a wyniki są bezpiecznie agregowane.
  Pakiet domeny nadal **nie importuje** klas sieciowych.
- **Infrastruktura** (`...step07.server`) — skopiowana bez zmian z kroku 06: `GET /aircraft`,
  `GET /areas`, `GET /`, `POST /tick`. Serwer nie zna `Radar` — to wątek domeny.

## Cel ćwiczenia
Poznasz **Virtual Threads** (JEP 444): `Thread.ofVirtual()` oraz wirtualny executor. W Flight
Control to przetwarzanie wielu samolotów „na raz" — każde zadanie to osobny, tani wątek
wirtualny, więc możesz skalować bez kosztu wątków platformowych. Nauczysz się bezpiecznie
**agregować wyniki** (atomowy licznik w mapie concurrent) i czekać na wszystkie (`join`).

## Co zrobić
1. Uruchom **N zadań** (po jednym na samolot) na `Thread.ofVirtual().start(...)`.
2. Każde zadanie **klasyfikuje** samolot (`ThreatClassifier.classify`) i aktualizuje wspólny,
   thread-safe akumulator (np. `ConcurrentHashMap<Category, AtomicInteger>`).
3. Poczekaj, aż **wszystkie** wątki się zakończą (`join()`).
4. Zweryfikuj, że każdy wątek jest **wirtualny** (`isVirtual() == true`).
5. Zbuduj **niezmienny** raport (np. `RadarReport`): liczniki per `Category`, liczba
   przetworzonych, znacznik `allVirtual`. Kolejność i determinizm — ten sam wynik dla tych
   samych danych.

## Dane testowe (YAML)
```yaml
scenario:
  aircraft:
    - {label: HOT,  pos: {x: 1, y: 0}, vel: {dx: 20, dy: 0}}   # -> ALARM
    - {label: HOT,  pos: {x: 1, y: 0}, vel: {dx: 30, dy: 0}}   # -> ALARM
    - {label: COOL, pos: {x: 1, y: 0}, vel: {dx: 5,  dy: 0}}   # -> SECTOR
    - {label: AWAY, pos: {x: 9, y: 9}, vel: {dx: 1,  dy: 1}}   # -> NORMAL
  expect:
    processed: 4          # po join wszystkie przetworzone
    counts: {ALARM: 2, SECTOR: 1, NORMAL: 1, UNKNOWN: 0}
    virtual: true         # każdy wątek isVirtual()==true
```

## Napisz testy (akceptacja)
- Po `join` wszystkie zadania zakończone i stan spójny (`processed == 4`).
- Poprawne zliczenie per `Category` — bez zgubionych aktualizacji (atomowy licznik).
- `isVirtual()` == `true` dla utworzonych wątków (`allVirtual`).
- **Determinizm:** dwa `report()` na tych samych danych dają identyczne liczniki.
- `RadarReport` jest **niezmienny** (rzut na modyfikację mapy liczników).
- (ciągłość) testy domeny i kontrakt serwera z kroku 06 nadal przechodzą; pakiet
  `step07.domain` ma **100% pokrycia linii**.

## Wskazówki
- Wirtualne wątki nie dodają kosztu `1:1` z OS — możesz ich mieć setki/tysiące; to jest sens.
- **Współdzielony stan:** nie używaj zwykłego `int`/`ArrayList` z wielu wątków — `AtomicInteger`,
  `ConcurrentHashMap` lub zbieraj wyniki po `join`.
- `Thread.ofVirtual().start(task)` zwraca `Thread` z `join()`; wirtualny executor ma `close()`
  (od JDK 21) — zatrzymuje się sam po zakończeniu zadań.
- **Kolejność nie jest gwarantowana** — agreguj liczniki, nie polegaj na kolejności ukończenia.
- **Do przemyślenia:** dlaczego przy setkach zadań na wirtualnych wątkach aplikacja nie „pada",
  a przy platformowych — tak? (limit wątków OS / koszt przełączenia kontekstu)

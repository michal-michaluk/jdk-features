# Step 04 — JEP 431 — Sequenced Collections

> **JDK 21 (final)** · Porządkujesz dane radaru. Działasz wyłącznie w warstwie **domeny**.

## Architektura: DOMENA vs INFRASTRUKTURA

Podział z kroku 03 pozostaje: model i operacje na nim tworzą **domenę**, a serwer to tylko
cienka **infrastruktura** wystawiająca dane. Ten krok dodaje **kolejność** (ordered views)
wyłącznie po stronie domeny:

- **Domena** (`...step04.domain`) — agregat `Airspace` dostaje nowe metody zwracające
  **uporządkowane widoki**: sortowanie wg odległości, `SequencedSet` etykiet obszarów,
  `SequencedMap id→aircraft`. Pakiet domeny nadal **nie importuje** klas sieciowych.
- **Infrastruktura** (`...step04.server`) — bez zmian w kontrakcie: `GET /aircraft`,
  `GET /areas`, `GET /`, `POST /tick`. Serwer nie zna kolejności — to wątek domeny.

Agregat pozostaje **niezmienny**: pola to niezmienne kopie list, a nowe metody budują
uporządkowane struktury **od zera** z zapisanych list i je zwracają — nigdy nie modyfikują
stanu wewnętrznego.

## Cel ćwiczenia
Poznasz **Sequenced Collections** (JEP 431): wspólne interfejsy `SequencedCollection`,
`SequencedSet`, `SequencedMap` z operacjami `addFirst`/`addLast`, `getFirst`/`getLast`,
`removeFirst`/`removeLast`, `reversed()`, a dla map `firstEntry`/`lastEntry`/`putFirst`.
W Flight Control to naturalny  „listy samolotów w kolejności":
- platforma (kolejność zgłoszeń/alarmów),
- sortowanie wg odległości,
- mapa `id→aircraft` z widokami od końca.

## Co zrobić
1. Uporządkuj samoloty wg odległości od punktu referencyjnego (np. CTR) — w jedną stronę
   i `reversed` w drugą: `aircraftSortedByDistance(Point center, boolean ascending)`.
2. `SequencedSet` **etykiet** obszarów (`areaLabels()`) — bez duplikatów, z zachowaniem
   kolejności dodania (kolejność na liście obszarów).
3. `SequencedMap` `id→aircraft` (`aircraftById()`) — `putFirst`, `firstEntry`/`lastEntry`,
   `reversed`, `getFirst`/`getLast`.
4. Zachowaj **niezmienność**: buduj widoki z zapisanych list i zwracaj je; nie modyfikuj
   pól wewnętrznych.
5. Sprawdź, że serwer z kroku 03 nadal działa bez zmian (ten krok nie zmienia kontraktu).

## Dane testowe (YAML) — wejście do uporządkowania
```yaml
order:
  by: distance         # odległość od aktualnej pozycji CTR
  center: {x: 0, y: 0}
  direction: asc      # asc | desc (desc = reversed)
aircraft:              # te same co w kroku 01/02
  - {id: a1, label: FOX,  pos: {x: 10, y: 0}}
  - {id: a2, label: ECHO, pos: {x: 0,  y: 5}}
areas:                 # etykiety w kolejności: CTR, TMA
  - {kind: circle, label: CTR, center: {x: 0, y: 0}, radius: 3}
  - {kind: polygon, label: TMA, vertices: [{x: 0, y: 0}, {x: 4, y: 0}, {x: 4, y: 4}]}
```

## Napisz testy (akceptacja)
- `dist` asc: najbliższy pierwszy; `reversed`/desc → najdalszy pierwszy (odwrócony, nie
  posortowany od nowa inaczej). Wynik deterministyczny.
- `areaLabels()`: brak duplikatów, kolejność wstawiania zachowana (`getFirst`/`getLast`).
- `aircraftById()`: `putFirst` wstawia na początek, `firstEntry`/`lastEntry` poprawne,
  `reversed()` odwraca widok, oryginał bez zmian.
- **Niezmienność:** po wywołaniu metod porządkujących stan wewnętrzny (`aircraft()`,
  `areas()`) pozostaje w pierwotnej kolejności.
- (ciągłość) testy domeny i kontrakt serwera z kroku 03 nadal przechodzą.

## Wskazówki
- `reversed()` zwraca **widok** (view) — nie kopię: zmiany w widoku odbijają się w
  oryginale (lub rzucają wyjątek, jeśli niezmienne). Zdecyduj świadomie.
- Sortowanie wg odległości to kwestia **komparatora po odległości Euklidesowej** — zbuduj
  go do punktu referencyjnego (`sqrt(dx² + dy²)`), a kierunek wybierz **odwracając**
  komparator, a nie sortując od nowa drugi raz.
- `SequencedSet`/`SequencedMap` dobierz świadomie między porządkiem **wstawiania** a
  porządkiem **naturalnym** — to decyduje, czy kolejność wynika z kolejności dodania, czy
  z porządku samych elementów. Wybierz typ, który oddaje intencję (np. kolejność listy
  obszarów / samolotów).
- **Immutability:** jeśli chcesz niezmienne zbiory, użyj niezmiennych kopii — ale wtedy
  wstawianie na krańcach nie działa; dobierz typ do celu (widok do odczytu vs. struktura
  do budowy).
- Zwróć uwagę: `SequencedMap.entrySet()` też jest `SequencedSet<Entry<K,V>>`.
- **Dziedzinowo:** kolejność to cecha domeny, nie serwera — rozbudowa serwera jest
  opcjonalna i nie wchodzi w zakres tego kroku (kontrakt `GET /aircraft` zostaje stabilny).

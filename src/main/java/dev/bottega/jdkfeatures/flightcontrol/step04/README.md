# Step 04 — JEP 431 — Sequenced Collections

> **JDK 21 (final)** · Porządkujesz dane radaru. Wykorzystaj swój model z kroku 01.

## Cel ćwiczenia
Poznasz **Sequenced Collections** (JEP 431): wspólne interfejsy `SequencedCollection`,
`SequencedSet`, `SequencedMap` z operacjami `addFirst`/`addLast`, `getFirst`/`getLast`,
`removeFirst`/`removeLast`, `reversed()`, a dla map `firstEntry`/`lastEntry`/`putFirst`.
W Flight Control to naturalny narząd „listy samolotów w kolejności" — platforma (kolejność
zgłoszeń/alarmów), sortowanie wg odległości, mapa `id→aircraft` z widokami od końca.

## Co zrobić
1. Uporządkuj samoloty wg odległości od punktu referencyjnego (np. CTR) — w jedną stronę
   i `reversed` w drugą (bez kopiowania sortowanej listy).
2. `SequencedSet` **labeli** obszarów — bez duplikatów, z zachowaniem kolejności dodania.
3. `SequencedMap` `id→aircraft` — `putFirst`, `firstEntry`/`lastEntry`, `reversed`.
4. (opcjonalnie rozbudowa serwera z kroku 03) endpoint zwracający samoloty w zadanej
   kolejności — to pokazuje porządek „na żywo".

## Dane testowe (YAML) — wejście do uporządkowania
```yaml
order:
  by: distance         # odległość od aktualnej pozycji CTR
  center: {x: 0, y: 0}
  direction: asc      # asc | desc (desc = reversed)
aircraft:              # te same co w kroku 01/02
  - {id: a1, label: FOX,  pos: {x: 10, y: 0}}
  - {id: a2, label: ECHO, pos: {x: 0,  y: 5}}
```

## Napisz testy (akceptacja)
- `dist` asc: najbliższy pierwszy; `reversed` → najdalszy pierwszy (odwrócony, nie
  posortowany od nowa inaczej).
- `SequencedSet` labeli: brak duplikatów, `addFirst`/`addLast` wstawiają na krańcach,
  `getFirst`/`getLast` poprawne.
- `SequencedMap`: `putFirst`/`putLast`, `firstEntry`/`lastEntry`, `reversed()` odwraca
  widok, oryginał bez zmian.

## Wskazówki
- `reversed()` zwraca **widok** (view) — nie kopię: zmiany w widoku odbijają się w
  oryginale (lub rzucają wyjątek, jeśli niezmienne). Zdecyduj świadomie.
- Sortowanie wg odległości: `Comparator.comparingDouble(a -> dist(a.pos(), center))`.
- `SequencedSet` = `LinkedHashSet`/`TreeSet` wg potrzeby (kolejność wstawiania vs. naturalna).
- **Immutability:** jeśli chcesz niezmienne zbiory — `List.copyOf`/`Set.copyOf`, ale wtedy
  `addFirst` nie działa; dobierz typ do celu (widok do odczytu vs. struktura do budowy).
- Zwróć uwagę: `SequencedMap.entrySet()` też jest `SequencedSet<Entry<K,V>>`.

# Step 07 — JEP 444 — Virtual Threads

> **JDK 21 (final)** · Równoległe „radary" — wiele lekkich scenariuszy naraz.

## Cel ćwiczenia
Poznasz **Virtual Threads** (JEP 444): `Thread.ofVirtual()` i
`Executors.newVirtualThreadPerTaskExecutor()`. W Flight Control to przetwarzanie wielu
samolotów/sektorów „na raz" — każde zadanie to osobny, tani wątek wirtualny, więc możesz
skalować bez kosztu platformowych wątków. Nauczysz się bezpiecznie **agregować wyniki**
(atomowy licznik, kolekcja concurrent) i czekać na wszystkie (`join`).

## Co zrobić
1. Uruchom **N zadań** (po jednym na samolot/sektor) na `Thread.ofVirtual().start(...)` lub
   executorze wirtualnym.
2. Każde zadanie **aktualizuje** wspólny stan (np. licznik przetworzonych, listę wyników).
3. Poczekaj, aż **wszystkie** się zakończą (`join()`/`executor.shutdown()+awaitTermination`).
4. Zweryfikuj, że wątki są **wirtualne** (`isVirtual()` == true).
5. (rozbudowa serwera z kroku 03) obsłuż `POST /tick` równolegle dla wielu samolotów —
   wirtualne wątki to naturalne miejsce.

## Dane testowe (YAML)
```yaml
scenario:
  aircraft: 200          # ile zadań -> ile wątków wirtualnych
  sectorCount: 5
  expect:
    processed: 200       # po join wszystkie przetworzone
    virtual: true        # każdy wątek isVirtual()==true
```

## Napisz testy (akceptacja)
- Po `join` wszystkie zadania zakończone i stan spójny (`processed == 200`).
- Licznik atomowy (`AtomicInteger`) ma końcową wartość, bez zgubionych aktualizacji.
- `isVirtual()` == true dla utworzonych wątków.

## Wskazówki
- Wirtualne wątki nie dodają kosztu `1:1` z OS — możesz ich mieć setki/tysiące; to jest sens.
- **Współdzielony stan:** nie używaj zwykłego `int`/`ArrayList` z wielu wątków — `AtomicInteger`,
  `ConcurrentHashMap`, `Collections.synchronizedList` lub zbieraj wyniki po `join`.
- `Thread.ofVirtual().start(task)` zwraca `Thread` z `join()`; executor wirtualny ma
  `close()` (od JDK 21) — zatrzymuje się sam po zakończeniu zadań.
- **Wystarczy `join`:** nie ma gwarancji kolejności ukończenia — agreguj, nie polegaj na
  kolejności.
- **Do przemyślenia:** dlaczego przy `200` zadaniach na wirtualnych wątkach nie „pada"
  aplikacja, a przy platformowych — tak? (przełącz kontekstu/limit wątków OS)

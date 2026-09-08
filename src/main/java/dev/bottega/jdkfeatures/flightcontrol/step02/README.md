# Step 02 — JEP 356 — Enhanced Pseudo-Random Number Generators

> **JDK 17 (final)** · Do modelu z kroku 01 dodajesz **sensowny ruch**.

## Cel ćwiczenia
Poznasz **Enhanced PRNG** (JEP 356): nowy interfejs `RandomGenerator` + fabrykę
`RandomGeneratorFactory` i algorytmy `L64X128MixRandom` / `L64X128StarStarRandom`.
W domenie Flight Control `step()` nie przesuwa już tylko po linii prostej — samolot
zmienia kurs i prędkość w sposób **deterministyczny** (ten sam seed ⇒ ta sama trajektoria),
a nie losowy przy każdym uruchomieniu. To kluczowe, by przebieg dało się odtworzyć i
przetestować.

## Co zrobić
Rozszerz swój model z kroku 01:
1. Dodaj **parametry ruchu** (max kąt skrętu, max delta prędkości, seed, algorytm).
2. W `step()` przed przesunięciem wybierz nowy wektor: lekka zmiana **kursu** (kąta) i
   **prędkości** (długości), ograniczona do zadanego zakresu.
3. Zbuduj generator z `RandomGeneratorFactory.of(name).create(seed)` — jeden generator,
   jeden seed ⇒ powtarzalna trajektoria.
4. `step()` dalej zwraca niezmienny stan (nowy `Airspace`, nowe `Aircraft`).

## Dane testowe (YAML) — parametry do kodowania
```yaml
movement:
  maxTurnDeg: 5        # max zmiana kąta kursu na krok (w stopniach)
  maxSpeedDelta: 2     # max zmiana prędkości na krok
  seed: 42
  algorithm: L64X128MixRandom
```

Przykład akceptacji (wygeneruj po swojemu i zweryfikuj testem):
- `seed: 42`, `maxTurnDeg: 5`, `maxSpeedDelta: 2` → konkretne, **te same** pozycje po
  N krokach przy każdym uruchomieniu (zapisz oczekiwane w teście).

## Napisz testy (akceptacja)
- **Determinizm:** ten sam `seed` ⇒ identyczna trajektoria po N krokach.
- **Granice:** wektor nigdy nie zmienia się o więcej niż `maxTurnDeg` / `maxSpeedDelta`.
- **Immutability:** `step()` nie modyfikuje poprzedniego stanu.
- **Sens:** po wielu krokach samolot nie „ucieka" w nieskończoność — ruch jest ograniczony.

## Wskazówki
- Kurs → współrzędne: przelicz kąt na `dx = cos(θ)`, `dy = sin(θ)` (pamiętaj o
  konwersji stopnie↔radiany).
- Prędkość = długość wektora `(dx,dy)`; zmień długość, zachowując kierunek, potem obróć.
- Wybieraj z generatora kolejne liczby (np. `nextDouble`) — kolejność determinuje trajektorię.
- Do sprawdzenia „nie ucieka": ogranicz wektor do sensownego zakresu (mały `maxSpeedΔ`
  przy stałym `maxTurnDeg` trzyma samolot w polu).
- **API:** `RandomGenerator` ma `nextDouble(origin, bound)`; `RandomGeneratorFactory.all()`
  wylicza dostępne algorytmy.

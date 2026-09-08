# Step 09 — JEP 485 Stream Gatherers (instruction)

> Wzorzec: ten pakiet niesie **rozwiązanie kroku 08** (kod + testy) oraz instrukcję kroku 09.
> / *Reference solution for step 08 lands here; this file holds the task.* /

## Zadanie
Agregacja telemetrii: `Stream.gather(Gatherers.windowFixed(3))` po sektorach, okno przesuwne
pozycji w czasie, `fold` do bounding box.

## Akceptacja
- `windowFixed(3)` daje oczekiwane chunki,
- `fold` zwraca sumę,
- `windowSliding(2)` poprawne okna.

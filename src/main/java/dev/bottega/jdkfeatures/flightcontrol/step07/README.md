# Step 07 — JEP 444 Virtual Threads (instruction)

> Wzorzec: ten pakiet niesie **rozwiązanie kroku 6** (kod + testy) oraz instrukcję
> kroku 07. / *Reference solution for step 6 lands here; this file holds the task.* /

## Zadanie
Równoległe radary: aktualizacja sektorów przez Thread.ofVirtual()/newVirtualThreadPerTaskExecutor; bezpieczny akumulator.

## Akceptacja
Po join spójny stan; isVirtual true; atomowy licznik.

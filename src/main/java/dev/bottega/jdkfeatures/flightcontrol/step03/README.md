# Step 03 — JEP 408 Simple Web Server (instruction)

> Wzorzec: ten pakiet niesie **rozwiązanie kroku 2** (kod + testy) oraz instrukcję
> kroku 03. / *Reference solution for step 2 lands here; this file holds the task.* /

## Zadanie
Wystaw stan przez REST (GET /aircraft, /areas; POST /tick) + serwuj statyczny widok SVG (ten HTML jest dany: src/main/resources/flightcontrol/view/index.html). Odświeżenie strony pokazuje stan.

## Akceptacja
GET /aircraft zwraca pojazdy; POST /tick przesuwa; GET / zwraca dokument z <svg>.

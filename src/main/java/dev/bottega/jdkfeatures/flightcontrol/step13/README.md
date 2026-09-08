# Step 13 — JEP 517 HTTP/3 (instruction)

> Wzorzec: ten pakiet niesie **rozwiązanie kroku 12** (kod + testy) oraz instrukcję
> kroku 13. / *Reference solution for step 12 lands here; this file holds the task.* /

## Zadanie
Klient HttpClient z version(HTTP_3) konsumujący REST (fallback HTTP/1.1).

## Akceptacja
client.version==HTTP_3, request.version->HTTP_3 (realny QUIC wymaga serwera HTTP/3).

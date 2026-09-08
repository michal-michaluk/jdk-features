# Step 01 — model (instruction only)

Zbuduj **niezmienny model** domeny „Flight Control". **Rozwiązanie wzorcowe znajdziesz w `step02`** — tutaj jest tylko zadanie.

## Co zbudować
- `record` dla: `Point(x,y)`, `Velocity(dx,dy)`, `Aircraft(id,label,callsign,pos,vel)`,
  `Circle(center,radius,label,props)`, `Polygon(vertices,label,props)`.
- `sealed interface Area permits Circle, Polygon` — wspólne `label()` i `props()`.
- `Airspace` (final class) — listy `aircraft`/`areas`, `step()` przesuwa każdy samolot
  o `vel`, oraz `describe(Area)` (wyczerpujący `switch` po sealed `Area`).

## Dane (YAML — zamapuj na kod, nie kopiuj)
```yaml
aircraft:
  - {id: a1, label: FOX,   pos: {x: 10, y: 0}, vel: {dx: 1, dy: 0}}
  - {id: a2, label: ECHO,  pos: {x: 0,  y: 5}, vel: {dx: 0, dy: 1}}
areas:
  - {circle: {center: {x: 0, y: 0}, radius: 3, label: CTR, props: {type: control}}}
  - {polygon: {vertices: [{x: 0, y: 0}, {x: 4, y: 0}, {x: 4, y: 4}], label: TMA, props: {kind: terminal}}}
after step(): a1 -> (11,0), a2 -> (0,6)
```

## Akceptacja (napisz testy)
- `describe(Area)` wyczerpujący **bez** `default`.
- model jest immutable (rekorady); `Airspace` przyjmuje oba podtypy `Area`.
- `step()` przesuwa o `vel`; oryginał bez zmian.

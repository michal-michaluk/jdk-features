# Step 02 — Enhanced PRNG (instruction)

> **Ten pakiet (`step02`) zawiera rozwiązanie kroku 01** (model: records + sealed `Area`
> + `Airspace`) **oraz jego testy** (`FlightControlModelTest`). To wzorzec: krok N niesie
> rozwiązanie kroku N‑1 i instrukcję kroku N.

## Twoje zadanie w kroku 02
Dodaj **sensowny ruch jak samoloty** — mała, ograniczona zmiana kursu i prędkości w każdym
`step()`, deterministyczna z seedu. Użyj `RandomGeneratorFactory` (np. `L64X128MixRandom`).

## Parametry (YAML)
```yaml
movement:
  maxTurnDeg: 5        # max delta kąta kursu na krok
  maxSpeedDelta: 2     # max delta prędkości na krok
  seed: 42
  algorithm: L64X128MixRandom
```

## Zachowanie
- ten sam `seed` → ta sama trajektoria (determinizm).
- wektor zmienia się w granicach `maxTurnDeg`/`maxSpeedDelta`.
- `step()` wraca do nowego (niezmiennego) stanu.

## Akceptacja (napisz testy do `Airspace`)
- determinizm z seedu po N krokach,
- zmiana wektora w granicach,
- ruch nie wyskakuje poza sensowne pole.

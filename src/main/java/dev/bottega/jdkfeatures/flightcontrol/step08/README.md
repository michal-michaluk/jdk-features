# Step 08 — JEP 467 — Markdown Documentation Comments

> **JDK 23 (final)** · Dokumentujesz publiczne API modelu w Markdown.

## Cel ćwiczenia
Poznasz **Markdown Documentation Comments** (JEP 467): komentarze `///`/`/** … */` z
**Markdown** zamiast czystego HTML w Javadoc. W Flight Control udokumentujesz kontrakt
`Airspace`/`Area`/`step()` tak, by wygenerowana dokumentacja była czytelna dla innych
uczestników — co robi `step`, co znaczy `props`, jak interpretować kategorie.

## Co zrobić
1. Dodaj Javadoc/markdown do **publicznego API** modelu (z kroku 01+): `Airspace`,
   `Area` (`Circle`/`Polygon`), `Aircraft`, `step()`, `describe()`.
2. Opisz **zachowanie** (`step` przesuwa o `vel`, niezmienność) i **sens pól** (`props` =
   metadane, `label` = nazwa na radarze).
3. Używaj Markdown: nagłówki, listy, `code`, tabele, linki — bez `{@code}`/`<pre>`.
4. Wygeneruj Javadoc i sprawdź, że czyta się dobrze.

## Dane (YAML — do udokumentowania, jako przykład opisu)
```yaml
documentation:
  step: "Przesuwa każdy samolot o jego wektor prędkości; zwraca NOWY, niezmienny stan."
  props:
    - "type/sector — metadane obszaru (np. control = CTR, terminal = TMA)"
  describe: "Klasyfikuje obszar po typie (Circle/Polygon) — wyczerpujący switch."
```

## Akceptacja (bez testu jednostkowego)
- Publiczne API ma komentarze Markdown, nie surowy HTML.
- Wygenerowany Javadoc czytelny: nagłówki/listy/tabele renderują się poprawnie.
- `step()` opisany od strony zachowania + niezmienności.

## Wskazówki
- `/** */` (wieloliniowy) vs `///` (liniowy) — JEP 467 obsługuje oba; wybierz spójnie.
- Markdown w Javadoc: `#`/`##`, `-`, `` `code` ``, `| tabela |`, `[link](url)`, bloki
  ``` fenced ```. Nie musisz używać `{@literal}`/`@code`.
- Opisuj **kontrakt** (co gwarantuje metoda), nie implementację.
- **pomocnik:** `./gradlew javadoc` — zajrzyj do `build/docs/javadoc/`.
- **Do przemyślenia:** jakie założenia domenowe (immutability, wyczerpalność `Area`)
  warto utrwalić w Javadoc, by współpracownik ich nie złamał?

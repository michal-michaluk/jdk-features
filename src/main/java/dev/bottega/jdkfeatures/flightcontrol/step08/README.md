# Step 08 — JEP 467 — Markdown Documentation Comments

> **JDK 23 (final)** · Krok **wyłącznie dokumentacyjny** — dokumentujesz publiczne API modelu w Markdown.

## Architektura: DOMENA vs INFRASTRUKTURA

Podział z kroku 07 pozostaje bez zmian: **domena** to model + operacje (bez sieci), a **serwer**
to cienka infrastruktura wystawiająca dane. Ten krok **nie zmienia zachowania** — dokładamy
wyłącznie dokumentację Javadoc po stronie domeny:

- **Domena** (`...step08.domain`) — model i operacje bez zmian; każdy publiczny typ i publiczna
  metoda dostają **Markdown Javadoc** (JEP 467). Pakiet domeny nadal nie importuje klas
  sieciowych.
- **Infrastruktura** (`...step08.server`) — skopiowana bez zmian z kroku 07 (tylko pakiet i
  string `step-08` w Javadoc/bannerze): `GET /aircraft`, `GET /areas`, `GET /`,
  `POST /tick`. Kontrakt HTTP pozostaje stabilny.

## Cel ćwiczenia
Poznasz **Markdown Documentation Comments** (JEP 467): komentarze `/** … */` i `///`, w których
treść piszesz w **Markdown** (nagłówki, listy, `code`, tabele, linki), a nie w surowym HTML.
W Flight Control udokumentujesz kontrakt domeny tak, by wygenerowana dokumentacja była czytelna
dla współpracowników: co robi `step()`, jak `describe()` destrukturyzuje `Area`, jak
klasyfikuje `ThreatClassifier`, co zwraca `Radar.report()` i na czym polega niezmienność modelu.

## Co zrobić
1. Dodaj Javadoc (Markdown, nie HTML) do **publicznego API** domeny: `Airspace` (+ metody
   `step()`, `describe()`, `aircraftSortedByDistance`, `areaLabels`, `aircraftById`),
   `Airspace.sample()`, `Aircraft`, `Area` (`Circle`/`Polygon`), `Point`, `Velocity`,
   oraz (skopiowane z kroku 07) `Radar`/`RadarReport`/`Category`/`ThreatClassifier`.
2. Opisz **zachowanie**, nie implementację:
   - `step()` przesuwa każdy samolot o `vel` i zwraca **nowy**, niezmienny stan;
   - `describe()` to wyczerpujący `switch` po zapieczętowanym `Area` z **record patterns**;
   - `ThreatClassifier.classify()` → `ALARM`/`SECTOR`/`NORMAL`/`UNKNOWN` (guards + `case null`);
   - `Radar.report()` — wątki wirtualne, agregacja per `Category`, deterministyczny niezmienny raport.
3. Utrwal w Javadoc **kontrakt niezmienności** (defensywne `List.copyOf`, świeże widoki,
   `step()` nie mutuje odbiorcy) — założenia, których współpracownik nie powinien złamać.
4. Używaj składni Markdown: `#`/`##`, `-`, `` `code` ``, tabele `| … |`, linki `[tekst](url)`,
   bloki ` ``` `. Nie używaj `{@code}`/`<pre>`/`<b>`/`<p>`.

## Dane testowe (YAML — przykład tego, co dokumentujesz)
```yaml
documentation:
  step: "Przesuwa każdy samolot o jego wektor prędkości; zwraca NOWY, niezmienny stan."
  props:
    - "type/sector — metadane obszaru (np. control = CTR, terminal = TMA)"
  describe: "Klasyfikuje obszar po typie (Circle/Polygon) — wyczerpujący switch z record patterns."
  category:
    - ALARM  -> szybki i wewnątrz sektora
    - SECTOR -> wewnątrz, ale nie ponad próg alarmu
    - NORMAL -> poza sektorem
    - UNKNOWN-> null
```

## Napisz testy (akceptacja)
- **Ten krok nie wymaga nowego testu jednostkowego** — dodajesz wyłącznie dokumentację.
- Skopiowane testy z kroku 07 (`AirspaceDomainTest` + `AirspaceServerSmokeTest`) muszą nadal
  przechodzić — nie usuwaj pokrycia, ale `step08.domain` **nie musi** mieć 100% linii
  (cały pakiet/bundle trzyma próg ≥90%).
- Kryterium: publiczne API ma komentarze **Markdown** (nie surowy HTML); Javadoc czytelnie
  opisuje `step()`, `describe()`, `classify()`, `report()` i kontrakt niezmienności.

## Wskazówki
- `/** … */` (wieloliniowy) vs `///` (liniowy) — JEP 467 obsługuje oba; wybierz spójnie.
- W Markdown Javadoc nie musisz używać `{@literal}`/`{@code}`/`<pre>` — wystarczą `` `code` ``.
- Opisuj **kontrakt** (co gwarantuje metoda), a nie szczegóły implementacji.
- **pomocnik:** `./gradlew javadoc` dla całego projektu może się nie zbudować, bo demo
  preview/incubator (FFM, Vector) wymagają flag JVM nieustawionych dla zadania `javadoc`;
  dokumentację konkretnego kroku obejrzysz po `./gradlew classes` w narzędziu IDE.
- **Do przemyślenia:** jakie założenia domenowe (immutability, wyczerpalność `Area`) warto
  utrwalić w Javadoc, by współpracownik ich nie złamał?

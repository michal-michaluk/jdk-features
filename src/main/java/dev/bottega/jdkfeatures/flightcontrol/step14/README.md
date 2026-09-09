# Step 14 — JEP 524 — PEM Encodings (preview)

> **JDK 26 (preview)** · Eksport/import konfiguracji sektora jako PEM
> (`-----BEGIN ...-----`) przez `PEMEncoder`/`PEMDecoder`.

## Cel ćwiczenia
Poznasz **PEM Encodings** (JEP 524): wbudowany `PEMEncoder`/`PEMDecoder` + typ
`java.security.DEREncodable`. W Flight Control zamieniasz klucz sektora (albo mały
serializowany `props`-payload) na czytelny tekst PEM i z powrotem — **round-trip** musi
odtworzyć te same dane.

## Co zrobić
1. Stwórz dane: wygeneruj parę kluczy RSA (`KeyPairGenerator`) albo weź mały payload
   metadanych obszaru.
2. Zakoduj do **PEM** przez `PEMEncoder.encodeToString(derEncodable)`.
3. Zdekoduj z powrotem (`PEMDecoder.decode(pem, ...)`) — sprawdź, że dane są takie same.
4. Sprawdź nagłówek (`-----BEGIN ...-----`) i typ (klucz prywatny / publiczny).

## Dane testowe (YAML)
```yaml
pem:
  begin: "-----BEGIN PRIVATE KEY-----"
  public_begin: "-----BEGIN PUBLIC KEY-----"
  payload: <para kluczy RSA wygenerowana przez KeyPairGenerator>
roundtrip:
  decode(encode(payload)) == payload
```

## Napisz testy (akceptacja)
- **Round-trip:** po `encode`→`decode` dostajesz ten sam klucz (`equals` na kluczu).
- PEM zaczyna się od `-----BEGIN PRIVATE KEY-----` / `-----BEGIN PUBLIC KEY-----`
  i kończy odpowiednim `-----END ...-----`.
- Generacja klucza działa (`generate(keySize)`), metoda `roundTrips()` zwraca `true`.
- `KeyMaterial` jest niezmienny: `privateKey()`, `publicKey()`, `equals`/`hashCode`.
- (ciągłość) testy domeny i kontrakt serwera z kroku 13 nadal przechodzą; pakiet
  `step14.domain` ma **100% pokrycia linii**.

## Wskazówki
- JEP 524 jest **preview** (JDK 26) → `--enable-preview` przy kompilacji/run.
- `PEMEncoder.of().encodeToString(...)` zwraca tekst z nagłówkiem; `PEMDecoder.of()`
  ma `decode(String)` (→ `DEREncodable`) oraz typowane `decode(String, Class<S>)`.
- W `JEP 524` klucz `java.security` (`java.security.PrivateKey`/`PublicKey`/`KeyPair`)
  jest akceptowany jako `DEREncodable` — nie trzeba ręcznie `getEncoded()`.
- **Do przemyślenia:** dlaczego PEM (z `BEGIN`/`END`) jest wygodniejszy do osadzania
  w konfiguracji (text/yaml) niż surowe bajty DER?

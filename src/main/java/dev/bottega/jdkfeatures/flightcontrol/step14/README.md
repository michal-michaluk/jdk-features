# Step 14 — JEP 524 — PEM Encodings (preview)

> **JDK 26 (preview)** · Eksport/import konfiguracji jako PEM.

## Cel ćwiczenia
Poznasz **PEM Encodings** (JEP 524): standardowe API `java.util.Base64`-niezależne do
**enkodowania/dekodowania PEM** (`-----BEGIN ...-----`) przez `PEMEncoder`/`PEMDecoder` +
`DEREncodable`. W Flight Control eksportujesz/importujesz klucz lub metadane obszaru do/jako
PEM — np. `props`/adres obszaru, które chcesz przekazać między instancjami.

## Co zrobić
1. Stwórz dane (np. klucz RSA lub serializowane metadane obszaru) i zakoduj do **PEM**.
2. Użyj `PEMEncoder` + `DEREncodable` (klucz z `java.security`) — sprawdź składnię w
   dokumentacji (nie dostajesz gotowca).
3. Zdekoduj z powrotem (`PEMDecoder`) i porównaj z oryginałem (round-trip).
4. Sprawdź nagłówek (`-----BEGIN ...-----`) i typ.

## Dane testowe (YAML)
```yaml
pem:
  begin: "-----BEGIN PRIVATE KEY-----"
  payload: <klucz RSA lub bajty metadanych>
roundtrip:
  encode(decode(pem)) == oryginał
```

## Napisz testy (akceptacja)
- **Round-trip:** po `encode`→`decode` dostajesz te same dane (klucz/bajty).
- PEM zaczyna się od `-----BEGIN ...-----` i kończy `-----END ...-----`.
- `PEMEncoder`/`PEMDecoder` obsługują klucz (nagłówek typu).

## Wskazówki
- JEP 524 jest **preview** (JDK 26) → `--enable-preview` przy kompilacji/run.
- `PEMEncoder`/`PEMDecoder` z `java.security`; `DEREncodable` w `java.security` ma
  `toDer()`/`fromDer(...)` — sprawdź realne nazwy metod w dokumentacji.
- Dla klucza: najłatwiej wygenerować parę `KeyPairGenerator` i eksportować klucz prywatny
  (`getEncoded()` → `DER`).
- **Do przemyślenia:** dlaczego PEM (z `BEGIN`/`END`) jest wygodniejszy do przekazywania
  niż surowe bajty DER? (czytelność, osadzanie w konfiguracji/text)

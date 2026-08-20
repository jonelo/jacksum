*Diese Seite auf Englisch: [ALGORITHMS.md](ALGORITHMS.md)*

**Inhaltsverzeichnis**
 - [Algorithmen-Unterstützung](#algorithm_support)
 - [Standardalgorithmen](#standard_algorithms)
   - [Standardalgorithmen, alphabetisch sortiert](#standard_hash_functions_sorted_alphabetically)
   - [Standardalgorithmen, logisch sortiert](#standard_hash_functions_sorted_logically)
     - [Kryptografische Hashfunktionen](#cryptographic_hash_functions)
     - [Hashbäume](#hash_trees)
     - [Nicht-kryptografische Hashfunktionen](#non_cryptographic_hash_functions)
 - [Angepasste Algorithmen](#customized_hash_functions)
   - [Verkettete Algorithmen](#concatenated_algorithms)
   - [HMAC](#hmac)
   - [Verkürzter HMAC](#truncated_hmac)
   - [Anpassbare CRCs](#customizable_crcs)
 - [Details zu einem bestimmten Algorithmus ermitteln](#getting_details)
 - [Fußnoten](#footnotes)

<a name="algorithm_support"></a>

# Algorithmen-Unterstützung

Jacksum unterstützt **586 Standardalgorithmen**: kryptografische Hashfunktionen, Hashbäume,
nicht-kryptografische Hashfunktionen, CRCs und klassische Prüfsummen.

Darüber hinaus unterstützt Jacksum auch **angepasste Algorithmen**. Dazu gehören verkettete
Algorithmen, HMAC, verkürzter HMAC und anpassbare CRCs.

Die beiden folgenden Abschnitte zeigen genau dieselbe Menge von 586 Algorithmen zweimal: einmal
[alphabetisch sortiert](#standard_hash_functions_sorted_alphabetically), wenn Sie einen bestimmten
Namen nachschlagen möchten, und einmal
[logisch sortiert](#standard_hash_functions_sorted_logically), wenn Sie verstehen möchten, woher ein
Algorithmus stammt und wie vertrauenswürdig er ist.

Siehe auch

- [Alle verfügbaren Algorithmen mit Details](https://github.com/jonelo/jacksum/wiki/Manpage#algorithms)
- [Anleitung zur Algorithmenauswahl](ALGORITHM_SELECTION_GUIDE_de.md) - welcher dieser Algorithmen zu Ihrem Anwendungsfall passt
- [Funktionen](FEATURES_de.md)


<a name="standard_algorithms"></a>
<a name="standard_hash_functions"></a>

# Standardalgorithmen

<a name="standard_hash_functions_sorted_alphabetically"></a>

## Standardalgorithmen, alphabetisch sortiert

Adler-32, ascon-hash, ascon-hasha, ascon-xof, ascon-xofa, AST strsum PRNG hash,
belt-hash (STB 34.101.31), BLAKE-[224,256,384,512], BLAKE2b-[8..512], BLAKE2s-[8..256],
BLAKE2bp, BLAKE2sp, BLAKE3, BlueMidnightWish-[224,256,384,512], cksum (Minix), cksum (Unix),
CubeHash-[224,256,384,512], CRC-8 (SMBus, FLAC), CRC-16 (ARC, LHA), CRC-16 (Minix), FCS-16,
CRC-24 (OpenPGP), CRC-32 (FCS-32), CRC-32 (MPEG-2), CRC-32 (bzip2),
CRC-32 (FDDI, a.k.a. sum on Plan 9), CRC-32 (UBICRC32, a.k.a. JAMCRC), CRC-32 (PHP's crc32),
CRC-32 (Go Koopman), CRC-32c (Castagnoli, iSCSI), CRC-64 <sup><a href="#iso3309">Anm.</a></sup>,
CRC-64 (ECMA-182), CRC-64 (prog lang GO, const ISO), CRC-64 (NVM Express 64b CRC),
CRC-64 (.xz and prog lang GO, const ECMA), CRC-82/DARC, DHA-256, ECHO-[224,256,384,512], ed2k,
Edon-R-[224,256,384,512], ELF (Unix), esch256, esch384, Fletcher's Checksum,
FNV-0_[32,64,128,256,512,1024], FNV-1_[32,64,128,256,512,1024],
FNV-1a_[32,64,128,256,512,1024], FORK-256, Fugue-[224,256,384,512], Fugue2-[224,256,384,512],
GOST Crypto-Pro (GOST R 34.11-94), GOST R 34.11-94, Groestl-[224,256,384,512],
Hamsi-[224,256,384,512], HAS-160 (KISA), HAVAL-128-[3,4,5], HAVAL-[160,192,224,256]-[3,4,5],
JH[224,256,384,512], joaat, KangarooTwelve, Keccak[224,256,288,384,512],
Kupyna-[256,384,512] (DSTU 7564:2014), LSH-256-[224,256], LSH-512-[224,256,384,512] (KS X 3262),
Luffa-[224,256,384,512], MD2, MD4, MD5, MD6-[8..512], MDC2, MarsupilamiFourteen, PANAMA,
photon-beetle, PHP Tiger variants (tiger192,4, tiger160,4, and tiger128,4), PRNG hash,
RadioGatun[32,64], RIPEMD-128, RIPEMD-[160,256,320], Romulus-H, SHA-0, SHA-1,
SHA-[224,256,384,512], SHA-512/[224,256] (NIST FIPS 180-4), SHA3-[224,256,384,512],
Shabal-[192,224,256,384,512], SHAKE[128,256] (NIST FIPS 202), SIMD-[224,256,384,512], SM3,
Skein-256-[8..256], Skein-512-[8..512], Skein-1024-[8..1024],
Streebog-[256,512] (GOST R 34.11-2012), sum (BSD Unix), sum (Minix), sum (System V Unix),
sum [8,16,24,32,40,48,56,64], Tiger, Tiger/128, Tiger/160, Tiger2, TTH (Tiger Tree Hash),
TTH2, VSH-1024, Whirlpool-0, Whirlpool-1 (a.k.a. Whirlpool-T), Whirlpool, Xoodyak, xor8 und
XXH32.

<a name="standard_hash_functions_sorted_logically"></a>

## Standardalgorithmen, logisch sortiert

<a name="cryptographic_hash_functions"></a>

### Kryptografische Hashfunktionen

- Internationale und nationale Standards kryptografischer Hashfunktionen (absteigende alphabetische Sortierung der Länder):

  - Vereinigte Staaten von Amerika (USA)

    - SHA-1 <sup><a href="#broken">gebrochen</a></sup>
    - SHA-2-Familie: SHA-[224,256,384,512], SHA-512/[224,256] (NIST FIPS 180-4)
    - SHA-3-Familie: SHA3-[224,256,384,512], SHAKE[128,256] (NIST FIPS 202)

  - Ukraine

    - Kupyna-[256,384,512] (DSTU 7564:2014)

  - Russische Föderation

    - GOST R 34.11-94 <sup><a href="#broken">gebrochen</a></sup>
    - GOST Crypto-Pro (GOST R 34.11-94) <sup><a href="#broken">gebrochen</a></sup>
    - Streebog-256 (GOST R 34.11-2012)
    - Streebog-512 (GOST R 34.11-2012) <sup><a href="#broken">teilweise gebrochen</a></sup>

  - Republik Korea (ROK)

    - HAS-160 (KISA)
    - LSH-256-[224,256], LSH-512-[224,256,384,512] (KS X 3262)

  - Volksrepublik China (VRC)

    - SM3

  - Republik Belarus

    - belt-hash (STB 34.101.31)

- eXtendable Output Functions (XOF) als kryptografische Hashfunktionen mit fester Länge:

  - KangarooTwelve
  - MarsupilamiFourteen
  - SHAKE128
  - SHAKE256

  Ascon-Xof und Ascon-XofA sind ebenfalls XOFs; sie sind weiter unten mit dem Rest der Ascon-Familie aufgeführt.

- International akzeptierte, moderne, starke kryptografische Hashfunktionen:

  - BLAKE2s-[8..256]
  - BLAKE2b-[8..512]
  - BLAKE2sp-256
  - BLAKE2bp-512
  - BLAKE3
  - RadioGatun[32,64]
  - PHPs Varianten von Tiger ("tiger192,4", "tiger160,4" und "tiger128,4")
  - RIPEMD-[128,160,256,320]
  - Tiger2
  - Tiger
  - Tiger/128
  - Tiger/160
  - Whirlpool
  - Whirlpool-0 <sup><a href="#superseded">überholt</a></sup>
  - Whirlpool-1, auch bekannt als Whirlpool-T <sup><a href="#superseded">überholt</a></sup>

- Alle 5 Finalisten des NIST-Lightweight-Cryptography-Wettbewerbs (2019–2023), die Hashing unterstützen:

  - Ascon-Hash und Ascon-Hasha, Ascon-Xof und Ascon-XofA
  - Esch[256,384]
  - PHOTON-Beetle Hash
  - Romulus-H
  - Xoodyak

- Alle 5 Finalisten (Runde 3) des NIST-SHA-3-Wettbewerbs (2007–2012):

  - BLAKE-[224,256,384,512]
  - Groestl-[224,256,384,512]
  - JH[224,256,384,512]
  - Keccak[224,256,288,384,512]
  - Skein-256-[8..256], Skein-512-[8..512], Skein-1024-[8..1024]

- 8 von 9 Kandidaten aus Runde 2 des NIST-SHA-3-Wettbewerbs, ohne die Finalisten (2007–2012):

  - ECHO-[224,256,384,512]
  - Fugue-[224,256,384,512] und Fugue2-[224,256,384,512], die Überarbeitung von Fugue aus dem
    Jahr 2012
  - Luffa-[224,256,384,512]
  - BlueMidnightWish-[224,256,384,512]
  - SIMD-[224,256,384,512]
  - CubeHash-[224,256,384,512]
  - Hamsi-[224,256,384,512] <sup><a href="#broken">teilweise gebrochen</a></sup>
  - Shabal-[192,224,256,384,512]

- 2 von 37 Kandidaten aus Runde 1 des NIST-SHA-3-Wettbewerbs, ohne die Kandidaten aus Runde 2 und die Finalisten (2007–2012):

  - Edon-R-[224,256,384,512] <sup><a href="#broken">teilweise gebrochen</a></sup>
  - MD6-[8..512]

- Vorschläge aus den NIST-Workshops von 2005, also vor dem SHA-3-Wettbewerb:

  - DHA-256
  - FORK-256 <sup><a href="#broken">gebrochen</a></sup>
  - VSH-1024 <sup><a href="#broken">teilweise gebrochen</a></sup>

- Gebrochene kryptografische Hashfunktionen für Bildungszwecke und zur Rückwärtskompatibilität:

  - ed2k <sup><a href="#broken">gebrochen</a></sup>
  - HAVAL-128-[3,4,5], HAVAL-[160,192,224,256]-[3,4,5] <sup><a href="#broken">gebrochen</a></sup>
  - MD2 <sup><a href="#broken">teilweise gebrochen</a></sup>
  - MD4 <sup><a href="#broken">gebrochen</a></sup>
  - MD5 <sup><a href="#broken">gebrochen</a></sup>
  - MDC2 <sup><a href="#broken">gebrochen</a></sup>
  - PANAMA <sup><a href="#broken">gebrochen</a></sup>
  - SHA-0 <sup><a href="#broken">gebrochen</a></sup>
  - SHA-1 <sup><a href="#broken">gebrochen</a></sup>

<a name="hash_trees"></a>

### Hashbäume

Merkle-Baum-Modi, die auf einer kryptografischen Hashfunktion aufsetzen. Der Baummodus selbst
führt keine Schwäche ein, jeder ist also genauso stark wie die zugrunde liegende Hashfunktion.

- TTH, der Tiger Tree Hash (basiert auf Tiger)
- TTH2 (basiert auf Tiger2)

<a name="non-cryptographic_hash_functions"></a>
<a name="non_cryptographic_hash_functions"></a>

### Nicht-kryptografische Hashfunktionen

- Standardisierte zyklische Redundanzprüfungen (CRCs)

  - CRC-8 (SMBus, FLAC)
  - CRC-16 (ARC, LHA), FCS-16
  - CRC-24 (OpenPGP, RFC 2440)
  - CRC-32 (FCS-32; ISO 3309, ISO/IEC 13239:2002, ITU-T V.42), CRC-32 (MPEG-2),
    CRC-32 (bzip2), CRC-32 (UBICRC32, a.k.a. JAMCRC), CRC-32 (PHP's crc32),
    CRC-32 (GO KOOPMAN), CRC-32c (Castagnoli, iSCSI, RFC 7143 Abschnitt 13.1)
  - CRC-64 <sup><a href="#iso3309">Anm.</a></sup>, CRC-64 (ECMA-182),
    CRC-64 (prog lang GO, const ISO), CRC-64 (.xz and prog lang GO, const ECMA),
    CRC-64 (NVM Express 64b CRC)
  - CRC-82 (DARC)

- CRCs, die mehr als die 6 Standardparameter des Rocksoft-(tm)-Modells benötigen, weil sie
  die Länge der Nachricht in den CRC einbeziehen

  - cksum (Unix, POSIX 1003.2)
  - CRC-32 (FDDI), was genau derselbe Algorithmus ist wie sum auf Plan 9

- Nicht-kryptografische Hashfunktionen

  - AST strsum PRNG hash
  - ELF (Unix)
  - FNV-0_[32,64,128,256,512,1024]
  - FNV-1_[32,64,128,256,512,1024]
  - FNV-1a_[32,64,128,256,512,1024]
  - joaat (Bob Jenkins' One-at-a-Time Hash)
  - PRNG hash (einschließlich Parameter)
  - XXH32

- Klassische Prüfsummen

  - Adler-32
  - cksum (Minix) <sup><a href="#rocksoft">Anm.</a></sup>
  - CRC-16 (Minix) <sup><a href="#rocksoft">Anm.</a></sup>
  - Fletcher's Checksum
  - sum (BSD Unix)
  - sum (Minix)
  - sum (System V Unix)
  - sum [8,16,24,32,40,48,56,64]
  - xor8

<a name="customized_hash_functions"></a>
<a name="customized_algorithms"></a>

# Angepasste Algorithmen

Zusätzlich zu den 586 Standardalgorithmen können Sie mit Jacksum eigene Algorithmen bauen.
Angepasste Algorithmen werden von `jacksum -a all -l` nicht aufgelistet, sie können aber genau wie
jeder Standardalgorithmus ausgewählt und verwendet werden.

<a name="concatenated_algorithms"></a>

## Verkettete Algorithmen

Beliebig viele Algorithmen können mit `+` verkettet werden. Alle Dateien werden nur einmal
gelesen, unabhängig davon, wie viele Algorithmen ausgewählt wurden, und jeder Algorithmus kann in
seinem eigenen Thread laufen.

    $ jacksum -a sha256+crc32 -q txt:"Hello World"

Die Filtersyntax funktioniert hier ebenfalls, `-a all:32+all:64` wählt also alle 32-Bit- und alle
64-Bit-Algorithmen auf einmal aus.

<a name="hmac"></a>

## HMAC

Jacksum unterstützt **HMAC**, einen Mechanismus zur Nachrichtenauthentisierung, der eine iterierte
kryptografische Hashfunktion in Kombination mit einem geheimen, gemeinsam genutzten Schlüssel
verwendet.

    $ jacksum -a hmac:sha256 -k txt:secret -q txt:"Hello World"

**492** der 586 Algorithmen können als zugrunde liegende Funktion eines HMAC verwendet werden;
rufen Sie `jacksum --hmacs` für die vollständige Liste auf, oder `jacksum --hmacs -V summary` für
die Anzahl.

Die übrigen 94 Algorithmen können aus einem der folgenden Gründe nicht verwendet werden:

- Sie sind überhaupt keine kryptografischen Hashfunktionen: alle CRCs, klassischen Prüfsummen und
  nicht-kryptografischen Hashfunktionen.
- Ihre Blockgröße ist nicht größer als ihre Digest-Größe, was HMAC (RFC 2104 / FIPS 198-1)
  verlangt. Deshalb sind schwammbasierte (sponge) und XOF-basierte Konstruktionen ausgeschlossen,
  darunter die Ascon-Familie, Esch, Xoodyak, PHOTON-Beetle, Romulus-H, RadioGatun, CubeHash,
  Fugue, Fugue2, Luffa und Hamsi.
- Sie sind keine einfachen iterierten Hashfunktionen: die Baum-Hashes TTH und TTH2 sowie die
  parallelen und baumbasierten Modi BLAKE3 und BLAKE2bp.

Beachten Sie, dass ein HMAC, der auf einer <a href="#broken">gebrochenen</a> Hashfunktion aufbaut,
nicht automatisch selbst gebrochen ist, und umgekehrt; Details liefert
`--info --verbose details`.

<a name="truncated_hmac"></a>

## Verkürzter HMAC

Die HMAC-Ausgabe kann durch Anhängen von `:<bits>` auf eine bestimmte Anzahl von Bits verkürzt
werden:

    $ jacksum -a hmac:sha256:128 -k txt:secret -q txt:"Hello World"

`jacksum --hmacs -V info` gibt für jeden Algorithmus die empfohlene Mindestlänge des Schlüssels
und die empfohlene Mindestlänge eines verkürzten HMAC aus.

<a name="customizable_crcs"></a>

## Anpassbare CRCs

Jacksum implementiert den **"Rocksoft (tm) Model CRC Algorithm"** zur Beschreibung von CRCs,
sodass zusätzlich 1,0399 * 10^267 angepasste CRCs verwendet werden können, mit einer Breite von 1
bis 64 Bit:

    crc:<width>,<poly>,<init>,<refIn>,<refOut>,<xorOut>[,<includeLen>[,<xorLen>]]

Die beiden optionalen Parameter `includeLen` und `xorLen` gehen über das klassische
6-Parameter-Modell hinaus und beziehen die Länge der Nachricht in den CRC ein. Zum Beispiel lässt
sich CRC-32 (FDDI) so schreiben:

    $ jacksum -a crc:32,04C11DB7,00000000,true,true,00000000,true,CC55CC55 -q txt:"Hello World"

Fügen Sie `--info` zu einer beliebigen `crc:`-Definition hinzu, um das Polynom in seiner normalen,
umgekehrten und Koopman-Darstellung zu sehen, dazu das reziproke Polynom. `jacksum -h crc:`
dokumentiert jeden Parameter im Detail; siehe auch die
[Manpage](https://github.com/jonelo/jacksum/wiki/Manpage#algorithms) für ausgearbeitete Beispiele.

<a name="getting_details"></a>

# Details zu einem bestimmten Algorithmus ermitteln

Die Manpage dokumentiert jeden Algorithmus mit Namen, Länge, Typ, Jahr der Veröffentlichung,
Website, Standard, einem Kommentar, seinem Sicherheitsstatus ("broken"), einer
Kompatibilitätsliste für andere Werkzeuge und Programmiersprachen, der Jacksum-Version, in der er
hinzugefügt wurde, und der Herkunft der Implementierung:

    $ jacksum -h sha3-256                     # Dokumentation eines Algorithmus (oder einer Familie)
    $ jacksum -a sha3-256 --info              # Laufzeitinformationen, einschließlich Sicherheitsstatus
    $ jacksum -a sha3-256 --info -V details   # dasselbe, mit der Begründung des Sicherheitsstatus
    $ jacksum -a all --info                   # dasselbe für jeden unterstützten Algorithmus

Um Algorithmus-IDs anstelle der Dokumentation aufzulisten und die Liste über die Bitlänge oder über
eine Teilzeichenkette der ID einzuschränken:

    $ jacksum -a all -l                       # die IDs aller 586 Algorithmen
    $ jacksum -a all:256 -l                   # nur die mit einer 256 Bit langen Ausgabe
    $ jacksum -a all:sha -l                   # nur die, deren ID "sha" enthält
    $ jacksum --hmacs                         # nur die, die für einen HMAC verwendet werden können

<a name="footnotes"></a>

# Fußnoten

<a name="broken"></a>

**gebrochen / teilweise gebrochen** — um den Sicherheitsabschnitt mit der eigentlichen Erklärung zu
sehen, rufen Sie auf

```
jacksum -a <algorithm> --info --verbose details
```

<a name="superseded"></a>

**überholt** — es wurde kein Angriff veröffentlicht, der auf den Algorithmus selbst zielt, das
Design wurde jedoch später überarbeitet. Bei Whirlpool-0 (2000) wurde in Whirlpool-1 (2001) die
S-Box ersetzt, und Whirlpool-1 enthält weiterhin den Fehler in der Diffusionsmatrix, der erst im
endgültigen Whirlpool (2003) korrigiert wurde.

<a name="iso3309"></a>

**CRC-64** — von diesem 64-Bit-CRC (Generatorpolynom x^64 + x^4 + x^3 + x + 1) wird häufig
fälschlicherweise angenommen, er sei eine in ISO 3309 definierte Frame Checking Sequence. Er wurde
bis 2009 von den Proteinsequenz-Datenbanken SWISS-PROT und TrEMBL verwendet. Tatsächlich ist nur
der 32-Bit-CRC CRC-32/FCS-32 in ISO 3309 spezifiziert.

<a name="rocksoft"></a>

**cksum (Minix) und CRC-16 (Minix)** — trotz ihrer Namen klassifiziert Jacksum beide als
Prüfsummen und nicht als CRCs, weil keiner von beiden durch das Rocksoft-(tm)-Modell beschrieben
werden kann: sie verwenden modifizierte Update-Methoden, die aus Kompatibilitätsgründen beibehalten
werden. Siehe [minix-bug-151](https://jacksum.net/downloads/minix-bug-151.txt).

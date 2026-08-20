*Diese Seite auf Englisch: [ALGORITHM_SELECTION_GUIDE.md](ALGORITHM_SELECTION_GUIDE.md)*

**Inhaltsverzeichnis**
 - [Welche Frage diese Anleitung beantwortet](#what_this_guide_answers)
 - [Begriffe](#vocabulary)
 - [Der Lawineneffekt](#avalanche)
 - [Drei Familien, drei Aufgaben](#three_families)
 - [Wo CRCs weiterhin ihren Platz haben](#crcs)
 - [Was einen Algorithmus kryptografisch macht](#cryptographic)
 - [Was "gebrochen" bedeutet](#broken_means)
   - [Length-Extension](#length_extension)
 - [Wie genau wurde das Design geprüft?](#scrutiny)
 - [Algorithmen verketten](#concatenation)
 - [Geschwindigkeit, und warum sie meist das falsche Kriterium ist](#speed)
 - [Zwei Implementierungen hinter einer Algorithmus-ID](#implementations)
 - [Die Betriebssystem-Einschränkung](#os_constraint)
 - [Algorithmen, die man nicht mehr verwenden sollte](#blacklist)
   - [Stufe A: Zu kurz](#tier_a)
   - [Stufe B: Konstruktiv defekt](#tier_b)
   - [Stufe C: Gebrochene kryptografische Hashfunktionen](#tier_c)
   - [Gleicher Name, andere Variante](#variants)
 - [Warum ein gebrochener Algorithmus dennoch verwendbar ist: HMAC](#hmac)
 - [Das Entscheidungsverfahren](#decision)
 - [Durchgerechnetes Beispiel: Tausende von Dateien](#workflow)
 - [Anti-Muster](#antipatterns)
 - [Kurzreferenz](#cheatsheet)
 - [Fußnoten und weiterführende Literatur](#footnotes)


<a name="what_this_guide_answers"></a>

# Welche Frage diese Anleitung beantwortet

Sie haben eine Festplatte mit tausenden von Dateien. Sie möchten heute für jede einzelne einen
Fingerprint festhalten, damit Sie in Monaten oder Jahren nachweisen können, dass sich nichts
verändert hat. Welchen der 586 Algorithmen von Jacksum wählen Sie?

Diese Anleitung geht darum, diese Entscheidung bewusst zu treffen und nicht aus Gewohnheit. Sie
erklärt die notwendigen Begriffe (Hashfunktion, CRC, Lawineneffekt, Kollisionsresistenz, was
"gebrochen" bedeutet), sie benennt die Algorithmen, die Sie nicht mehr einsetzen sollten, und sie
zeigt, wie Sie Jacksum dazu bringen, Ihnen das Nötige *selbst zu sagen*, anstatt einer Tabelle in
einem Blogartikel zu vertrauen.

**Die Kurzantwort**, falls Sie nur einen Absatz lesen:

| Situation | Wahl |
|---|---|
| Ein Angreifer ist möglich, und andere Werkzeuge müssen die Liste prüfen können | `sha-256` |
| Ein Angreifer ist möglich, und Jacksum läuft auf beiden Seiten | `sha3-256` |
| Langzeitarchiv, die Daten überleben den Algorithmus | `sha256+sha3-256` |
| Sie möchten mehr Reserve als 256 Bit | `sha-512/256` oder `sha-512` |
| Die Prüfliste selbst liegt dort, wo ein Angreifer sie erreichen könnte | `hmac:sha256` mit `-k` |
| Nur zufällige Verfälschung zählt, oder ein Protokoll gibt den Wert vor | `crc32c`, `crc64_nvme` |

Alles nach dieser Tabelle ist die Begründung — damit Sie die Wahl verteidigen, bei anderen
Randbedingungen anpassen und erkennen können, wann der Rat anderer veraltet ist.

Zwei Dinge tut diese Anleitung bewusst **nicht**. Sie ordnet Algorithmen nicht nach einer einzigen
"Stärke" ein, denn die richtige Wahl hängt von Ihrem Bedrohungsmodell ab und davon, wer das
Ergebnis prüfen muss. Und sie verlangt nicht, dass Sie ihren Zahlen glauben: jeder hier genannte
Wert lässt sich mit dem daneben stehenden Jacksum-Kommando reproduzieren.

Siehe auch

- [Algorithmen](ALGORITHMS_de.md) — die vollständige Liste dessen, was Jacksum unterstützt, und
  woher es stammt
- [Funktionen](FEATURES_de.md), [Beispiele](EXAMPLES_de.md),
  [Jacksum Hacks](JACKSUM_HACKS_de.md)
- [Manpage](https://github.com/jonelo/jacksum/wiki/Manpage#algorithms)


<a name="vocabulary"></a>

# Begriffe

Eine Handvoll Begriffe trägt die gesamte Argumentation. Es lohnt sich, sie genau festzulegen,
denn die meisten schlechten Algorithmus-Entscheidungen entstehen daraus, dass zwei davon
vermischt werden.

**Nachricht.** Die Eingabe. Für unsere Zwecke die Bytes einer Datei. Jacksum kann auch
Zeichenketten, stdin, Datenträger und NTFS-Alternate-Data-Streams hashen; die Überlegungen sind
dieselben.

**Hashfunktion.** Eine Funktion, die eine Nachricht beliebiger Länge auf einen Wert fester Länge
abbildet. "Beliebige Länge" und "feste Länge" zusammen sind der Grund dafür, dass die
interessanten Eigenschaften überhaupt möglich und die Probleme unvermeidbar sind: es gibt
unendlich viele Nachrichten und nur endlich viele Ausgaben.

**Hashwert.** Die Ausgabe. Je nach Tradition auch *Digest*, *Fingerprint*, *Prüfsumme* oder
*Prüfwert*. Jacksum verwendet durchgehend "Hashwert" und kann ihn mit `-E` in 17 Kodierungen
ausgeben (Hex, Base32, Base64, BubbleBabble, z-base-32, dezimal und weitere).

**Kollision.** Zwei verschiedene Nachrichten mit demselben Hashwert. Kollisionen existieren
immer — das Schubfachprinzip garantiert es. Die Frage ist nur, ob jemand eine *finden* kann.

**Kollisionsresistenz.** Es soll praktisch unmöglich sein, *irgendein* Paar kollidierender
Nachrichten zu finden. Der Angreifer darf beide Nachrichten wählen, und genau das macht diese
Eigenschaft zur am leichtesten brechbaren der drei.

**Urbildresistenz (Preimage).** Zu einem gegebenen Hashwert soll es praktisch unmöglich sein,
*eine* Nachricht zu finden, die ihn erzeugt. Das ist die Bedeutung von "Einwegfunktion".

**Zweite-Urbildresistenz (Second Preimage).** Zu einer gegebenen Nachricht soll es praktisch
unmöglich sein, eine *andere* Nachricht mit demselben Hashwert zu finden. Das ist die für die
Dateiintegrität wichtigste Eigenschaft: der Angreifer hat Ihre Datei und will eine andere Datei,
die Ihre Prüfung besteht.

**Blockgröße.** Iterierte Hashfunktionen verarbeiten die Nachricht in Blöcken fester Größe.
Jacksum zeigt sie in `--info` unter `Block size:`. Sie ist für HMAC relevant, das verlangt, dass
die Blockgröße größer als die Ausgabegröße ist.

**Merkle-Damgård-Konstruktion.** Das klassische Design: die Nachricht in Blöcke zerlegen und
jeden Block zusammen mit dem laufenden Zustand in eine Kompressionsfunktion geben. MD5, SHA-1
und die gesamte SHA-2-Familie arbeiten so. Das ist einfach und gut verstanden, und es hat eine
strukturelle Eigenheit — siehe [Length-Extension](#length_extension).

**Schwamm-Konstruktion (Sponge).** Das SHA-3-Design: die Nachricht in einen großen internen
Zustand absorbieren und die Ausgabe daraus herauspressen. Der Zustand ist größer als die Ausgabe,
was die Length-Extension-Eigenheit beseitigt und variable Ausgabelängen natürlich macht (genau
das ist eine XOF).

**Hashbaum (Merkle-Baum).** Die Blätter hashen, dann die Hashwerte hashen, bis zu einer einzigen
Wurzel. Jacksum bietet TTH und TTH2; BLAKE3 verwendet intern einen Baummodus. Bäume erlauben
Parallelität und teilweise Verifikation. Der Baummodus selbst führt keine Schwäche ein, das
Ergebnis ist also genau so stark wie die zugrunde liegende Hashfunktion.

**CRC (Cyclic Redundancy Check).** Keine Hashfunktion im kryptografischen Sinn, sondern der Rest
einer Polynomdivision über GF(2). Die Nachricht wird als Polynom aufgefasst und durch ein festes
*Generatorpolynom* geteilt; der Rest ist der CRC. Genau diese algebraische Struktur ist der
Grund, warum CRCs die Fehlermuster, für die sie entworfen wurden, hervorragend erkennen — und
genau der Grund, warum sie gegen einen Angreifer wertlos sind.

**Rocksoft (tm) Model.** Die Beschreibung mit sechs Parametern, die einen CRC vollständig
festlegt: Breite, Polynom, Anfangswert, ob Eingangs- und Ausgangsbits gespiegelt werden, und ein
finaler XOR-Wert. Jacksum implementiert es, sodass
`-a crc:<width>,<poly>,<init>,<refIn>,<refOut>,<xorOut>` jeden CRC reproduziert, dem Sie
begegnen. Zwei optionale Parameter gehen über das klassische Modell hinaus und mischen die
Nachrichtenlänge in den Wert. Siehe `jacksum -h crc:` und
[ALGORITHMS_de.md](ALGORITHMS_de.md#customizable_crcs).

**Klassische Prüfsumme.** Die Familie vor den CRCs: Bytes addieren (`sum8` … `sum64`), sie
XOR-verknüpfen (`xor8`) oder eine etwas geschicktere Variante mit zwei laufenden Summen
(Adler-32, Fletcher). Billig — und, wie wir sehen werden, oft auf Weisen defekt, die auch ohne
Angreifer relevant sind.

**Kryptografische Hashfunktion.** Eine Hashfunktion, die zusätzlich Urbild-, Zweite-Urbild- und
Kollisionsresistenz gegen einen *intelligenten Angreifer, der den Algorithmus kennt*, zusichert.
Dieser letzte Teilsatz ist der ganze Unterschied zu einem CRC oder einer Prüfsumme, und Jacksum
hält ihn im Feld `type:` jeder Algorithmus-Dokumentation fest.

**HMAC.** Eine schlüsselbasierte Konstruktion auf einer Hashfunktion. Nicht der Hash von
Schlüssel und Nachricht aneinandergehängt, sondern ein bestimmtes verschachteltes Verfahren
(RFC 2104 / FIPS 198-1), dessen Sicherheit auf anderen Annahmen ruht als reines Hashen — weshalb
es einige gebrochene Hashfunktionen übersteht.


<a name="avalanche"></a>

# Der Lawineneffekt

Eine gute Hashfunktion verhält sich wie eine Zufallsfunktion: kippt man ein Bit der Eingabe, soll
jedes Ausgabebit mit Wahrscheinlichkeit 1/2 und unabhängig kippen. Diese Eigenschaft heißt
**Lawineneffekt** (englisch avalanche effect, bzw. strict avalanche criterion), und der Zielwert
ist daher **50 %** — nicht 100 %. Eine Funktion, bei der jedes Mal alle Ausgabebits kippen, wäre
genauso vorhersagbar wie eine, bei der keines kippt.

Jacksum misst das für Sie. `--info --verbose details` (bzw. `-V details`) gibt einen
Lawinen-Block aus:

```
$ jacksum -a sha-256 --info --verbose details
```

```
  Avalanche effect:
    input length in bytes:                9
    input length in bits:                 72
    hash calculations:                    73
    input [hex]:                          313233343536373839
    input [bin]:                          001100010011001000110011001101000011010100110110001101110011100000111001
    avalanche min effect:                 42.19 %
    avalanche avg effect:                 50.04 %
    avalanche max effect:                 57.03 %
```

**Wie die Messung funktioniert.** Jacksum hasht die Eingabe einmal als Referenz, kippt dann jedes
Eingabebit einzeln und hasht erneut — 72 Bit bedeuten 72 Kippvorgänge, also insgesamt 73
Hashberechnungen. Für jeden Kippvorgang wird die Hamming-Distanz zwischen dem neuen Hashwert und
der Referenz bestimmt, also wie viele Ausgabebits sich geändert haben, und als Prozentsatz der
Ausgabebreite ausgedrückt. Minimum, Mittelwert und Maximum dieser 72 Prozentwerte werden
ausgegeben. Die Standardeingabe ist die 9 Byte lange Zeichenkette `123456789`; mit
`-q <sequence>` setzen Sie Ihre eigene ein:

```
$ jacksum -a sum32 --info -V details -q txt:"Hello World"
```

**Wie man es liest.** Der Mittelwert sollte nahe bei 50 % liegen. Genauso wichtig ist die
*Streuung*: ein Minimum von 42 % und ein Maximum von 57 % bedeuten, dass kein einzelnes
Eingabebit ungewöhnlich wenig oder ungewöhnlich viel Einfluss hat. Vergleichen Sie diese
gemessenen Werte:

| Algorithmus | min | avg | max | Urteil |
|---|---|---|---|---|
| `sha-256` | 42,19 % | 50,04 % | 57,03 % | lehrbuchmäßig |
| `sha3-256` | 42,58 % | 49,39 % | 54,69 % | lehrbuchmäßig, engste Streuung |
| `md5` | 39,84 % | 50,20 % | 61,72 % | lehrbuchmäßig (der Lawineneffekt war nicht MD5s Problem) |
| `crc32` | 34,38 % | 45,53 % | 62,50 % | für einen CRC respektabel |
| `adler32` | 6,25 % | 15,80 % | — | schlecht |
| `sum32` | 3,13 % | 7,07 % | 12,50 % | fast keine Diffusion |
| `elf` | 3,13 % | 5,56 % | — | fast keine Diffusion |
| `xor8` | 12,50 % | 12,50 % | 12,50 % | überhaupt keine |

`xor8` ist der perfekte Lehrfall: Minimum, Mittelwert und Maximum liegen alle bei exakt 12,50 %,
also 1/8. Das Kippen eines beliebigen Eingabebits kippt immer genau ein Ausgabebit. Es gibt keine
Diffusion — die Ausgabe ist eine lineare Funktion der Eingabe, und jedes Bit davon ist auf die
Bits zurückführbar, aus denen es kam.

**Zwei Einschränkungen, beide wichtig.**

*Ein guter Lawinenwert ist notwendig, aber nicht ausreichend.* CRC-32 erreicht respektable
45,53 % und ist dennoch trivial invertierbar: weil die Operation affin über GF(2) ist, kann
jeder einen Vier-Byte-Patch berechnen, der eine Datei auf einen beliebigen CRC-32-Wert zwingt.
MD5 hat einen prächtigen Lawineneffekt und ist vollständig gebrochen. Der Lawineneffekt ist ein
*Design-Schnelltest* — ein schlechter Wert beweist, dass die Funktion untauglich ist, ein guter
Wert beweist nichts über kryptografische Stärke.

*Der Wert hängt von der Eingabelänge ab.* Eine breite Funktion braucht genug Eingabe, um ihren
Zustand zu füllen. Der Standard-`crc64` erreicht bei der 9 Byte langen Standardeingabe 6,47 %
und selbst bei 64 Byte nur 13,84 %, weil sein Generatorpolynom sehr dünn besetzt ist. Wenn Sie
Kandidaten vergleichen, geben Sie ihnen eine Eingabe, deren Größe Ihren echten Daten ähnelt:

```
$ jacksum -a crc64 --info -V details -q txt:"0123456789012345678901234567890123456789012345678901234567890123"
```


<a name="three_families"></a>

# Drei Familien, drei Aufgaben

Die 586 Algorithmen von Jacksum verteilen sich auf drei Familien, die für drei verschiedene
Aufgaben entworfen wurden. Die Dokumentation jedes Algorithmus nennt die Familie im Feld `type:`
von `jacksum -h <algo>`.

| | Klassische Prüfsumme | CRC | Kryptografische Hashfunktion |
|---|---|---|---|
| Entworfen für | billiges Erkennen von Übertragungsfehlern | Erkennen der Fehlermuster eines physikalischen Kanals | Widerstand gegen einen intelligenten Angreifer |
| Typische Breite | 8–32 Bit | 8–64 Bit | 128–512 Bit |
| Erkennt | manche zufälligen Einzelbyte-Fehler | alle Bündelfehler bis zum Grad des Polynoms, beweisbar | jede Änderung, von jedem, mit überwältigender Wahrscheinlichkeit |
| Erkennt *nicht* | Umsortierung von Bytes, eingefügte Nullbytes, viele Mehrbyte-Fehler | absichtliche Veränderung | nichts Bekanntes, solange die Funktion hält |
| Kosten | trivial | sehr gering | gering bis moderat |
| Jacksum-`type:` | `checksum` | `CRC` | `cryptographic hash function` |
| Vom Angreifer invertierbar | ja | ja | nein |

Der Unterschied, der Ihre Wahl entscheidet, ist **das Bedrohungsmodell**, und es hat genau zwei
Fälle.

**Fall 1: Zufälle.** Ein Kabel verliert ein Bit, ein Plattensektor verrottet, RAM kippt ein Bit,
ein Download bricht ab. Solche Fehler sind *zufällig und ohne Absicht*. Ein 32-Bit-CRC fängt
praktisch alle davon: jeder einzelne Bündelfehler von bis zu 32 Bit wird mit Sicherheit erkannt,
und eine zufällige Verfälschung rutscht mit einer Wahrscheinlichkeit von etwa 2^-32 durch. Das
ist eine echte technische Garantie, und deshalb finden sich CRCs überall in der Hardware.

**Fall 2: Ein Angreifer.** Jemand *will*, dass Ihre Prüfung bei einer von ihm veränderten Datei
durchgeht. Jetzt ist der Fehler nicht zufällig — er ist gewählt, von jemandem, der die
Spezifikation des Algorithmus gelesen hat.

Gegen einen Angreifer bietet ein CRC nichts, und der Grund ist strukturell, nicht eine Frage der
Breite. Ein CRC ist eine affine Funktion über GF(2): `crc(a XOR b) = crc(a) XOR crc(b) XOR c`
für eine Konstante `c`. Daraus folgt, dass jeder eine veränderte Datei nehmen, die vier Bytes
berechnen kann, die angehängt oder eingesetzt werden müssen, und auf jedem beliebigen Ziel-CRC
landet. Von Rechenaufwand kann man dabei kaum sprechen; es ist das Lösen eines kleinen linearen
Gleichungssystems. Ein breiterer CRC hilft nicht, denn der Angriff sucht nicht — er löst.
Dasselbe gilt für `sum`, `xor` und Adler: die sind ebenfalls linear.

Das ist der folgenreichste Gedanke dieser Anleitung. **Ein CRC beantwortet "haben sich die Daten
zufällig verändert?" Eine kryptografische Hashfunktion beantwortet "haben sich die Daten
überhaupt verändert?"** Das sind verschiedene Fragen, und nur eine davon hat mit Sicherheit zu
tun.


<a name="crcs"></a>

# Wo CRCs weiterhin ihren Platz haben

Nichts davon macht CRCs obsolet. Sie sind aktuelle Technik, werden weiterhin in neue Standards
hineingeschrieben, und Jacksum dokumentiert, wo jeder einzelne verwendet wird. Es geht darum, sie
für die Aufgabe einzusetzen, die sie gut können.

**Sie stehen in heutigen Spezifikationen, nicht nur in denen von gestern.** Aus Jacksums eigener
Algorithmus-Dokumentation:

- **`crc64_nvme`** — die NVM Express NVM Command Set Specification, Revision 1d, vom Dezember
  2023. Derselbe CRC wird von **Amazon S3** verwendet: das AWS SDK berechnet ihn während des
  Uploads und gibt ihn Base64-kodiert zurück. Ein 2023 standardisierter und vom größten
  Objektspeicher der Welt eingesetzter CRC lässt sich schwer als Altlast bezeichnen.
- **`crc32c`** — Castagnolis CRC-32, für iSCSI in RFC 7143 §13.1 spezifiziert und weit verbreitet
  für Dateisystem- und Netzwerk-Metadaten. Moderne CPUs haben dafür einen eigenen Befehl.
- **`crc24`** — die Prüfsumme im ASCII-Armor von OpenPGP (RFC 2440).
- **`crc8`** — der System Management Bus (SMBus) und der Free Lossless Audio Codec (FLAC).
- **`crc32`** — die Linie ISO 3309 / ITU-T V.42, die Ethernet, ZIP, gzip und PNG verwenden;
  `crc32_bzip2` ist die Variante von bzip2 mit demselben Polynom.
- **`crc82_darc`** — der Data Radio Channel nach ETSI EN 300 751.
- **`cksum`** — POSIX 1003.2, weiterhin die Prüfsumme, die `cksum(1)` ausgibt.

**Warum sie diese Aufgaben weiterhin gewinnen.** Ein CRC ist billig genug, um in wenigen Gattern
implementiert zu werden, und das zählt, wenn er in einem Plattencontroller oder einem Funkchip
laufen muss. Er ist schnell: `crc32c` ist der schnellste Algorithmus in Jacksums
Geschwindigkeitsrangliste. Und anders als eine Hashfunktion bringt er *Beweise* mit, welche
Fehlermuster er erkennt — eine Eigenschaft, die eine kryptografische Hashfunktion nicht bieten
kann, weil ihre Garantien probabilistisch und nicht kombinatorisch sind. Für einen 512-Byte-Sektor
oder einen Netzwerk-Frame ist ein CRC das richtige Werkzeug, und ein SHA-256 wäre überdimensioniert.

**Dateisysteme.** Jacksums Dokumentation vermerkt, dass SHA-256 optional von ZFS verwendet werden
kann, und dass ZFS' `edonr`-Prüfsumme ein Edon-R-512 ist, das *mit einem poolspezifischen
Schlüssel gesalzen* wird — die Werte von ZFS stimmen also absichtlich nicht mit den reinen
Edon-R-Werten überein, die Jacksum berechnet. (ZFS' Standardprüfsumme ist Fletcher-4, das Jacksum
nicht implementiert; verfügbar ist nur `fletcher16`.)

**Einen beliebigen CRC nachbilden.** Weil Jacksum das Rocksoft (tm) Model implementiert, können
Sie jeden CRC reproduzieren, den ein Gerät oder Protokoll vorgibt, und das Polynom in jeder
Darstellung betrachten:

```
$ jacksum -a crc:32,04C11DB7,FFFFFFFF,true,true,FFFFFFFF --info
$ jacksum -h crc:
```

`--info` gibt das Polynom in mathematischer, normaler, gespiegelter und Koopman-Notation aus,
dazu das reziproke Polynom — unschätzbar, wenn ein Datenblatt und eine Quelldatei sich zu
widersprechen scheinen. Siehe [ALGORITHMS_de.md](ALGORITHMS_de.md#customizable_crcs).

**Die Regel.** Verwenden Sie einen CRC, um Zufälle zu erkennen und um mit etwas
zusammenzuarbeiten, das einen vorschreibt. Verwenden Sie ihn nie als Sicherheitsmaßnahme und nie
als primären Fingerprint für eine große Dateisammlung — aus den Gründen in [Stufe A](#tier_a).


<a name="cryptographic"></a>

# Was einen Algorithmus kryptografisch macht

Eine kryptografische Hashfunktion ist nicht einfach eine besser durchmischte Prüfsumme. Sie gibt
drei ausdrückliche Zusicherungen, und jede hat *generische* Angriffskosten, die kein Design
unterbieten kann — die Kosten für rohe Gewalt gegen eine ideale Funktion derselben Breite `n`:

| Eigenschaft | Was der Angreifer tun muss | Generische Kosten |
|---|---|---|
| Urbildresistenz | eine Nachricht zu einem gegebenen Hashwert finden | 2^n |
| Zweite-Urbildresistenz | eine *andere* Nachricht zum Hashwert einer gegebenen finden | 2^n |
| Kollisionsresistenz | *irgendzwei* kollidierende Nachrichten finden | 2^(n/2) |

Kollisionsresistenz kostet nur 2^(n/2) wegen des **Geburtstagsparadoxons**: der Angreifer zielt
nicht auf ein Ziel, sondern auf irgendeine Übereinstimmung unter vielen Kandidaten. Nachdem er
etwa √(2^n) Nachrichten gehasht hat, wird ein passendes Paar wahrscheinlich. Ein 256-Bit-Hash
bietet also 256 Bit Urbildresistenz, aber nur **128 Bit Kollisionsresistenz** — und 128 Bit gilt
in der Branche als langfristige Untergrenze. Deshalb sind heute 256 Bit Ausgabe der Standard und
nicht 128.

**Eine Unterscheidung, die meist übergangen wird und die die nötige Breite verändert.**

*Prüfung pro Datei.* Sie haben pro Datei einen Hashwert festgehalten und vergleichen später neu
berechnete Werte damit. Hier gibt es keinen Geburtstagseffekt: jede Datei wird gegen ihren eigenen
gespeicherten Wert geprüft. Bei zufälliger Verfälschung liegt die Wahrscheinlichkeit einer
unerkannten Änderung bei etwa 2^-b, und sie verschlechtert sich nicht, wenn Dateien hinzukommen.
Gegen Zufälle bietet ein 32-Bit-Wert etwa eine unerkannte Verfälschung auf vier Milliarden — pro
Datei und unabhängig davon, wie viele Dateien Sie haben.

*Identifikation über Dateien hinweg.* Sie deduplizieren, suchen Duplikate oder gleichen eine
[Wanted-Liste](#workflow) gegen einen ganzen Baum ab. Jetzt wird jede Datei mit jeder anderen
verglichen, die Geburtstagsschranke gilt für die *gesamte Menge*, und die Garantie verfällt mit
wachsender Sammlung. Hier brechen kurze Werte zusammen: siehe die Tabelle in [Stufe A](#tier_a).

Derselbe 32-Bit-Wert kann also für die eine Aufgabe ausreichen und für die andere hoffnungslos
sein. Und sobald ein Angreifer hinzukommt, ändern sich beide Fälle: er wartet nicht auf einen
Zufall, er konstruiert einen.

Jacksum benennt die Familie im Feld `type:`, sodass Sie immer nachsehen können, womit Sie es zu
tun haben:

```
$ jacksum -h sha3-256 | head -20
```


<a name="broken_means"></a>

# Was "gebrochen" bedeutet

In der Kryptografie gilt eine Funktion als **gebrochen**, wenn jemand einen Angriff veröffentlicht,
der die oben genannten generischen Kosten für mindestens eine der zugesicherten Eigenschaften
unterbietet. Beachten Sie, was das aussagt — und was nicht:

- Es ist eine Aussage über *veröffentlichtes Wissen*, nicht über Ihre konkreten Dateien.
- Der Angriff muss nicht praktikabel sein. Ein Angriff mit Kosten 2^127,5 dort, wo 2^128
  zugesichert waren, ist im akademischen Sinn ein Bruch und in der Praxis bedeutungslos.
- Es bedeutet nicht, dass die Funktion in allem versagt. MD5s Kollisionsresistenz ist vernichtet,
  während seine Urbildresistenz weiterhin hält (bester bekannter Angriff: 2^123,4).

Jacksum verfolgt das pro Algorithmus und gibt es in einem `Security:`-Block aus. Es gibt fünf
Zustände (`net.jacksum.algorithms.BrokenState`):

| Zustand | Bedeutung |
|---|---|
| `no` | Gegen die vollständige Funktion ist kein Angriff besser als generisch bekannt |
| `partly` | Mindestens eine, aber nicht alle zugesicherten Eigenschaften sind gebrochen |
| `yes` | Die Funktion ist gebrochen |
| `depends` | Hängt von einem Parameter ab, z. B. der Hashfunktion unter einem HMAC |
| `n/a` | Die Frage stellt sich nicht — ein CRC oder eine Prüfsumme sichert nichts davon zu |

```
$ jacksum -a md5 --info --verbose details
```

```
  Security:
    broken:                               yes
      yes, 2004: identical-prefix collisions can be computed in
      seconds and chosen-prefix collisions in hours on a standard PC;
      the attack has been demonstrated against real X.509
      certificates (2008) and was abused by the Flame malware (2012);
      the preimage resistance is not broken (best attack 2^123.4),
      but MD5 must not be used for signatures
      see also https://eprint.iacr.org/2004/199.pdf
```

**Lesen Sie den Satz, nicht nur das Schlagwort.** Ohne `-V details` erhalten Sie `broken: yes` und
nichts weiter; mit der Option bekommen Sie die Begründung, die Jahreszahlen und eine Primärquelle.
Der Unterschied zwischen "theoretisch geschwächt" und "Kollisionen in Sekunden auf einem Laptop"
steckt vollständig in diesem Text. Vier Beispiele, die man sich einprägen sollte:

- **`md5` → `yes`.** Identical-Prefix-Kollisionen in Sekunden, Chosen-Prefix-Kollisionen in
  Stunden, 2008 an echten X.509-Zertifikaten demonstriert, 2012 von Flame als Waffe eingesetzt.
- **`sha-1` → `yes`.** Die erste Identical-Prefix-Kollision wurde im Februar 2017 berechnet
  (SHAttered, 2^63,1); im Januar 2020 folgte die erste *Chosen-Prefix*-Kollision (SHA-1 is a
  Shambles, 2^63,4, etwa 45 000 USD gemietete GPU-Zeit). Chosen-Prefix ist diejenige, die echte
  Protokolle bricht, weil der Angreifer kontrolliert, was dem kollidierenden Block vorangeht.
- **`sha-256` → `no`.** "The best collision attacks reach 31 of 64 steps and are practical at that
  step count (2024), the best preimage attacks reach 41 of 64 steps; the full SHA-256 is
  unaffected." Solche rundenreduzierten Ergebnisse sind es, aus denen Vertrauen entsteht: der
  Abstand zwischen 31 und 64 Schritten ist der Sicherheitsabstand.
- **`sha3-256` → `no`.** Die besten Kollisionsangriffe erreichen 5 von 24 Runden, die besten
  Urbildangriffe 4 von 24 — ein enormer Abstand, und als Zugabe immun gegen Length-Extension.

**Teilweise Brüche sind keine vollen Brüche.** `md2` ist `partly` gebrochen (die Urbildresistenz
der vollständigen Funktion fiel 2005), `md4` dagegen `yes` (Kollisionen in einem Bruchteil einer
Sekunde). Beide gehören in [Stufe C](#tier_c), aber es sind nicht dieselben Aussagen, und `partly`
für sicher zu halten ist ein eigenes [Anti-Muster](#antipatterns).

<a name="length_extension"></a>

## Length-Extension

Eine Schwäche verdient eine eigene Behandlung, weil sie Funktionen betrifft, die *nicht* gebrochen
sind.

Jede Merkle-Damgård-Hashfunktion — MD5, SHA-1, die gesamte SHA-2-Familie — gibt ihren internen
Zustand in ihrer Ausgabe preis. Aus `sha256(secret || message)` und der *Länge* von `secret` kann
ein Angreifer, der `secret` nie erfährt, `sha256(secret || message || padding || anything)`
berechnen. Der Zustand am Ende Ihrer Nachricht ist genau das, was er zum Weiterhashen braucht.

Jacksum weist in SHA-256s eigener Sicherheitsnotiz darauf hin: "as a Merkle-Damgard construction it
permits length-extension attacks: use HMAC or sha-512/256 where that matters".

Drei Auswege, alle verfügbar:

- **`hmac:sha256`** — die HMAC-Konstruktion ist genau so entworfen, dass das nicht funktioniert.
- **`sha-512/256`** — SHA-512 mit anderem Anfangswert, auf 256 Bit gekürzt. Jacksum: "the
  truncation removes the length-extension weakness of sha-512". Die zurückgehaltene Hälfte des
  Zustands ist das, was der Angreifer nicht rekonstruieren kann.
- **`sha3-256`** — ein Schwamm. Sein Zustand ist größer als seine Ausgabe, die Ausgabe verrät also
  nie genug, um das Absorbieren fortzusetzen.

Für den Dateiintegritäts-Anwendungsfall dieser Anleitung ist einfaches `sha-256` auf einer Datei
hiervon *nicht* betroffen — Sie bauen ja kein `H(secret || message)`. Relevant wird es in dem
Moment, in dem Sie etwas authentifizieren wollen, indem Sie ein Geheimnis voranstellen; und genau
diesen Fehler soll HMAC verhindern.


<a name="scrutiny"></a>

# Wie genau wurde das Design geprüft?

`broken: no` ist nur die halbe Wahrheit. Die andere Hälfte lautet: **wie sehr hat es überhaupt
jemand versucht?** Eine Funktion, die niemand untersucht hat, und eine, die hundert Kryptografen
ein Jahrzehnt lang angegriffen haben, melden beide "kein Angriff bekannt" — und das sind nicht
annähernd gleichwertige Aussagen.

Das ist die am meisten unterschätzte Dimension der Algorithmusauswahl, und sie lässt sich einfach
formulieren: **"kein veröffentlichter Angriff" ist nur in dem Maß ein Indiz, in dem Aufmerksamkeit
aufgewendet wurde.**

[ALGORITHMS_de.md](ALGORITHMS_de.md#standard_hash_functions_sorted_logically) sortiert jeden
Algorithmus bereits nach seiner Herkunft. Als Prüfungsstufen gelesen, stärkste zuerst:

**Stufe 1 — nationale und internationale Standards.** Ein formales Normungsverfahren bedeutet
Jahre öffentlicher Begutachtung, eine offene Kommentierungsphase und institutionelle Verantwortung
danach. SHA-2 (NIST FIPS 180-4), SHA-3 und SHAKE (FIPS 202), Kupyna (Ukraine, DSTU 7564:2014),
Streebog (Russland, GOST R 34.11-2012), LSH (Republik Korea, KS X 3262), HAS-160 (KISA), SM3
(China), belt-hash (Belarus, STB 34.101.31), Whirlpool (ISO/IEC 10118-3).

**Stufe 2 — Finalisten offener Wettbewerbe.** Diese wurden *konstruktionsbedingt* hart
kryptanalysiert: viele unabhängige Teams, öffentliche Regeln und ein mehrjähriger Anreiz, die
Einreichungen der anderen zu brechen. Die fünf Finalisten der dritten SHA-3-Runde (BLAKE, Groestl,
JH, Keccak, Skein) und die fünf Finalisten des NIST-Wettbewerbs für Lightweight Cryptography, die
Hashen unterstützen (Ascon, Esch, PHOTON-Beetle, Romulus-H, Xoodyak). Ein Wettbewerb ist
wohl eine *stärker* adversarielle Prüfung als ein Normungsverfahren.

**Stufe 3 — weit verbreitete Nicht-Standards mit starker unabhängiger Begutachtung.** Kein
formaler Standard, aber intensive Praxisnutzung und dauerhafte Analyse durch Dritte: BLAKE2
(RFC 7693), BLAKE3, RIPEMD-160, Tiger und Tiger2.

**Stufe 4 — Ausgeschiedene aus Wettbewerben.** Kandidaten der Runden 1 und 2, die nicht
weiterkamen. Kandidat gewesen zu sein ist **keine** Empfehlung — mehrere wurden gerade *deshalb*
ausgeschieden, weil die Analyse Probleme fand. Unter denen, die Jacksum mitbringt, sind `hamsi`
und `edonr` heute als `partly` gebrochen markiert, und Fugue musste zu Fugue2 nachgebessert
werden. Es ist wertvoll, sie verfügbar zu haben; für ein Archiv sind sie keine Wahl.

**Stufe 5 — Einzelvorschläge mit dünner Kryptanalyse.** Die Gruppe der NIST-Workshops von 2005 vor
dem SHA-3-Wettbewerb ist das Lehrbuchbeispiel. Sie enthielt `dha256`, `fork256` und `vsh`.
FORK-256 wurde 2007 gebrochen. VSH 2006 teilweise. DHA-256 hat keinen veröffentlichten Bruch —
aber angesichts dessen, was mit seinen beiden Weggefährten passierte, ist dieses saubere Zeugnis
ein *schwaches Indiz* und keine Empfehlung.

## Jacksum sagt Ihnen das bereits

Die Fähigkeit, die es sich zu erwerben lohnt: Jacksums `broken:`-Text kodiert die Prüfungsdimension
ausdrücklich, nicht nur das Ja/Nein-Urteil. Vergleichen Sie:

```
$ jacksum -a dha256 --info -V details
```
> "no, but boomerang attacks reach 46 of 64 steps and a pseudo-collision for the full function
> is known (2^127.5); the full DHA-256 has no collision or preimage attack, but **it has
> received far less public analysis than SHA-256**, which it was designed to improve upon"

```
$ jacksum -a belt-hash --info -V details
```
> "no; no attack on belt-hash itself has been published; note however that **the public
> cryptanalysis of belt-hash is thin compared with SHA-2 or SHA-3**: the third-party results
> published so far target the underlying Bel-T block cipher and reach at most 6 of its 8 rounds"

```
$ jacksum -a groestl256 --info -V details
```
> "no; **Groestl was the most deeply analysed of the five SHA-3 finalists**: the best collision
> attacks on the hash function reach 3 of 10 rounds and the best distinguisher covers 9 of 10
> rounds of the permutation (2^368, 2012), the full Groestl is unaffected"

Drei Algorithmen, alle `broken: no`, drei sehr unterschiedliche Vertrauensniveaus. Und Jacksum
sagt Ihnen auch, wenn eine *standardisierte* Funktion trotzdem zu kurz ist:

```
$ jacksum -a has160 --info -V details
```
> "no, but the best collision attacks reach 53 of 80 steps (2^55) … the full HAS-160 is
> unaffected, but **its 160-bit output limits the collision resistance to 2^80, which is not
> sufficient anymore; prefer at least 256 bits**"

**Die Regel.** Für Daten, denen Sie in zehn Jahren noch vertrauen müssen, wählen Sie aus Stufe 1
oder 2 mit mindestens 256 Bit Ausgabe. Die Stufen 3 bis 5 sind interessant, manchmal
hervorragend und gelegentlich schneller — aber "keine bekannten Angriffe" auf ein unbekanntes
Design kann einfach bedeuten, dass niemand hingesehen hat.


<a name="concatenation"></a>

# Algorithmen verketten

Jacksum erlaubt es, beliebig viele Algorithmen mit `+` zu verketten:

```
$ jacksum -a sha256+sha3-256 -q txt:"Hello World"
$ jacksum -a sha256+crc32c --info
```

Das lohnt sich richtig zu verstehen, denn das naheliegende Argument dafür ist falsch und das
tatsächliche Argument ist stärker.

## Was Jacksum tut

Jede Datei wird **nur einmal** gelesen, unabhängig davon, wie viele Algorithmen Sie wählen, und
die Algorithmen können in getrennten Threads laufen (`--threads-hashing`). Die Grenzkosten eines
zweiten Algorithmus sind also CPU und nicht I/O — und das ist für tausende Dateien auf einer
Platte nahezu kostenlos, weil I/O der Engpass ist. `--info` bestätigt, was Sie gebaut haben:

```
$ jacksum -a sha256+crc32c --info
```

```
  Algorithm:
    name:                                 sha256+crc32c
    actual combined algorithms:           2

  Hash length:
    bits:                                 288
```

Standardmäßig werden die Werte als eine zusammenhängende Zeichenkette ausgegeben. Für eine
Prüfliste möchten Sie sie wahrscheinlich getrennt haben, und das leistet `-F` mit indizierten
Tokens:

```
$ jacksum -a sha256+sha3-256 -F "#ALGONAME{i}: #HASH{i}" myfile
sha256: 222d5dc399137f3d9a9b74681e273430e3af626d4b9630966cd87e95d58af3c6
sha3-256: fafb96e583781353478913250870b1ee6029c39db298e6ae2e1fe4970eec2031
```

```
$ jacksum -a sha256+sha3-256 -F "#HASH{0} #HASH{1} #FILESIZE #FILENAME" -r max mydir
```

`#HASH{<algo>}` funktioniert ebenfalls, `-F "#HASH{sha-256} #HASH{sha3-256} #FILENAME"` ist also
gleichwertig und selbsterklärend. Algorithmen kombinieren geht seit Jacksum 1.7.0 (Juli 2006).

## Die ehrliche Theorie: Verketten addiert keine Sicherheitsniveaus

Die verlockende Überlegung lautet "256 Bit plus 256 Bit ergeben 512 Bit Sicherheit". Das ist
falsch.

Joux hat 2004 gezeigt (*Multicollisions in Iterated Hash Functions*, CRYPTO 2004), dass für zwei
iterierte Merkle-Damgård-Hashfunktionen mit `n1` und `n2` Bit eine Kollision für die Verkettung
`H1(m) ‖ H2(m)` etwa

```
(n2/2) · 2^(n1/2)  +  2^(n2/2)
```

kostet und nicht `2^((n1+n2)/2)`. Das ist kaum mehr, als die *stärkere* der beiden allein
anzugreifen. Das Verfahren baut billig eine Multikollision — viele Nachrichten mit demselben
`H1`-Wert — indem es die iterierte Struktur ausnutzt, und sucht dann innerhalb dieser Menge eine
`H2`-Kollision. `sha256+sha512` liefert also deutlich weniger, als seine 768 Bit Ausgabe
suggerieren.

Zwei Konsequenzen, nach denen man handeln sollte:

- Verketten Sie nicht, um eine größere Zahl zu erreichen. Wenn Sie mehr Reserve als SHA-256
  wollen, nehmen Sie `sha-512` oder `sha-512/256`; das ist günstiger und ehrlicher als Stapeln.
- Weil das Multikollisions-Verfahren von der *iterierten* Struktur lebt, ist die Paarung
  **strukturell verschiedener** Designs eine echt bessere Absicherung als zwei Funktionen
  derselben Bauform. `sha256+sha3-256` kombiniert eine Merkle-Damgård-Funktion mit einem Schwamm;
  `sha256+sha512` kombiniert zwei nahe Verwandte.

## Warum es sich dennoch lohnt

Das eigentliche Argument ist technischer und nicht informationstheoretischer Natur: **Verketten
ist eine Versicherung dagegen, dass der von Ihnen gewählte Algorithmus später gebrochen wird.**

Sehen Sie sich an, wie Brüche tatsächlich abgelaufen sind. MD5 und SHA-1 fielen beide durch
*Chosen-Prefix-Kollisionsangriffe* — hochspezialisierte Konstruktionen um die
Differenzialstruktur einer Funktion herum, das Ergebnis jahrelanger gezielter Kryptanalyse genau
dieses Designs. Ein solcher Angriff erzeugt ein kollidierendes Paar für *diese* Funktion. Über
eine unabhängige zweite Funktion sagt er nichts und kann sie nicht gleichzeitig erfüllen. Niemand
hat je eine gleichzeitige Kollision für zwei unabhängige, strukturell verschiedene Hashfunktionen
vorgeführt.

Für das Szenario dieser Anleitung ist das sehr konkret. Nehmen Sie an, Sie versehen heute eine
Platte allein mit `sha-256`-Fingerprints. In zehn Jahren ist SHA-256 abgekündigt. Nun möchten Sie
das Vertrauen in das Archiv wiederherstellen — aber neu hashen erfordert, die Originaldaten zu
lesen, und bis dahin ist die Platte womöglich verschwunden oder bereits manipuliert. Ihre
Prüfliste ist genau in dem Moment unbrauchbar geworden, in dem Sie sie brauchten.

Wenn die Prüfliste zusätzlich `sha3-256` festgehalten hat, haben Sie weiterhin einen
vertrauenswürdigen Fingerprint, **ohne irgendetwas neu zu lesen**. Das ist einen CPU-Thread wert.

**Empfohlene Paarungen**

| Paarung | Begründung |
|---|---|
| `sha256+sha3-256` | Merkle-Damgård + Schwamm, verschiedene Entwicklerteams, beide Stufe 1. Die Standardwahl für Archive. |
| `sha256+blake3` | Wenn die Geschwindigkeit späterer Prüfungen wichtiger ist als die Verfügbarkeit von Werkzeugen. |
| `sha-512/256+sha3-512` | Maximale Reserve, beide immun gegen Length-Extension. |

**Nicht als Sicherheitsmaßnahme empfohlen:** eine kryptografische Hashfunktion plus ein CRC. Es
ist eine ansprechende Idee, und sie funktioniert nicht. Weil ein CRC affin ist, kann ein
Angreifer, der eine Hash-Kollision konstruiert hat, den CRC zusätzlich auf jeden gewünschten
Wert zwingen — es ist eine lineare Gleichung und keine Suche — der CRC trägt also überhaupt
keine Sicherheit gegen Angreifer bei. Ein CRC neben einem Hash bleibt als billiger Vorfilter für
zufällige Verfälschung nützlich, was eine echte, aber viel kleinere Aussage ist.


<a name="speed"></a>

# Geschwindigkeit, und warum sie meist das falsche Kriterium ist

`--info` platziert jeden Algorithmus in einer Geschwindigkeitsrangliste:

```
$ jacksum -a sha-256 --info
```

```
  Speed:
    relative rank:                        15/586
```

Rang 1 ist der schnellste. Die Rangliste stammt aus einer Gewichtstabelle im Quellcode
(`net.jacksum.multicore.manyalgos.HashAlgorithm`), wobei ein kleineres Gewicht schneller bedeutet:

| Algorithmus | Gewicht | Anmerkung |
|---|---|---|
| `crc32c` | 4 | der schnellste in Jacksum |
| `crc32` | 5 | |
| `adler32` | 6 | |
| `sum32` | 10 | |
| `sha-1` | 11 | |
| **`sha-256`** | **11** | **Rang 15/586** |
| `sha-512`, `sha-512/256` | 19 | |
| `sha3-256` | 32 | Rang 38/586 |
| **`md5`** | **35** | **Rang 42/586** |
| `blake2b-256` | 43 | |
| `blake3` | 95 | Rang 468/586 |

Zwei Schlussfolgerungen, die beide verbreiteten Ratschlägen widersprechen.

**SHA-256 ist schneller als MD5.** Gewicht 11 gegen 35; Rang 15 gegen 42. Moderne CPUs haben
SHA-2-Befehle und das JDK nutzt sie, während MD5 keine solche Unterstützung erhält. "MD5 nehmen,
weil es schneller ist" war 1998 richtig und ist seit Jahren falsch. Es bleibt kein
Geschwindigkeitsargument dafür, eine gebrochene Funktion zu wählen.

**Ein Rang ist eine Eigenschaft dieser Implementierung, nicht des Algorithmus.** BLAKE3 ist eine
der schnellsten Hashfunktionen überhaupt — in optimiertem C mit SIMD. In Jacksums
Java-Implementierung belegt es Rang 468 von 586. Der Algorithmus ist hervorragend; diese konkrete
Implementierung ist nicht schnell. Übertragen Sie Benchmark-Zahlen niemals zwischen
Implementierungen.

**Und meist ist all das ohne Bedeutung.** Bei tausenden Dateien auf einer Platte ist der Engpass
I/O und nicht Arithmetik. SHA-256 läuft mit Gigabyte pro Sekunde; eine drehende Platte liefert
das nicht. Was tatsächlich etwas bewegt:

```
$ jacksum -a sha-256 --threads-reading max -r max mydir     # parallele Lesevorgänge, gut für SSD/NVMe
$ jacksum -a sha256+sha3-256 --threads-hashing 2 -r max mydir  # beide Hashes, ein Lesedurchgang
```

Weil `-a a+b` jede Datei einmal liest, kostet ein zweiter Algorithmus nur CPU — siehe
[Verketten](#concatenation).

**Messen Sie auf Ihrer eigenen Hardware**, statt irgendeiner Tabelle zu glauben, auch dieser
nicht:

```
$ for a in crc32c sha-256 sha3-256 blake3 whirlpool; do
>   printf '%-12s' "$a"; /usr/bin/time -p jacksum -a $a bigfile.bin 2>&1 | awk '/real/{print $2" s"}'
> done
```

Verwenden Sie eine Datei, die groß genug ist, dass der JVM-Start (etwa 50 ms) im Rauschen
verschwindet — mindestens einige hundert MB.


<a name="implementations"></a>

# Zwei Implementierungen hinter einer Algorithmus-ID

Für 28 Algorithmen kann Jacksum denselben Hashwert auf zwei verschiedenen Wegen berechnen. Das ist
für die Performance wissenswert, und für das Vertrauen noch wertvoller.

## Wie es funktioniert

Standardmäßig verwendet Jacksum die Implementierung, die das **Java-API** bereitstellt —
`java.security.MessageDigest`, `java.util.zip.CRC32` und so weiter — weil JVM-Hersteller diese
stark optimieren und häufig auf CPU-Intrinsics oder nativen Code abbilden. Die Option `-A`
schaltet auf Jacksums eigene **reine Java-Implementierung** um, sofern es eine gibt. Laut
`jacksum -h` haben diese Algorithmen eine Alternative:

```
adler32, blake3, crc16, crc32, crc32_fddi, crc32c, fnv-0_32, fnv-0_64, fnv-1_32,
fnv-1_64, fnv-1a_32, fnv-1a_64, fugue224, fugue256, fugue384, fugue512, md2, md5,
sha-1, sha-256, sha-384, sha-512, sha-512/224, sha-512/256, sha3-224, sha3-256,
sha3-384, sha3-512
```

Für jeden anderen Algorithmus wird `-A` ignoriert. `--info` zeigt, welche aktiv ist:

```
$ jacksum -A -a sha-256 --info
```

```
  Speed:
    relative rank:                        unknown, speed is calculated for primary algorithms only

  Alternative/secondary implementation:
    has been requested:                   true
    is available and would be used:       true
```

Welche Implementierung Sie standardmäßig bekommen, ist ein Detail Ihres JRE-Herstellers und
dessen Version und kann sich zwischen Releases ändern. Der praktische Rat ist unverändert
gegenüber dem Tag, an dem diese Option eingeführt wurde: **verwenden Sie ein aktuelles JRE und
nehmen Sie die Performance, die es bietet.** Eine Messung auf einer Maschine (JDK 25, aarch64,
47 MB Datei):

| Implementierung | Laufzeit |
|---|---|
| Standard (JDK) | 0,10 s |
| `-A` (reines Java) | 0,19 s |

Beide Werte enthalten etwa 50 ms JVM-Start, das Hashen selbst unterscheidet sich also stärker, als
das Verhältnis vermuten lässt. Betrachten Sie das als einen Datenpunkt, nicht als Gesetz — auf
einer anderen JVM oder Architektur ist der Abstand anders, und gelegentlich gewinnt der reine
Java-Pfad.

## Der wichtigere Grund: Vertrauen in die Implementierung

Ein Hashwert ist nur so vertrauenswürdig wie der Code, der ihn erzeugt hat. Sie sind gerade dabei,
eine ganze Platte mit Fingerprints zu versehen und sich jahrelang auf diese Werte zu verlassen;
ein Implementierungsfehler würde viel zu spät entdeckt. Zwei *unabhängige* Implementierungen, die
sich bei einem bekannten Testvektor einig sind, sind ein echtes Indiz — und `-A` macht daraus
einen Einzeiler:

```
$ jacksum -a sha-256 -q txt:"abc"
ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad

$ jacksum -A -a sha-256 -q txt:"abc"
ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad
```

Dieser Wert ist gleichzeitig der von NIST veröffentlichte Testvektor für SHA-256 von `"abc"`.
Nehmen Sie eine dritte, völlig unabhängige Implementierung aus der
[Kompatibilitätsliste](#os_constraint) hinzu:

```
$ printf 'abc' | sha256sum
ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad  -
```

Drei unabhängige Codepfade, ein Wert, übereinstimmend mit dem Testvektor des Standards selbst.
Führen Sie das einmal aus, bevor Sie ein Archiv einem Algorithmus anvertrauen, und die Korrektheit
der Implementierung ist keine Sorge mehr.


<a name="os_constraint"></a>

# Die Betriebssystem-Einschränkung

Jacksum ist reines Java, alle 586 Algorithmen stehen Ihnen also auf jeder Plattform zur Verfügung,
auf der Java läuft. Dadurch vergisst man leicht die Einschränkung, die tatsächlich weh tut:

> Die Frage ist nicht *"kann ich diesen Hash berechnen?"*, sondern **"kann derjenige, der ihn
> später prüft, ihn mit den Werkzeugen berechnen, die er dann hat?"**

Eine Prüfliste ist eine Nachricht an die Zukunft. Wenn die lesende Person nur `sha256sum` hat oder
nur `certutil` von Windows, dann ist eine BLAKE3-Liste ein toter Brief, so gut BLAKE3 auch ist.

Genau dafür ist der Block `compatibility:` von `jacksum -h <algo>` da. Er nennt pro Algorithmus
den Aufruf für Dutzende von Betriebssystemen, Werkzeugen und Programmiersprachen:

```
$ jacksum -h sha-256
```

```
            compatibility:
                - 7z:              7z h -scrcsha256
                - BusyBox:         /bin/sha256sum
                - FreeBSD 6+:      /sbin/sha256
                - GNU/Linux:       /usr/bin/sha256sum
                - gpg:             gpg --print-md sha256
                - macOS 10.12+:    /usr/bin/shasum -a 256
                - OpenSSL:         openssl dgst -sha256
                - PowerShell:      Get-FileHash -Algorithm SHA256
                - Python 2.5+:     hashlib.sha256()
                - Solaris 10+:     /usr/bin/digest -a sha256
                - Windows 7+:      certutil -hashfile <file> SHA256
```

Verdichtet für die Kandidaten, die Sie wahrscheinlich erwägen (● natives Werkzeug vorhanden,
○ keines):

| | GNU/Linux | macOS | Windows | FreeBSD | Solaris | BusyBox | OpenSSL | 7-Zip | gpg | Python | Java | Go |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `sha-256` | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● |
| `sha-512` | ● | ● | ● | ● | ● | ● | ● | ○ | ● | ● | ● | ● |
| `sha-512/256` | ● | ● | ○ | ● | ● | ○ | ● | ○ | ○ | ○ | ● | ● |
| `sha3-256` | ○ | ○ | ○ | ○ | ● | ● | ● | ● | ○ | ● | ● | ● |
| `blake2b-256` | ● | ○ | ○ | ○ | ○ | ○ | ● | ○ | ○ | ● | ○ | ● |
| `blake3` | ○ | ○ | ○ | ○ | ○ | ○ | ○ | ○ | ○ | ○ | ○ | ○ |
| `md5` | ● | ● | ● | ● | ● | ● | ● | ● | ○ | ● | ● | ○ |
| `crc32` | ○ | ● | ○ | ● | ○ | ○ | ○ | ● | ○ | ● | ● | ● |

Lesen Sie die Zeilen genau, denn die Ergebnisse entsprechen nicht dem Ruf der Algorithmen:

- **`sha-256` ist der einzige Algorithmus mit wirklich universeller Unterstützung.** Jedes
  verbreitete Betriebssystem bringt ein Werkzeug dafür mit, ebenso 7-Zip, gpg, OpenSSL und jede
  Sprache, in der Sie skripten könnten. Wenn Interoperabilität eine Anforderung ist, beendet
  diese Zeile die Diskussion.
- **`sha3-256` hat kein natives Werkzeug auf GNU/Linux, macOS, Windows oder FreeBSD.** Es ist ein
  NIST-Standard von 2015, es ist gegen Length-Extension stärker als SHA-256, und Sie können es
  dennoch nicht mit den auf den drei verbreitetsten Desktopsystemen vorinstallierten Werkzeugen
  prüfen. Solaris 11.4+, BusyBox, OpenSSL 1.1.1+, Python 3.6+, Java 9+ und 7zFM 24.09+ können es
  — was reichlich ist, *sofern Sie die Prüfumgebung kontrollieren*.
- **`sha-512/256` hat unter Windows nichts.** `shasum -a 512256` deckt Linux und macOS ab,
  FreeBSD hat `/sbin/sha512t256`, Solaris 11.4+ hat `digest -a 512_t -t 256`. Windows hat weder
  `certutil`- noch PowerShell-Unterstützung.
- **`blake3` hat überhaupt keinen Eintrag.** In der Praxis ist es Jacksum-only. Eine
  hervorragende Funktion und die schlechtestmögliche Wahl für eine Liste, die jemand anders prüfen
  muss.
- **`md5` und `crc32` sind am breitesten verfügbar**, und genau deshalb werden sie immer wieder
  für Aufgaben gewählt, die sie nicht erfüllen können. Verfügbarkeit ist keine Eignung.

**Die daraus folgenden Regeln**

1. Wenn Jacksum auf beiden Seiten läuft, entfällt die Einschränkung — wählen Sie allein nach
   kryptografischer Güte.
2. Andernfalls wählen Sie den stärksten Algorithmus, den die *Prüfumgebung* unterstützt, und
   prüfen Sie das mit `-h <algo>`, statt es anzunehmen.
3. Wählen Sie außerdem ein **Format**, das das Zielwerkzeug lesen kann. Der richtige Algorithmus
   im falschen Layout ist genauso unbrauchbar, wählen Sie also ein `--style`: `gnu-linux`, `bsd`,
   `bsd-r`, `solaris-digest`, `fciv`, `openssl-dgst`, `sfv` und weitere. `jacksum -h`
   dokumentiert alle 18.
4. Wenn Sie mehrere Umgebungen bedienen müssen, erzeugen Sie mehrere Listen aus einem einzigen
   Lesedurchgang oder halten Sie mehr als einen Algorithmus fest — siehe
   [Verketten](#concatenation).


<a name="blacklist"></a>

# Algorithmen, die man nicht mehr verwenden sollte

Jacksum bringt 586 Algorithmen mit, weil es ein *umfassendes* Framework ist: es muss eine 1998
erzeugte Prüfliste lesen, den CRC eines Geräts nachbilden und historische Funktionen zu
Lehrzwecken vorführen können. Verfügbarkeit in Jacksum ist ausdrücklich keine Empfehlung.

Für das Fingerprinting einer großen Dateisammlung sollte Folgendes nicht auf Ihrer Liste stehen.
Die Gruppierung erfolgt nach dem *Warum*, denn die Gründe sind völlig verschieden und die
Gegenmaßnahme unterscheidet sich ebenfalls.

<a name="tier_a"></a>

## Stufe A: Zu kurz

Die Ausgabebreite begrenzt die Kollisionsresistenz, ganz gleich wie gut das Design ist. Jacksum
hat 9 Algorithmen mit 8 Bit Ausgabe, 14 mit 16 Bit und 8 mit 24 Bit:

```
$ jacksum -a all:8 -l
blake2b-8 blake2s-8 crc8 md6-8 skein-256-8 skein-512-8 skein-1024-8 sum8 xor8
```

Unter der Annahme einer idealen, gleichverteilten Funktion genügen etwa `1,1774 · √(2^b)`
Elemente für eine 50-prozentige Chance, dass *irgendein* Paar kollidiert:

| Breite | 50 % Chance auf eine Kollision | Kollision garantiert (Schubfachprinzip) | Beispiele |
|---|---|---|---|
| 8 Bit | ~19 Dateien | 257 Dateien | `sum8`, `xor8`, `crc8`, `blake2b-8` |
| 16 Bit | ~301 Dateien | 65 537 Dateien | `sum16`, `sum_bsd`, `sum_sysv`, `crc16`, `fcs16`, `fletcher16` |
| 24 Bit | ~4 800 Dateien | 16 777 217 Dateien | `sum24`, `crc24` |
| 32 Bit | ~77 000 Dateien | 2^32 + 1 Dateien | `crc32`, `adler32`, `xxh32`, `elf` |
| 64 Bit | ~5,1 · 10^9 Dateien | — | `crc64_xz`, `sum64` |
| 128 Bit | ~2,2 · 10^19 Dateien | — | `md5`, `ripemd128`, `tiger128` |
| 256 Bit | ~4 · 10^38 Dateien | — | `sha-256`, `sha3-256`, `blake3` |

Für "tausende Dateien auf einer Platte" sind die Zeilen mit 8, 16 und 24 Bit einfach
disqualifiziert: bereits ein paar hundert Dateien kollidieren. Die 32-Bit-Zeile übersteht die
*Prüfung pro Datei*, versagt aber bei der *Identifikation über Dateien hinweg* — bei 77 000
Dateien stehen die Chancen 50:50, dass ein gemeldetes Duplikat keines ist. Siehe die
[Unterscheidung](#cryptographic).

**Gutes Design kann fehlende Breite nicht zurückkaufen.** Das ist der lehrreichste Fall dieser
ganzen Anleitung:

```
$ jacksum -a blake2b-8 --info -V details
```

BLAKE2b, auf 8 Bit gekürzt, erreicht einen lehrbuchmäßigen mittleren Lawineneffekt von **51,91 %**
— besser als die 50,04 % von SHA-256. Es leitet sich von einer hervorragenden, gut begutachteten
Funktion ab. Und es ist für eine Platte voller Dateien völlig unbrauchbar, denn 8 Bit sind 8 Bit:
256 mögliche Werte, also müssen 257 Dateien kollidieren. Ein perfekter Lawineneffekt in einem
winzigen Ausgaberaum bleibt ein winziger Ausgaberaum.

Dieselbe Rechnung setzt mehrere Funktionen außer Dienst, die **nicht** gebrochen sind:

| Algorithmus | `broken:` | Warum trotzdem meiden |
|---|---|---|
| `has160` | `no` | 160 Bit → 2^80 Kollisionsresistenz. Jacksum: "not sufficient anymore; prefer at least 256 bits" |
| `ripemd128` | `no` | 128 Bit → 2^64. Außerdem Landelle/Peyrin 2013: Kollision für die vollständige Kompressionsfunktion und ein Distinguisher für die vollständige Hashfunktion |
| `tiger128`, `tiger160` | `no` | Kürzungen von Tiger-192 → 2^64. Jacksum: "the truncation itself is not a weakness" — die Breite ist es |
| `md6-16`, `skein-512-32`, … | `no` | gute Funktionen, auf eine unbrauchbare Breite konfiguriert |

**Kürzen Sie nicht, um Platz zu sparen.** Ein 32-Byte-Hash für eine Million Dateien sind 32 MB. Es
gibt kein Speicherplatzargument, das die Aufgabe von Kollisionsresistenz rechtfertigt.

<a name="tier_b"></a>

## Stufe B: Konstruktiv defekt

Diese versagen sogar im Fall der *zufälligen Verfälschung*, also bei der Aufgabe, für die sie
nominell gebaut wurden. Jedes Versagen lässt sich in einer Zeile zeigen.

**`sum8` … `sum64` — die Byte-Reihenfolge wird ignoriert, und Nullbytes verschwinden.**

Jacksums eigene Dokumentation sagt es: der Algorithmus "does not consider the order of the bytes in
the data stream. And since the algorithm only adds up the values of the bytes, all zero bytes are
ignored." Sehen Sie:

```
$ jacksum -a sum32 -q txt:"abc"
294 3
$ jacksum -a sum32 -q txt:"cba"
294 3
$ jacksum -a sum32 -q txt:"bca"
294 3
```

Jede Permutation der Bytes einer Datei erzeugt denselben Wert. Datensätze in einer Datei
umsortieren, zwei Felder tauschen, einen Block umdrehen — alles unsichtbar. Und das Anhängen von
Nullbytes ändert ebenfalls nichts:

```
$ jacksum -a sum32 -q hex:616263
294 3
$ jacksum -a sum32 -q hex:61626300000000
294 7
```

Die Prüfsumme bleibt `294`; nur das Größenfeld wandert von 3 auf 7.

**`xor8` — doppelte Bytes heben sich auf.**

```
$ jacksum -a xor8 -q txt:"abc"
96 3
$ jacksum -a xor8 -q txt:"abcxx"
96 5
```

Da `x XOR x = 0` gilt, trägt jedes Byte, das eine gerade Anzahl von Male vorkommt, nichts bei.
Zusammen mit dem konstanten Lawineneffekt von 12,50 % erkennt `xor8` fast nichts.

**`elf` — ein Symboltabellen-Hash, keine Prüfsumme.** Mittlerer Lawineneffekt 5,56 %, Minimum
3,13 %. Er wurde entworfen, um Namen in der Hashtabelle des ELF-Objektformats zu verteilen, und
darin ist er gut.

**`sum_bsd`, `sum_sysv`, `sum_minix`, `cksum` (Minix)** — 16-Bit-Varianten von `sum(1)`, die nur
existieren, damit Jacksum reproduzieren kann, was historische Unix-Werkzeuge ausgaben.

**`prng`, `strsum`, `joaat`, `fnv-*`** — nicht-kryptografische Hashfunktionen für Hashtabellen.
Schnell und für diesen Zweck gut verteilt; `prng` und `strsum` zeigen Lawinen-Minima von 3,13 %.

Im Gegensatz dazu verhalten sich die Algorithmen, die *sehr wohl* auf die Reihenfolge achten, wie
erhofft:

```
$ jacksum -a adler32 -q txt:"abc"   →  38600999 3    $ jacksum -a adler32 -q txt:"cba"   →  38863143 3
$ jacksum -a crc32   -q txt:"abc"   →  891568578 3   $ jacksum -a crc32   -q txt:"cba"   →  3635344512 3
$ jacksum -a sha-256 -q txt:"abc"   →  ba7816bf…     $ jacksum -a sha-256 -q txt:"cba"   →  6d970874…
```

**Ein abmilderndes Detail, das man kennen sollte.** Jacksums Standardausgabe und der Stil `full`
halten die **Dateigröße** neben dem Wert fest, was den Nullbyte-Trick von oben auffängt (`294 3`
gegenüber `294 7`). Ein Größenfeld ist eine nützliche Zusatzsicherung und kein Ersatz für einen
echten Hash — gegen das Umsortieren von Bytes hilft es nicht, und ein Angreifer kontrolliert es so
leicht wie den Inhalt.

**Meiden Sie das SFV-Format** für alles, was zählt. Jacksum kann es schreiben, was nicht dasselbe
ist wie eine Empfehlung. Seine Definition
(`src/main/resources/net/jacksum/compats/defs/sfv.properties`) lautet:

```
algorithm.default=crc32
formatter.format=#FILENAME #CHECKSUM{hex-uppercase}
```

Nur CRC-32, und überhaupt keine Dateigröße — es verliert also sogar die schwache Sicherung von
oben.

<a name="tier_c"></a>

## Stufe C: Gebrochene kryptografische Hashfunktionen

Verwenden Sie diese nur, um eine alte Prüfliste zu *lesen*, nie um eine neue zu erzeugen. Jacksum
markiert 15 Algorithmen als `yes` oder `partly` gebrochen:

| Algorithmus | Zustand | Warum |
|---|---|---|
| `md5` | `yes` | Kollisionen in Sekunden (2004); X.509-Demonstration 2008; Flame 2012 |
| `sha-1` | `yes` | SHAttered 2017 (2^63,1); Chosen-Prefix Shambles 2020 (2^63,4, ≈45 000 USD) |
| `md4` | `yes` | Kollisionen in einem Bruchteil einer Sekunde |
| `md2` | `partly` | Urbildresistenz der vollständigen Funktion gebrochen (2005) |
| `sha0` | `yes` | Kollisionsresistenz der vollständigen Funktion gebrochen (2004) |
| `ed2k` | `yes` | auf MD4 aufgebaut |
| `haval` | `yes` | Kollisionen für die vollständige Funktion bei jeder Rundenzahl bekannt |
| `gost` | `yes` | Kollisions- *und* Urbildresistenz gebrochen (2008) |
| `mdc2` | `yes` | die hier implementierte blockchiffrenbasierte Variante |
| `panama` | `yes` | der Hash-Modus ist praktisch gebrochen (2007) |
| `fork256` | `yes` | Kollisionsresistenz der vollständigen Funktion gebrochen (2007) |
| `streebog512` | `partly` | Zweite-Urbildresistenz der vollständigen Funktion (2014) |
| `hamsi<n>` | `partly` | Zweite-Urbildresistenz der vollständigen Funktion (2010) |
| `edonr<n>` | `partly` | Urbildresistenz theoretisch gebrochen (2009); Secret-Prefix-MAC praktisch gebrochen |
| `vsh` | `partly` | Urbildresistenz gebrochen (2006) |

`md5` und `sha-1` sind die beiden, die in der Praxis zählen, weil sie noch überall installiert
sind und noch von viel zu vielen Werkzeugen standardmäßig angeboten werden. Um es zu wiederholen:
**"weit verbreitet" ist kein Argument**, und wie der [Abschnitt zur Geschwindigkeit](#speed)
zeigt, ist MD5 nicht einmal mehr schnell.

<a name="variants"></a>

## Gleicher Name, andere Variante

Eine Falle, in die auch umsichtige Menschen tappen. "CRC-64" ist nicht ein Algorithmus, und die
Varianten sind nicht gleich gut:

| ID | Lawineneffekt avg (9-Byte-Eingabe) | Lawineneffekt avg (64-Byte-Eingabe) |
|---|---|---|
| `crc64` | 6,47 % | 13,84 % |
| `crc64_go-iso` | 6,47 % | — |
| `crc64_ecma` | 50,13 % | — |
| `crc64_xz` | 50,13 % | — |

`crc64` und `crc64_go-iso` verwenden das dünn besetzte Polynom x^64 + x^4 + x^3 + x + 1 (jenes,
das die Proteindatenbank SWISS-PROT bis 2009 verwendete — und das, wie
[ALGORITHMS_de.md](ALGORITHMS_de.md#footnotes) anmerkt, häufig fälschlich ISO 3309 zugeschrieben
wird). Mit nur fünf Termen diffundiert es sehr langsam. `crc64_ecma` und `crc64_xz` basieren auf
ECMA-182 und verhalten sich, wie sich ein 64-Bit-CRC verhalten sollte.

Der Name sagt also fast nichts, die Parameter dagegen alles. Sehen Sie immer nach:

```
$ jacksum -a crc64 --info -V details      # Polynom, init, Spiegelung, xorOut, Lawineneffekt
$ jacksum -a crc64_xz --info -V details
```

**Die abschließende Regel für diesen ganzen Abschnitt.** Um tausende Dateien für eine spätere
Prüfung mit Fingerprints zu versehen, wählen Sie eine **kryptografische Hashfunktion mit
mindestens 256 Bit, markiert als `broken: no`, aus [Prüfungsstufe 1 oder 2](#scrutiny)**. Alles in
Stufe A und Stufe B existiert für Formatkompatibilität, Alt-Interoperabilität,
Protokollkonformität und Lehre — und das ist der einzige Grund, warum Jacksum es anbietet.


<a name="hmac"></a>

# Warum ein gebrochener Algorithmus dennoch verwendbar ist: HMAC

Hier ein Ergebnis, das überrascht: **HMAC-MD5 hat keinen praktikablen Angriff, obwohl MD5
vollständig gebrochen ist.**

Das ist keine Lücke, sondern eine Folge dessen, was HMAC voraussetzt. Reines Hashen beruht auf
Kollisionsresistenz — der Eigenschaft, die MD5 2004 verlor. HMAC nicht. Sein Sicherheitsbeweis
ruht darauf, dass sich die Kompressionsfunktion unter einem geheimen Schlüssel wie eine
Pseudozufallsfunktion verhält, und ein Kollisionsangriff sagt darüber nichts. Obendrein kennt der
Angreifer den Schlüssel nicht, kann eine Kollision also nicht einmal auf den richtigen internen
Zustand ausrichten.

Jacksum sagt das selbst:

```
$ jacksum -a hmac:md5 -k txt:secret --info --verbose details
```

```
  Security:
    broken:                               depends
      depends on the underlying hash function; note that a broken
      hash function does not necessarily yield a broken HMAC:
      HMAC-MD5 has no practical attack although MD5 itself is broken,
      because the HMAC construction does not rely on collision
      resistance; nevertheless HMAC should be instantiated with a
      hash function that is not broken

  HMAC parameters:
    underlying cryptographic hash:        md5
    truncate to bits:                     no truncation
    trunc. length should have min. bits:  80
    key length should have min. bytes:    16
    key length follows above recom.:      false
    key will be hashed:                   false
```

Beachten Sie die letzten vier Zeilen: Jacksum prüft Ihren Schlüssel und die Kürzung gegen die
Empfehlungen von RFC 2104 / FIPS 198-1 und sagt Ihnen, wenn sie zu kurz greifen.
`key length follows above recom.: false` bedeutet, dass der sechs Zeichen lange Schlüssel `secret`
unter den empfohlenen 16 Byte liegt.

**"Kann" ist nicht "soll".** Instanziieren Sie HMAC für alles Neue mit einer Funktion, die nicht
gebrochen ist. Dass `hmac:md5` heute sicher ist, ist eine Aussage über den *aktuellen* Stand der
Kryptanalyse von MD5s Kompressionsfunktion und kein Versprechen. Es gibt keinen Vorteil, das
auszureizen: `hmac:sha256` kostet dasselbe und steht auf viel festerem Grund.

Es gibt einen zweiten, davon unabhängigen Grund, warum eine gebrochene Hashfunktion manchmal
akzeptabel ist: wenn es **überhaupt keinen Angreifer** gibt. Deduplizierung im eigenen Speicher,
Cache-Schlüssel, "habe ich diese Datei schon gesehen" — bei diesen Aufgaben versucht niemand, eine
Kollision zu konstruieren, und MD5s 128 Bit Ausgabe und gute Verteilung erledigen die Arbeit. In
dem Moment, in dem der Wert eine Vertrauensgrenze überschreitet, löst sich diese Begründung auf.

## Was HMAC Ihnen für eine Prüfliste bringt

Das ist der für unser Szenario entscheidende Teil, und er hat gar nichts mit gebrochenen
Algorithmen zu tun.

Eine einfache Hashliste schützt die *Dateien*. Sie schützt nicht *sich selbst*. Ein Angreifer, der
Ihre Dateien verändern kann, kann meist auch `files.sha256` verändern — die Hashwerte der
manipulierten Dateien neu berechnen, in die Liste schreiben, und Ihre Prüfung läuft sauber durch.
Die Liste ist nur dann vertrauenswürdig, wenn ihre Integrität durch etwas außerhalb der Reichweite
des Angreifers garantiert wird.

Genau das leistet HMAC, mit einem Schlüssel, den der Angreifer nicht hat:

```
$ jacksum -a hmac:sha256 -k password --style full -o files.hmac -r max mydir
$ jacksum -a hmac:sha256 -k password --style full -c files.hmac
```

`-k` akzeptiert `txt:`, `hex:` und weitere Präfixe, oder wörtlich `readline` bzw. `password`, um
interaktiv zu fragen, statt den Schlüssel in Ihrer Shell-History zu hinterlassen. Ohne den
Schlüssel kann ein Angreifer keine Werte erzeugen, die die Prüfung bestehen, ganz gleich wie viel
des Dateisystems ihm gehört.

Alternativen, die dasselbe Ziel anders erreichen: die Liste auf einmalig beschreibbaren oder
schreibgeschützten Medien ablegen, sie auf einer getrennten vertrauenswürdigen Maschine halten
oder sie signieren (`gpg --detach-sign files.sha256`). Nehmen Sie, was zu Ihrem Aufbau passt —
aber nehmen Sie eines. Eine Hashliste auf demselben beschreibbaren Datenträger wie die Daten, die
sie schützt, ist ein [Anti-Muster](#antipatterns).

**Umfang.** 492 der 586 Algorithmen von Jacksum lassen sich mit HMAC verwenden:

```
$ jacksum --hmacs -V summary          # die Liste und die Anzahl
$ jacksum --hmacs -V info             # pro Algorithmus: Ausgabegröße, Blockgröße, empfohlene Mindestwerte
```

Die übrigen 94 sind ausgeschlossen, weil sie keine kryptografischen Hashfunktionen sind, oder weil
ihre Blockgröße ihre Ausgabegröße nicht übersteigt (was RFC 2104 verlangt — das schließt die
Schwamm- und XOF-Konstruktionen aus), oder weil sie keine einfachen iterierten Funktionen sind
(die Baummodi). Details in [ALGORITHMS_de.md](ALGORITHMS_de.md#hmac).


<a name="decision"></a>

# Das Entscheidungsverfahren

Fünf Fragen, in dieser Reihenfolge. Jede verkleinert das Feld, und keine davon fragt, welcher
Algorithmus am eindrucksvollsten klingt.

**1. Ist ein Angreifer im Bedrohungsmodell?**
Nicht "ist einer wahrscheinlich" — *ist einer möglich*. Könnte irgendjemand davon profitieren,
dass Ihre Prüfung bei veränderten Daten durchgeht? Wenn es um Software, Backups, Beweismittel,
juristische oder finanzielle Unterlagen oder überhaupt etwas geht, das Ihre Kontrolle verlässt,
lautet die Antwort ja. Lautet sie nein und bleibt sie nein, ist ein CRC wirklich ausreichend und
deutlich günstiger.

**2. Wer prüft, und womit?**
Wenn Jacksum auf beiden Seiten läuft, springen Sie zu Frage 3. Andernfalls sehen Sie mit
[`-h <algo>`](#os_constraint) für die Umgebung nach, die die Liste lesen muss, und streichen Sie
alles, was sie nicht berechnen kann. Diese Frage entscheidet mehr echte Fälle als
kryptografische Stärke.

**3. Lässt sich die Prüfliste selbst schützen?**
Schreibgeschützte Medien, eine getrennte Maschine oder eine Signatur — gut, dann genügt ein
einfacher Hash. Muss die Liste neben den Daten auf beschreibbarem Speicher liegen, brauchen Sie
[HMAC](#hmac) mit einem anderswo aufbewahrten Schlüssel.

**4. Wie lange muss das halten?**
Wochen: jede ungebrochene Funktion genügt. Jahre bis Jahrzehnte: beschränken Sie sich auf
[Prüfungsstufe 1 oder 2](#scrutiny) mit ≥ 256 Bit und erwägen Sie Frage 5.

**5. Werden die Daten den Algorithmus überleben?**
Wenn es in zehn Jahren unmöglich oder nicht vertrauenswürdig ist, die Quelldaten erneut zu lesen,
halten Sie jetzt zwei strukturell verschiedene Hashwerte fest — siehe
[Verketten](#concatenation). Ein CPU-Thread heute erkauft Ihnen eine Prüfliste, die die
Abkündigung eines ihrer Algorithmen übersteht.

```
                    Ist ein Angreifer möglich?
                       │
             ┌─────────┴─────────┐
           nein                  ja
             │                   │
    crc32c / crc64_nvme    Müssen andere Werkzeuge prüfen?
    (schnell, für               │
     Zufälle genug)   ┌─────────┴────────┐
                      ja               nein
                      │                 │
                  sha-256          sha3-256
                      │                 │
                      └────────┬────────┘
                               │
                    Liegt die Prüfliste selbst
                    außerhalb der Reichweite des Angreifers?
                               │
                      ┌────────┴────────┐
                      ja              nein
                      │                 │
                 so lassen       hmac:<algo> -k
                      │
                    Müssen die Daten den
                    Algorithmus überleben?
                      │
             ┌────────┴────────┐
           nein                ja
             │                 │
        so lassen      sha256+sha3-256
```

**Die resultierenden Empfehlungen**

| Situation | Wahl | Warum |
|---|---|---|
| Angreifer möglich, Interoperabilität nötig | `sha-256` | die einzige universell unterstützte ungebrochene Funktion |
| Angreifer möglich, Jacksum auf beiden Seiten | `sha3-256` | Schwamm, kein Length-Extension, enormer Sicherheitsabstand; seit 3.0.0 Jacksums Standard |
| Langzeitarchiv | `sha256+sha3-256` | übersteht die Abkündigung einer der Funktionen, ein Lesedurchgang |
| Mehr Reserve gewünscht | `sha-512/256` oder `sha-512` | 512-Bit-Innenleben; `/256` beseitigt zusätzlich Length-Extension |
| Liste liegt in Reichweite eines Angreifers | `hmac:sha256 -k …` | authentifiziert die Liste, nicht nur die Dateien |
| Nur Zufälle, oder Protokollkonformität | `crc32c`, `crc64_nvme` | am schnellsten, mit echten Fehlererkennungsgarantien |
| Eine Altliste lesen | was auch immer sie nutzte | Grenzen mit `--info -V details` prüfen und mit etwas Aktuellem neu hashen |


<a name="workflow"></a>

# Durchgerechnetes Beispiel: Tausende von Dateien

Der vollständige Ablauf für das Szenario, mit dem diese Anleitung begann. Jedes Kommando unten
wurde gegen Jacksum 4.0.0 ausgeführt.

**1. Den Kandidaten begründen.** Bevor Sie einem Algorithmus ein Archiv anvertrauen, sehen Sie ihn
sich an:

```
$ jacksum -a sha-256 --info --verbose details
```

Prüfen Sie vier Dinge: `broken:` sagt `no`, die Begründung dahinter beruhigt, der mittlere
Lawineneffekt liegt nahe 50 %, und die Breite beträgt mindestens 256 Bit.

**2. Prüfen, ob die Zukunft es lesen kann.**

```
$ jacksum -h sha-256
```

**3. Die Implementierung gegenprüfen** (siehe [oben](#implementations)):

```
$ jacksum -a sha-256 -q txt:"abc"
$ jacksum -A -a sha-256 -q txt:"abc"
$ printf 'abc' | sha256sum
```

Alle drei müssen `ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad` ausgeben.

**4. Die Prüfliste erzeugen.** Beachten Sie, dass die Optionen *vor* den Dateiparametern stehen
und dass `--style full` Hashwert, Zeitstempel, Größe und Name festhält:

```
$ jacksum -a sha-256 --style full -o files.sha256 -r max /data
```

Nützliche Ergänzungen:

| Option | Wirkung |
|---|---|
| `-r max` | vollständige Rekursion (der Standard, wenn `-r` weggelassen wird; `-r <n>` begrenzt die Tiefe) |
| `--header` | schreibt einen Kopf mit Version, Betriebssystem, JVM, Datum und Aufruf |
| `--path-relative-to <path>` | speichert relative Pfade, damit die Liste ein Verschieben übersteht |
| `-8` / `--utf8` | UTF-8-Ausgabe — tun Sie das, wenn Dateinamen nicht rein ASCII sind |
| `--charset-output-file <cs>` | expliziter Zeichensatz für die Liste |
| `-u errors.txt` | sammelt unlesbare Dateien, statt sie im Scrollpuffer zu verlieren |
| `-E base64` | eine kompaktere Kodierung als Hex |

Der Stil `full` ist die zusätzlichen Bytes wert: Größe und Zeitstempel festzuhalten kostet nichts
und gibt Ihnen etwas in der Hand, wenn eine Prüfung fehlschlägt.

**5. Die Archivvariante** — zwei Algorithmen, ein Lesedurchgang, lesbare Spalten:

```
$ jacksum -a sha256+sha3-256 -F "#HASH{0} #HASH{1} #FILESIZE #FILENAME" -r max /data
```

**6. Die Liste schützen.** Kopieren Sie sie auf schreibgeschützte oder offline gehaltene Medien,
signieren Sie sie (`gpg --detach-sign files.sha256`) oder erzeugen Sie sie mit [HMAC](#hmac) und
bewahren Sie den Schlüssel anderswo auf. Diesen Schritt zu überspringen macht den größten Teil der
Arbeit zunichte.

**7. Später prüfen.** Verwenden Sie *dieselben* `-a` und `--style` wie beim Erzeugen — das ist
wichtig:

```
$ jacksum -a sha-256 --style full -c files.sha256
```

Lassen Sie `--style full` weg, kann Jacksum nicht wissen, dass die Spalten für Zeitstempel und
Größe vorhanden sind, liest sie als Teil des Dateinamens und meldet alles als `MISSING`. Lassen
Sie `-a` weg, fällt es auf `sha3-256` zurück und alles ist `FAILED`. Beide Fehlbilder sehen
alarmierend aus und bedeuten nichts.

Der Bericht unterscheidet fünf Ergebnisse:

```
Jacksum: matches (OK): 2
Jacksum: mismatches (FAILED): 0
Jacksum: new files (NEW): 0
Jacksum: missing files (MISSING): 0
Jacksum: files with errors (ERROR): 0
Jacksum: strict check: PASSED
```

| Ergebnis | Bedeutung |
|---|---|
| `OK` | die Datei ist vorhanden und ihr Hashwert stimmt |
| `FAILED` | vorhanden, Hashwert weicht ab — die Datei hat sich geändert |
| `MISSING` | in der Liste, nicht auf der Platte |
| `NEW` | auf der Platte, nicht in der Liste |
| `ERROR` | konnte nicht gelesen werden |

**8. Die strenge Prüfung.** `--check-strict` macht jedes `FAILED`, `MISSING`, `NEW` oder `ERROR` zu
einem Gesamtfehlschlag, und genau das wollen Sie für "beweise, dass sich nichts geändert hat":

```
$ jacksum -a sha-256 --style full --check-strict -c files.sha256 /data
```

Beachten Sie zwei Dinge. Der strenge Modus braucht auch den Verzeichnisparameter, um neu
hinzugekommene Dateien zu erkennen. Und er verlangt `--list-filter all` (den Standard) — ein Filter
würde das Hashen unterdrücken, von dem die Erkennung abhängt, weshalb die Kombination mit
`--list-filter bad` von Jacksum abgelehnt wird.

**Exit-Codes** machen das skriptfähig:

| Code | Bedeutung |
|---|---|
| 0 | alles in Ordnung |
| 1 | mindestens eine Abweichung bei der Prüfung |
| 2 | Parameterfehler |
| 3 | Parse-Fehler in der Prüfdatei |
| 4 | I/O-Fehler |
| 5 | ein gesuchter Hashwert wurde nicht gefunden |
| 6 | die strenge Prüfung ist fehlgeschlagen |

Eine einfache Abweichung endet also mit 1, eine fehlgeschlagene strenge Prüfung mit 6 — in einem
Überwachungsskript lohnt es sich, das zu unterscheiden.

**9. Für sehr große Läufe** filtern Sie die Ausgabe auf das, was Aufmerksamkeit braucht (ohne
`--check-strict`):

```
$ jacksum -a sha-256 --style full --list-filter bad -c files.sha256
```

`bad` zeigt nur `FAILED`, `MISSING` und `ERROR`; `good` zeigt `OK` und `NEW`; `none` gibt nur die
Zusammenfassung aus.

**10. Durchsatz einstellen.**

```
$ jacksum -a sha-256 --threads-reading max --style full -o files.sha256 -r max /data
```

`--threads-reading max` hilft bei SSD und NVMe, wo parallele Lesevorgänge skalieren; bei einer
einzelnen drehenden Platte kann es schaden, weil das Positionieren der Köpfe der Kostenfaktor ist.
`--threads-hashing` steuert die Hash-Seite, die ein verketteter Algorithmus nutzt.

**11. Stichproben und Suchen**, ohne eine vollständige Liste:

```
$ jacksum -a sha-256 -q txt:"Hello World" -e a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e
$ jacksum -a sha-256 -e <hash> -r max /data          # Dateien zu einem Hashwert finden
$ jacksum -a sha-256 -w wanted.txt -r max /data      # Dateien zu einem der Hashwerte einer Liste finden
$ jacksum -a sha-256 --style gnu-linux --check-line "<eine Zeile aus einer Liste>"
```

**12. Einen unbekannten Hashwert identifizieren.** Wenn Sie die Daten und den Wert haben, aber
nicht den Algorithmus, kann Jacksum danach suchen (`-E` ist erforderlich):

```
$ jacksum -a unknown:256 -E hex -q txt:"abc" -e ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad
Trying 55 algorithms with a width of 256 bits that are supported by Jacksum 4.0.0 ...
sha-256
    --> SHA-256 (SHA-2 family)
```


<a name="antipatterns"></a>

# Anti-Muster

Jedes davon ist verbreitet, und jedes hat einen bestimmten Grund, falsch zu sein.

**Einen CRC als Sicherheitsmaßnahme verwenden.** Ein CRC ist affin über GF(2); ein Angreifer löst
ein kleines lineares Gleichungssystem, um jeden Wert zu erzwingen. Ihn zu verbreitern hilft nicht,
weil es keine Suche gibt, die man verlängern könnte. Siehe
[drei Familien](#three_families).

**Einen CRC ergänzen, "falls der Hash irgendwann gebrochen wird".** `sha256+crc32c` sieht wie eine
mehrschichtige Verteidigung aus und ist keine. Wer eine Kollision für den Hash konstruieren kann,
zwingt den CRC im selben Schritt auf jeden Wert, der zweite Wert kostet also CPU und bringt
überhaupt keine Sicherheit gegen einen Angreifer. Kombinieren Sie stattdessen zwei strukturell
verschiedene *kryptografische* Hashfunktionen — siehe [Verketten](#concatenation).

**MD5 wählen, "weil es schneller ist".** Ist es nicht. SHA-256 hat in Jacksums Rangliste Gewicht
11, MD5 hat 35 — moderne CPUs beschleunigen SHA-2 und nicht MD5. Der Tausch, den Sie zu machen
glauben, existiert nicht. Siehe [Geschwindigkeit](#speed).

**Einen Hashwert kürzen, um Plattenplatz zu sparen.** 32 Byte pro Datei sind 32 MB pro Million
Dateien. Das Kürzen kostet dauerhaft Kollisionsresistenz und bringt nichts ein. Siehe
[Stufe A](#tier_a).

**Die Prüfliste auf demselben beschreibbaren Datenträger ablegen wie die Daten.** Wer die Dateien
ändern kann, kann die Liste ändern. Verwenden Sie schreibgeschützte Medien, einen getrennten Host,
eine Signatur oder [HMAC](#hmac).

**Mit anderen Optionen prüfen als erzeugen.** `--style full` weglassen macht jeden Eintrag
`MISSING`; `-a` weglassen fällt still auf `sha3-256` zurück und macht alles `FAILED`. Halten Sie
den genauen Aufruf fest — `--header` tut das für Sie.

**Hashwerte über Werkzeuggrenzen hinweg vergleichen, ohne die Kodierung abzugleichen.** Derselbe
Wert kann als Kleinbuchstaben-Hex, Großbuchstaben-Hex, dezimal, Base32 oder Base64 ausgegeben
werden. Mehrere Jacksum-Algorithmen geben standardmäßig *dezimal* aus (alle CRCs und klassischen
Prüfsummen). Wenn zwei Werkzeuge sich widersprechen, prüfen Sie `-E` und den Stil, bevor Sie eine
Verfälschung vermuten.

**`partly` gebrochen für sicher halten.** `partly` bedeutet, dass mindestens eine zugesicherte
Eigenschaft gefallen ist. Das ist keine bestandene Prüfung, sondern ein Grund zu wechseln. Siehe
[was "gebrochen" bedeutet](#broken_means).

**Einen exotischen Algorithmus wählen, weil "es keine bekannten Angriffe gibt".** Für ein
unerforschtes Design trägt diese Aussage fast keine Information. Lesen Sie den `broken:`-Text —
Jacksum sagt Ihnen, wenn die Kryptanalyse dünn ist. Siehe [Prüfung](#scrutiny).

**Dateinamen oder Metadaten statt Inhalte hashen.** Ein Stil wie `names-only`,
`sizes-and-names` oder `timestamps-and-names` ist für eine schnelle Inventur nützlich und ist
kein Integritätsschutz. Dasselbe gilt für die Spalten mit Größe und Zeitstempel in einer
Full-Style-Liste: hilfreicher Kontext, trivial zu fälschen.

**Annehmen, SFV genüge, weil ein Werkzeug es geschrieben hat.** CRC-32, Großbuchstaben-Hex, nur
Dateiname, keine Größe. Siehe [Stufe B](#tier_b).

**Einmal Fingerprints erzeugen und nie wieder prüfen.** Eine Prüfliste, die Sie nie verifizieren,
beweist nichts. Planen Sie die Prüfung ein und machen Sie den Exit-Code für etwas sichtbar, das es
bemerkt.


<a name="cheatsheet"></a>

# Kurzreferenz

Alles, was diese Anleitung verwendet hat, an einer Stelle.

**Einen Algorithmus untersuchen**

| Kommando | Zeigt |
|---|---|
| `jacksum -a <algo> --info` | Breite, Blockgröße, HMAC-Fähigkeit, `broken:`-Schlagwort, Geschwindigkeitsrang |
| `jacksum -a <algo> --info -V details` | dasselbe plus Sicherheitsbegründung und Lawinenmessung |
| `jacksum -a <algo> --info -q <sequence>` | Lawineneffekt, gemessen an *Ihrer* Eingabe statt an `123456789` |
| `jacksum -A -a <algo> --info` | welche Implementierung aktiv ist |
| `jacksum -h <algo>` | vollständige Dokumentation: Typ, Jahr, Standard, Kommentar, `broken:`, Kompatibilitätsliste |
| `jacksum -h crc:` | jeden Parameter des Rocksoft (tm) Model |
| `jacksum -h hmac:` | HMAC-Syntax und Kürzung |
| `jacksum -h algorithms` | die Dokumentation aller Algorithmen |
| `jacksum -h examples` | durchgerechnete Beispiele aus der Manpage |

**Algorithmen auflisten**

| Kommando | Ergebnis |
|---|---|
| `jacksum -a all -l` | die IDs aller 586 Algorithmen |
| `jacksum -a all:256 -l` | nur 256-Bit-Ausgaben (55) |
| `jacksum -a all:8 -l` | nur 8-Bit-Ausgaben (9) — die "zu kurz"-Liste |
| `jacksum -a all:sha -l` | nur IDs, die "sha" enthalten |
| `jacksum --hmacs` | die 492 mit HMAC verwendbaren Algorithmen |
| `jacksum --hmacs -V info` | pro Algorithmus: Ausgabegröße, Blockgröße, empfohlene Mindestwerte für Schlüssel und Kürzung |
| `jacksum --hmacs -V summary` | nur die Anzahl |
| `jacksum -a all --info` | den Info-Block für jeden Algorithmus |

**Erzeugen, schützen, prüfen**

| Kommando | Zweck |
|---|---|
| `jacksum -a sha-256 --style full -o list.txt -r max /data` | eine Prüfliste erzeugen |
| `jacksum -a sha256+sha3-256 -F "#HASH{0} #HASH{1} #FILESIZE #FILENAME" -r max /data` | zwei Algorithmen, ein Lesedurchgang |
| `jacksum -a hmac:sha256 -k password --style full -o list.txt -r max /data` | authentifizierte Prüfliste |
| `jacksum -a sha-256 --style full -c list.txt` | prüfen |
| `jacksum -a sha-256 --style full --check-strict -c list.txt /data` | strenge Prüfung (erkennt auch neue Dateien) |
| `jacksum -a sha-256 --style full --list-filter bad -c list.txt` | nur zeigen, was Aufmerksamkeit braucht |
| `jacksum -a sha-256 -e <hash> -r max /data` | Dateien zu einem Hashwert finden |
| `jacksum -a sha-256 -w wanted.txt -r max /data` | Dateien zu einem der Hashwerte einer Liste finden |
| `jacksum -a unknown:256 -E hex -q <sequence> -e <hash>` | den Algorithmus hinter einem Hashwert identifizieren |
| `jacksum -a sha-256 --threads-reading max -r max /data` | parallele Lesevorgänge |


<a name="footnotes"></a>

# Fußnoten und weiterführende Literatur

**Jacksum-Dokumentation**

- [Algorithmen](ALGORITHMS_de.md) — alle 586 Algorithmen, alphabetisch und nach Herkunft sortiert;
  außerdem [HMAC](ALGORITHMS_de.md#hmac) und
  [anpassbare CRCs](ALGORITHMS_de.md#customizable_crcs)
- [Funktionen](FEATURES_de.md), [Beispiele](EXAMPLES_de.md),
  [Jacksum Hacks](JACKSUM_HACKS_de.md)
- [Manpage](https://github.com/jonelo/jacksum/wiki/Manpage) und
  [Cheat Sheet](https://github.com/jonelo/jacksum/wiki/Cheat-Sheet) im Wiki

**Primärquellen für die Sicherheitsaussagen**

Dies sind die Quellen, die Jacksums eigene `broken:`-Texte anführen; `--info -V details` gibt für
jeden Algorithmus die jeweils zutreffende aus.

- MD5-, HAVAL-128-3- und die Kollisionen des ursprünglichen RIPEMD (Wang et al., 2004) —
  https://eprint.iacr.org/2004/199.pdf
- SHA-1, erste Identical-Prefix-Kollision (SHAttered, 2017) — https://shattered.io
- SHA-1, erste Chosen-Prefix-Kollision (Shambles, 2020) — https://sha-mbles.github.io
- SHA-256, beste bekannte schrittreduzierte Angriffe (2024) —
  https://eprint.iacr.org/2024/349.pdf
- SHA-3 / Keccak, Kryptanalyse — https://eprint.iacr.org/2019/147
- RIPEMD-128, Kollision für die vollständige Kompressionsfunktion (Landelle, Peyrin 2013) —
  https://eprint.iacr.org/2013/607
- Whirlpool, Rebound-Angriffe (2010) — https://eprint.iacr.org/2010/198
- Edon-R (2009) — https://eprint.iacr.org/2009/135
- CRC-64/NVMe — https://nvmexpress.org

**In dieser Anleitung genannte Standards und Arbeiten**

- A. Joux, *Multicollisions in Iterated Hash Functions. Application to Cascaded Constructions*,
  CRYPTO 2004 — das Ergebnis, dass Verketten keine Sicherheitsniveaus addiert
- NIST FIPS 180-4 (SHA-1, SHA-2), FIPS 202 (SHA-3, SHAKE), FIPS 198-1 (HMAC)
- RFC 2104 (HMAC), RFC 2440 (OpenPGP), RFC 7143 (iSCSI, CRC-32c), RFC 7693 (BLAKE2)
- ISO/IEC 10118-3 (Whirlpool), ISO 3309 / ITU-T V.42 (CRC-32), ETSI EN 300 751 (CRC-82/DARC)
- POSIX 1003.2 (`cksum`)

**Eine Schlussbemerkung zum Vertrauen.** Jede Zahl in dieser Anleitung kam aus Jacksum selbst, und
jedes Kommando ist auf Ihrer Maschine wiederholbar. Das ist Absicht: es geht nicht darum, eine
Empfehlung zu behalten, sondern darum, sie neu herleiten zu können, wenn sich die Empfehlungen
ändern — denn das werden sie. `--info -V details` ist die Gewohnheit, die es zu behalten lohnt.

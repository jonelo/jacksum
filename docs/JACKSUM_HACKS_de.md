*Diese Seite auf Englisch: [JACKSUM_HACKS.md](JACKSUM_HACKS.md)*

> [!TIP]
> Der eigentliche Zweck von Jacksum ist der Umgang mit Hashwerten. Da **Jacksum aber sowohl viele Kodierungen als auch eine anpassbare Formatierung unterstützt**, bekommen Sie zusätzliche Fähigkeiten, die manchmal ziemlich nützlich sind.
> **Die Jacksum Hacks waren nicht geplant.** Ich war selbst recht überrascht über diesen Nebeneffekt, der erstaunlich gut funktioniert.

Zwei Optionen schalten das Hashen ab, und der Unterschied zwischen ihnen ist der Schlüssel zu fast
jedem Hack auf dieser Seite:

| | liest den Inhalt von Dateien | meldet Lesefehler | kennt Dateigröße und Zeitstempel |
|---|---|---|---|
| `-a none` | nein | nein | ja |
| `-a read` | ja   | ja   | ja |

`-a none` macht Jacksum also zu einem Umwandler und zu einem Werkzeug für Metadaten, und `-a read`
macht es zu einem Werkzeug, das jedes einzelne Byte anfasst, ohne einen Hashwert zu erzeugen.

> [!NOTE]
> Mit `-a none` braucht die Option `-E` ein explizites `-F`, sonst bricht Jacksum ab:
> `Jacksum: Parameter Error: -a none and -E without -F cannot go together.`
> Schreiben Sie entweder `-F "#SEQUENCE" -E hex` oder die Kurzform `-F "#SEQUENCE{hex}"`.

**Inhaltsverzeichnis**

- [Teil 1 — Kodierungen und Zahlensysteme](#part1)
  - [Datei-Dumps](#file-dumps)
  - [Mit Zeichenketten arbeiten ...](#strings)
  - [Binär nach ...](#binary-to)
  - [Oktal nach ...](#octal-to)
  - [Dezimal nach ...](#decimal-to)
  - [Hex nach ...](#hex-to)
  - [Base32 nach ...](#base32-to)
  - [Base32hex nach ...](#base32hex-to)
  - [Base64 nach ...](#base64-to)
  - [Base64url nach ...](#base64url-to)
  - [Z85 nach ...](#z85-to)
  - [z-base-32 nach ...](#z-base-32-to)
  - [BubbleBabble nach ...](#bubblebabble-to)
  - [Alle 19 Kodierungen auf einen Blick](#all-encodings)
  - [Hin und zurück](#round-trips)
- [Teil 2 — Hacks, die weder mit Hashing noch mit Kodierungen zu tun haben](#part2)
  - [Ist auf dem Medium noch alles lesbar?](#medium-readable)
  - [Dateien finden (`find`)](#find-files)
  - [Größen und Zeitstempel (`stat`, `du`, `ls`)](#sizes-and-timestamps)
  - [Ein Verzeichnis als Momentaufnahme sichern und Änderungen später erkennen](#snapshot)
  - [Pfad-Werkzeug (`realpath`, `dirname`, `basename`, `cygpath`)](#path-tool)
  - [Zeitstempel-Werkzeug (`date -r`)](#timestamp-tool)
  - [Eine Template-Engine für Dateibäume](#template-engine)
  - [Zeichensatz-Konverter (`iconv`)](#charset-converter)
  - [Die Bytes einer Pipe zählen (`wc -c`)](#count-bytes)
  - [Ein durchsuchbares Nachschlagewerk](#reference-book)
  - [Worauf läuft das hier eigentlich?](#what-am-i-running-on)

<a name="part1"/>

# Teil 1 — Kodierungen und Zahlensysteme

Für alle Beispiele in diesem Teil setzen wir `-a none`, weil uns das Hashen überhaupt nicht
interessiert.

Die unten verwendete Datei `myfile.dat` enthält die sieben Bytes `Jacksum`.

<a name="file-dumps"/>

## Datei-Dumps

### Hex-Dump

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE" -E hex -g 1
    4a 61 63 6b 73 75 6d

### Hex-Dump, gruppiert

`-g <count>` gruppiert die Ausgabe in `<count>` Bytes, `-G <char>` setzt den Trenner.

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE" -E hex -g 2 -G :
    4a61:636b:7375:6d

### Base16-Dump

Base16 (RFC 4648) ist Hexadezimal in Großbuchstaben.

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base16}"
    4A61636B73756D

### Base32-Dump

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base32}"
    JJQWG23TOVWQ====

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base32-nopadding}"
    JJQWG23TOVWQ

### Base32hex-Dump

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base32hex}"
    99GM6QRJELMG====

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base32hex-nopadding}"
    99GM6QRJELMG

### Base64-Dump

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base64}"
    SmFja3N1bQ==

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base64-nopadding}"
    SmFja3N1bQ

### Base64url-Dump

Sicher für URLs und Dateinamen, weil `+` und `/` durch `-` und `_` ersetzt werden.

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base64url}"
    SmFja3N1bQ==

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base64url-nopadding}"
    SmFja3N1bQ

### Z85-Dump

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{z85}"
    n)#jBB9hs

### z-base-32-Dump

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{z-base-32}"
    jjosg45uqiso

Seit 4.0.0 liest Jacksum z-base-32 auch wieder ein, siehe [z-base-32 nach ...](#z-base-32-to).

### BubbleBabble-Dump

Aussprechbare Pseudowörter, wie sie OpenSSH und SSH2 verwenden.

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{bubblebabble}"
    xidik-comuk-resyl-hyrix

Seit 4.0.0 liest Jacksum BubbleBabble auch wieder ein, siehe [BubbleBabble nach ...](#bubblebabble-to).

### Dezimal-, Oktal- und Binär-Dump

Die ganze Datei wird als eine einzige große Zahl behandelt, nicht als Liste von Byte-Werten.

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{dec}"
    20936227908973933

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{oct}"
    1123026155334672555

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{bin}"
    01001010011000010110001101101011011100110111010101101101

### Größe einer Datei

    $ jacksum -a none -q file:myfile.dat -F "#LENGTH"
    7

> [!NOTE]
> `-q file:` hält die gesamte Datei im Speicher und ist deshalb auf 128 MiB begrenzt. Übergeben Sie
> größere Dateien als normalen Parameter, statt `-q` zu verwenden.


<a name="strings"/>

## Mit Zeichenketten arbeiten ...

### Zeichen einer Zeichenkette zählen

    $ jacksum -a none -q "txt:Hello World" -F "#LENGTH"
    11

`#LENGTH` zählt **Bytes**, nicht Zeichen. Die fünf Zeichen von `Grüße` sind sieben Bytes in UTF-8:

    $ jacksum -a none -q "txt:Grüße" -F "#LENGTH bytes, hex: #SEQUENCE{hex}"
    7 bytes, hex: 4772c3bcc39f65

### Zeichenkette nach Hex

    $ jacksum -a none -q "txt:Hello World" -F "#SEQUENCE" -E hex -g 1
    48 65 6c 6c 6f 20 57 6f 72 6c 64

### Formatierte Zeichenkette nach Hex

`txtf:` interpretiert `\t`, `\n`, `\r`, `\"`, `\'`, `\\` und `\xhh`.

    $ jacksum -a none -q "txtf:Hello World\n" -F "#SEQUENCE" -E hex -g 1
    48 65 6c 6c 6f 20 57 6f 72 6c 64 0a

    $ jacksum -a none -q "txtf:tab\there\n" -F "#SEQUENCE" -E hex -g 1
    74 61 62 09 68 65 72 65 0a


### Zeichenkette nach Base64, Base32, Z85 und BubbleBabble

    $ jacksum -a none -q "txt:Hello World" -F "#SEQUENCE{base64}"
    SGVsbG8gV29ybGQ=

    $ jacksum -a none -q "txt:Hello World" -F "#SEQUENCE{base32}"
    JBSWY3DPEBLW64TMMQ======

    $ jacksum -a none -q "txt:Hello World" -F "#SEQUENCE{z85}"
    nm=QNzY&b1A+]m

    $ jacksum -a none -q "txt:Hello World" -F "#SEQUENCE{bubblebabble}"
    xidak-hyryk-sored-buhok-zusuk-sunex

### Zeichenkette in alles

    $ jacksum -a none -q "txt:Hello World" -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 0100100001100101011011000110110001101111001000000101011101101111011100100110110001100100, dec: 87521618088882533792115812, oct: 044145330661571005355734466144, hex:48656C6C6F20576F726C64

### Viele Zeichenketten auf einmal umwandeln

`--string-list` liest eine Zeichenkette pro Zeile. `#MESSAGE` ist die Zeichenkette selbst.

    $ cat strings.txt
    Hello World
    foo
    Grüße

    $ jacksum -a none --string-list strings.txt -F "#MESSAGE -> #SEQUENCE{base64}"
    Hello World -> SGVsbG8gV29ybGQ=
    foo -> Zm9v
    Grüße -> R3LDvMOfZQ==

    $ jacksum -a none --string-list strings.txt -F "#LENGTH #MESSAGE"
    11 Hello World
    3 foo
    7 Grüße

### Ein interaktiver Umwandler

`-q readline` liest eine Zeile von der Konsole und gibt sie in jedem gewünschten Format wieder aus —
praktisch, um Zeichenketten und Kodierungen zu untersuchen, ohne sie in der Shell-History zu
hinterlassen.

    $ jacksum -a none -q readline -F "msg=#MESSAGE len=#LENGTH hex=#SEQUENCE{hex} b64=#SEQUENCE{base64}"

Erfordert eine echte Konsole; es kann nicht über eine Pipe oder eine Umleitung gefüttert werden.


<a name="binary-to"/>

## Binär nach ...

### Binär nach Dezimal

    $ jacksum -a none -q bin:1010101010101010 -F "#SEQUENCE{dec}"
    43690

### Binär nach Oktal

    $ jacksum -a none -q bin:1010101010101010 -F "#SEQUENCE{oct}"
    125252

### Binär nach Hex

    $ jacksum -a none -q bin:1010101010101010 -F "#SEQUENCE{hex}"
    aaaa

### Binär in alles

    $ jacksum -a none -q bin:1010101010101010 -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 1010101010101010, dec: 43690, oct: 0125252, hex:AAAA

### Binär in alles, als JSON

    $ jacksum -a none -q bin:1010101010101010 -F '{ "bin": "#SEQUENCE{bin}", "dec": "#SEQUENCE{dec}", "oct": "0#SEQUENCE{oct}", "hex": "0x#SEQUENCE{hex-uppercase}" }'
    { "bin": "1010101010101010", "dec": "43690", "oct": "0125252", "hex": "0xAAAA" }

### Binär nach Base64

    $ jacksum -a none -q bin:1010101010101010 -F "#SEQUENCE{base64}"
    qqo=

### Binär nach Z85

    $ jacksum -a none -q bin:1010101010101010 -F "#SEQUENCE{z85}"
    S&u


<a name="octal-to"/>

## Oktal nach ...

Die Oktal-Eingabe ist eine Liste von **Byte**-Werten, getrennt durch Kommas oder Leerzeichen.

### Oktal nach Binär

    $ jacksum -a none -q oct:312,376 -F "#SEQUENCE{bin}"
    1100101011111110

### Oktal nach Dezimal

    $ jacksum -a none -q oct:312,376 -F "#SEQUENCE{dec}"
    51966

### Oktal nach Hex

    $ jacksum -a none -q oct:312,376 -F "#SEQUENCE{hex-uppercase}"
    CAFE

### Oktal in alles

    $ jacksum -a none -q oct:312,376 -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 1100101011111110, dec: 51966, oct: 0145376, hex:CAFE

### Oktal in alles, als JSON

    $ jacksum -a none -q oct:312,376 -F '{ "bin": "#SEQUENCE{bin}", "dec": "#SEQUENCE{dec}", "oct": "0#SEQUENCE{oct}", "hex": "0x#SEQUENCE{hex-uppercase}" }'
    { "bin": "1100101011111110", "dec": "51966", "oct": "0145376", "hex": "0xCAFE" }

### Oktal nach Base64

    $ jacksum -a none -q oct:312,376 -F "#SEQUENCE{base64}"
    yv4=

### Oktal nach Z85

    $ jacksum -a none -q oct:312,376 -F "#SEQUENCE{z85}"
    +kI

### Oktal nach Text

    $ jacksum -a none -q oct:"145 147 40 64 62 12" -F "#SEQUENCE{hex}"
    65672034320a


<a name="decimal-to"/>

## Dezimal nach ...

### Dezimal nach Binär

    $ jacksum -a none -q dec:42 -F "#SEQUENCE{bin}"
    00101010

### Dezimal nach Hex

    $ jacksum -a none -q dec:42 -F "#SEQUENCE{hex-uppercase}"
    2A

### Dezimal nach Oktal

    $ jacksum -a none -q dec:42 -F "#SEQUENCE{oct}"
    52

### Dezimal in alles

    $ jacksum -a none -q dec:42 -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 00101010, dec: 42, oct: 052, hex:2A

### Dezimal in alles, als JSON

    $ jacksum -a none -q dec:42 -F '{ "bin": "#SEQUENCE{bin}", "dec": "#SEQUENCE{dec}", "oct": "0#SEQUENCE{oct}", "hex": "0x#SEQUENCE{hex-uppercase}" }'
    { "bin": "00101010", "dec": "42", "oct": "052", "hex": "0x2A" }

### Dezimal nach Base64

    $ jacksum -a none -q dec:42 -F "#SEQUENCE{base64}"
    Kg==

### Dezimal nach Z85

    $ jacksum -a none -q dec:42 -F "#SEQUENCE{z85}"
    dG


<a name="hex-to"/>

## Hex nach ...

### Hex nach Binär

    $ jacksum -a none -q hex:cafe08 -F "#SEQUENCE{bin}"
    110010101111111000001000

### Hex nach Dezimal

    $ jacksum -a none -q hex:7A -F "#SEQUENCE{dec}"
    122

### Hex nach Oktal

    $ jacksum -a none -q hex:7A -F "#SEQUENCE{oct}"
    172

### Hex in Kleinbuchstaben nach Hex in Großbuchstaben

    $ jacksum -a none -q hex:cafe08 -F "#SEQUENCE{hex-uppercase}"
    CAFE08

### Hex in Großbuchstaben nach Hex in Kleinbuchstaben

    $ jacksum -a none -q hex:CAFE08 -F "#SEQUENCE{hex-lowercase}"
    cafe08

### Hex in alles

    $ jacksum -a none -q hex:CAFE -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 1100101011111110, dec: 51966, oct: 0145376, hex:CAFE

### Hex in alles, als JSON

    $ jacksum -a none -q hex:CAFE -F '{ "bin": "#SEQUENCE{bin}", "dec": "#SEQUENCE{dec}", "oct": "0#SEQUENCE{oct}", "hex": "0x#SEQUENCE{hex-uppercase}" }'
    { "bin": "1100101011111110", "dec": "51966", "oct": "0145376", "hex": "0xCAFE" }

### Hex nach Base32

    $ jacksum -a none -q hex:C0DECAFE -F "#SEQUENCE{base32}"
    YDPMV7Q=

### Hex nach Base64

    $ jacksum -a none -q hex:C0DECAFE -F "#SEQUENCE{base64}"
    wN7K/g==

### Hex nach Base64url

    $ jacksum -a none -q hex:C0DECAFE -F "#SEQUENCE{base64url}"
    wN7K_g==

### Hex nach Z85

    $ jacksum -a none -q hex:C0DECAFE -F "#SEQUENCE{z85}"
    Z#0lk

### Hex nach BubbleBabble

    $ jacksum -a none -q hex:C0DECAFE -F "#SEQUENCE{bubblebabble}"
    xubat-vidyz-vexox


<a name="base32-to"/>

## Base32 nach ...

### Base32 dekodieren und nach Hex kodieren

    $ jacksum -a none -q base32:YDPMV7Q= -F "#SEQUENCE{hex-uppercase}"
    C0DECAFE

### Base32 dekodieren und nach Base32hex kodieren

    $ jacksum -a none -q base32:YDPMV7Q= -F "#SEQUENCE{base32hex}"
    O3FCLVG=

### Base32 dekodieren und nach Base64 kodieren

    $ jacksum -a none -q base32:YDPMV7Q= -F "#SEQUENCE{base64}"
    wN7K/g==

### Base32 dekodieren und nach Z85 kodieren

    $ jacksum -a none -q base32:YDPMV7Q= -F "#SEQUENCE{z85}"
    Z#0lk

### Base32 in alles

    $ jacksum -a none -q base32:YDPMV7Q -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 11000000110111101100101011111110, dec: 3235826430, oct: 030067545376, hex:C0DECAFE


<a name="base32hex-to"/>

## Base32hex nach ...

### Base32hex dekodieren und nach Hex kodieren

    $ jacksum -a none -q base32hex:O3FCLVG= -F "#SEQUENCE{hex-uppercase}"
    C0DECAFE

### Base32hex dekodieren und nach Base32 kodieren

    $ jacksum -a none -q base32hex:O3FCLVG= -F "#SEQUENCE{base32}"
    YDPMV7Q=

### Base32hex dekodieren und nach Base64 kodieren

    $ jacksum -a none -q base32hex:O3FCLVG= -F "#SEQUENCE{base64}"
    wN7K/g==

### Base32hex in alles

    $ jacksum -a none -q base32hex:O3FCLVG= -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 11000000110111101100101011111110, dec: 3235826430, oct: 030067545376, hex:C0DECAFE


<a name="base64-to"/>

## Base64 nach ...

### Base64 dekodieren und nach Hex kodieren

    $ jacksum -a none -q base64:wN7K/g== -F "#SEQUENCE{hex-uppercase}"
    C0DECAFE

### Base64 dekodieren und nach Base64url kodieren

    $ jacksum -a none -q base64:wN7K/g== -F "#SEQUENCE{base64url}"
    wN7K_g==

### Base64 dekodieren und nach Z85 kodieren

    $ jacksum -a none -q base64:wN7K/g== -F "#SEQUENCE{z85}"
    Z#0lk

### Base64 ohne Padding funktioniert auch

    $ jacksum -a none -q base64:wN7K/g -F "#SEQUENCE{hex-uppercase}"
    C0DECAFE


<a name="base64url-to"/>

## Base64url nach ...

### Base64url dekodieren und nach Hex kodieren

    $ jacksum -a none -q base64url:wN7K_g== -F "#SEQUENCE{hex-uppercase}"
    C0DECAFE

### Base64url dekodieren und nach Standard-Base64 kodieren

    $ jacksum -a none -q base64url:wN7K_g== -F "#SEQUENCE{base64}"
    wN7K/g==

### Base64url dekodieren und nach Z85 kodieren

    $ jacksum -a none -q base64url:wN7K_g== -F "#SEQUENCE{z85}"
    Z#0lk

### Base64url in alles

    $ jacksum -a none -q base64url:wN7K_g== -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 11000000110111101100101011111110, dec: 3235826430, oct: 030067545376, hex:C0DECAFE


<a name="z85-to"/>

## Z85 nach ...

### Z85 dekodieren und nach Hex kodieren

    $ jacksum -a none -q "z85:Z#0lk" -F "#SEQUENCE{hex}"
    c0decafe

### Z85 dekodieren und nach Base64 kodieren

    $ jacksum -a none -q "z85:Z#0lk" -F "#SEQUENCE{base64}"
    wN7K/g==

### Z85 dekodieren und nach Base64url kodieren

    $ jacksum -a none -q "z85:Z#0lk" -F "#SEQUENCE{base64url}"
    wN7K_g==


<a name="z-base-32-to"/>

## z-base-32 nach ...

`-q z-base-32:` ist neu in Jacksum 4.0.0. Bis dahin war z-base-32 nur eine Ausgabekodierung, eine
z-base-32-Zeichenkette konnte also erzeugt, aber nicht wieder eingelesen werden.

### z-base-32 dekodieren und nach Hex kodieren

    $ jacksum -a none -q z-base-32:adxci9o -F "#SEQUENCE{hex-uppercase}"
    C0DECAFE

### z-base-32 dekodieren und nach Base32 kodieren

Dieselben Bytes, ein anderes Alphabet:

    $ jacksum -a none -q z-base-32:adxci9o -F "#SEQUENCE{base32}"
    YDPMV7Q=

### z-base-32 dekodieren und nach Base64 kodieren

    $ jacksum -a none -q z-base-32:adxci9o -F "#SEQUENCE{base64}"
    wN7K/g==

### z-base-32 in alles

    $ jacksum -a none -q z-base-32:adxci9o -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 11000000110111101100101011111110, dec: 3235826430, oct: 030067545376, hex:C0DECAFE

### Einen z-base-32-Dump zurücklesen

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{z-base-32}"
    jjosg45uqiso

    $ jacksum -a none -q z-base-32:jjosg45uqiso -F "#SEQUENCE{hex}"
    4a61636b73756d

> [!NOTE]
> Die z-base-32-Eingabe ist **ausschließlich in Kleinbuchstaben** und trägt **kein Padding**. Sowohl
> `ADXCI9O` als auch `adxci9o====` werden mit `Invalid z-base32 input data.` abgewiesen, und das
> Alphabet kennt weder `l` noch `v`, `0` oder `2`.

Der Typ steht auch für `-k` zur Verfügung, weil der geheime Schlüssel eines HMAC dieselbe Syntax
verwendet wie `-q`:

    $ jacksum -a hmac:sha256 -k z-base-32:adxci9o myfile.dat


<a name="bubblebabble-to"/>

## BubbleBabble nach ...

`-q bubblebabble:` ist ebenfalls neu in Jacksum 4.0.0. BubbleBabble ist die einzige Kodierung auf
dieser Seite, die **Redundanz** mitbringt: jedes zweite Tupel enthält eine Prüfsumme, und die
Zeichenkette ist von einem `x` eingerahmt. Der Dekodierer prüft das alles, eine vertippte
Zeichenkette wird also abgewiesen, statt zu beliebigen Bytes dekodiert zu werden.

### BubbleBabble dekodieren und nach Hex kodieren

    $ jacksum -a none -q bubblebabble:xubat-vidyz-vexox -F "#SEQUENCE{hex-uppercase}"
    C0DECAFE

### BubbleBabble dekodieren und nach Base32 kodieren

    $ jacksum -a none -q bubblebabble:xubat-vidyz-vexox -F "#SEQUENCE{base32}"
    YDPMV7Q=

### BubbleBabble dekodieren und nach Base64 kodieren

    $ jacksum -a none -q bubblebabble:xubat-vidyz-vexox -F "#SEQUENCE{base64}"
    wN7K/g==

### BubbleBabble dekodieren und nach Z85 kodieren

    $ jacksum -a none -q bubblebabble:xubat-vidyz-vexox -F "#SEQUENCE{z85}"
    Z#0lk

### BubbleBabble in alles

    $ jacksum -a none -q bubblebabble:xubat-vidyz-vexox -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 11000000110111101100101011111110, dec: 3235826430, oct: 030067545376, hex:C0DECAFE

### Einen BubbleBabble-Dump zurücklesen

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{bubblebabble}"
    xidik-comuk-resyl-hyrix

    $ jacksum -a none -q bubblebabble:xidik-comuk-resyl-hyrix -F "#SEQUENCE{hex}"
    4a61636b73756d

### Ein Tippfehler-Detektor

Drei Arten von Beschädigung, drei verschiedene Meldungen, und nie ein stillschweigend falsches
Ergebnis (in allen drei Fällen Exit-Code 2):

    $ jacksum -a none -q bubblebabble:xidik-comuk-resyl-hyrax -F "#SEQUENCE{hex}"
    BubbleBabble decoding error: the checksum of the tuple at index 19 does not match.

    $ jacksum -a none -q bubblebabble:xidqk-comuk-resyl-hyrix -F "#SEQUENCE{hex}"
    BubbleBabble decoding error: 'q' is not a valid vowel.

    $ jacksum -a none -q bubblebabble:idik-comuk-resyl-hyrix -F "#SEQUENCE{hex}"
    BubbleBabble decoding error: 22 is not a valid length for a BubbleBabble string.

Genau das macht BubbleBabble zur Kodierung der Wahl, wenn ein Fingerabdruck vorgelesen, am Telefon
diktiert oder von einem Menschen abgetippt werden muss.

### Die klassischen Testvektoren

Die drei Beispiele aus der BubbleBabble-Spezifikation, in beiden Richtungen:

    $ jacksum -a none -q "txt:" -F "#SEQUENCE{bubblebabble}"
    xexax

    $ jacksum -a none -q "txt:1234567890" -F "#SEQUENCE{bubblebabble}"
    xesef-disof-gytuf-katof-movif-baxux

    $ jacksum -a none -q "txt:Pineapple" -F "#SEQUENCE{bubblebabble}"
    xigak-nyryk-humil-bosek-sonax

    $ jacksum -a none -q bubblebabble:xesef-disof-gytuf-katof-movif-baxux -F "#SEQUENCE{hex}"
    31323334353637383930

> [!NOTE]
> Anders als bei z-base-32 wird bei der BubbleBabble-Eingabe **die Groß- und Kleinschreibung nicht
> unterschieden**, `XIDIK-COMUK-RESYL-HYRIX` wird also ebenfalls akzeptiert. Die leere Eingabe ist
> `xexax`, nicht die leere Zeichenkette. Für `-q` und `-k` ist die Langform `bubblebabble:` der
> einzige gültige Indikator; `bb:` ist kein Typ und landet im Hex-Rückfall (`Not a hex number.`).
> Überall dort, wo eine *Kodierung* erwartet wird, ist `bb` in Ordnung: `-E bb`, `#SEQUENCE{bb}`.

Der Typ steht auch für `-k` zur Verfügung, weil der geheime Schlüssel eines HMAC dieselbe Syntax
verwendet wie `-q`:

    $ jacksum -a hmac:sha256 -k bubblebabble:xubat-vidyz-vexox myfile.dat

Und da Jacksum eine BubbleBabble-Zeichenkette wieder in Bytes verwandeln kann, vergleicht die Option
`-e` einen BubbleBabble-kodierten Hashwert jetzt byteweise, ganz gleich, wie er geschrieben wurde:

    $ jacksum -a sha256 -E bb -e XIDOF-GANEG-HUFUV-VIBOM-PYZYG-FUNUL-GUFIB-BEHIG-KAKAS-GUVYK-NUVAH-NUZOH-ZIZUH-RIVEV-TORIB-TYSOP-GUXUX myfile.dat
        MATCH  myfile.dat (XIDOF-GANEG-HUFUV-VIBOM-PYZYG-FUNUL-GUFIB-BEHIG-KAKAS-GUVYK-NUVAH-NUZOH-ZIZUH-RIVEV-TORIB-TYSOP-GUXUX)

    Jacksum: Expectation met.
    Jacksum: 1 of the successfully read files matches the expected hash value.


<a name="all-encodings"/>

## Alle 19 Kodierungen auf einen Blick

Dieselben vier Bytes, `C0DECAFE`, durch jede Kodierung, die Jacksum kennt:

    $ jacksum -a none -q hex:C0DECAFE -F "#SEQUENCE{<encoding>}"

| `<encoding>` | Ausgabe |
|---|---|
| `bin`                 | `11000000110111101100101011111110` |
| `dec`                 | `3235826430` |
| `oct`                 | `30067545376` |
| `hex`                 | `c0decafe` |
| `hex-lowercase`       | `c0decafe` |
| `hex-uppercase`       | `C0DECAFE` |
| `hexup`               | `C0DECAFE` (veraltet seit 3.0, verwenden Sie `hex-uppercase`) |
| `base16`              | `C0DECAFE` |
| `base32`              | `YDPMV7Q=` |
| `base32-nopadding`    | `YDPMV7Q` |
| `base32hex`           | `O3FCLVG=` |
| `base32hex-nopadding` | `O3FCLVG` |
| `base64`              | `wN7K/g==` |
| `base64-nopadding`    | `wN7K/g` |
| `base64url`           | `wN7K_g==` |
| `base64url-nopadding` | `wN7K_g` |
| `z85`                 | `Z#0lk` |
| `z-base-32`           | `adxci9o` |
| `bb` / `bubblebabble` | `xubat-vidyz-vexox` |

Dieselben 19 Werte sind für `-E` gültig, und die Eingabeseite (`-q <type>:`) versteht
`bin`, `dec`, `oct`, `hex`, `base32`, `base32hex`, `base64`, `base64url`, `z85`, `z-base-32` und
`bubblebabble` (die beiden letzten sind neu in 4.0.0), `txt`, `txtf` und `file`.


<a name="round-trips"/>

## Hin und zurück

Alles, was Jacksum mit `-q <type>:` lesen kann, kann es auch mit `#SEQUENCE{<encoding>}` schreiben,
Sie können Umwandlungen also aneinanderreihen:

    $ jacksum -a none -q "txt:Hello World" -F "#SEQUENCE{z85}"
    nm=QNzY&b1A+]m

    $ jacksum -a none -q "z85:nm=QNzY&b1A+]m" -F "#SEQUENCE{hex}"
    48656c6c6f20576f726c64

    $ jacksum -a none -q hex:48656c6c6f20576f726c64 -F "#SEQUENCE{z-base-32}"
    jb1sa5dxrbms6hucco

    $ jacksum -a none -q z-base-32:jb1sa5dxrbms6hucco -F "#SEQUENCE{base64}"
    SGVsbG8gV29ybGQ=

    $ jacksum -a none -q base64:SGVsbG8gV29ybGQ= -F "#SEQUENCE{bubblebabble}"
    xidak-hyryk-sored-buhok-zusuk-sunex

    $ jacksum -a none -q bubblebabble:xidak-hyryk-sored-buhok-zusuk-sunex -F "#SEQUENCE{hex}"
    48656c6c6f20576f726c64

> [!NOTE]
> Jacksum schreibt immer *Text*. Es kann eine Binärdatei lesen (`-q file:`) und sie in jeder
> Kodierung ausgeben, aber es kann keine rohen Bytes in eine Datei zurückschreiben — für
> `base64 -d > file.bin` gibt es also keine Entsprechung in Jacksum.


<a name="part2"/>

# Teil 2 — Hacks, die weder mit Hashing noch mit Kodierungen zu tun haben

Alles Folgende verwendet `-a none` oder `-a read`, es wird also nie ein Hashwert berechnet. Die
Beispiele laufen gegen diesen kleinen Baum:

    myfile.dat            7 bytes
    readme.txt           12 bytes
    strings.txt          24 bytes
    photos/a.jpg          1 byte
    photos/2024/b.jpg     2 bytes

<a name="medium-readable"/>

## Ist auf dem Medium noch alles lesbar?

Das ist der eine Hack, der überhaupt nichts mit Hashen zu tun hat: `-a read` liest jedes einzelne
Byte jeder Datei und wirft die Bytes weg. Alles, was dabei schiefgeht — eine zerkratzte DVD, ein
sterbender USB-Stick, Bitfäule auf einer alten Backup-Platte, eine weggebrochene Netzwerkfreigabe,
ein Rechteproblem — kommt als Lesefehler zum Vorschein.

    $ jacksum -a read -V summary,errors -r max . > /dev/null

    Jacksum: total files read successfully: 5
    Jacksum: total bytes read: 46
    Jacksum: total bytes read (human readable): 46 bytes
    Jacksum: total file read errors: 0

Die Ausgabe pro Datei geht auf die Standardausgabe (und wird von `> /dev/null` weggeworfen), während
Fehler und die Zusammenfassung auf die Standardfehlerausgabe gehen. Mit einer kaputten Datei im
Baum:

    $ jacksum -a read -V summary,errors -r max . > /dev/null
    Jacksum: Error: ./bad.bin (Permission denied)

    Jacksum: total files read successfully: 5
    Jacksum: total bytes read: 46
    Jacksum: total bytes read (human readable): 46 bytes
    Jacksum: total file read errors: 1

    $ echo $?
    4

`total file read errors` und der Exit-Code 4 machen das skriptfähig. Mit `-u <file>` sammeln Sie die
Liste der beschädigten Dateien ein:

    $ jacksum -a read -V summary,errors -u unreadable.log -r max /Volumes/BACKUP > /dev/null

Beachten Sie, dass `-a none` diese Dateien **nicht** findet, weil es sie nie öffnet:

    $ jacksum -a none -F "#FILESIZE #FILENAME" bad.bin
    1 bad.bin

Genau das ist der Unterschied: Verwenden Sie `-a none`, wenn Sie nur Metadaten wollen, und
`-a read`, wenn Sie wissen wollen, ob die Daten noch da sind.


<a name="find-files"/>

## Dateien finden (`find`)

`--style names-only` durchläuft einen Verzeichnisbaum und gibt nichts als die Pfade aus.

    $ jacksum --style names-only .
    ./myfile.dat
    ./readme.txt
    ./photos/2024/b.jpg
    ./photos/a.jpg
    ./strings.txt

`-r <depth>` begrenzt die Rekursionstiefe:

    $ jacksum --style names-only -r 1 .
    Jacksum: Info: "./photos" is a directory, but the maximum number of allowed directory levels (1) has been reached.
    ./myfile.dat
    ./readme.txt
    ./strings.txt

`-d` und `-f` halten Jacksum davon ab, symbolischen Links auf Verzeichnisse bzw. auf Dateien zu
folgen:

    $ jacksum --style names-only -d -f .
    Jacksum: Info: Ignoring "./photolink", because it is a symlink to a dir.
    Jacksum: Info: Ignoring "./link.txt", because it is a symlink to a file.
    ./myfile.dat
    ./readme.txt
    ./photos/2024/b.jpg
    ./photos/a.jpg
    ./strings.txt

Standardmäßig akzeptiert Jacksum nur reguläre Dateien und beschwert sich über alles andere:

    $ jacksum --style names-only .
    Jacksum: Error: ./queue.fifo: is not a regular file.
    ./myfile.dat
    ./readme.txt
    ...

`--scan-all-unix-file-types` schließt blockorientierte Geräte, zeichenorientierte Geräte, benannte
Pipes, Sockets und Solaris-Doors mit ein:

    $ jacksum --style names-only --scan-all-unix-file-types .
    ./myfile.dat
    ./queue.fifo
    ./readme.txt
    ./photos/2024/b.jpg
    ./photos/a.jpg
    ./strings.txt

Unter Microsoft Windows listet `--scan-ntfs-ads` zusätzlich alternative NTFS-Datenströme auf — ein
Ort, an dem sich so manches gerne versteckt.

Die entstehende Liste kann mit `--file-list` direkt wieder in Jacksum hineingegeben werden.


<a name="sizes-and-timestamps"/>

## Größen und Zeitstempel (`stat`, `du`, `ls`)

Drei fertige Styles erzeugen Listen ohne Hashwerte. Kombinieren Sie sie **nicht** mit `-a`, sie
bringen ihren eigenen Algorithmus mit.

    $ jacksum --style sizes-and-names .
    7 ./myfile.dat
    12 ./readme.txt
    2 ./photos/2024/b.jpg
    1 ./photos/a.jpg
    24 ./strings.txt

    $ jacksum --style timestamps-and-names -t iso .
    2026-03-01T10:15:00.000+01:00 ./myfile.dat
    2026-04-22T09:30:00.000+02:00 ./readme.txt
    2026-01-05T07:45:00.000+01:00 ./photos/2024/b.jpg
    2025-12-24T18:00:00.000+01:00 ./photos/a.jpg
    2026-02-14T00:00:00.000+01:00 ./strings.txt

    $ jacksum --style without-hashes --no-header .
    2026-03-01T10:15:00.000+01:00 7 ./myfile.dat
    2026-04-22T09:30:00.000+02:00 12 ./readme.txt
    2026-01-05T07:45:00.000+01:00 2 ./photos/2024/b.jpg
    2025-12-24T18:00:00.000+01:00 1 ./photos/a.jpg
    2026-02-14T00:00:00.000+01:00 24 ./strings.txt

Mit `-F` bestimmen Sie das Layout selbst:

    $ jacksum -a none -F "#FILESIZE #FILENAME" .
    7 ./myfile.dat
    12 ./readme.txt
    2 ./photos/2024/b.jpg
    1 ./photos/a.jpg
    24 ./strings.txt

Und `-V summary` zählt Dateien und Bytes eines ganzen Baums:

    $ jacksum -a read -V summary -r max . > /dev/null

    Jacksum: total files read successfully: 5
    Jacksum: total bytes read: 46
    Jacksum: total bytes read (human readable): 46 bytes
    Jacksum: total file read errors: 0


<a name="snapshot"/>

## Ein Verzeichnis als Momentaufnahme sichern und Änderungen später erkennen

`--style without-hashes` schreibt eine Liste aus Zeitstempeln, Größen und Pfaden. Da es ein
richtiger Prüflisten-Style ist, kann Jacksum sie mit `-c` wieder einlesen — Sie bekommen einen
Änderungsdetektor ohne einen einzigen Hashwert.

    $ jacksum --style without-hashes --no-header . > ../snapshot.txt

Legen Sie die Momentaufnahme **außerhalb** des Baums ab, den Sie aufnehmen, sonst taucht sie in
ihrer eigenen Liste auf.

Später, nachdem `readme.txt` gewachsen ist, `notes.md` dazugekommen und `photos/a.jpg` gelöscht
worden ist:

    $ jacksum --style without-hashes -c ../snapshot.txt --no-header -V nosummary,noinfo .
    Jacksum: Error: ./photos/a.jpg: does not exist.
      MISSING  ./photos/a.jpg
           OK  ./myfile.dat
       FAILED  ./readme.txt
           OK  ./photos/2024/b.jpg
           OK  ./strings.txt
          NEW  ./notes.md

    $ echo $?
    4

Bei der Standard-Ausführlichkeit sagt Ihnen Jacksum auch, *warum* eine Datei durchgefallen ist:

       FAILED  ./readme.txt
               [filesize expected: 12, actual: 19]

`--list-filter` schränkt den Bericht auf das ein, was Sie interessiert:

    $ jacksum --style without-hashes -c ../snapshot.txt --no-header --list-filter new -V nosummary,noinfo,noerrors .
          NEW  ./notes.md

    $ jacksum --style without-hashes -c ../snapshot.txt --no-header --list-filter bad -V nosummary,noinfo .
    Jacksum: Error: ./photos/a.jpg: does not exist.
      MISSING  ./photos/a.jpg
       FAILED  ./readme.txt

Gültige Filter sind `ok`, `failed`, `missing`, `new` sowie die Abkürzungen `all`, `good`
(`none,ok,new`), `bad` (`none,failed,missing`) und `none`.

Wenn Sie nur wissen wollen, ob die Dateien überhaupt noch *da* sind, verwenden Sie stattdessen
`--style names-only` — diese Liste enthält nichts als Pfade:

    $ jacksum --style names-only --no-header . > ../names.txt
    $ jacksum --style names-only -c ../names.txt --no-header --list-filter bad -V nosummary,noinfo .
    $ echo $?
    0

Schweigen plus Exit-Code 0 heißt „nichts fehlt“. Sobald etwas verschwindet:

    $ jacksum --style names-only -c ../names.txt --no-header --list-filter bad -V nosummary,noinfo .
    Jacksum: Error: ./photos/a.jpg: does not exist.
      MISSING  ./photos/a.jpg
    $ echo $?
    4

Ergänzen Sie `--check-strict`, wenn auch fehlerhaft formatierte Zeilen in der Liste zu einem
Exit-Code ungleich null führen sollen.


<a name="path-tool"/>

## Pfad-Werkzeug (`realpath`, `dirname`, `basename`, `cygpath`)

    $ jacksum -a none --path-absolute -F "#FILENAME" myfile.dat
    /private/tmp/jacksum-hacks-demo/myfile.dat

    $ jacksum -a none --path-relative-to /private/tmp -F "#FILENAME" .
    jacksum-hacks-demo/myfile.dat
    jacksum-hacks-demo/readme.txt
    jacksum-hacks-demo/photos/2024/b.jpg
    jacksum-hacks-demo/photos/a.jpg
    jacksum-hacks-demo/strings.txt

`#FILENAME{path}` und `#FILENAME{name}` sind `dirname` und `basename`:

    $ jacksum -a none -F "#FILENAME{path} :: #FILENAME{name}" .
    . :: myfile.dat
    . :: readme.txt
    ./photos/2024 :: b.jpg
    ./photos :: a.jpg
    . :: strings.txt

`--no-path` lässt das Verzeichnis vollständig weg:

    $ jacksum -a none --no-path -F "#FILENAME" .
    myfile.dat
    readme.txt
    b.jpg
    a.jpg
    strings.txt

`-P <char>` tauscht den Pfadtrenner aus — machen Sie aus Unix-Pfaden Windows-Pfade und zurück, oder
erzeugen Sie unter Windows Schrägstriche für HTML:

    $ jacksum -a none -P "\\" -F "#FILENAME" .
    .\myfile.dat
    .\readme.txt
    .\photos\2024\b.jpg
    .\photos\a.jpg
    .\strings.txt


<a name="timestamp-tool"/>

## Zeitstempel-Werkzeug (`date -r`)

`-t` formatiert die Zeit der letzten Änderung einer Datei in jedem gewünschten Format.

    $ jacksum -a none -t unixtime -F "#TIMESTAMP #FILENAME" .
    1772356500 ./myfile.dat
    1776843000 ./readme.txt
    1767595500 ./photos/2024/b.jpg
    1766595600 ./photos/a.jpg
    1771023600 ./strings.txt

Vordefinierte Formate sind `default`, `default-utc`, `iso` (`iso8601`), `iso-utc` (`iso8601utc`),
`unixtime` und `unixtime-ms`:

    $ jacksum -a none -t iso-utc -F "#TIMESTAMP #FILENAME" myfile.dat
    2026-03-01T09:15:00.000Z myfile.dat

    $ jacksum -a none -t default -F "#TIMESTAMP #FILENAME" myfile.dat
    20260301101500000 myfile.dat

    $ jacksum -a none -t unixtime-ms -F "#TIMESTAMP #FILENAME" myfile.dat
    1772356500000 myfile.dat

Alles, was Javas `SimpleDateFormat` versteht, funktioniert ebenfalls:

    $ jacksum -a none -t "yyyy-MM-dd" -F "#TIMESTAMP #FILENAME" .
    2026-03-01 ./myfile.dat
    2026-04-22 ./readme.txt
    2026-01-05 ./photos/2024/b.jpg
    2025-12-24 ./photos/a.jpg
    2026-02-14 ./strings.txt


<a name="template-engine"/>

## Eine Template-Engine für Dateibäume

`-F` ist freier Text mit einer Handvoll Platzhalter. Zusammen mit `-a none` wird Jacksum damit zu
einem kleinen Codegenerator, der einen Verzeichnisbaum durchläuft. `#QUOTE` fügt ein `"` ein,
`#SEPARATOR` fügt das ein, was `-s` sagt.

Eine HTML-Linkliste:

    $ jacksum -a none -P / -F "<li><a href=#QUOTE#FILENAME#QUOTE>#FILENAME{name}</a> &mdash; #FILESIZE bytes</li>" .
    <li><a href="./myfile.dat">myfile.dat</a> &mdash; 7 bytes</li>
    <li><a href="./readme.txt">readme.txt</a> &mdash; 12 bytes</li>
    <li><a href="./photos/2024/b.jpg">b.jpg</a> &mdash; 2 bytes</li>
    <li><a href="./photos/a.jpg">a.jpg</a> &mdash; 1 bytes</li>
    <li><a href="./strings.txt">strings.txt</a> &mdash; 24 bytes</li>

JSON Lines, fertig für `jq`:

    $ jacksum -a none -t iso -F '{"path":"#FILENAME","size":#FILESIZE,"mtime":"#TIMESTAMP"}' .
    {"path":"./myfile.dat","size":7,"mtime":"2026-03-01T10:15:00.000+01:00"}
    {"path":"./readme.txt","size":12,"mtime":"2026-04-22T09:30:00.000+02:00"}
    {"path":"./photos/2024/b.jpg","size":2,"mtime":"2026-01-05T07:45:00.000+01:00"}
    {"path":"./photos/a.jpg","size":1,"mtime":"2025-12-24T18:00:00.000+01:00"}
    {"path":"./strings.txt","size":24,"mtime":"2026-02-14T00:00:00.000+01:00"}

CSV, mit dem von `-s` gesetzten Trenner:

    $ jacksum -a none -s ";" -F "#FILENAME#SEPARATOR#FILESIZE" .
    ./myfile.dat;7
    ./readme.txt;12
    ./photos/2024/b.jpg;2
    ./photos/a.jpg;1
    ./strings.txt;24

Ein Shell-Skript:

    $ jacksum -a none -F "cp -p #QUOTE#FILENAME#QUOTE /backup/" .
    cp -p "./myfile.dat" /backup/
    cp -p "./readme.txt" /backup/
    cp -p "./photos/2024/b.jpg" /backup/
    cp -p "./photos/a.jpg" /backup/
    cp -p "./strings.txt" /backup/

SQL:

    $ jacksum -a none -F "INSERT INTO files (path, bytes) VALUES ('#FILENAME', #FILESIZE);" .
    INSERT INTO files (path, bytes) VALUES ('./myfile.dat', 7);
    INSERT INTO files (path, bytes) VALUES ('./readme.txt', 12);
    INSERT INTO files (path, bytes) VALUES ('./photos/2024/b.jpg', 2);
    INSERT INTO files (path, bytes) VALUES ('./photos/a.jpg', 1);
    INSERT INTO files (path, bytes) VALUES ('./strings.txt', 24);

Verwenden Sie `-o <file>` (oder `-O <file>` zum Überschreiben), wenn das Ergebnis in eine Datei
statt ins Terminal gehen soll.


<a name="charset-converter"/>

## Zeichensatz-Konverter (`iconv`)

`--string-list` liest die Zeichenketten, `--charset-string-list` sagt, wie sie kodiert sind, und
`--charset-stdout` sagt, wie sie geschrieben werden sollen. `#MESSAGE` reicht die Zeichenkette
unverändert durch — das Einzige, was also passiert, ist eine Zeichensatzumwandlung.

    $ jacksum -a none --string-list strings.txt --charset-string-list UTF-8 --charset-stdout ISO-8859-1 -F "#MESSAGE" | xxd
    00000000: 4865 6c6c 6f20 576f 726c 640a 666f 6f0a  Hello World.foo.
    00000010: 4772 fcdf 650a                           Gr..e.

`Grüße` kommt als `47 72 fc df 65` an — fünf ISO-8859-1-Bytes statt sieben UTF-8-Bytes. Ergänzen Sie
`--bom`, wenn der Zielzeichensatz eine Bytereihenfolge-Markierung bekommen soll. Jacksum kennt über
170 Zeichensätze; `--charset-output-file` macht dasselbe für `-o`.


<a name="count-bytes"/>

## Die Bytes einer Pipe zählen (`wc -c`)

`-` liest die Standardeingabe, `#LENGTH` gibt aus, wie viele Bytes durchgekommen sind.

    $ printf 'Hello World' | jacksum -a read -F "#LENGTH" -
    11


<a name="reference-book"/>

## Ein durchsuchbares Nachschlagewerk

Die Manpage ist im Programm eingebaut, und `-h <word>` gibt genau den Abschnitt aus, der passt. Das
macht Jacksum zu einem ordentlichen Nachschlagewerkzeug, selbst wenn Sie gerade gar nichts hashen.

    $ jacksum -h -E              # all 19 encodings, with their alphabets
    $ jacksum -h --style         # every style, with a feature matrix
    $ jacksum -h -q              # all input types for -q
    $ jacksum -h algorithms      # all 586 algorithms
    $ jacksum -h examples        # the whole EXAMPLES section
    $ jacksum -h "exit status"   # the exit codes

`--exact` beschränkt den Treffer auf eine einzige Option:

    $ jacksum --exact -h -g
        -g <count>
        --group-bytes <count>

                Group the hex output for the checksum in <count> bytes for
                better readability, only valid if encoding is hex or hexup.
                ...

Passt nichts, ist der Exit-Code 1 — so können Sie in einem Skript prüfen, ob es eine Option
überhaupt gibt:

    $ jacksum --exact -h --nonexistent > /dev/null 2>&1 ; echo $?
    1


<a name="what-am-i-running-on"/>

## Worauf läuft das hier eigentlich?

`--header` stellt der eigentlichen Ausgabe einen Block mit Umgebungsinformationen voran — Hersteller
und Version der JVM, Betriebssystem, Architektur, das Arbeitsverzeichnis und die genauen Argumente,
mit denen Jacksum aufgerufen wurde. Nützlich für reproduzierbare Berichte und für Fehlermeldungen.

    $ jacksum --header -q txt:
    #
    # created by: Jacksum (https://jacksum.net, version: 4.0.0)
    # invoked on JVM: OpenJDK 64-Bit Server VM (vendor: Eclipse Adoptium, version: 25.0.4+7-LTS)
    # invoked on OS: Mac OS X (arch: aarch64, version: 26.6.1)
    # invoked on date: 2026-08-16T22:35:01.152+02:00
    #
    # invoked from: /private/tmp/jacksum-hacks-demo
    # invocation args: --header -q hex:
    #___________________________________________________________________________________________
    a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a

*Diese Seite auf Englisch: [EXAMPLES.md](EXAMPLES.md)*

**Inhaltsverzeichnis**
 - [Bevor Sie beginnen](#before)
 - [1. Erste Schritte](#getting_started)
   - [Eine Datei hashen](#gs_one_file)
   - [Viele Dateien und Verzeichnisbäume hashen](#gs_many_files)
   - [Den Algorithmus auswählen](#gs_algorithm)
   - [Die Kodierung des Hashwerts auswählen](#gs_encoding)
 - [2. Andere Eingaben als Dateien](#input)
   - [Standardeingabe und Pipes](#input_stdin)
   - [Sequenzen auf der Kommandozeile](#input_sequences)
   - [Dateilisten](#input_filelist)
   - [Zeichenkettenlisten](#input_stringlist)
 - [3. Ausgabe und Formate](#output)
   - [Vordefinierte Styles](#output_styles)
   - [Benutzerdefinierte Formate](#output_formats)
   - [Zeitstempel, Trenner, Pfade und Gruppierung](#output_misc)
   - [Wohin die Ausgabe geht](#output_files)
   - [Formate anderer Werkzeuge](#output_othertools)
 - [4. Datenintegrität überprüfen](#verify)
   - [Eine Datei gegen einen bekannten Hashwert prüfen](#verify_one)
   - [Eine Hashliste erzeugen und später überprüfen](#verify_list)
   - [OK, FAILED, MISSING und NEW](#verify_states)
   - [Strikte Überprüfung (Audit)](#verify_strict)
   - [Den Bericht filtern](#verify_filter)
   - [Listen überprüfen, die von anderen Werkzeugen erzeugt wurden](#verify_foreign)
   - [Exit-Codes](#verify_exitcodes)
 - [5. Objekte finden](#find)
   - [Alle Duplikate einer Datei finden](#find_duplicates)
   - [Eine Datei anhand ihres Hashwerts finden](#find_byhash)
   - [Dateien anhand einer Liste bekannter Hashwerte finden](#find_wantedlist)
   - [Negativer Abgleich](#find_negative)
   - [Zeichenketten finden, die zu einem Hashwert passen](#find_strings)
   - [Schadsoftware anhand von Hashwerten finden](#find_malware)
   - [Den Algorithmus finden, der einen Hashwert erzeugt hat](#find_algorithm)
 - [6. HMAC](#hmac)
   - [Einen HMAC berechnen](#hmac_calculate)
   - [Woher der Schlüssel kommt](#hmac_key)
   - [Verkürzte HMACs](#hmac_truncated)
   - [Einen HMAC überprüfen](#hmac_verify)
   - [Interoperabilität mit anderen Werkzeugen](#hmac_interop)
 - [7. Mehr als Hashing](#beyond)
   - [Reproduzierbare, eindeutige, sichere Passwörter](#beyond_passwords)
   - [Große Pseudozufallszahlen](#beyond_random)
   - [Umwandlungen von Kodierungen](#beyond_encodings)
 - [8. CRCs anpassen](#crcs)
   - [6 Parameter](#crcs_6)
   - [7 Parameter](#crcs_7)
   - [8 Parameter](#crcs_8)
 - [9. Performance und Steuerung der Traversierung](#performance)
 - [10. Informationen sammeln](#info)
   - [Über einen Algorithmus](#info_one_algo)
   - [CRC-Parameter untersuchen](#info_crc)
   - [Über viele Algorithmen](#info_many_algos)
   - [Über Styles und Kodierungen](#info_styles)
   - [Über das Programm](#info_program)
   - [In der Hilfe navigieren](#info_help)

<a name="before"></a>

# Bevor Sie beginnen

Alle nachfolgenden Beispiele wurden gegen **Jacksum 4.0.0** verifiziert. Sie sind von leicht nach
schwer geordnet: Abschnitt 1 benötigt nichts weiter als eine Datei, Abschnitt 10 ist
Referenzmaterial, das Sie nachschlagen, wenn Sie es brauchen.

Einige Konventionen, die durchgehend verwendet werden:

- Kommandos werden als `jacksum ...` geschrieben. Wenn Sie kein Startskript in Ihren `PATH` gelegt
  haben, ersetzen Sie `jacksum` überall durch `java -jar jacksum-4.0.0.jar`.
- **Der Standardalgorithmus ist SHA3-256.** Er wird immer dann verwendet, wenn `-a` weggelassen
  wird. In Skripten sollten Sie den Algorithmus immer explizit benennen, weil sich der
  Standardwert in einem künftigen Release ändern kann.
- Kurze und lange Optionsnamen sind gleichwertig: `-a`/`--algorithm`, `-c`/`--check-file`,
  `-e`/`--expect`, `-C`/`--compat`/`--style`, `-w`/`--wanted-list`. Die langen Formen werden
  nachfolgend bevorzugt, weil sie sich besser lesen.
- Ein abschließender `.` bedeutet "das aktuelle Verzeichnis und alles darunter". Jacksum
  traversiert standardmäßig rekursiv.
- Das Quoting unterscheidet sich zwischen den Shells. Verwenden Sie in `bash`/`zsh` einfache
  Anführungszeichen, um den wörtlichen Wert jedes Zeichens innerhalb der Anführungszeichen zu
  erhalten; verwenden Sie in der Windows-Shell `cmd` doppelte Anführungszeichen.

Siehe auch die [Manpage](https://github.com/jonelo/jacksum/wiki/Manpage) für die verbindliche
Beschreibung jeder Option (`jacksum -h`), [Use Cases](USE_CASES_de.md) für vollständige Rezepte, die
ein Problem von Anfang bis Ende lösen, und
[Jacksum Hacks](JACKSUM_HACKS_de.md) für die Dinge, die Jacksum
kann, die nichts mit Hashing zu tun haben.

<a name="getting_started"></a>

# 1. Erste Schritte

Eine Hashfunktion H bildet eine Bitfolge m ∈ {0, 1}<sup>*</sup> beliebiger Länge auf eine Bitfolge
h ∈ {0, 1}<sup>n</sup> fester Länge n ∈ ℕ ab:

    h = H(m)

m wird häufig die Nachricht oder die Daten genannt, und h wird — abhängig vom Design und von der
Sicherheitsstärke der Hashfunktion H — Prüfsumme, CRC, Hash, Hashwert, Message Digest,
Fingerabdruck der Daten oder Daumenabdruck der Daten genannt.

Das Berechnen von Hashwerten ist üblicherweise der erste Schritt, den Sie gehen, um überhaupt
später die Datenintegrität prüfen zu können.

<a name="gs_one_file"></a>

## Eine Datei hashen

```
jacksum ubuntu-26.04-desktop-amd64.iso
```

Berechnet den SHA3-256-Hashwert (der Standardalgorithmus) einer Datei. Die Ausgabe ist
`<hash> <filename>`.

```
jacksum -a sha3-256 ubuntu-26.04-desktop-amd64.iso
40822f93d646a7644f24be73f21b2998ee5ffd1ff16cb49a319dcd9d5538c508 ubuntu-26.04-desktop-amd64.iso
```

Dasselbe, mit explizit benanntem Algorithmus. Das ist es, was Sie in Skripten wollen.

<a name="gs_many_files"></a>

## Viele Dateien und Verzeichnisbäume hashen

```
jacksum -a crc32 -x *.txt
```

Berechnet einen 32-Bit-CRC aller Textdateien im aktuellen Ordner, ausgegeben in hexadezimaler Form
(`-x`). Die Wildcard-Expansion übernimmt Ihre Shell; Jacksum erledigt das auch selbst auf Shells,
die es nicht tun.

```
jacksum -a sha3-256 .
```

Traversiert das aktuelle Verzeichnis und alles darunter. Die Verzeichnis-Traversierung ist
standardmäßig rekursiv; verwenden Sie `-r <depth>`, um sie zu begrenzen.

```
jacksum -a cksum /mnt/share
```

Berechnet einen 32-Bit-CRC mit dem Standard-Unix-Algorithmus `cksum` für alle Dateien unter
`/mnt/share` und dessen Unterordnern.

<a name="gs_algorithm"></a>

## Den Algorithmus auswählen

```
jacksum -a haval_256_5 .
```

Berechnet einen 256-Bit-Hashwert mit 5 Runden unter Verwendung des HAVAL-Algorithmus.

```
jacksum -a sha1+crc32 .
```

Berechnet SHA-1 und CRC-32 in **einem Durchgang über die Daten**. Standardmäßig werden die
einzelnen Hashwerte zu einem Hashwert verkettet, was Integritätsprüfungen über einen einzigen Wert
einfach macht.

```
jacksum -a sha1+crc32 -F "sha1=#HASH{0} crc32=#HASH{1} #FILENAME" .
```

Dieselben zwei Algorithmen, aber getrennt ausgegeben. `{0}` und `{1}` indizieren die Algorithmen in
der Reihenfolge, in der Sie sie benannt haben.

```
jacksum -a all:sha -F "#ALGONAME{i}(#FILENAME) = #HASH{i}" .
```

Filtern von Algorithmen: `all:<string>` wählt jeden Algorithmus aus, dessen ID `<string>` enthält,
und `{i}` iteriert über alle davon. Beachten Sie, dass `all:sha` in Jacksum 4.0.0 auch auf
`ascon-hasha`, `sha0`, `shabal*` und `shake*` passt — rufen Sie zuerst
`jacksum -a all:sha --list` auf, um zu sehen, was Sie erhalten.

```
jacksum -a sha-1+sha-224+sha-256+sha-384+sha-512+sha-512/224+sha-512/256+sha3-224+sha3-256+sha3-384+sha3-512 \
        -F "#ALGONAME{i}(#FILENAME) = #HASH{i}" .
```

Wenn Sie genau die Familien SHA-1, SHA-2 und SHA-3 wollen und nichts anderes, benennen Sie sie
explizit. `jacksum -a all:sha --list` liefert Ihnen die IDs zum Kopieren.

<a name="gs_encoding"></a>

## Die Kodierung des Hashwerts auswählen

Standardmäßig hängt die Kodierung vom Algorithmus ab: eine klassische Prüfsumme wird üblicherweise
dezimal ausgegeben, eine Einwegfunktion hexadezimal. Legen Sie sie immer dann explizit fest, wenn
die Ausgabe später wieder geparst werden soll.

```
jacksum -a crc32 -x -q txt:123456789
jacksum -a crc32 -X -q txt:123456789
```

`-x` ist Hex in Kleinbuchstaben, `-X` ist Hex in Großbuchstaben.

```
jacksum -a sha3-256 -E base64 file.dat
```

`-E` akzeptiert `bin`, `dec`, `oct`, `hex`, `hex-uppercase`, `base16`, `base32`,
`base32-nopadding`, `base32hex`, `base32hex-nopadding`, `base64`, `base64-nopadding`,
`base64url`, `base64url-nopadding`, `z-base-32`, `z85` und `bb`/`bubblebabble`.
`jacksum -h -E` dokumentiert jede davon.

<a name="input"></a>

# 2. Andere Eingaben als Dateien

Jacksum hasht nahezu jede Eingabe: Dateien, Dateibäume, Kommandozeilenargumente, einfache
Zeichenketten, kodierte Zeichenketten, die Konsole, die Standardeingabe, NTFS-ADS, Pipes, Sockets,
Doors, Partitionen und Datenträger.

<a name="input_stdin"></a>

## Standardeingabe und Pipes

Ein einzelner Bindestrich als Dateiname bedeutet "von der Standardeingabe lesen".

```
echo -n "Hello World" | jacksum -V summary -
```

Berechnet einen SHA3-256-Hashwert von stdin. `-V summary` lohnt sich hier, weil es zeigt, wie viele
Bytes tatsächlich gelesen wurden. Beachten Sie, dass sich `echo` auf verschiedenen Plattformen
unterschiedlich verhält — die Option `-q` (siehe unten) ist der plattformunabhängige Weg.

```
printf "Hello World\r\n" | jacksum -
```

`printf` ist in GNU/Linux-Shells wie `bash` und `zsh` eingebaut und verhält sich vorhersehbarer als
`echo`.

```
jacksum -a md5 -
```

Berechnet den MD5-Hashwert einer im Terminal eingetippten Eingabe. Beenden Sie die Eingabe unter
GNU/Linux und macOS mit Strg+D, unter Windows mit Strg+Z.

```
cat fat.iso | jacksum -
```

Gibt den SHA3-256 einer Binärdatei unter GNU/Linux und macOS aus. Verwenden Sie unter Microsoft
Windows `type` anstelle von `cat`.

<a name="input_sequences"></a>

## Sequenzen auf der Kommandozeile

`-q` (`--quick`) verarbeitet eine Sequenz und beendet sich. Das optionale Präfix `<type>` sagt
Jacksum, wie es sie interpretieren soll; ohne Präfix wird die Sequenz als Hex erwartet.

```
jacksum -q txt:"The quick brown fox jumps over the lazy dog"
```

Unformatierter Text, interpretiert im Standardzeichensatz der Plattform. `\n` zählt als zwei
Zeichen (Backslash und "n"), nicht als eines.

```
jacksum -q txtf:"Hallo Welt\r\n"
```

Formatierter Text, immer als UTF-8 interpretiert, mit den Escape-Sequenzen
`\t \n \r \" \' \\ \xHH`.

```
jacksum -a crc32 -q 48656C6C6F20576F726C6421
jacksum -a crc32 -q hex:48,65,6C,6C,6F,20,57,6F,72,6C,64,21
```

Eine Hex-Sequenz — hier die Bytes von `Hello World!`. Werte dürfen durch Kommas oder Leerzeichen
getrennt werden oder auch gar nicht, solange die 8-Bit-Grenzen eindeutig sind.

```
jacksum -q bin:1100101,1100111,100000,110100,110010,1010
jacksum -q dec:"101 103 32 52 50 10"
jacksum -q oct:145,147,40,64,62,12
jacksum -q base32:MFXHG53FOIQGS4ZAGQZA
jacksum -q base64:dGhlIGFuc3dlciBpcyA0Mg==
jacksum -q base64url:wN7K_g
jacksum -q z-base-32:cfz8g75fqeog1h3ygo3y
jacksum -q "z85:vqZdgwPw]cB09p{"
```

Binäre, dezimale, oktale, Base32-, Base64-, Base64url-, z-base-32- und Z85-Eingabe.

```
jacksum -q file:myfile.img
```

Liest alle Bytes einer Datei in den Speicher und hasht sie. Begrenzt auf 128 MiB, weil dieser Modus
auch den Algorithmus-Finder versorgt (siehe [unten](#find_algorithm)), der E/A vermeiden muss. Um
große Dateien zu hashen, übergeben Sie sie stattdessen als normale Programmargumente.

```
jacksum -a crc64 -q txt:
```

Berechnet einen CRC-64 einer leeren Zeichenkette. Das Ergebnis ist dasselbe wie für eine Datei mit
einer Größe von 0 Bytes.

```
jacksum -V summary -q txtf:abc\n -F "#ALGONAME(#SEQUENCE) = #HASH" -x
```

Weil `#SEQUENCE` und `-V summary` gesetzt sind, sehen Sie die Eingabe als Hex **und** die Anzahl
der gelesenen Bytes — nützlich, wenn Sie nicht sicher sind, was Ihre Shell übergeben hat.

```
jacksum -q readline -F "#HASH msg=#MESSAGE msglen=#LENGTH hex=#SEQUENCE{hex}"
```

Liest eine Zeile von der Konsole und gibt das Getippte dabei aus. Praktisch zum Untersuchen von
Zeichenketten und Kodierungen. `-q password` macht dasselbe ohne Ausgabe des Getippten (siehe
[Reproduzierbare, eindeutige, sichere Passwörter](#beyond_passwords)).

<a name="input_filelist"></a>

## Dateilisten

```
jacksum --file-list filelist.txt
```

Liest für jeden Dateinamen in `filelist.txt` die Datei und berechnet die Hashwerte. Solche Listen
werden unter GNU/Linux und Unix von `find` erzeugt, in `cmd` von `dir /b`, in der PowerShell von
`dir -n` — oder von Jacksum selbst mit `--style files-only`.

```
jacksum --file-list filelist.txt *.mp3 *.info myfolder
```

Eine Dateiliste und normale Programmargumente können beliebig kombiniert werden.

```
chcp 65001 & echo "a filename that contains unicode chars" | jacksum --utf8 --file-list - --file-list-format ssv
```

Schalten Sie in der Windows-Shell `cmd` die Codepage auf UTF-8 um und leiten Sie Dateinamen per Pipe
in Jacksum. `--file-list-format ssv` bedeutet space separated values (Namen, die Leerzeichen
enthalten, werden in doppelte Anführungszeichen eingeschlossen), und `--utf8` bringt Jacksum dazu,
die Namen in UTF-8 zu lesen.

<a name="input_stringlist"></a>

## Zeichenkettenlisten

```
jacksum -a sha3-256 --string-list words.txt
271878f8a927b4566ac951fc815b18dfad8d0302d61d11d80cbe15b7a3a056af alpha
f0277d92062bd9a41dd26cddbaf2c41d576cf7b0173cbe96c23d5f5a4f92cc8f beta
6dfbbc6ef6895dcd07e69effe2a7486bccd7a75609f39c08e7b3a55d399d3955 gamma
```

Hasht jede **Zeile** von `words.txt` als Zeichenkette, anstatt die Zeilen als Dateinamen zu
behandeln. Siehe auch [Zeichenketten finden, die zu einem Hashwert passen](#find_strings) und
[Große Pseudozufallszahlen](#beyond_random).

<a name="output"></a>

# 3. Ausgabe und Formate

Jacksum unterstützt 18 vordefinierte Styles zum Lesen und Schreiben von Prüflisten, 17 Kodierungen,
6 Zeitstempelformate und ein vollständig anpassbares Ausgabeformat.

<a name="output_styles"></a>

## Vordefinierte Styles

`--style` (auch `-C`/`--compat`) setzt den Algorithmus, die Kodierung und das Layout in einem Zug,
und es dient zugleich als **Parser-Definition**, wenn Sie eine Liste wieder einlesen (siehe
[Abschnitt 4](#verify)). Alle Beispiele verwenden dieselbe Datei, damit Sie die Formen vergleichen
können:

```
jacksum -a sha3-256 ubuntu-22.04-desktop-amd64.iso
c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43 ubuntu-22.04-desktop-amd64.iso
```

Standard-Style.

```
jacksum -a sha3-256 --style linux ubuntu-22.04-desktop-amd64.iso
c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43 *ubuntu-22.04-desktop-amd64.iso
```

GNU/Linux-Style, wie er von `sha256sum` und Verwandten erzeugt wird. Der `*` markiert den
Binärmodus.

```
jacksum -a sha3-256 --style bsd ubuntu-22.04-desktop-amd64.iso
SHA3-256 (ubuntu-22.04-desktop-amd64.iso) = c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43
```

BSD-Style.

```
jacksum -a sha3-256 --style openssl ubuntu-22.04-desktop-amd64.iso
SHA3-256(ubuntu-22.04-desktop-amd64.iso)= c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43
```

OpenSSL-Style, wie er von `openssl dgst` erzeugt wird.

```
jacksum -a sha3-256 --style solaris-digest ubuntu-22.04-desktop-amd64.iso
(ubuntu-22.04-desktop-amd64.iso) = c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43
```

Style von Solaris' `digest -v`.

```
jacksum --style sfv -O list.sfv *
```

Simple-File-Verificator-Format — CRC-32 in Hex-Großbuchstaben, Dateiname zuerst.

```
jacksum -a sha3-256 --style hdb --no-path ./malware/
```

Format der Hash-Datenbank von ClamAVs `sigtool` (`hash:filesize:filename`).

Der vollständige Satz lautet `bsd`, `bsd-r`, `fciv`, `files-only`, `full`, `gnu-linux` (Alias
`linux`), `hdb`, `hexhashes-only`, `openssl-dgst` (Alias `openssl`), `openssl-dgst-r`, `sfv`,
`sizes-and-names`, `solaris-digest`, `solaris-digest-v`, `timestamps-and-names`, `without-hashes`,
`without-sizes`, `without-timestamps`. Sie können auch eigene schreiben — siehe
[File Format of Styles](https://github.com/jonelo/jacksum/wiki/File-Format-of-Styles).

Styles und `-a` lassen sich kombinieren: wenn ein Style einen benutzerdefinierten Algorithmus
unterstützt, können Sie das Layout beibehalten und den Algorithmus austauschen.

```
jacksum -a sha3-256 --style sfv .
```

SHA3-256-Hashwerte im altbekannten SFV-Layout.

<a name="output_formats"></a>

## Benutzerdefinierte Formate

`-F` (`--format`) gibt Ihnen die volle Kontrolle. Die wichtigsten Platzhalter sind `#HASH` (Alias
`#CHECKSUM`, `#DIGEST`, `#FINGERPRINT`), `#HASHES`, `#ALGONAME`, `#ALGONAMES`, `#FILENAME`,
`#FILESIZE`, `#TIMESTAMP`, `#SEQUENCE`, `#LENGTH`, `#SEPARATOR` und `#QUOTE`. `jacksum -h -F`
dokumentiert alle davon.

Jeder Platzhalter nimmt Modifikatoren in geschweiften Klammern auf: einen **Index** (`{0}`, `{1}`,
... oder `{i}` zum Iterieren), einen **Algorithmusnamen** (`{sha1}`), eine **Kodierung**
(`{base64}`) sowie `{uppercase}` / `{name}`.

```
jacksum -a crc32c+sha-256+sha3-256 -F "#ALGONAME{i,uppercase} (#FILENAME) = #HASH{i,base64-nopadding}" .
CRC32C (./kali-linux-2023.1-installer-amd64.iso) = dUWxuQ
SHA-256 (./kali-linux-2023.1-installer-amd64.iso) = RuBXOaILKdtgyh//LpBoXqYyBxwxSpwkFtfEas7ye/A
SHA3-256 (./kali-linux-2023.1-installer-amd64.iso) = ffPkMr8uVPCO5GIHm8YpAbmaOVeBMaxvdLPI/N4NkbE

CRC32C (./ubuntu-22.04.2-desktop-amd64.iso) = hIXQsw
SHA-256 (./ubuntu-22.04.2-desktop-amd64.iso) = uY2slAqCsRDmJlynjRMg8fcQOGHpIqoaVOQgJobpu9M
SHA3-256 (./ubuntu-22.04.2-desktop-amd64.iso) = bvOhwtwckCQuzgm4LLEJoqPbjcbWSRSuNUOcyY4/1L0
```

`{i}` iteriert: eine Zeile pro Algorithmus, pro Datei.

```
jacksum -a crc32c+sha-256+sha3-256 -F "#ALGONAMES{uppercase} (#FILENAME) = #HASHES{base64-nopadding}" .
CRC32C,SHA-256,SHA3-256 (./kali-linux-2023.1-installer-amd64.iso) = dUWxuQ,RuBXOaILKdtgyh//LpBoXqYyBxwxSpwkFtfEas7ye/A,ffPkMr8uVPCO5GIHm8YpAbmaOVeBMaxvdLPI/N4NkbE
```

Die Plural-Platzhalter `#ALGONAMES`/`#HASHES` setzen alles in eine Zeile, durch Kommas getrennt.

```
jacksum -a crc32c+sha-256+sha3-256 -F "#ALGONAMES{uppercase} (#FILENAME) = #HASH{base64-nopadding}" .
CRC32C,SHA-256,SHA3-256 (./kali-linux-2023.1-installer-amd64.iso) = dUWxuUbgVzmiCynbYMof/y6QaF6mMgccMUqcJBbXxGrO8nvwffPkMr8uVPCO5GIHm8YpAbmaOVeBMaxvdLPI/N4NkbE
```

Das einfache `#HASH` ohne Index ist die **Verkettung** aller drei Hashwerte, als ein Wert kodiert.
Das ist es, was eine Integritätsprüfung über eine einzige Zeichenkette möglich macht.

```
jacksum -a sha1+sha1+sha3-256 -s \n \
        -F "#ALGONAME{0}/hex: #HASH{0,hex} #FILENAME{name}#SEPARATOR#ALGONAME{1}/base32: #HASH{1,base32} #FILENAME{name}#SEPARATOR#ALGONAME{2}/base64: #HASH{2,base64} #FILENAME{name}#SEPARATOR" \
        *.txt
```

Sie wollen Hex **und** Base32 für SHA-1 und Base64 für SHA3-256? Benennen Sie `sha1` zweimal. Jede
Textdatei wird dennoch nur einmal gelesen, und die Engine von SHA-1 läuft dennoch nur einmal pro
Datei.

```
jacksum -a blake2b+sha3-512 -q txtf:123456789\x0a -E hex -g 1 -F "$(cat template.txt)"
```

`-F` akzeptiert eine mehrzeilige Vorlage. Wenn `template.txt` Folgendes enthält

```
INPUT:
    hex:     #SEQUENCE{hex}
    base32:  #SEQUENCE{base32}
    base64:  #SEQUENCE{base64}
OUTPUT of #ALGONAME{i}:
    hex:     #HASH{i,base16}
    base32:  #HASH{i,base32}
    base64:  #HASH{i,base64}
```

werden sowohl die Eingabe als auch beide Hashwerte jeweils in drei Kodierungen ausgegeben.

<a name="output_misc"></a>

## Zeitstempel, Trenner, Pfade und Gruppierung

```
jacksum -a crc32 -t default .
909783072 6 20260816195904048 a.txt
```

`-t` fügt den Zeitstempel der Datei hinzu. Neben `default` gibt es `iso8601`, `unixtime` und frei
formulierte Muster.

```
jacksum -a sha1 -s "\t" -t "EEE, MMM d, yyyy 'at' h:mm a" .
```

Ein eigenes Zeitstempelmuster, mit dem Tabulator als Feldtrenner (`-s`).

```
jacksum -a sha256 -P / -F "<a href=\"#FILENAME\">#HASH</a><br>" mp4s
```

`-P` erzwingt das Pfadtrennzeichen, sodass Sie auch unter Windows Schrägstriche erhalten.

```
jacksum -a none -q "txt:Hello World" -F "#SEQUENCE" -E hex -g 1
48 65 6c 6c 6f 20 57 6f 72 6c 64
```

`-g <count>` gruppiert die kodierten Bytes; `-G <char>` ändert den Gruppentrenner.

<a name="output_files"></a>

## Wohin die Ausgabe geht

```
jacksum -a sha3-256 -o hashes.list /data
```

Schreibt die Ausgabe nach `hashes.list`. Weil der Pfad mit `/` begann, sind die Pfade in der Liste
absolut. `-o` weigert sich, eine vorhandene Datei zu überschreiben; `-O` überschreibt.

```
jacksum -a sha3-256 -o hashes.list data
```

Dasselbe, aber die Pfade werden relativ gespeichert, weil der Pfad nicht mit `/` begann.

```
jacksum -a blake3+ -E base64 -t iso8601 -O hashes.list data
```

Das Suffix `+` am Algorithmus fügt der Ausgabe die Dateigröße hinzu. Zusammen mit `-t iso8601`
enthält die Liste Hashwert, Größe und Zeitstempel — und genau das ermöglicht es einer späteren
Prüfung, eine geänderte Datei von einer lediglich mit einem neuen Zeitstempel versehenen zu
unterscheiden.

```
jacksum --header -a sha3-256 --style linux -O log4j.hashes --no-path .
```

`--header` stellt einen Kommentarblock zur Herkunft voran. `--no-path` speichert bloße Dateinamen.

<details>
<summary>Inhalt der erzeugten Datei ...</summary>

```
#
# created by: Jacksum (https://jacksum.net, version: 4.0.0)
# invoked on JVM: OpenJDK 64-Bit Server VM (vendor: Eclipse Adoptium, version: 25.0.4+7-LTS)
# invoked on OS: Linux (arch: amd64, version: 6.8.0-51-generic)
# invoked on date: 2026-08-16T20:10:25.122+02:00
#
# invoked from: /home/user/log4j
# invocation args: --header -a sha3-256 --style linux -O log4j.hashes --no-path .
#_______________________________________________________________________________________________
df6b2529d9c3de4ad32e5adc798faaf6165fbd2f701eda7897eea5cfe2791c51 *log4j-api-2.10.0.jar
848eb8989417cf96cabb5038913705cc95e8304e3c7df2f5394d5ec15d99ac1b *log4j-api-2.11.1.jar
d82d35aae99b11d7873127014043f992d62f56947f7898ca0b0f0f06ea2e10d7 *log4j-core-2.0-beta9.jar
...
```
</details>

```
jacksum -a sha3-256 --output-file-replace-tokens -O "hashes-#ALGONAME.txt" data
```

Platzhalter im **Namen der Ausgabedatei** werden expandiert, hier zu `hashes-sha3-256.txt`.
Nützlich, wenn Sie über Algorithmen iterieren.

```
jacksum -a sha3-256 -U errors.log .
```

`-u`/`-U` leiten den Fehlerkanal in eine Datei um, sodass stdout eine saubere Hashliste bleibt.

<a name="output_othertools"></a>

## Formate anderer Werkzeuge

```
jacksum -a md5+sha1 -F "MD5-SHA1(#FILENAME)= #HASH{hex}" file
```

Erzeugt dieselbe Ausgabe wie `openssl dgst -md5-sha1 file`.

```
jacksum -a crc32c+sha-256+sha3-256 -F "#FILESIZE,#HASHES{hex},#FILENAME" .
3875536896,7545b1b9,46e05739a20b29db60ca1fff2e90685ea632071c314a9c2416d7c46acef27bf0,7df3e432bf2e54f08ee462079bc62901b99a39578131ac6f74b3c8fcde0d91b1,./kali-linux-2023.1-installer-amd64.iso
4927586304,8485d0b3,b98dac940a82b110e6265ca78d1320f1f7103861e922aa1a54e4202686e9bbd3,6ef3a1c2dc1c90242ece09b82cb109a2a3db8dc6d64914ae35439cc98e3fd4bd,./ubuntu-22.04.2-desktop-amd64.iso
```

Das hashdeep-Format (`filesize,hash1,...,hashN,filename`), aber mit modernen Algorithmen.

```
jacksum -a ed2k -F "ed2k://|file|#FILENAME{name}|#FILESIZE|#HASH{hex}|/" .
```

Erzeugt ed2k-Links.

```
jacksum -a ed2k -P / -F "<a href=#QUOTEed2k://|file|#FILENAME|#FILESIZE|#HASH{hex}|#QUOTE>#FILENAME</a>" .
```

Dasselbe als HTML. `#QUOTE` fügt ein doppeltes Anführungszeichen ein, ohne dass Sie sich darüber mit
Ihrer Shell streiten müssen.

```
jacksum -a tth+ed2k+sha1+md5 -F "magnet:?xl=#FILESIZE&dn=#FILENAME{name}&xt=urn:tree:tiger:#HASH{tth,base32}&xt=urn:ed2k:#HASH{ed2k,hex}&xt=urn:bitprint:#HASH{sha1,base32}.#HASH{tth,base32}&xt=urn:sha1:#HASH{sha1,base32}&xt=urn:md5:#HASH{md5,hex}" -
```

Erzeugt Magnet-Links. Beachten Sie, dass die Platzhalter hier **über den Algorithmusnamen**
indiziert werden, was lesbarer ist als `{0}`..`{3}`, wenn es vier davon sind.

```
jacksum -a sum_sysv -E dec -t unixtime -F "1 i #FILENAME{name} #FILESIZE #HASH #TIMESTAMP" install/*
1 i a.txt 6 542 1786903144
```

Einträge, die mit der Syntax einer `pkgmap`-Datei von Solaris 10+ kompatibel sind — nützlich, wenn
Sie einen Solaris-Patch patchen wollen.

```
jacksum -a tree:tiger -F "urn:#ALGONAME:#HASH" -q hex:
urn:tree:tiger:LWPNACQDBZRYXW3VHJVCJ64QBZNGHOHHHZWCLNQ
```

Der Wurzel-Hashwert eines Tiger Tree Hash (eine weit verbreitete Form des Merkle-Baums), hier über
eine leere Eingabe.

<a name="verify"></a>

# 4. Datenintegrität überprüfen

Datenintegrität stellt sicher, dass Daten seit ihrer Erzeugung, Übertragung oder Speicherung nicht
auf unbefugte oder versehentliche Weise verändert oder zerstört wurden und nicht verloren gegangen
sind.

Als Werkzeug für Datei-/Datenintegrität kann Jacksum Hashwerte erzeugen, speichern und vergleichen,
um Änderungen an Dateien zu erkennen. Es erkennt passende, nicht passende, fehlende und neue
Dateien.

<a name="verify_one"></a>

## Eine Datei gegen einen bekannten Hashwert prüfen

Von der Ubuntu-Website kennen wir den SHA-256-Hashwert von
`ubuntu-22.04.1-desktop-amd64.iso`. Wir erwarten, dass die Datei, die wir heruntergeladen haben,
diese Datei ist. Es gibt drei Wege, das auszudrücken.

### Mit `--expect` / `-e`

```
jacksum -a sha256 -e c396e956a9f52c418397867d1ea5c0cf1a99a49dcf648b086d2fb762330cc88d ubuntu-22.04.1-desktop-amd64.iso
    MATCH  ubuntu-22.04.1-desktop-amd64.iso (c396e956a9f52c418397867d1ea5c0cf1a99a49dcf648b086d2fb762330cc88d)

Jacksum: Expectation met.
Jacksum: 1 of the successfully read files matches the expected hash value.
```

Die einfachste Form: Sie haben einen Hashwert, Sie haben eine Datei. Wenn Sie mehrere Dateien oder
Verzeichnisse übergeben, findet Jacksum **alle** Dateien, die passen — siehe
[Abschnitt 5](#find).

### Mit `--check-line`

```
jacksum -a sha256 --check-line "c396e956a9f52c418397867d1ea5c0cf1a99a49dcf648b086d2fb762330cc88d *ubuntu-22.04.1-desktop-amd64.iso" -V nosummary
       OK  ubuntu-22.04.1-desktop-amd64.iso
```

Wenn Sie einen kompletten **Datensatz** aus Hashwert und Datei haben, den Sie von einer Website
kopiert haben, übergeben Sie ihn wortgetreu. Sie brauchen hier kein `--style linux`, weil der
Standardparser das Linux-Format versteht.

```
jacksum -a sha256 --style bsd --check-line "SHA-256 (ubuntu-22.04.1-desktop-amd64.iso) = c396e956a9f52c418397867d1ea5c0cf1a99a49dcf648b086d2fb762330cc88d" -V noinfo,nosummary
       OK  ubuntu-22.04.1-desktop-amd64.iso
```

Derselbe Datensatz im BSD-Style — jetzt wird `--style` benötigt, weil es den Parser auswählt.

### Über eine Pipe in `--check-file -`

```
echo c396e956a9f52c418397867d1ea5c0cf1a99a49dcf648b086d2fb762330cc88d *ubuntu-22.04.1-desktop-amd64.iso | jacksum -a sha256 --check-file - -V nosummary
       OK  ubuntu-22.04.1-desktop-amd64.iso
```

`-` als Prüfliste bedeutet "lies die Liste von der Standardeingabe". Beachten Sie, dass sich `echo`
nicht auf allen Plattformen gleich verhält — unter Windows müssen Sie die Anführungszeichen und die
Leerzeichen weglassen, sonst gibt `echo` diese Zeichen in die Pipe weiter. `--check-line` vermeidet
das Problem vollständig.

<a name="verify_list"></a>

## Eine Hashliste erzeugen und später überprüfen

Es ist gute Praxis, den Algorithmus und die Kodierung explizit zu benennen oder einen Style zu
verwenden, der alle drei Dinge festlegt: Algorithmus, Kodierung und Layout. Ein Style enthält
außerdem die Regex-Informationen, die der Parser braucht, um die Liste wieder einzulesen.

```
jacksum -a sha3-256 --style linux -O checkfile .
```

Die Liste erzeugen.

```
jacksum -a sha3-256 --style linux --check-file checkfile .
```

Die Liste überprüfen. Dass Sie denselben Ordner erneut übergeben, ist es, was Jacksum in die Lage
versetzt, auch Dateien zu melden, die seit dem Schreiben der Liste **hinzugefügt** wurden.

```
jacksum -a blake3+ -E base64 -t iso8601 --check-file hashes.list data
```

Wenn die Liste mit `-a blake3+ -E base64 -t iso8601` erzeugt wurde, müssen Sie beim Wiedereinlesen
denselben Algorithmus, dieselbe Kodierung und dasselbe Zeitstempelformat angeben.

<a name="verify_states"></a>

## OK, FAILED, MISSING und NEW

```
jacksum -a sha3-256 --check-file file.hashes .
```

<details>
<summary>Ergebnis ...</summary>

```
Jacksum: Error: drei: does not exist.
  MISSING  drei
   FAILED  eins
       OK  zwei
       OK  vier
      NEW  ./file.hashes
      NEW  ./fünf

Jacksum: total lines in check file: 4
Jacksum: improperly formatted lines in check file: 0
Jacksum: properly formatted lines in check file: 4
Jacksum: ignored lines (empty lines and comments): 0
Jacksum: correctness of check file: 100.00 %

Jacksum: matches (OK): 2
Jacksum: mismatches (FAILED): 1
Jacksum: new files (NEW): 2
Jacksum: missing files (MISSING): 1
Jacksum: strict check: FAILED

Jacksum: total files read successfully: 3
Jacksum: total bytes read: 20
Jacksum: total bytes read (human readable): 20 bytes
Jacksum: total file read errors: 1

Jacksum: elapsed time: 40 ms
```
</details>

Beachten Sie, dass die Prüfliste selbst als `NEW` erscheint, weil sie innerhalb des geprüften
Verzeichnisses liegt. Speichern Sie sie außerhalb des Baums (`-O ../checkfile`), wenn Sie das stört
— für eine strikte Prüfung ist es zwingend erforderlich.

<a name="verify_strict"></a>

## Strikte Überprüfung (Audit)

Eine strikte Prüfung garantiert, dass keine Datei verändert, keine Datei hinzugefügt und keine Datei
entfernt wurde — und sie lässt den **Exit-Code** diese Einschätzung widerspiegeln.

```
jacksum -a sha3-256 --check-file ../.SHA3 --check-strict data
Jacksum: strict check: PASSED
```

Damit die Einschätzung aussagekräftig ist, muss alles Folgende zutreffen:

- Die Dateien/Verzeichnisse dürfen **nicht** weggelassen werden, sonst können neue Dateien nicht
  erkannt werden.
- Es müssen dieselben sein, die beim Erzeugen der Liste angegeben wurden.
- Die Prüfliste darf **nicht** innerhalb davon liegen, sonst wird sie selbst als neu oder verändert
  gemeldet.
- `--list-filter` muss auf `all` bleiben (der Standardwert), weil nichts herausgefiltert werden
  darf.
- `-V` muss `summary` enthalten (der Standardwert bei `--check-file`).
- Fügen Sie unter Windows `--scan-ntfs-ads` hinzu, weil ein Alternate Data Stream hinzugefügt worden
  sein könnte, der sonst unbemerkt bliebe.
- Fügen Sie unter GNU/Linux und Unix `--scan-all-unix-file-types` hinzu, um nicht-reguläre Dateien
  zu erkennen, die hinzugefügt wurden.

```
jacksum -a sha256 --check-file ../.SHA256 --check-strict --scan-ntfs-ads .
```

Ein vollständiges Audit unter Windows.

<a name="verify_filter"></a>

## Den Bericht filtern

```
jacksum -a sha3-256 --check-file my.hashes --list --list-filter bad -V nosummary .
```

Listet nur die Dateinamen fehlerhafter Dateien auf (fehlgeschlagen **oder** fehlend) — genau die
Form, die Sie in einem Cronjob wollen, der still bleiben soll, wenn alles in Ordnung ist.

```
jacksum -a sha3-256 --check-file my.hashes --list --list-filter new -V nosummary data
data/3.txt
```

Nur Dateien, die seit dem Schreiben der Liste hinzugefügt wurden.

```
jacksum --check-file my.hashes --list-filter none,missing,new .
```

`--list-filter` nimmt eine durch Kommas getrennte Menge auf, sodass Sie genau die Zustände
kombinieren können, die Sie interessieren.

<a name="verify_foreign"></a>

## Listen überprüfen, die von anderen Werkzeugen erzeugt wurden

Jacksum liest nicht nur seine eigene Ausgabe, sondern auch die vieler anderer Werkzeuge. `--style`
wählt den Parser aus; `--charset-check-file` den Zeichensatz, falls die Liste nicht UTF-8 ist.

```
jacksum --check-file /var/lib/dpkg/info/sudo.md5sums --style linux -a md5 --path-relative-to /
```

Überprüft ein Debian-Paket. Debian liefert vorab berechnete MD5-Listen in `/var/lib/dpkg/info/`
aus, in denen die Pfade relativ zum Wurzelordner gespeichert sind, `--path-relative-to /` macht sie
also wieder absolut. Um ein Paket einschließlich aller seiner Abhängigkeiten zu überprüfen,
installieren Sie das Paket `debsums` und rufen Sie `rdebsums` auf.

```
jacksum -a sha3-256 --check-file list --ignore-timestamps --ignore-sizes .
```

Vergleicht nur Hashwerte, obwohl die Liste auch Größen und Zeitstempel enthält.

```
jacksum -a sha3-256 --check-file list --ignore-lines-starting-with-string ";" .
```

Überspringt Kommentarzeilen in einem fremden Format (kurz `-I`). `--ignore-empty-lines` und
`--ignore-hashes` existieren aus demselben Grund.

<a name="verify_exitcodes"></a>

## Exit-Codes

| Code | Bedeutung |
|---|---|
| `0` | alles ist in Ordnung |
| `1` | mindestens eine Abweichung während der Überprüfung, oder `--exact -h <word>` hat nichts gefunden |
| `>1` | Parameter-, `.jacksum`- oder E/A-Fehler (ein fehlgeschlagener `--check-strict`-Aufruf endet mit `6`) |

```
jacksum -a sha3-256 --check-file ../.SHA3 --check-strict -V nosummary data || echo "audit failed"
```

Das ist es, was Jacksum in Skripten und Cronjobs verwendbar macht.

<a name="find"></a>

# 5. Objekte finden

Weil ein Hashwert Inhalte unabhängig vom Dateinamen identifiziert, kann Jacksum als Suchmaschine
über Inhalte verwendet werden: Duplikate finden, eine bekannte Datei finden, wo auch immer sie sich
versteckt, Dateien finden, die **nicht** auf einer freigegebenen Liste stehen, oder den Algorithmus
hinter einem Hashwert finden.

<a name="find_duplicates"></a>

## Alle Duplikate einer Datei finden

```
jacksum -a sha3-256 -e c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43 .
    MATCH  ./ubuntu-22.04-desktop-amd64.iso (c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43)
    MATCH  ./ubuntu-22.04-desktop-amd64 (1).iso (c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43)

Jacksum: Expectation met.
Jacksum: 2 of the successfully read files match the expected hash value.
```

```
jacksum -a md5 -E hex -F "#FILENAME" -e 9666f5e2632d05b806e782d7d50855e8 .
```

Dasselbe, aber nur die Dateinamen werden ausgegeben — bereit, per Pipe an `rm` oder `ln` übergeben
zu werden.

<a name="find_byhash"></a>

## Eine Datei anhand ihres Hashwerts finden

Wenn Sie den Hashwert einer Datei kennen, können Sie die Datei finden, selbst wenn Sie ihren Namen
nicht kennen. Suchen wir unter macOS nach Satoshi Nakamotos Bitcoin-Whitepaper:

```
jacksum -a sha256 -x -e b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553 --threads-reading max .
```

<details>
<summary>Ergebnis ...</summary>

```
    MATCH  /System/Library/Image Capture/Devices/VirtualScanner.app/Contents/Resources/simpledoc.pdf (b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553)

Jacksum: Expectation met.
Jacksum: 1 of the successfully read files matches the expected hash value.
```
</details>

`--threads-reading max` zahlt sich auf SSDs aus, wo nicht der Datenträger der Flaschenhals ist.

<a name="find_wantedlist"></a>

## Dateien anhand einer Liste bekannter Hashwerte finden

`--wanted-list` gleicht jede Datei gegen eine ganze **Menge** bekannter Hashwerte ab. Der klassische
Anwendungsfall ist die Jagd nach verwundbaren Bibliotheken.

Bauen Sie zuerst die Liste aus Kopien der betroffenen Artefakte. SHA3-256 ist hier eine gute Wahl:
eine moderne, nicht gebrochene Hashfunktion minimiert Kollisionen und damit falsch positive
Treffer.

```
jacksum --header -a sha3-256 --style linux -O log4j.hashes --no-path .
```

Durchsuchen Sie dann die gesamte Maschine nach allem, was passt:

```
jacksum --wanted-list log4j.hashes --style linux --threads-reading max -V summary,noinfo /
```

<details>
<summary>Ergebnis ...</summary>

```
    MATCH  /opt/serverapp/log4j.jar (log4j-core-2.12.0.jar)

Jacksum: total lines in check file: 42
Jacksum: improperly formatted lines in check file: 0
Jacksum: properly formatted lines in check file: 33
Jacksum: ignored lines (empty lines and comments): 9
Jacksum: correctness of check file: 100.00 %

Jacksum: total number of wanted hashes: 33
Jacksum: files matching wanted hashes (MATCH): 1
Jacksum: files not matching wanted hashes (NO MATCH): 252298

Jacksum: total files read successfully: 252299
Jacksum: total bytes read: 117670015750
Jacksum: total bytes read (human readable): 109 GiB, 602 MiB, 892 KiB, 774 bytes
Jacksum: total file read errors: 0

Jacksum: elapsed time: 8 min, 38 s, 215 ms
```
</details>

Beachten Sie den Namen in Klammern: der Treffer wird mit dem Namen aus der **Fahndungsliste**
gemeldet, sodass Sie erfahren, *welches* bekannte Artefakt Sie gerade gefunden haben, ganz
unabhängig davon, wie es auf der Platte umbenannt wurde.

Siehe auch
[CVE-2021-44832: Find vulnerable .jar files using Jacksum](https://loefflmann.blogspot.com/2022/06/CVE-2021-44832%20Find%20vulnerable%20.jar%20files%20using%20Jacksum%203.4.0%20or%20later.html)

<a name="find_negative"></a>

## Negativer Abgleich

```
jacksum -a sha3-256 --wanted-list known.hashes --wanted-list-filter negative -V nosummary .
 NO MATCH  rogue.bin (e86c0e881ea1ba2245f051f6e18aa4aff92ec00784386a95cec08cc2a890fbf3)
```

Dreht die Frage um: melde alles, was **nicht** auf der freigegebenen Liste steht. So finden Sie die
eine Datei in einem Deployment, die sich niemand erklären kann. `--wanted-list-filter` (Alias
`--match-filter`) akzeptiert `match`/`positive` (der Standardwert) und `nomatch`/`negative`.

<a name="find_strings"></a>

## Zeichenketten finden, die zu einem Hashwert passen

```
jacksum -a sha3-256 --string-list words.txt -e f0277d92062bd9a41dd26cddbaf2c41d576cf7b0173cbe96c23d5f5a4f92cc8f
f0277d92062bd9a41dd26cddbaf2c41d576cf7b0173cbe96c23d5f5a4f92cc8f beta
```

Die Kombination von `--string-list` mit `-e` durchsucht eine Wortliste anstelle eines Dateisystems —
nützlich, wenn Sie wissen, dass ein Hashwert über eine kurze, erratbare Zeichenkette gebildet wurde.

<a name="find_malware"></a>

## Schadsoftware anhand von Hashwerten finden

Um Schadsoftware anhand von Hashwerten zu identifizieren, brauchen Sie zuerst Hashwerte von
Schadsoftware.

**Besorgen Sie sich eine vorhandene Datenbank.** Skripte wie
[dumahadaba](https://github.com/jonelo/dumahadaba) wandeln eine öffentliche
Schadsoftware-Datenbank in eine reine Textdatei um, die Jacksum weiterverarbeiten kann.

**Oder bauen Sie Ihre eigene.** Wenn alle Samples in `./malware` liegen, speichern Sie die Hashwerte
im `hdb`-Format, das [ClamAVs sigtool](https://docs.clamav.net/manual/Signatures.html#hash-based-signatures)
verwendet (`hash:filesize:filename`). `--no-path` behält nur die Dateinamen:

```
jacksum -a sha256 --style hdb --no-path -O malware.sha256.hdb ./malware/
```

Gehen Sie dann damit auf die Jagd:

```
jacksum -a sha256 --style hdb --wanted-list malware.sha256.hdb .
```

<a name="find_algorithm"></a>

## Den Algorithmus finden, der einen Hashwert erzeugt hat

Da Jacksum so viele Algorithmen unterstützt, kann es auch rückwärts arbeiten: sind die Daten und der
Hashwert gegeben, findet es den Algorithmus, mit einer schnellen und schlauen Brute-Force-Suche.

```
jacksum -a unknown:16 -q hex:050000 -E hex -e d893
```

<details>
<summary>Ergebnis ...</summary>

```
Trying 14 algorithms with a width of 16 bits that are supported by Jacksum 4.0.0 ...

Trying 31 CRC algorithms with a width of 16 bits by testing against well known CRCs ...
crc:16,1021,FFFF,false,false,FFFF
    --> CRC-16/GENIBUS

Trying all CRC algorithms with a width of 16 bits by brute force (be patient!) ...
crc:16,1021,FFFF,false,false,FFFF
crc:16,37D2,FFFF,true,false,FFFF
crc:16,3E2D,0000,true,false,FFFF
crc:16,4175,FFFF,true,false,FFFF
crc:16,4A5B,FFFF,true,true,0000
crc:16,5A41,FFFF,true,false,FFFF
crc:16,5C63,FFFF,true,true,0000
crc:16,6287,FFFF,true,true,0000
crc:16,649C,0000,false,true,FFFF
crc:16,6D55,FFFF,true,true,0000
crc:16,75AC,FFFF,true,false,FFFF
crc:16,7D64,FFFF,false,false,FFFF
crc:16,81A6,FFFF,true,false,FFFF
crc:16,B9F9,FFFF,true,true,0000
crc:16,C3D6,FFFF,false,false,FFFF
crc:16,D436,0000,true,false,FFFF
crc:16,D6D2,0000,false,true,FFFF
crc:16,DA9C,FFFF,true,false,FFFF
crc:16,E03E,FFFF,false,false,FFFF
crc:16,F701,FFFF,true,false,FFFF


Jacksum: algorithms tested: 1048622
Jacksum: algorithms found: 21

Jacksum: elapsed time: 5 s, 217 ms
```
</details>

Jacksum hat in etwa 5 Sekunden mehr als eine Million Algorithmen getestet und 21 gefunden, die
denselben Wert erzeugen. Testen Sie mit mehr und/oder längeren Ein-/Ausgabe-Paaren, um die Auswahl
einzugrenzen. Der wahrscheinlichste Kandidat wird mit einem Namen ausgegeben, wenn es ein bekannter
CRC ist — hier CRC-16/GENIBUS.

Sobald Sie ihn identifiziert haben, verwenden Sie die gefundene CRC-Definition auf Ihren eigenen
Daten:

```
jacksum -a crc:16,1021,FFFF,false,false,FFFF -E hex -q txt:"Hello World"
```

`<bits>` in `unknown:<bits>` darf alles von 1 bis 1024 sein. Siehe auch
[Investigating Algorithms](https://github.com/jonelo/jacksum/wiki/Investigating-Algorithms).

<a name="hmac"></a>

# 6. HMAC

Ein HMAC (Keyed-Hash Message Authentication Code, RFC 2104) belegt nicht nur, dass Daten unverändert
sind, sondern auch, dass sie von jemandem kommen, der ein gemeinsames Geheimnis kennt. Jacksum
unterstützt HMAC für 492 seiner 586 Algorithmen — `jacksum --hmacs` listet sie auf.

<a name="hmac_calculate"></a>

## Einen HMAC berechnen

```
jacksum -a hmac:sha256 -k txt:secret -q txt:"Hello World"
82ce0d2f821fa0ce5447b21306f214c99240fecc6387779d7515148bbdd0c415
```

Das Präfix `hmac:` (oder `hmac-`) vor einem beliebigen unterstützten Algorithmus schaltet Jacksum in
den HMAC-Modus; `-k` liefert den Schlüssel.

```
jacksum -a hmac:sha3-256 -k file:key.txt message.txt
```

Ein HMAC über eine Datei. Alles, was Sie über `-a`, `-E`, `-F`, `--style` und die
Verzeichnis-Traversierung wissen, funktioniert weiterhin.

<a name="hmac_key"></a>

## Woher der Schlüssel kommt

`-k` akzeptiert genau dieselben Formen wie `-q`: `txt:`, `txtf:`, `hex:`, `bin:`, `dec:`, `oct:`,
`base32:`, `base32hex:`, `base64:`, `base64url:`, `z-base-32:`, `z85:`, `file:`, `readline` und
`password`.

```
jacksum -a hmac:sha256 -k file:key.txt message.txt
jacksum -a hmac:sha256 -k password message.txt
Key (echo off):
```

In Mehrbenutzerumgebungen sind nur `file:<file>`, `readline` und `password` ratsam — jede andere
Form hinterlässt das Geheimnis in Prozesslisten und in Ihrer Shell-History. Um Shoulder Surfing zu
vermeiden, bevorzugen Sie `file:` oder `password` gegenüber `readline`.

<a name="hmac_truncated"></a>

## Verkürzte HMACs

```
jacksum -a hmac:sha3-256:160 -k txt:test --info
```

`hmac:<algo>:<bits>` verkürzt das Ergebnis auf `<bits>`. Das Verkürzen ist eine legitime und
verbreitete Praxis — es begrenzt, wie viel Sie vom zugrunde liegenden Hashwert offenlegen. `--info`
zeigt Ihnen die resultierenden Parameter.

<a name="hmac_verify"></a>

## Einen HMAC überprüfen

```
jacksum -a hmac:sha256 -q password -k password -e 60273a1e778ed009a6fb32fa11dbb16f905148fc2ec84a67f8a3b3a6cabaa9b7
```

`-e` funktioniert auch im HMAC-Modus, wobei sowohl der Schlüssel als auch die Nachricht von der
Konsole gelesen werden.

<a name="hmac_interop"></a>

## Interoperabilität mit anderen Werkzeugen

Derselbe HMAC, ausgedrückt in fünf Ökosystemen:

```
Jacksum:  jacksum -a hmac:<algo>[:<bits>] -k <key> <message>
OpenSSL:  openssl dgst -<algo> -mac hmac -macopt hexkey:<key> <message>
Python:   hmac.new(<key>, <message>, hashlib.<algo>).hexdigest()
PHP:      hash_hmac('<algo>', '<message>', '<key>');
```

`jacksum -h hmac:` gibt die vollständige Kompatibilitätsliste aus.

<a name="beyond"></a>

# 7. Mehr als Hashing

<a name="beyond_passwords"></a>

## Reproduzierbare, eindeutige, sichere Passwörter

Sie können Jacksum als Passwortgenerator verwenden, der das Passwort für eine Website bei Bedarf neu
erzeugt. Der Vorteil: Sie müssen sich nur **ein** Master-Passwort merken, und trotzdem bekommt jedes
Konto ein anderes, starkes. Kein Passwortmanager ist beteiligt, nichts wird auf der Platte
gespeichert, und wenn das Passwort einer Website abfließt, bleibt das Master-Passwort geheim.

```
jacksum -a hmac:sha3-512:240 -8 -k password -q password -E base64
Key (echo off): <Ihr Master-Passwort>
Password: <die Adresse der Website><Ihr Master-Passwort>
```

Das Rezept hinter dieser Kommandozeile:

1. Wende einen **HMAC** an, was vorab berechnete Rainbow-Tabellen nutzlos macht.
2. Wähle eine kryptografische, nicht gebrochene Hashfunktion mit langer Ausgabe (SHA3-512).
3. **Verkürze** sie (`:240`), damit die Website nie den vollständigen Hashwert erfährt.
4. Der HMAC-Schlüssel (`-k`) ist Ihr Master-Passwort.
5. Die Nachricht (`-q`) kombiniert die Adresse der Website mit dem Master-Passwort.
6. Kodiere mit Base64. 240 Bit = 30 Byte ist ein Vielfaches von 3, es gibt also kein Padding, und
   Sie erhalten ein sauberes 40 Zeichen langes Passwort mit Groß- und Kleinbuchstaben, Ziffern und
   manchmal einem Sonderzeichen.

Sowohl `-k password` als auch `-q password` lesen von der Konsole mit abgeschalteter Ausgabe des
Getippten. In diesem Modus weigert sich Jacksum, das Geheimnis im Klartext auszugeben, selbst wenn
Sie mit `-F` danach fragen, es weigert sich, Pipes und Umleitungen zu akzeptieren (eine Konsole ist
erforderlich), und es löscht das Passwort anschließend aus dem Speicher. Verwenden Sie `-o`/`-O`,
wenn Sie auch den Hashwert nicht auf dem Bildschirm sehen möchten.

Die vollständige Begründung finden Sie unter <https://bit.ly/secure-passwords-with-jacksum>.

<a name="beyond_random"></a>

## Große Pseudozufallszahlen

```
jacksum -h | sort | uniq > strings.txt
jacksum -a hmac:sha256:64 -k txt:run42 --string-list strings.txt -F "#HASH" -E dec
482543917333917802
2258785350139739212
2746638413740063416
```

Ein Generator für viele große Pseudozufallszahlen: nehmen Sie eine Menge eindeutiger Zeichenketten,
wenden Sie einen HMAC an, verkürzen Sie ihn auf die gewünschte Bitbreite und kodieren Sie dezimal.
Der HMAC-Schlüssel wirkt als **Startwert (Seed)** für den gesamten Aufruf, die Folge ist also
reproduzierbar; jede Zeichenkette wirkt als individueller Startwert für eine Zahl.

<a name="beyond_encodings"></a>

## Umwandlungen von Kodierungen

Mit `-a none` hört Jacksum vollständig mit dem Hashen auf und wird zu einem Umwandler zwischen Hex,
Binär, Dezimal, Oktal, Base32/64, Z85 und BubbleBabble:

```
jacksum -a none -q hex:CAFE -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex: #SEQUENCE{hex-uppercase}"
bin: 1100101011111110, dec: 51966, oct: 0145376, hex: CAFE
```

Die vollständige Sammlung finden Sie auf der Seite
[Jacksum Hacks](JACKSUM_HACKS_de.md).

<a name="crcs"></a>

# 8. CRCs anpassen

Über die CRCs hinaus, die mit Namen ausgeliefert werden, können Sie mit Jacksum eigene mit einer
Breite von 1 bis 64 Bit definieren. Siehe auch
[Working with CRCs](https://github.com/jonelo/jacksum/wiki/Working-with-CRCs).

<a name="crcs_6"></a>

## 6 Parameter

Jacksum unterstützt den Quasi-Standard mit dem Namen "Rocksoft (tm) Model CRC Algorithm", der einen
CRC durch Breite, Polynom, Init-Wert, refIn, refOut und xorOut beschreibt.

```
jacksum -a crc:32,1EDC6F41,FFFFFFFF,true,true,FFFFFFFF -x -q txt:123456789
e3069283 9
```

Der Castagnoli-CRC-32, ausgeschrieben.

```
jacksum -a crc32c -x -q txt:123456789
e3069283 9
```

Dasselbe über seinen eingebauten Alias.

<a name="crcs_7"></a>

## 7 Parameter

Ein erweitertes Modell mit einem 7. Parameter definiert CRCs, die die **Länge** der Nachricht
einbeziehen. Ist er `true`, wird das höchstwertige Oktett der Länge zuerst an die Update-Methode des
CRC übergeben; ist er `false`, geht das niedrigstwertige Oktett zuerst.

```
jacksum -a crc:32,04C11DB7,0,false,false,FFFFFFFF,false -x -q txt:123456789
377a6011 9
```

Der CRC-Algorithmus nach POSIX 1003.2.

```
jacksum -a cksum -x -q txt:123456789
377a6011 9
```

Dasselbe über seinen Alias.

<a name="crcs_8"></a>

## 8 Parameter

Ein 8. Parameter XOR-verknüpft den Längenwert, bevor er in den CRC einbezogen wird.

```
jacksum -a crc:32,04C11DB7,0,true,true,0,true,CC55CC55 -x -q txt:123456789
afcbb09a 9
```

Die Ausgabe des Befehls `sum` von [Plan 9](https://en.wikipedia.org/wiki/Plan_9_from_Bell_Labs).

```
jacksum -a sum_plan9 -x -q txt:123456789
afcbb09a 9
```

Dasselbe über seinen Alias.

<a name="performance"></a>

# 9. Performance und Steuerung der Traversierung

Jacksum hat zwei unabhängige Nebenläufigkeits-Subsysteme: eines, das mehrere Algorithmen über
dieselben Daten parallel berechnet, und eines, das viele Dateien parallel durchläuft und liest.
Siehe
[Multi-Core Processor Support](https://github.com/jonelo/jacksum/wiki/Multi-Core-Processor-Support).

```
jacksum -a crc32c+md5+sha256 -V all -r 1 --threads-reading 4 --threads-hashing max --header -F "#HASHES #FILENAME" .
```

Drei Algorithmen in einem Durchgang, eine Verzeichnisebene tief, 4 Lese-Threads, so viele
Hashing-Threads, wie es Kerne gibt.

```
jacksum -a sha3-256 --threads-reading max /
```

`max` ist die richtige Einstellung auf SSDs und NVMe, wo mehrere gleichzeitige Lesezugriffe
schneller sind als einer. Bei rotierenden Platten belassen Sie es beim Standardwert — das Suchen
kostet Sie mehr, als die Parallelität einbringt.

```
jacksum -A -a md5 -V summary bigfile.iso
```

`-A` fordert die alternative Implementierung eines Algorithmus an, wo es eine gibt, und
`-V summary` meldet die verstrichene Zeit — so vergleichen Sie die beiden.

```
jacksum -a sha3-256 -r 2 .
```

`-r <depth>` begrenzt die Rekursionstiefe.

```
jacksum -a sha3-256 -d -f .
```

`-d` folgt keinen symbolischen Links auf Verzeichnisse, `-f` folgt keinen symbolischen Links auf
Dateien. Jacksum erkennt Dateisystem-Zyklen in beiden Fällen.

```
jacksum -a sha3-256 --scan-all-unix-file-types .
jacksum -a sha3-256 --scan-ntfs-ads .
```

Standardmäßig liest Jacksum reguläre Dateien, Verzeichnisse und symbolische Links. Diese Optionen
erweitern das auf alle Unix-Dateitypen bzw. auf NTFS Alternate Data Streams — beides relevant für
eine [strikte Prüfung](#verify_strict).

<a name="info"></a>

# 10. Informationen sammeln

<a name="info_one_algo"></a>

## Über einen Algorithmus

```
jacksum -h blake2b
```

Gibt den BLAKE2b-Abschnitt der Manpage aus, einschließlich einer Kompatibilitätsliste, die zeigt,
wie derselbe Wert mit anderen Werkzeugen erzeugt wird.

```
jacksum -a blake2b --info
```

Gibt Details zur Implementierung aus: Hashlänge in Bits und Bytes, Blockgröße,
HMAC-Kompatibilität, ob der Algorithmus als gebrochen gilt, den Lawineneffekt, den relativen
Geschwindigkeitsrang und ob eine alternative Implementierung existiert.

```
jacksum -a md5 --info -V details
```

`-V details` ergänzt die Begründung hinter der Einschätzung `broken:`.

<details>
<summary>Ergebnis (Auszug) ...</summary>

```
  Compatibility:
    HMAC:                                 true

  Security:
    broken:                               yes
      yes, 2004: identical-prefix collisions can be computed in
      seconds and chosen-prefix collisions in hours on a standard PC;
      the attack has been demonstrated against real X.509
      certificates (2008) and was abused by the Flame malware (2012);
      the preimage resistance is not broken (best attack 2^123.4),
      but MD5 must not be used for signatures
      see also https://eprint.iacr.org/2004/199.pdf

  Speed:
    relative rank:                        42/586
```
</details>

<a name="info_crc"></a>

## CRC-Parameter untersuchen

Bei einem CRC liefert `--info` zusätzlich das Polynom als mathematischen Ausdruck und in normaler,
umgekehrter und Koopman-Darstellung, dasselbe für das reziproke Polynom sowie die
Jacksum-CRC-Definition, die Sie an `-a` übergeben können.

```
jacksum -a crc32c --info
```

oder gleichwertig, indem alle Parameter ausgeschrieben werden:

```
jacksum -a crc:32,1EDC6F41,FFFFFFFF,true,true,FFFFFFFF --info
```

<details>
<summary>Ergebnis ...</summary>

```
  Algorithm:
    name:                                 crc32c

  Hash length:
    bits:                                 32
    bytes:                                4
    nibbles:                              8

  Compatibility:
    HMAC:                                 false

  Security:
    broken:                               n/a

  CRC parameters:
    width (in bits):                      32
    polynomial [hex]:                     1edc6f41
    init [hex]:                           ffffffff
    refIn [boolean]:                      true
    refOut [boolean]:                     true
    xorOut [hex]:                         ffffffff
    Jacksum CRC algo def:                 crc:32,1EDC6F41,FFFFFFFF,true,true,FFFFFFFF

  Polynomial representations:
    mathematical:                         x^32 + x^28 + x^27 + x^26 + x^25 + x^23 + x^22 + x^20 + x^19 + x^18 + x^14 + x^13 + x^11 + x^10 + x^9 + x^8 + x^6 + 1
    normal/MSB first [binary]:            00011110110111000110111101000001
    normal/MSB first [hex]:               1edc6f41
    reversed/LSB first [binary]:          10000010111101100011101101111000
    reversed/LSB first [hex]:             82f63b78
    Koopman [binary]:                     10001111011011100011011110100000
    Koopman [hex]:                        8f6e37a0

  Reciprocal polynomial representations (the reciprocal poly has a similar error detection strength):
    mathematical:                         x^32 + x^26 + x^24 + x^23 + x^22 + x^21 + x^19 + x^18 + x^14 + x^13 + x^12 + x^10 + x^9 + x^7 + x^6 + x^5 + x^4 + 1
    normal/MSB first [binary]:            00000101111011000111011011110001
    normal/MSB first [hex]:               5ec76f1
    reversed/LSB first [binary]:          10001111011011100011011110100000
    reversed/LSB first [hex]:             8f6e37a0
    Koopman [binary]:                     10000010111101100011101101111000
    Koopman [hex]:                        82f63b78

  Avalanche effect:
    input length in bytes:                9
    input length in bits:                 72
    hash calculations:                    73
    input [hex]:                          313233343536373839
    input [bin]:                          001100010011001000110011001101000011010100110110001101110011100000111001
    avalanche min effect:                 34.38 %
    avalanche avg effect:                 50.26 %
    avalanche max effect:                 71.88 %

  Speed:
    relative rank:                        1/586

  Alternative/secondary implementation:
    has been requested:                   false
    is available and would be used:       false
```
</details>

```
jacksum -a crc64_xz --info
```

Dasselbe für CRC-64/xz.

<a name="info_many_algos"></a>

## Über viele Algorithmen

```
jacksum -a all --list
```

Gibt jede unterstützte Algorithmus-ID aus, die an `-a` übergeben werden kann. Jacksum 4.0.0
unterstützt **586**; die kommentierte Liste finden Sie auf der Seite
[Algorithmen](ALGORITHMS_de.md).

```
jacksum -a all --list --verbose summary
```

Ergänzt die Anzahl.

```
jacksum -a all:skein --list
jacksum -a all:128 --list
```

Filtert nach Teilzeichenkette bzw. nach Ausgabebreite in Bits.

```
jacksum -a all:8 --list --info
```

Jeder 8-Bit-Algorithmus, jeweils mit seinem vollständigen `--info`-Block.

```
jacksum --hmacs
```

Listet alle Algorithmen auf, für die ein HMAC gebildet werden kann.

```
jacksum -a all:crc -F "#ALGONAME{i},#SEQUENCE,#HASH{i,hex}" -q txt:0123456789
```

Erzeugt Testvektoren als CSV (`name,input as hex,hash as hex`) für eine ganze Algorithmenfamilie auf
einmal.

<details>
<summary>Ergebnis (Jacksum 4.0.0) ...</summary>

```
crc8,30313233343536373839,45
crc16,30313233343536373839,443d
crc16_minix,30313233343536373839,f833
crc24,30313233343536373839,d08ea3
crc32,30313233343536373839,a684c7c6
crc32_mpeg2,30313233343536373839,694f1b1f
crc32_bzip2,30313233343536373839,96b0e4e0
crc32_fddi,30313233343536373839,9d14a594
crc32_ubi,30313233343536373839,597b3839
crc32_php,30313233343536373839,e0e4b096
crc32c,30313233343536373839,280c069e
crc32_go-koopman,30313233343536373839,b29f672b
crc64,30313233343536373839,469959388a5beffe
crc64_ecma,30313233343536373839,2a71ab4164c3bbe8
crc64_go-iso,30313233343536373839,b966f5c775a41001
crc64_xz,30313233343536373839,2765cf2c7f12731e
crc64_nvme,30313233343536373839,15f9b1ee4cfd9c1d
crc82_darc,30313233343536373839,0011481c15b81e5180135c
fcs16,30313233343536373839,3c16
```
</details>

<a name="info_styles"></a>

## Über Styles und Kodierungen

```
jacksum --style bsd --info
```

Gibt alle Eigenschaften der Kompatibilitätsdefinition für `bsd` aus — der Ausgangspunkt, wenn Sie
eine eigene schreiben wollen.

```
jacksum -h --style
jacksum -h -E
jacksum -h -F
jacksum -h parameters
```

Die vollständige Dokumentation zu Styles, Kodierungen, der Formatsprache und den
Programmparametern.

<a name="info_program"></a>

## Über das Programm

```
jacksum -v
jacksum --version
Jacksum 4.0.0
```

```
jacksum --info
```

Version, primäre IDs und Beschreibungen aller unterstützten Algorithmen, die Anzahl der Algorithmen,
unterstützte Zeichensätze, Systemeigenschaften, verfügbare Prozessoren und viel mehr. Hängen Sie
diese Ausgabe an Support-Anfragen an.

```
jacksum --license
jacksum --copyright
```

Der vollständige Lizenztext bzw. die Copyrights und Lizenzinformationen für jeden Softwareanteil,
den Jacksum lizenziert hat.

<a name="info_help"></a>

## In der Hilfe navigieren

Die Manpage ist im Jar eingebaut, und `-h` ist eine Suche darüber.

```
jacksum -h
jacksum -h | more
```

Die gesamte Manpage, optional Seite für Seite (`less` funktioniert unter macOS und Linux, `more`
funktioniert überall).

```
jacksum -h examples
jacksum -h exa
```

Ein ganzer Abschnitt. Abschnittsnamen dürfen abgekürzt werden, solange sie eindeutig bleiben —
`jacksum -h ex` gibt sowohl EXIT STATUS als auch EXAMPLES aus, weil beide mit `ex` beginnen.

```
jacksum -h synopsis
jacksum -h options
jacksum -h "operating modes"
jacksum -h "option "
```

Weitere Abschnitte. Der letzte gibt sowohl OPTION TYPES als auch OPTION SUPPORT MATRIX aus, weil
beide mit `option ` beginnen.

```
jacksum -h whirlpool
```

Informationen über jeden Algorithmus, dessen Name mit `whirlpool` beginnt.

```
jacksum -h -h
jacksum -h --path
jacksum -h -
```

Hilfe zu einer einzelnen Option, zu jeder Option, die mit `--path` beginnt, und zu allen Optionen
(jede Option beginnt mit einem Minuszeichen).

```
jacksum --exact -h --path
```

`--exact` macht aus der Präfixsuche eine exakte Übereinstimmung — dieser Aufruf gibt nichts aus,
weil es keine Option gibt, die genau `--path` heißt.

---

**Siehe auch:**
[Manpage](https://github.com/jonelo/jacksum/wiki/Manpage) ·
[Algorithmen](ALGORITHMS_de.md) ·
[Funktionen](FEATURES_de.md) ·
[Working with CRCs](https://github.com/jonelo/jacksum/wiki/Working-with-CRCs) ·
[Investigating Algorithms](https://github.com/jonelo/jacksum/wiki/Investigating-Algorithms) ·
[Jacksum Hacks](JACKSUM_HACKS_de.md) ·
[File Format of Styles](https://github.com/jonelo/jacksum/wiki/File-Format-of-Styles) ·
[Multi-Core Processor Support](https://github.com/jonelo/jacksum/wiki/Multi-Core-Processor-Support)

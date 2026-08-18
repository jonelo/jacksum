*Diese Seite auf Englisch: [FEATURES.md](FEATURES.md)*

**Inhaltsverzeichnis**
 - [Auf einen Blick](#at_a_glance)
 - [Anwendungsfälle](#use_cases)
 - [Algorithmen](#algorithms)
   - [Standardalgorithmen](#standard_algorithms)
   - [Angepasste Algorithmen](#customized_algorithms)
   - [Auswahl der Algorithmen](#algorithm_selection)
 - [Eingabe](#input)
   - [Dateien, Dateibäume und Dateilisten](#input_files)
   - [Zeichenketten, Sequenzen und Datenströme](#input_sequences)
   - [Plattformspezifische Eingaben](#input_platform)
   - [Zeichensätze, Unicode und BOM](#input_charsets)
   - [Korrektheit der Dateibehandlung](#input_correctness)
 - [Ausgabe und Formate](#output)
   - [Vordefinierte Styles](#output_styles)
   - [Benutzerdefinierte Formate](#output_formats)
   - [Kodierungen](#output_encodings)
   - [Zeitstempel](#output_timestamps)
   - [Pfade und Dateinamen](#output_paths)
   - [Wohin die Ausgabe geht](#output_files)
   - [Zeichensätze, Unicode und BOM](#output_charsets)
 - [Überprüfung der Datenintegrität](#verification)
 - [Objekte finden](#finding)
 - [Performance](#performance)
 - [Plattformen und Integration](#platforms)
   - [Betriebssysteme und Architekturen](#platforms_os)
   - [Zusammenspiel mit anderen Werkzeugen](#platforms_tools)
   - [Benutzeroberflächen](#platforms_ui)
 - [Informationen, Untersuchung und Lernen](#information)
 - [Unterstützung für Entwickler](#developer)
 - [Freie Software, ausgereift und stabil](#free_software)

Die nachfolgenden Funktionen beschreiben **Jacksum 4.0.0**. Die verbindliche Beschreibung jeder
Option finden Sie in der [Manpage](https://github.com/jonelo/jacksum/wiki/Manpage) (`jacksum -h`).

<a name="at_a_glance"/>

# Auf einen Blick

  - **586 Algorithmen**: kryptografische Hashfunktionen, Hashbäume, nicht-kryptografische
    Hashfunktionen, CRCs und klassische Prüfsummen
  - **HMAC**-Unterstützung für **492** dieser Algorithmen, mit optionaler Verkürzung
  - **Anpassbare CRCs** von 1 bis 64 Bit Breite
  - **70 Kommandozeilenoptionen** zur Steuerung des Verhaltens von Jacksum
  - **Plattformübergreifende Ausführbarkeit mit identischem Verhalten**
  - **Multithreading** für parallele Hashberechnungen und für parallele Datenzugriffe, um
    Mehrkernprozessoren und schnelle SSD-Speicher auszunutzen
  - **Rekursive Traversierung** mit Tiefensteuerung, Regeln zum Folgen symbolischer Links auf
    Dateien und/oder Ordner sowie Erkennung von Dateisystem-Zyklen
  - **19 vordefinierte Styles** zum Lesen und Schreiben von Prüflisten und Fahndungslisten, dazu
    benutzerdefinierte Styles
  - **17 Kodierungen** zur Darstellung von Hashwerten
  - **6 vordefinierte Formate** zur Darstellung von Datei-Zeitstempeln, dazu frei definierbare
    Formate
  - **170+ Zeichensätze**, um Prüflisten und Fahndungslisten korrekt zu lesen und zu schreiben
  - **Vollständig anpassbares Ausgabeformat** bei Bedarf
  - **10.000+ Zeilen Manpage** mit Beschreibungen, Beispielen und Kompatibilitätslisten für alle
    unterstützten Algorithmen

<a name="use_cases"/>

# Anwendungsfälle

Jacksum deckt viele Arten von Anwendungsfällen ab, in denen Hashwerte sinnvoll sind. Jeder davon
ist ein Betriebsmodus, den Jacksum automatisch auswählt, abhängig von den Optionen und Parametern,
die Sie setzen; siehe auch den Abschnitt OPERATING MODES der
[Manpage](https://github.com/jonelo/jacksum/wiki/Manpage).

  - **Berechnen von Hashwerten/Fingerabdrücken**
    - von nahezu jeder Eingabe: Dateien, Dateibäume, Kommandozeilenargumente, einfache
      Zeichenketten, kodierte Zeichenketten, Konsoleneingabe, Standardeingabe, NTFS-ADS, Pipes,
      Sockets, Doors, Partitionen und Datenträger
    - Erzeugen und Speichern von Prüflisten und Fahndungslisten
    - Erzeugen großer Pseudozufallszahlen
    - Erzeugen reproduzierbarer, eindeutiger, sicherer Passwörter aus einem Master-Passwort

  - **Überprüfen der Datenintegrität**
    - Finden aller OK-, FAILED-, MISSING- und NEW-Dateien
    - Durchführen einer strikten Integritätsprüfung, auch Audit genannt

  - **Finden von Objekten**
    - Finden von Zeichenketten, die zu einem Hashwert passen
    - Finden von Dateien anhand ihrer Fingerabdrücke (positiver Abgleich)
    - Finden von Dateien, die nicht zu bestimmten Fingerabdrücken passen (negativer Abgleich)
    - Finden aller Duplikate einer Datei anhand ihres Hashwerts
    - Finden des/der Algorithmus/-en, der/die einen bestimmten Hashwert erzeugt hat/haben

  - **Sammeln von Informationen**
    - Sammeln detaillierter Informationen zu Hash-Algorithmen, einschließlich der Angabe, ob ein
      Algorithmus gebrochen ist
    - Untersuchen von CRC-Polynomen
    - Untersuchen von HMAC-Parametern

Ausgearbeitete Beispiele zu all diesen Modi finden Sie unter
[Jacksum am Beispiel](https://github.com/jonelo/jacksum/blob/main/docs/EXAMPLES_de.md),
mit `jacksum -h examples` und im
[Cheat Sheet](https://github.com/jonelo/jacksum/wiki/Cheat-Sheet).

<a name="algorithms"/>

# Algorithmen

<a name="standard_algorithms"/>

## Standardalgorithmen

  - Jacksum unterstützt **586 Standardalgorithmen**: kryptografische Hashfunktionen, Hashbäume,
    nicht-kryptografische Hashfunktionen, CRCs und klassische Prüfsummen. Dazu gehören nationale
    und internationale Standards, alle Kandidaten der Endrunde des NIST-SHA-3-Wettbewerbs,
    Lightweight Cryptography, eXtendable Output Functions (XOF), gebrochene Algorithmen für
    Bildungszwecke und zur Rückwärtskompatibilität sowie die Prüfsummen und CRCs, die in
    Betriebssystemen, Softwareprodukten und Protokollen zu finden sind.
    Die vollständige Liste finden Sie unter [Algorithmen](https://github.com/jonelo/jacksum/wiki/Algorithms).
  - Einige Algorithmen haben eine **alternative, reine Java-Implementierung**, die mit der Option
    `-A` ausgewählt werden kann, während die Standardimplementierung diejenige der Java-API ist,
    weil sie üblicherweise vom JVM-Hersteller optimiert wurde.
  - Die Pseudo-Algorithmen `none` und `read` erlauben es Ihnen, nur mit Datei-Metadaten zu
    arbeiten: `none` liest den Dateiinhalt nicht einmal, `read` liest ihn, aber keiner von beiden
    berechnet einen Hashwert.

<a name="customized_algorithms"/>

## Angepasste Algorithmen

  - **Verkettete Algorithmen**: Algorithmen können mit dem Plus-Zeichen verkettet werden
    (z. B. `-a ascon-hash+sha256+crc32c`), um viele Algorithmen **in einem Durchgang** über die
    Daten zu berechnen. Das Ergebnis wird entweder als ein kombinierter Wert oder als getrennte
    Werte zurückgegeben. Damit können Sie außerdem eine Kombination von Algorithmen mit einem
    vordefinierten Standard-Style verwenden, der eigentlich nur für einen Algorithmus entworfen
    wurde.
  - **HMAC**: der Keyed-Hash Message Authentication Code wird für 492 Algorithmen unterstützt
    (z. B. `-a hmac:sha256`), der Schlüssel wird mit `-k` angegeben und kann aus einer Datei, von
    der Konsole oder wie ein Passwort verdeckt von der Konsole gelesen werden.
  - **Verkürzter HMAC**: die HMAC-Ausgabe kann verkürzt werden (z. B. `-a hmac:sha256:64`).
  - **Angepasste CRCs**: der "Rocksoft (tm) Model CRC Algorithm" (die 6 Parameter `width`,
    `poly`, `init`, `refIn`, `refOut`, `xorOut`) wird mit einer Breite von 1 bis 64 Bit
    vollständig unterstützt. Ein erweitertes Modell mit 7 oder 8 Parametern (`incLen`, `xorLen`)
    erlaubt es Ihnen, CRCs anzugeben, die die Länge der Eingabedaten einbeziehen.

<a name="algorithm_selection"/>

## Auswahl der Algorithmen

  - Wählen Sie einen, einige, viele oder alle Algorithmen für die Hashberechnung, die
    Integritätsprüfung oder das Sammeln von Informationen aus.
  - Geben Sie Algorithmen manuell an, oder filtern Sie sie über die Länge des Message Digests
    (`-a all:256`) oder über eine Teilzeichenkette ihres Namens (`-a all:sha`). Filter können mit
    dem Plus-Zeichen kombiniert werden.
  - Lassen Sie Jacksum den Algorithmus für Sie finden, wenn Sie sowohl die Eingabe als auch den
    Hashwert kennen (`-a unknown:<bits>`).

<a name="input"/>

# Eingabe

<a name="input_files"/>

## Dateien, Dateibäume und Dateilisten

  - Verarbeitet Verzeichnisse **rekursiv** und erlaubt es Ihnen, die Tiefe zu begrenzen (`-r`).
  - **Erkennt Dateisystem-Zyklen** und vermeidet Endlosschleifen.
  - Erlaubt es Ihnen, auf allen Betriebssystemen zu steuern, wie **symbolische Links** auf Dateien
    (`-f`) und/oder auf Verzeichnisse (`-d`) behandelt werden sollen.
  - **Unterstützung von Wildcards**, abhängig von der verwendeten Shell.
  - Liest die zu verarbeitenden Dateien aus einer **Dateiliste** (`-L`), entweder im Format ein
    Dateiname pro Zeile oder als durch Leerzeichen getrennte Werte (`--file-list-format`). Solche
    Listen können von `find`, `dir /b`, `dir -n` oder von Jacksum selbst (`--style files-only`)
    erzeugt werden.

<a name="input_sequences"/>

## Zeichenketten, Sequenzen und Datenströme

  - Hasht Daten, die **direkt auf der Kommandozeile** angegeben werden (`-q`). Die Sequenz kann
    einfacher Text (`txt:`), formatierter Text (`txtf:`), eine binäre, dezimale, oktale oder
    hexadezimale Sequenz (`bin:`, `dec:`, `oct:`, `hex:`), eine Base32-, Base32hex-, Base64-,
    Base64url- oder z85-kodierte Zeichenkette, der Inhalt einer Datei (`file:`, max. 128 MiB) oder
    eine auf der Konsole eingetippte Eingabe sein, entweder sichtbar (`readline`) oder verdeckt
    (`password`).
  - Hasht **Zeichenketten, die in einer Textdatei gespeichert sind**, eine Zeichenkette pro Zeile
    (`--string-list`).
  - Liest vom **Standardeingabestrom**, wenn der Dateiname ein Bindestrich (`-`) ist. Der Strom
    wird in der Ausgabe `<stdin>` genannt; `--legacy-stdin-name` stellt den historischen Namen `-`
    wieder her.

<a name="input_platform"/>

## Plattformspezifische Eingaben

  - **Jedes Betriebssystem**: Dateien, Dateibäume, Dateilisten, Kommandozeilenargumente,
    Standardeingabe, Datenträger und Partitionen.
  - **Unix-artige Betriebssysteme** (einschließlich macOS, BSD und GNU/Linux): blockorientierte
    Geräte, zeichenorientierte Geräte, benannte Pipes (FIFOs), Sockets und Sparse-Dateien.
    Verwenden Sie `--scan-all-unix-file-types`, um diese Dateitypen bei einer rekursiven
    Traversierung einzubeziehen.
  - **Solaris**: Doors.
  - **Microsoft Windows**: Partitionen (`\\.\c:`), verborgene Partitionen, die standardmäßig nicht
    eingebunden sind, etwa die Wiederherstellungs- oder die EFI-Partition
    (`\\?\Volume{...}\`), physische Datenträger, CD-ROMs, DVDs, RAM-Disks und
    **NTFS Alternate Data Streams** auf Dateien (`my-file.txt:secret:$DATA`). Verwenden Sie
    `--scan-ntfs-ads`, um NTFS-ADS bei einer rekursiven Traversierung einzubeziehen.

<a name="input_charsets"/>

## Zeichensätze, Unicode und BOM

  - Vollständige Unicode-Unterstützung für Dateinamen von Eingabedateien.
  - Erlaubt es Ihnen, den Zeichensatz getrennt anzugeben für Prüflisten
    (`--charset-check-file`), Fahndungslisten (`--charset-wanted-list`), Dateilisten
    (`--charset-file-list`), Zeichenkettenlisten (`--charset-string-list`) und die Konsole
    (`--charset-console`). Alle Zeichensätze, die das JDK bereitstellt, werden unterstützt,
    einschließlich aller verbreiteten Unicode-fähigen Zeichensätze wie UTF-8, UTF-16, UTF-16BE,
    UTF-16LE, UTF-32, UTF-32BE, UTF-32LE und GB18030.
  - Ignoriert eine optionale **Byte-Order-Mark (BOM)** in der Eingabe, wenn eine BOM beim gewählten
    Zeichensatz erlaubt, aber nicht erforderlich ist.

<a name="input_correctness"/>

## Korrektheit der Dateibehandlung

  - Behandelt Sonderzeichen in Dateinamen korrekt, z. B. wenn ein Dateiname unter GNU/Linux mit
    einem Leerzeichen endet oder wenn er Backslashes oder Zeilenumbrüche enthält.
  - Behandelt die erlaubte Maximallänge von Dateinamen korrekt, z. B. 255 Zeichen für einen
    Dateinamen auf NTFS-Dateisystemen von Microsoft Windows.
  - Behandelt die erlaubte Maximallänge von Pfaden korrekt, z. B. 32.767 Zeichen für den gesamten
    Pfad auf NTFS-Dateisystemen von Microsoft Windows.
  - Jacksum ist **Large-File-fähig**: es kann Dateigrößen bis zu 8 Exbibyte
    (= 8.000.000.000 Gibibyte) verarbeiten, vorausgesetzt, dass Ihr Betriebssystem bzw. Ihr
    Dateisystem ebenfalls Large-File-fähig ist.

<a name="output"/>

# Ausgabe und Formate

<a name="output_styles"/>

## Vordefinierte Styles

Jacksum kann nicht nur seine eigene Ausgabe lesen und schreiben, es ist auch in der Lage, Ausgaben
zu lesen, die von anderen Werkzeugen erzeugt wurden, und Ausgaben zu schreiben, die andere
Werkzeuge lesen können. 19 vordefinierte Styles stehen zur Verfügung (Option `--style`, auch `-C`,
auch `--compat`):

| `<style>` | Zweck |
| --- | --- |
| `bsd`, `bsd-r` | getaggtes und ungetaggtes BSD-Format, wird unter GNU/Linux auch von `md5sum --tag` usw. sowie von `cksum` der GNU Core Utilities 9.0 und später erzeugt |
| `gnu-linux` | das klassische `md5sum`/`sha256sum`-Format |
| `openssl-dgst`, `openssl-dgst-r` | das Format von `openssl dgst` (OpenSSL 3.x und neuer) |
| `openssl111-dgst` | das Format von `openssl dgst` mit den Algorithmusnamen von OpenSSL 1.1.1 und älter |
| `sfv` | das Simple-File-Verification-Format |
| `fciv` | das Format des "File Checksum Integrity Verifier" von Microsoft |
| `solaris-digest`, `solaris-digest-v` | das Format des `digest`-Befehls von Solaris |
| `hdb` | das Hash-Datenbank-Format, das von ClamAVs `sigtool` verwendet wird |
| `full` | Hashwert, Zeitstempel, Dateigröße und Dateiname |
| `without-hashes`, `without-sizes`, `without-timestamps` | full, jeweils ohne einen der Prüfwerte |
| `sizes-and-names`, `timestamps-and-names`, `files-only` | leichtgewichtige Listen |
| `hexhashes-only` | nur Hashwerte, z. B. für Fahndungslisten, die von Dritten stammen |

  - Alle Styles außer `hexhashes-only` können zum Schreiben und Lesen von **Prüflisten**
    verwendet werden (Option `-c`), einschließlich der hashfreien, die dann die Existenz, die
    Größe und/oder den Zeitstempel von Dateien prüfen.
  - Alle Styles, die einen Hashwert enthalten, können als **Fahndungslisten** verwendet werden
    (Option `-w`), was `files-only`, `without-hashes`, `sizes-and-names` und
    `timestamps-and-names` ausschließt.
  - Jeder Style funktioniert mit **jedem Algorithmus**, den Jacksum unterstützt, ebenso mit
    verketteten Algorithmen. Damit können Sie zum Beispiel SHA3-256-Hashwerte im altbekannten
    SFV-Format ausgeben.
  - `jacksum --style <style> --info` gibt alle Eigenschaften eines vordefinierten Styles aus.
  - Sie können **eigene Styles** in einer kleinen Property-Datei definieren und sie sowohl zum
    Schreiben als auch zum Parsen verwenden, siehe
    [File Format of Styles](https://github.com/jonelo/jacksum/wiki/File-Format-of-Styles).
    Ein eigener Style kann dem Header auch einen Text Ihrer Wahl voranstellen
    (`formatter.leadingHeader`).

<a name="output_formats"/>

## Benutzerdefinierte Formate

  - Verwenden Sie die umfassende Formatoption `-F`/`--format`, um genau die Ausgabe zu erhalten,
    die Sie benötigen. Mehr als 30 Token werden unterstützt, darunter `#ALGONAME`, `#HASH`,
    `#HASHES`, `#FILENAME`, `#FILESIZE`, `#TIMESTAMP`, `#SEQUENCE`, `#SEPARATOR` und `#QUOTE`.
    Token können einen einzelnen Algorithmus aus einer Verkettung auswählen (`#HASH{2}`) sowie
    eine explizite Kodierung (`#HASH{base64}`).
  - Erzeugen Sie **ed2k-Links**, **Magnet-Links** und das **pkgmap**-Format von Solaris.
  - Ein **Header** mit den Metadaten des Aufrufs kann ausgegeben werden (`--header`), und er kann
    für jene Styles unterdrückt werden, die standardmäßig einen ausgeben (`--no-header`). Das
    Kommentarzeichen des Headers folgt `-I`.
  - Der **Trenner** zwischen den Ausgabefeldern ist frei wählbar (`-s`).
  - Die Bytes eines Hashwerts können zur besseren Lesbarkeit **gruppiert** und getrennt werden
    (`-g` und `-G`).

<a name="output_encodings"/>

## Kodierungen

17 Kodierungen stehen zur Darstellung von Hashwerten zur Verfügung (Option `-E`/`--encoding`):

`bin`, `dec`, `oct`, `hex` (Kleinbuchstaben), `hex-uppercase`, `base16`, `base32`,
`base32-nopadding`, `base32hex`, `base32hex-nopadding`, `base64`, `base64-nopadding`,
`base64url`, `base64url-nopadding`, `bubblebabble`, `z-base-32` und `z85`.

Dieselben Kodierungen können verwendet werden, um die Eingabesequenz der Option `-q` anzugeben.

<a name="output_timestamps"/>

## Zeitstempel

  - 6 vordefinierte Zeitstempelformate stehen zur Verfügung (Option `-t`/`--timestamp`):
    `default`, `default-utc`, `iso8601` (Alias `iso`), `iso8601utc` (Alias `iso-utc`),
    `unixtime` und `unixtime-ms`.
  - Zusätzlich dazu kann jedes Format frei definiert werden, das Javas `SimpleDateFormat`
    versteht, einschließlich der Token `#SEPARATOR` und `#QUOTE`.

<a name="output_paths"/>

## Pfade und Dateinamen

  - Pfade können weggelassen (`--no-path`), absolut ausgegeben (`--path-absolute`) oder relativ zu
    einem anderen Pfad (`--path-relative-to`) bzw. zu einem der Parameter des Aufrufs
    (`--path-relative-to-entry`) ausgegeben werden.
  - Das Pfadtrennzeichen kann ersetzt werden (`-P`), was nützlich ist, um unter Microsoft Windows
    Ausgaben zu erzeugen, die unter GNU/Linux lesbar sind, und umgekehrt.
  - **GNU-Dateinamen-Escaping** wird unterstützt und kann pro Style aktiviert oder deaktiviert
    werden (`--gnu-filename-escaping`).

<a name="output_files"/>

## Wohin die Ausgabe geht

  - Die reguläre Ausgabe geht auf die Standardausgabe oder in eine Datei (`-o`, bzw. `-O`, um eine
    vorhandene zu überschreiben). Die Ausgabedatei wird automatisch vom Berechnungsprozess
    ausgeschlossen.
  - Fehlermeldungen gehen auf die Standardfehlerausgabe oder in eine Datei (`-u`, bzw. `-U`, um
    eine vorhandene zu überschreiben).
  - Der Name der Ausgabedatei kann Token enthalten, die zur Laufzeit ersetzt werden
    (`--output-file-replace-tokens`), z. B. schreibt `-O /myisos/.#ALGONAME{uppercase}` eine Datei
    mit dem Namen `.SHA-256`.
  - Jacksum gibt einen **Exit-Code** zurück, der vom Ergebnis der Berechnung bzw. des
    Überprüfungsprozesses abhängt, damit Sie den Programmablauf in Ihren eigenen Skripten steuern
    können.

<a name="output_charsets"/>

## Zeichensätze, Unicode und BOM

  - Vollständige Unicode-Unterstützung für Dateinamen von Ausgabedateien.
  - Erlaubt es Ihnen, den Zeichensatz getrennt anzugeben für die Standardausgabe
    (`--charset-stdout`), die Standardfehlerausgabe (`--charset-stderr`), die Ausgabedatei
    (`--charset-output-file`) und die Fehlerdatei (`--charset-error-file`). `-8`/`--utf8` ist eine
    Kurzform, die UTF-8 sowohl für stdout als auch für stderr setzt.
  - Fügt der Ausgabe eine optionale **Byte-Order-Mark (BOM)** hinzu (`--bom`), wenn eine BOM beim
    gewählten Zeichensatz erlaubt, aber nicht erforderlich ist.

<a name="verification"/>

# Überprüfung der Datenintegrität

  - Jeder Algorithmus, den Jacksum unterstützt, kann zur Überprüfung der Integrität verwendet
    werden, einschließlich verketteter Algorithmen und HMACs.
  - Überprüfen gegen eine **Prüfliste** (`-c`), gegen eine **einzelne Zeile** einer solchen Liste
    (`--check-line`) oder gegen einen einzelnen **erwarteten Hashwert**, der auf der Kommandozeile
    angegeben wird (`-e`/`--expect`).
  - Erkennt **OK**-, **FAILED**-, **MISSING**- und **NEW**-Dateien.
  - **Strikter Prüfmodus**, auch **Audit** genannt (`--check-strict`): der Aufruf ist nur dann
    erfolgreich, wenn alle Dateien die Überprüfung bestehen und keine Dateien hinzugefügt oder
    entfernt wurden. Die Zusammenfassung der Überprüfung und der Exit-Code folgen diesem Ergebnis.
  - Nicht nur Hashwerte, sondern auch **Dateigrößen** (`--filesize`) und
    **Änderungszeitstempel** (`-t`) können Teil einer Prüfliste sein, sodass auch Metadaten
    überprüft werden.
  - Hashwerte, Dateigrößen und Zeitstempel, die in einer Prüfliste gespeichert sind, können
    während einer Prüfung selektiv **ignoriert** werden (`--ignore-hashes`, `--ignore-sizes`,
    `--ignore-timestamps`).
  - Die Ausgabe kann nach Ergebnis **gefiltert** werden (`--list-filter ok,failed,missing,new`
    sowie die Kurzformen `all`, `none`, `good`, `bad`), was auch dann nützlich ist, wenn Sie nur
    an der Zusammenfassung interessiert sind. Wählt der Filter weder `ok` noch `failed` aus, wird
    überhaupt nicht gehasht.
  - Robustes Parsen: Leerzeilen können ignoriert werden (`--ignore-empty-lines`), und Zeilen, die
    mit einer bestimmten Zeichenkette beginnen, werden als Kommentare behandelt (`-I`).

<a name="finding"/>

# Objekte finden

  - **Finden aller Dateien, die zu einem bestimmten Hashwert passen**, z. B. um alle Duplikate
    einer Datei zu finden (`-e <hash>`).
  - **Finden aller Dateien, die zu den Hashwerten einer vorab berechneten Fahndungsliste passen**
    (`-w <list>`), z. B. um verwundbare, illegale oder bösartige Software auf einem Computer zu
    finden, unabhängig von den tatsächlichen Dateinamen. Zusammen mit dem Style `hdb` kann eine
    ClamAV-Signaturdatenbank als Fahndungsliste verwendet werden.
  - **Finden aller Dateien, die nicht zu den Hashwerten einer Fahndungsliste passen**, indem
    `--match-filter negative` gesetzt wird. Der Filter unterstützt außerdem `match`, `nomatch`,
    `all`, `none` und `positive`.
  - **Finden von Zeichenketten, die zu einem Hashwert passen**, durch Kombination von
    `--string-list` mit `-e`.
  - **Finden des Algorithmus, der verwendet wurde**, um eine bestimmte Prüfsumme, einen CRC oder
    einen Hashwert zu berechnen, wenn sowohl die Eingabe als auch der Hashwert bekannt sind
    (`-a unknown:<bits> -q <sequence> -e <hash>`).

<a name="performance"/>

# Performance

  - Jacksum unterstützt **Multithreading** sowohl auf Mehrprozessor- als auch auf
    Mehrkern-Computersystemen.
  - **Mehrere Algorithmen**: Jacksum kann mehrere Hashwerte **gleichzeitig** berechnen
    (`--threads-hashing`), d. h. Dateien werden nur einmal gelesen und die Berechnungslast wird
    auf die verfügbaren Kerne verteilt.
  - **Mehrere Dateien**: Jacksum kann mehrere Dateien **gleichzeitig** lesen
    (`--threads-reading`), was sich besonders bei schnellen SATA-SSDs und NVMe-M.2-SSDs auszahlt.
  - Der Benutzer kann die **Anzahl der Threads** für beides **steuern**.

<a name="platforms"/>

# Plattformen und Integration

<a name="platforms_os"/>

## Betriebssysteme und Architekturen

  - Microsoft Windows (z. B. Microsoft Windows 10 und 11)
  - GNU/Linux (z. B. Ubuntu)
  - Unix (z. B. BSD-Varianten, macOS, Solaris)
  - jedes andere Betriebssystem oder jede andere Architektur mit einer OpenJDK-kompatiblen Java
    Runtime Environment (JRE) oder einem Java Development Kit (JDK), die bzw. das kompatibel ist
    mit **Java 21 LTS oder später** (OpenJDK 25 LTS oder später wird empfohlen)
  - Die unterstützten Hardware-Architekturen hängen vom Betriebssystem und vom JDK-Hersteller ab.
    Üblicherweise sind das x86 64 Bit (x64), x86 32 Bit (x86), ARM 64 Bit (AArch64 bzw. Apple
    Silicon), ARM 32 Bit (AArch32) und PPC 64 Bit (ppc64).
  - **Keine Neukompilierung erforderlich**: Jacksum ist vollständig in Java geschrieben, führen Sie
    einfach die .jar-Datei mit Ihrer JRE oder Ihrem JDK aus.
  - **Identisches Verhalten** auf allen unterstützten Plattformen.
  - 2 MiB Speicherplatz.

<a name="platforms_tools"/>

## Zusammenspiel mit anderen Werkzeugen

  - Verwenden Sie die vordefinierten Styles, um verbreitete Formate von Drittanbietern zu lesen
    und zu schreiben (GNU/Linux, BSD, SFV, FCIV, openssl, Solaris, ClamAV usw.), oder definieren
    Sie eigene.
  - Jacksum kann in **Skripten**, **Cronjobs**, **Autostart-Umgebungen** und von **KI-Agenten**
    verwendet werden, und es gibt einen Exit-Code zurück, der das Ergebnis widerspiegelt.
  - Funktioniert mit der **SendTo-Funktion** vieler Dateibrowser auf allen wichtigen
    Betriebssystemen.

<a name="platforms_ui"/>

## Benutzeroberflächen

  - **CLI**: die Kommandozeilen-Schnittstelle mit 70 Optionen.
  - **API**: Jacksum ist auch eine Bibliothek, siehe [Unterstützung für Entwickler](#developer).
  - **GUI**: [HashGarten](https://github.com/jonelo/HashGarten), ein Unterprojekt von Jacksum, ist
    eine grafische Benutzeroberfläche, die die Jacksum-API verwendet. Sie läuft auch eigenständig
    und unterstützt Drag and Drop.
  - **FBI**: die Installer der File Browser Integration integrieren sowohl Jacksum als auch
    HashGarten in Ihren bevorzugten Dateibrowser: Finder unter macOS, Datei-Explorer unter
    Microsoft Windows sowie Caja, Dolphin, elementary Files, GNOME Nautilus, Konqueror, Krusader,
    muCommander, Nemo, ROX-Filer, SpaceFM, Thunar, Xfe oder zzzFM unter GNU/Linux. Siehe
    [Jacksum for Windows](https://github.com/jonelo/jacksum-for-windows),
    [Jacksum for Linux](https://github.com/jonelo/jacksum-for-linux) und
    [Jacksum for macOS](https://github.com/jonelo/jacksum-for-macos).
  - **Docker**: [Jacksum auf Docker](https://hub.docker.com/r/jonelo/jacksum).

<a name="information"/>

# Informationen, Untersuchung und Lernen

  - `--info` gibt detaillierte Informationen über den ausgewählten Algorithmus aus:
    - die Hashlänge in Bits, Bytes und Nibbles
    - ob der Algorithmus HMAC-kompatibel ist und, bei einem HMAC, seine Parameter: die zugrunde
      liegende Hashfunktion, eine angegebene Verkürzung sowohl in Bits als auch in Bytes, die
      empfohlene Mindestverkürzung und ob der angegebene Schlüssel der Empfehlung des RFC folgt
    - seinen **Sicherheitsstatus** (`broken:` — einer von `yes`, `no`, `partly`, `depends` oder
      `n/a`), mit einer Erläuterung der Einschätzung, wenn `-V details` gesetzt ist: welche
      Sicherheitseigenschaft betroffen ist, ob ein Angriff theoretisch oder praktisch ist, das
      Jahr und eine Referenz
    - bei CRCs: die **CRC-Parameter**, den Jacksum-CRC-Definitionscode und das Polynom in seiner
      mathematischen, normalen, umgekehrten und Koopman-Darstellung, sowohl für das Polynom als
      auch für sein reziprokes Gegenstück
    - den **Lawineneffekt** (min, avg und max), berechnet über eine Sequenz, die Sie mit `-q`
      angeben können
    - den **relativen Geschwindigkeitsrang** des Algorithmus
    - ob eine alternative Implementierung verfügbar ist und verwendet würde
  - `jacksum --info` ohne Algorithmus gibt die unterstützten Algorithmen, die
    Java-Systemeigenschaften, die Anzahl der verfügbaren Prozessoren und die Situation des
    **Java-Heaps** aus, was für die Fehlersuche nützlich ist.
  - `jacksum -a all -l` listet alle Algorithmus-IDs auf, `jacksum --hmacs` listet alle Algorithmen
    auf, die einen HMAC bilden können.
  - Die Manpage dokumentiert **jeden Algorithmus** mit einer Kompatibilitätsliste, die zeigt, wie
    derselbe Hashwert mit anderen Betriebssystemen, Werkzeugen, APIs und Programmiersprachen
    berechnet wird.
  - Die **Hilfe ist durchsuchbar**: `jacksum -h <word>` findet alle Optionen, Algorithmen und
    Abschnitte, die passen, und `--exact` beschränkt die Suche auf eine exakte Übereinstimmung,
    damit Optionen wie `-` und `--` überhaupt nachgeschlagen werden können.
  - Die Hilfe ist auf **Englisch und Deutsch** verfügbar (`jacksum -h de`).
  - Geben Sie Ihre bevorzugte **Ausführlichkeitsstufe** an (`-V`): `info`, `warnings`, `errors`,
    `summary` und `details` können einzeln aktiviert und deaktiviert werden, oder über die
    Kurzformen `all`, `default` und `none`.

<a name="developer"/>

# Unterstützung für Entwickler

  - Der gesamte Quellcode ist offen, wird auf GitHub gehostet und ist über git zugänglich.
  - Das Projekt ist mit einer `pom.xml` mavenisiert, was das Arbeiten in Ihrer bevorzugten IDE
    einfach macht. Eine IntelliJ-`.idea`-Konfiguration ist Teil des Repos.
  - Jacksum stellt eine **Java-API** bereit, sodass Sie Jacksum in Ihre eigenen Projekte einbinden
    können. Unter anderem erlauben `JacksumAPI.getBrokenState()`,
    `JacksumAPI.getBrokenDescription()` und `JacksumAPI.preloadBrokenStates()` Ihrem Programm
    festzustellen, ob ein Algorithmus gebrochen ist.
  - **Javadoc** ist verfügbar.
  - Jacksum bleibt kompatibel mit Java 21 LTS, nutzt aber alle Vorteile späterer Releases, sofern
    verfügbar.
  - Siehe den [Developer Guide](https://github.com/jonelo/jacksum/wiki/Developer-Guide).

<a name="free_software"/>

# Freie Software, ausgereift und stabil

  - Jacksum ist **Freie Software**, veröffentlicht unter der
    [GPL-3.0 oder einer beliebigen späteren Version](https://github.com/jonelo/jacksum/blob/main/LICENSE),
    es läuft auf vollständig freien Plattformen und ist im Verzeichnis der Free Software Foundation
    aufgeführt.
  - Es ist **OSI Certified Open Source Software**.
  - Es ist **kostenlos**, frei von Werbung, frei von Ablaufdaten und frei von Registrierung.
  - Jacksum ist **ausgereift und stabil**: das erste Release wurde im Juli 2002 veröffentlicht, und
    das Projekt wird aktiv weiterentwickelt, siehe die
    [Release Notes](https://github.com/jonelo/jacksum/blob/main/RELEASE-NOTES.txt).
  - [Testfälle, um neue Releases zu testen und Regressionen zu finden](https://github.com/jonelo/jacksum-testcases),
    stehen für die CLI zur Verfügung.

Siehe auch

- [Algorithmen](https://github.com/jonelo/jacksum/wiki/Algorithms)
- [Manpage](https://github.com/jonelo/jacksum/wiki/Manpage)
- [Cheat Sheet](https://github.com/jonelo/jacksum/wiki/Cheat-Sheet)
- [File Format of Styles](https://github.com/jonelo/jacksum/wiki/File-Format-of-Styles)
- [Jacksum Hacks](https://github.com/jonelo/jacksum/wiki/Jacksum-Hacks)

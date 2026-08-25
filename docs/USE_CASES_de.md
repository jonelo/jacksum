*Diese Seite auf Englisch: [USE_CASES.md](USE_CASES.md)*

**Inhaltsverzeichnis**
 - [Bevor Sie beginnen](#before)
 - [1. Einen Download oder eine Dateiübertragung prüfen](#transfer)
 - [2. Zwei Verzeichnisbäume oder Datenträger vergleichen](#compare)
   - [Beide Bäume verfügbar](#compare_both)
   - [Nur ein Baum zur Zeit](#compare_offline)
   - [Ein Fingerabdruck für einen ganzen Baum](#compare_onehash)
 - [3. Langzeit-Integrität archivierter Medien](#archive)
 - [4. Ist das Medium noch lesbar?](#readable)
 - [5. Unidirektionale Verzeichnis-Synchronisation](#sync)
 - [6. Inkrementelle Backups](#incremental)
 - [7. Einen Patch für Ihre Kunden erstellen](#patch)
   - [Schritt für Schritt](#patch_manual)
   - [Als Shell-Skript](#patch_script)
   - [Als Ant-Build-Datei](#patch_ant)
 - [8. Erkennung von Einbrüchen (Intrusion Detection)](#ids)
 - [9. Änderungserkennung bei Webseiten](#web)
 - [10. Dateien über ihren Fingerabdruck finden](#find)
 - [11. Reproduzierbare Passwörter und Zufallszahlen](#generate)

<a name="before"></a>

# Bevor Sie beginnen

Dieses Dokument ist ein **Kochbuch**. Jeder Abschnitt beginnt mit einem Problem, das jemand
tatsächlich hat, und endet mit den Kommandos, die es lösen. Wenn Sie wissen wollen, was eine
bestimmte Option tut, dann suchen Sie stattdessen [Beispiele](EXAMPLES_de.md) — jenes Dokument ist
nach Features geordnet, dieses nach Zielen.

Alle nachfolgenden Rezepte wurden gegen **Jacksum 4.0.0** unter macOS mit OpenJDK 25 verifiziert; die
gezeigten Programmausgaben sind aus diesen Läufen kopiert.

Einige Konventionen, die durchgehend verwendet werden:

- Kommandos werden als `jacksum ...` geschrieben. Wenn Sie kein Startskript in Ihren `PATH` gelegt
  haben, ersetzen Sie `jacksum` überall durch `java -jar jacksum-4.0.0.jar`.
- Die meisten Rezepte verwenden `sha3-256`, den Standardalgorithmus von Jacksum 4. `sha256` wird dort
  verwendet, wo eine Liste auch von `sha256sum`/`shasum` gelesen werden können muss. Benennen Sie den
  Algorithmus in Skripten immer explizit, weil sich der Standardwert in einem künftigen Release
  ändern kann.
- `-o <Datei>` schreibt die Ausgabe in eine Datei und **weigert sich**, eine bestehende zu
  überschreiben; `-O <Datei>` überschreibt. Rezepte, die wiederholt laufen sollen, verwenden `-O`.
- Ein abschließender `.` bedeutet "das aktuelle Verzeichnis und alles darunter". Jacksum traversiert
  standardmäßig rekursiv.
- Exit-Codes sind hier wichtiger als anderswo: `0` bedeutet, alles war in Ordnung, `1` bedeutet
  mindestens eine Abweichung, und alles über `1` ist ein Fehler (eine fehlende Datei ergibt `4`, ein
  fehlgeschlagener `--check-strict`-Audit ergibt `6`). Die vollständige Tabelle finden Sie unter
  [Exit-Codes](EXAMPLES_de.md#verify_exitcodes).

Mehrere der nachfolgenden Rezepte gehen auf FAQs von <https://jacksum.net> zurück und waren für
Jacksum 1.x geschrieben. Die Kommandos wurden **für Jacksum 4 neu gefasst**, weil sich in der
Zwischenzeit eine Handvoll Optionen in ihrer Bedeutung geändert hat. Wenn Sie ein eigenes altes
Skript portieren, dann ist diese Tabelle die Stelle, an der wir gestolpert sind:

| Jacksum 1.x | Jacksum 4.0.0 |
|---|---|
| `-S` (ein Hash über einen ganzen Baum) | entfernt, siehe [2.3](#compare_onehash) |
| `-m` (Metainformationen im Kopf) | `--header` |
| `-p` (Pfadinformationen ausgeben) | entfernt |
| `-w <Verz>` (Arbeitsverzeichnis) | **`--wanted-list <Hash-Datei>`** — ein völlig anderes Feature; verwenden Sie `cd` oder ein Pfadargument |
| `-r` (rekursiv) | Rekursion ist der Standard; `-r <Tiefe>` *erfordert* jetzt ein Tiefenargument |
| `-f` (nur reguläre Dateien) | `--dont-follow-symlinks-to-files` |
| `-l` (die Unterschiede auflisten) | `--list` — aber der Standardfilter ist `all`, ergänzen Sie also `--list-filter bad` |
| `-P <Zeichen>` (Pfadtrenner) | unverändert, und weiterhin nützlich |
| `-E <Kodierung>` | unverändert — **nicht** blind weglassen, siehe die Anmerkung unten |

Die Standardkodierung hängt vom Algorithmus ab: hexadezimal bei `md5`, `sha*` und Verwandten, aber
dezimal bei CRCs und bei den klassischen Unix-Prüfsummen, damit Jacksum mit den nativen Werkzeugen
übereinstimmt.

```
jacksum -a sum_bsd -F "#HASH" readme.txt
35080
jacksum -a sum_bsd -E hex -F "#HASH" readme.txt
8908
```

Der erste Wert ist genau der, den BSDs `sum` für dieselbe Datei ausgibt. Verwenden Sie `-E hex`, wenn
Sie hexadezimal von einem Algorithmus wollen, dessen native Kodierung nicht hexadezimal ist — und
lassen Sie es bei `sha*` weg, wo es ohnehin der Standard ist.

Die meisten Beispiele laufen gegen diesen kleinen Baum:

    readme.txt              14 Bytes
    version.properties      14 Bytes
    docs/manual.txt         16 Bytes
    docs/changes.txt        16 Bytes
    lib/liba.jar            13 Bytes
    lib/libb.jar            13 Bytes

<a name="transfer"></a>

# 1. Einen Download oder eine Dateiübertragung prüfen

**Problem.** Sie haben einen Installer, ein `.iso` oder ein Release-Archiv heruntergeladen, und der
Hersteller hat daneben einen Hashwert veröffentlicht. Hat die Datei die Übertragung überlebt, und ist
es die Datei, die der Hersteller tatsächlich gebaut hat?

Geben Sie den erwarteten Hashwert nach `-e` an:

```
jacksum -a sha256 -e 0df7a0f53c85a97b9e3e08e4ba148b6754d606416fc71a7d8ce853d55c0c6daf readme.txt
    MATCH  readme.txt (0df7a0f53c85a97b9e3e08e4ba148b6754d606416fc71a7d8ce853d55c0c6daf)

Jacksum: Expectation met.
Jacksum: 1 of the successfully read files matches the expected hash value.
```

Passt die Datei nicht, dann sagt Jacksum das und endet mit `6`:

```
jacksum -a sha256 -e 0000000000000000000000000000000000000000000000000000000000000000 readme.txt

Jacksum: Expectation not met.
Jacksum: 0 of the successfully read files match the expected hash value.
```

Wenn der Hersteller eine ganze *Zeile* statt eines nackten Hashwerts veröffentlicht hat, dann
übergeben Sie diese Zeile wörtlich mit `--check-line` und lassen Jacksum sie parsen:

```
jacksum -a sha256 --check-line "0df7a0f53c85a97b9e3e08e4ba148b6754d606416fc71a7d8ce853d55c0c6daf *readme.txt" -V nosummary readme.txt
       OK  readme.txt
```

Zwei Dinge sind dabei zu bedenken. Erstens beweist ein passender Hashwert nur, dass Ihre Kopie *der
Datei entspricht, aus der der Hashwert berechnet wurde* — wer den Hashwert veröffentlichen kann, kann
auch eine passende Datei veröffentlichen. Der Hashwert muss also über einen Kanal kommen, dem Sie
vertrauen (eine signierte Release-Seite, ein zweiter Mirror). Zweitens verwenden Sie den Algorithmus,
den der Hersteller verwendet hat; wenn dort nur MD5 oder SHA-1 angeboten wird, erkennt die Prüfung
weiterhin Übertragungsfehler, ist aber kein Beweis gegen eine absichtliche Fälschung.

Weitere Varianten — Pipes, BSD-Datensätze, ganze Listen — finden Sie unter
[Datenintegrität überprüfen](EXAMPLES_de.md#verify).

<a name="compare"></a>

# 2. Zwei Verzeichnisbäume oder Datenträger vergleichen

**Problem.** Sie haben einen Baum kopiert, eine DVD gebrannt, ein Backup zurückgespielt oder eine
Freigabe per rsync abgeglichen, und nun wollen Sie wissen, ob die Kopie wirklich identisch mit dem
Original ist — und wenn nicht, welche Dateien genau abweichen.

`diff -r` beantwortet das, wenn beide Bäume gleichzeitig eingebunden sind. Jacksum beantwortet es in
diesem Fall auch, und zusätzlich in zwei Fällen, mit denen `diff` nicht umgehen kann: wenn die beiden
Bäume nie gleichzeitig verfügbar sind (zwei DVDs, ein Laufwerk) und wenn sie auf zwei Rechnern ohne
Verbindung dazwischen liegen.

<a name="compare_both"></a>

## Beide Bäume verfügbar

Erstellen Sie einen Fingerabdruck des Referenzbaums und prüfen Sie den anderen Baum gegen diese
Liste:

```
cd dir1
jacksum -a sha3-256 -O /tmp/dir1.list .

cd ../dir2
jacksum -a sha3-256 -c /tmp/dir1.list .
```

Wechseln Sie immer mit `cd` in den Baum und übergeben Sie `.`, damit die Liste *relative* Pfade
enthält. Eine Liste voller `/Volumes/BACKUP/...`-Pfade ist auf dem nächsten Rechner nutzlos, wo
dieselben Daten irgendwo anders liegen.

Für einen Baum, der Byte für Byte identisch ist, bekommen Sie nichts als `OK`-Zeilen und Exit-Code
`0`:

```
       OK  ./docs/changes.txt
       OK  ./docs/manual.txt
       OK  ./version.properties
       OK  ./lib/libb.jar
       OK  ./lib/liba.jar
       OK  ./readme.txt

Jacksum: matches (OK): 6
Jacksum: mismatches (FAILED): 0
Jacksum: new files (NEW): 0
Jacksum: missing files (MISSING): 0
Jacksum: files with errors (ERROR): 0
Jacksum: strict check: PASSED
```

Und so sieht ein Baum aus, in dem eine Datei geändert, eine gelöscht und eine hinzugefügt wurde:

```
Jacksum: Error: ./docs/changes.txt: does not exist.
  MISSING  ./docs/changes.txt
       OK  ./docs/manual.txt
       OK  ./version.properties
       OK  ./lib/libb.jar
       OK  ./lib/liba.jar
   FAILED  ./readme.txt
      NEW  ./lib/obsolete.jar

Jacksum: matches (OK): 4
Jacksum: mismatches (FAILED): 1
Jacksum: new files (NEW): 1
Jacksum: missing files (MISSING): 1
Jacksum: files with errors (ERROR): 0
Jacksum: strict check: FAILED
```

Lesen Sie die vier Zustände als Vergleich zweier Mengen: `FAILED` = gleicher Name, anderer Inhalt,
`MISSING` = in der Referenz, aber nicht hier, `NEW` = hier, aber nicht in der Referenz, `OK` =
identisch. Dieser Lauf endet mit `4`, weil eine in der Liste genannte Datei nicht gelesen werden
konnte.

Wenn die Liste zwischen Windows und Unix wandern soll, dann ergänzen Sie beim Erstellen `-P /`. Das
normalisiert den Pfadtrenner, sodass dieselbe Liste auf beiden Seiten funktioniert:

```
jacksum -a sha3-256 -P / -O /tmp/dir1.list .
```

<a name="compare_offline"></a>

## Nur ein Baum zur Zeit

**Problem.** Sie wollen zwei DVDs vergleichen, haben aber nur ein Laufwerk. Oder das Original liegt
auf einem Rechner, den Sie von der Kopie aus nicht erreichen.

Es ändert sich nichts außer dem *Zeitpunkt* — die Hashliste ist der transportable Stellvertreter des
Baums, den sie beschreibt. Sie ist eine kleine Textdatei und passt damit auf einen USB-Stick, in eine
E-Mail oder in ein Git-Repository:

1. Legen Sie die erste DVD ein (oder setzen Sie sich an den ersten Rechner) und schreiben Sie die
   Liste:

       cd /Volumes/DVD1
       jacksum -a sha3-256 -O ~/dvd1.list .

2. Werfen Sie die DVD aus. Legen Sie die zweite ein und prüfen Sie sie gegen die Liste:

       cd /Volumes/DVD2
       jacksum -a sha3-256 -c ~/dvd1.list .

Der Bericht ist genau der aus [2.1](#compare_both). Bewahren Sie die Liste bei Ihrem Archivverzeichnis
auf, nicht auf dem Medium, das sie beschreibt — eine Liste auf der DVD kann Ihnen nicht sagen, dass
die DVD unlesbar ist, und sie taucht außerdem in ihrer eigenen Auflistung auf.

Dieses Offline-Muster ist die Grundlage der nächsten vier Rezepte: auf der Referenzseite eine Liste
schreiben, sie hinübertragen und die Zielseite sagen lassen, was abweicht.

<a name="compare_onehash"></a>

## Ein Fingerabdruck für einen ganzen Baum

**Problem.** Sie wollen keine Liste, sondern *einen* Wert, den Sie auf die Hülle einer DVD schreiben
oder in ein Ticket einfügen und mit dem Auge vergleichen können.

Jacksum 1.5 hatte dafür die Option `-S`; sie ist entfallen. Bauen Sie den Wert stattdessen mit einer
Pipe: jede Datei hashen, die Hashwerte sortieren, das Ergebnis hashen:

```
jacksum -a sha3-256 --style hexhashes-only . | sort | jacksum -a sha3-256 -
258bebd7e2bdf4b72e6a6c422747e1b3c2c3ebe34d13846dfff74713fadcee4e <stdin>
```

Derselbe Baum an einem anderen Pfad ergibt denselben Wert, und genau darum geht es:

```
cd /woanders/kopie-des-baums
jacksum -a sha3-256 --style hexhashes-only . | sort | jacksum -a sha3-256 -
258bebd7e2bdf4b72e6a6c422747e1b3c2c3ebe34d13846dfff74713fadcee4e <stdin>
```

Ändern Sie irgendwo im Baum ein einzelnes Byte, und der Wert ändert sich vollständig:

```
dce61e54592e0ddef7b67c8aa0445f1c2bca3532d419634b712b32d230e3dd37 <stdin>
```

`sort` ist keine Dekoration. Jacksum verspricht keine Reihenfolge, in der es einen Baum durchläuft —
in den obigen Läufen war die Rohreihenfolge `docs/changes.txt`, `docs/manual.txt`,
`version.properties`, `lib/libb.jar`, `lib/liba.jar`, `readme.txt`, also weder alphabetisch noch
tiefenorientiert. Ohne `sort` können dieselben Daten auf einem anderen Dateisystem einen anderen Wert
ergeben.

`hexhashes-only` verwirft die Dateinamen, diese Variante ist also blind gegenüber Umbenennungen:
Benennen Sie `readme.txt` in `README.md` um, und der Fingerabdruck bleibt `258bebd7...`. Wenn Namen
Teil dessen sind, was Sie vergleichen, dann verwenden Sie einen Stil, der sie mitführt:

```
jacksum -a sha3-256 --style linux . | sort | jacksum -a sha3-256 -
61510463ec3b388476a553cbbaff82283cb1c4740fe662d41f2cab06cbf368a4 <stdin>
```

Nach derselben Umbenennung ändert *dieser* Wert sich, nämlich auf `a77e39dc...`.

Der Preis gegenüber [2.1](#compare_both) ist Information: ein einzelner Wert sagt Ihnen, *ob* sich
zwei Bäume unterscheiden, nie *wo*. Verwenden Sie ihn als billigen Stolperdraht und halten Sie die
vollständige Liste für den Fall bereit, dass er auslöst.

<a name="archive"></a>

# 3. Langzeit-Integrität archivierter Medien

**Problem.** Sie brennen Daten, die Sie in fünfzehn Jahren noch lesen wollen. Bis dahin haben sich
das Betriebssystem, die Werkzeuge und möglicherweise die Hardware geändert. Werden Sie dann noch
feststellen können, ob die DVD unbeschädigt ist?

Nicht die Hashwerte sind der fragile Teil, sondern das *Dateiformat*. Schreiben Sie die Liste in einem
Format, das auch andere Werkzeuge als Jacksum lesen können, und die Prüfung übersteht es, dass Sie
Jacksum vollständig aufgeben:

```
jacksum -a sha256 --style linux --header -O SHA256SUMS .
```

Das erzeugt eine schlichte, `sha256sum`-kompatible Liste mit einem Dokumentationsblock darüber:

```
#
# created by: Jacksum (https://jacksum.net, version: 4.0.0)
# invoked on JVM: OpenJDK 64-Bit Server VM (vendor: Eclipse Adoptium, version: 25.0.4+7-LTS)
# invoked on OS: Mac OS X (arch: aarch64, version: 26.6.2)
# invoked on date: 2026-08-24T22:54:47.708+02:00
#
# invoked from: /Volumes/ARCHIVE-2026
# invocation args: -a sha256 --style linux --header -O SHA256SUMS .
#________________________________________________________________________
d0e2dc2e66b82a670659736963da9a56feeb25d78d79eda405bcbd84b37d711c *./docs/changes.txt
0b398916a560e8c357b8d7374bd93dd7865d0c528ed842abc47413d2cfb0bc70 *./docs/manual.txt
112773e2a370ee8a61667937e79f4f223ef5fe4db4504cb7ec1a5256060cf975 *./version.properties
4d8095c96f86709e5c5b9291ac6e2ca77488d0ccfeb3d4fbcac383d5eac5e527 *./lib/libb.jar
213bb7ff99ae7fd27edfcd55ab5be34c2c8ab79264ac1bce46c50e060e837eee *./lib/liba.jar
0df7a0f53c85a97b9e3e08e4ba148b6754d606416fc71a7d8ce853d55c0c6daf *./readme.txt
```

`--header` ist das, was dies zukunftssicher macht: der Algorithmus, die Jacksum-Version, die
Plattform und das Datum sind im Klartext festgehalten, sodass jeder, der die DVD findet, weiß, was er
mit den Zahlen tun soll. Zeilen, die mit `#` beginnen, sind Kommentare, und jedes
`sha256sum`-kompatible Werkzeug überspringt sie:

```
shasum -a 256 -c SHA256SUMS
./docs/changes.txt: OK
./docs/manual.txt: OK
./version.properties: OK
./lib/libb.jar: OK
./lib/liba.jar: OK
./readme.txt: OK
```

Praktische Hinweise:

- **Wählen Sie einen langweiligen Algorithmus.** `sha256` ist für Archive die sichere Wahl, gerade
  weil er überall implementiert ist. `sha3-256` ist die bessere Hashfunktion, aber 2041 halten Sie
  vielleicht die DVD in der Hand und einen Rechner, der nur `sha256sum` hat. Nichts hindert Sie
  daran, beide Listen zu schreiben.
- **Brennen Sie die Liste auf die DVD *und* behalten Sie eine Kopie anderswo.** Auf der DVD reist sie
  mit den Daten; außerhalb der DVD ist sie noch lesbar, wenn die DVD es nicht mehr ist.
- **Verwenden Sie relative Pfade** (mit `cd` in den Baum wechseln, `.` übergeben) und `-P /`, wenn die
  DVD auch unter Windows gelesen werden soll.
- **Legen Sie die Liste nicht in den Baum, den Sie hashen**, es sei denn, Sie nehmen in Kauf, dass sie
  sich bei jeder Prüfung selbst als `NEW` meldet.

<a name="readable"></a>

# 4. Ist das Medium noch lesbar?

**Problem.** Eine alte Backup-Festplatte, eine zehn Jahre alte DVD, ein USB-Stick, den Sie in einer
Schublade gefunden haben. Bevor Sie irgendetwas davon vertrauen, wollen Sie wissen, ob überhaupt noch
jedes Byte gelesen werden kann — unabhängig davon, ob der Inhalt *korrekt* ist.

`-a read` liest jedes Byte jeder Datei und verwirft es. Alles, was dabei schiefgeht — ein Kratzer,
Bitrot, ein Rechteproblem, eine ausgefallene Netzwerkfreigabe — tritt als Fehler zutage:

```
jacksum -a read -V summary,errors -r max . > /dev/null
Jacksum: Error: ./locked.bin (Permission denied)

Jacksum: total files read successfully: 1
Jacksum: total bytes read: 3
Jacksum: total bytes read (human readable): 3 bytes
Jacksum: total file read errors: 1
```

Exit-Code `4` und `total file read errors` machen das skriptfähig. Beachten Sie, dass `-a none` diese
Dateien *nicht* findet, weil es sie nie öffnet.

Wenn Sie außerdem eine Hashliste aus Abschnitt 3 haben, dann führen Sie stattdessen die Prüfung durch
— die beantwortet "lesbar *und* unverändert" in einem Durchgang. Verwenden Sie `-a read`, wenn es
keine Liste gibt, oder wenn Sie ein Medium sichten wollen, bevor Sie Zeit in den Vergleich stecken.

`-u <Datei>` sammelt die beschädigten Dateien in einer Liste, mit der Sie weiterarbeiten können.
Details unter [Ist auf dem Medium noch alles lesbar?](JACKSUM_HACKS_de.md#medium-readable).

<a name="sync"></a>

# 5. Unidirektionale Verzeichnis-Synchronisation

**Problem.** Zwei Rechner halten dasselbe Verzeichnis — sollten es jedenfalls. Einer davon ist
fehlerfrei (nennen wir ihn `good`), der andere ist abgedriftet (nennen wir ihn `bad`): Dateien wurden
versehentlich geändert, gelöscht oder hinzugefügt. Es gibt keine Netzwerkverbindung zwischen den
beiden, nur die Möglichkeit, eine Datei hinüberzutragen — einen USB-Stick, einen E-Mail-Anhang, eine
Übertragung über eine Luftlücke.

Der Trick ist, dass Sie nie beide Bäume gleichzeitig brauchen. Sie brauchen eine Hashliste von `good`,
eine Differenzliste von `bad` und ein Archiv von `good`. Drei Dateien reisen, sonst nichts.

**Schritt 1 — auf dem fehlerfreien Rechner den Referenzbaum erfassen.**

```
cd good
jacksum -a sha3-256 -O /tmp/hashes.list .
```

Tragen Sie `/tmp/hashes.list` zum fehlerhaften Rechner.

**Schritt 2 — auf dem fehlerhaften Rechner fragen, was falsch ist.**

```
cd bad
jacksum -a sha3-256 -c /tmp/hashes.list --list-filter bad --list . > /tmp/files.list 2>/dev/null
```

`--list-filter bad` verengt den Bericht auf `failed`, `missing` und `error`; `--list` reduziert jede
Zeile auf nichts als den Pfad. Was Sie erhalten, ist eine schlichte Liste der Dateien, die ersetzt
werden müssen:

```
./docs/changes.txt
./readme.txt
```

Das Kommando endet mit `1` oder `4` — das ist hier das *erwartete* Ergebnis und kein Fehlschlag,
lassen Sie also `set -e` Ihr Skript an dieser Zeile nicht abbrechen. Fehler gehen auf die
Standardfehlerausgabe, weshalb die Umleitung wichtig ist: ohne `2>/dev/null` wären die Zeilen
`Jacksum: Error: ... does not exist.` in Ihrem Terminal dazwischengemischt (in `files.list` würden
sie nicht landen, aber sie sind Lärm).

Tragen Sie `/tmp/files.list` zurück zum fehlerfreien Rechner.

**Schritt 3 — auf dem fehlerfreien Rechner genau diese Dateien einpacken.**

```
cd good
tar cf /tmp/patch.tar -T /tmp/files.list      # GNU/Linux, macOS
bzip2 -9 /tmp/patch.tar
```

Unter Solaris und älterem BSD-`tar` heißt die Option für "lies die Dateinamen aus dieser Datei" `-I`
statt `-T`. Vorsicht: bei GNU-`tar` bedeutet `-I` *verwende dieses Kompressionsprogramm*, die beiden
sind also nicht austauschbar. Wenn Sie ein Zip-Archiv bevorzugen:

```
cd good
zip -@ /tmp/patch.zip < /tmp/files.list
```

Unter Windows lautet derselbe Schritt:

```
cd good
type files.list | zip -@ patch.zip
```

**Schritt 4 — auf dem fehlerhaften Rechner über den Baum entpacken.**

```
cd bad
bunzip2 -c /tmp/patch.tar.bz2 | tar xf -
```

Das Archiv enthält die guten Fassungen jeder geänderten Datei und jeder Datei, die verschwunden war,
mitsamt ihren relativen Pfaden — es an der richtigen Stelle zu entpacken repariert also beide Fälle
auf einmal.

**Schritt 5 — die Dateien behandeln, die dort nicht sein sollten.**

Das ist der Schritt, den die ursprüngliche FAQ nie erwähnt hat, und er ist wichtig:
`--list-filter bad` deckt `failed`, `missing` und `error` ab, aber **nicht** `new`. Dateien, die nur
auf dem fehlerhaften Rechner existieren, sind für die Schritte 2–4 unsichtbar und überleben die
Reparatur. Fragen Sie sie separat ab:

```
cd bad
jacksum -a sha3-256 -c /tmp/hashes.list --list-filter new --list . > /tmp/obsolete.list 2>/dev/null
```

```
./lib/obsolete.jar
```

Sehen Sie diese Liste durch, bevor Sie darauf reagieren — dies ist der einzige destruktive Schritt im
Rezept, und ein `--list-filter new`-Eintrag sieht genauso aus wie eine legitime lokale Datei. Wenn Sie
die beiden Bäume dann wirklich identisch haben wollen:

```
cd bad
while IFS= read -r f; do rm -- "$f"; done < /tmp/obsolete.list
```

**Schritt 6 — bestätigen.**

```
cd bad
jacksum -a sha3-256 -c /tmp/hashes.list .
       OK  ./docs/changes.txt
       OK  ./docs/manual.txt
       OK  ./version.properties
       OK  ./lib/libb.jar
       OK  ./lib/liba.jar
       OK  ./readme.txt

Jacksum: matches (OK): 6
Jacksum: mismatches (FAILED): 0
Jacksum: new files (NEW): 0
Jacksum: missing files (MISSING): 0
Jacksum: files with errors (ERROR): 0
Jacksum: strict check: PASSED
```

Sechs `OK`-Zeilen und Exit-Code `0`: aus dem fehlerhaften Rechner ist ein fehlerfreier geworden.

Warum "unidirektional"? Weil `good` die Autorität ist und `bad` überschrieben wird. Jacksum sagt
Ihnen, *dass* sich zwei Bäume unterscheiden, nie *welche Seite neuer ist* — es gibt keine Heuristik
über Änderungszeiten und keine Konfliktauflösung. Wenn beide Seiten Änderungen halten, die Sie
behalten wollen, dann ist dies das falsche Werkzeug; wenn eine Seite die Wahrheit ist, dann ist es
ein sehr kleines und sehr transportables.

<a name="incremental"></a>

# 6. Inkrementelle Backups

**Problem.** Ein Vollbackup Ihrer Daten dauert Stunden. Sie wollen, dass der tägliche Lauf nur
archiviert, was sich wirklich geändert hat.

Der übliche Ansatz ist `find -newer`, das den Änderungszeiten vertraut. Jacksum vergleicht den
*Inhalt*, und das erkennt drei Dinge, die Zeitstempel nicht erkennen: Dateien, die geändert und deren
mtime danach zurückgesetzt wurde; Dateien, die angefasst, aber nicht geändert wurden (und die umsonst
gesichert würden); und stille Datenkorruption, bei der die Bytes verrotten, während die Metadaten
unberührt bleiben.

**Einmalig — das Vollbackup und die Basislinie.**

```
cd data
tar czf /backup/full.tar.gz .
jacksum -a sha3-256 -O /backup/base.list .
```

**Bei jedem Lauf — fragen, was sich geändert hat, das archivieren, dann die Basislinie fortschreiben.**

```
cd data
jacksum -a sha3-256 -c /backup/base.list --list-filter failed,new --list . > /backup/changed.list 2>/dev/null
```

`failed,new` ist hier der richtige Filter: `failed` sind die Dateien, deren Inhalt sich geändert hat,
`new` sind die Dateien, die es zum Zeitpunkt der Basislinie nicht gab. `missing` ist absichtlich
ausgenommen — eine gelöschte Datei ist nichts, was man in ein Archiv legt (wenn Sie Löschungen
nachspielen müssen, erfassen Sie diese Liste separat mit `--list-filter missing --list`).

An einem ruhigen Tag ist die Liste leer und das Kommando endet mit `0`:

```
wc -l < /backup/changed.list
       0
```

Nachdem `readme.txt` bearbeitet und `docs/notes.txt` hinzugefügt wurde, endet es mit `1` und enthält:

```
./readme.txt
./docs/notes.txt
```

Packen Sie diese ein und schreiben Sie dann die Basislinie fort, damit der Lauf von morgen gegen
heute vergleicht:

```
tar czf /backup/inc-$(date +%Y%m%d).tar.gz -T /backup/changed.list
jacksum -a sha3-256 -O /backup/base.list .
```

Beachten Sie das `-O`: die Basislinie wird bei jedem Lauf neu geschrieben, `-o` (das sich weigert zu
überschreiben) würde also am zweiten Tag scheitern.

Die Reihenfolge ist wichtig. Schreiben Sie die Basislinie **nach** dem erfolgreichen Schreiben des
Archivs fort — wenn Sie sie zuerst fortschreiben und das `tar` danach scheitert, dann gelten diese
Änderungen als gesichert und werden nie wieder aufgegriffen. Behalten Sie die alte Basislinie, bis
das Archiv verifiziert ist, und legen Sie Basislinien außerhalb des Baums ab, den sie beschreiben.

Wiederherstellen heißt: das Vollbackup entpacken und danach jedes Inkrement in der richtigen
Reihenfolge. Eine Wiederherstellung zu verifizieren ist ein einziges Kommando:
`jacksum -a sha3-256 -c /backup/base.list .` gegen den wiederhergestellten Baum.

<a name="patch"></a>

# 7. Einen Patch für Ihre Kunden erstellen

**Problem.** Sie liefern Version 4.1.0 eines Produkts aus, dessen Kunden 4.0.0 installiert haben. Die
vollständige Distribution ist 400 MB groß; die tatsächliche Änderung sind 3 MB. Sie wollen 3 MB
ausliefern.

Das ist Abschnitt 5 mit anderen Worten: die neue Version ist die Referenz, die alte Version ist das
Ziel, und der "Patch" ist das Archiv der Dateien, die abweichen. Jacksums Anteil daran ist die
Entscheidung, *welche* Dateien das sind — anhand des Inhalts, damit Dateien, die neu gebaut, aber
nicht geändert wurden, den Patch nicht aufblähen.

<a name="patch_manual"></a>

## Schritt für Schritt

**1. Die neue Version erfassen.**

```
cd ~/newversion
jacksum -a sha3-256 -O /tmp/new.list .
```

**2. Die alte Version fragen, was abweicht.**

```
cd ~/oldversion
jacksum -a sha3-256 -c /tmp/new.list --list-filter bad --list . > /tmp/files.list 2>/dev/null
```

```
./lib/libc.jar
./version.properties
./lib/libb.jar
```

`version.properties` und `lib/libb.jar` haben sich zwischen den Releases geändert; `lib/libc.jar` ist
in 4.1.0 neu und wird daher aus der Sicht der alten Version als `missing` gemeldet. Alle drei gehören
in den Patch, und genau das wählt `--list-filter bad` aus.

**3. Diese Dateien einpacken — aus der neuen Version.**

```
cd ~/newversion
tar cf /tmp/patch.tar -T /tmp/files.list      # GNU/Linux, macOS
bzip2 -9 /tmp/patch.tar
```

Unter Solaris und älterem BSD-`tar` verwenden Sie `-I` statt `-T` (und beachten Sie die Warnung in
[Abschnitt 5](#sync) — GNU-`tar` verwendet `-I` für etwas völlig anderes). Für ein Zip-Archiv:

```
cd ~/newversion
zip -@ /tmp/patch.zip < /tmp/files.list
```

Ihre Kunden entpacken das über ihrer Installation. Was sie danach ausführen sollten, ist die Liste aus
Schritt 1, die Sie gleich mit in den Patch legen können:

```
cd ~/oldversion
tar xf patch.tar
jacksum -a sha3-256 -c new.list .
       OK  ./docs/changes.txt
       OK  ./docs/manual.txt
       OK  ./version.properties
       OK  ./lib/libb.jar
       OK  ./lib/libc.jar
       OK  ./lib/liba.jar
       OK  ./readme.txt
```

Sieben `OK`-Zeilen: die 4.0.0-Installation ist nun Byte für Byte eine 4.1.0-Installation.

Ein Vorbehalt, derselbe wie in Abschnitt 5: Dateien, die 4.1.0 *entfernt* hat, sind nicht im Patch.
Wenn Ihr Release Dateien löscht, dann erzeugen Sie auch diese Liste und liefern sie als
Deinstallationsschritt mit:

```
cd ~/oldversion
jacksum -a sha3-256 -c /tmp/new.list --list-filter new --list . > /tmp/remove.list 2>/dev/null
```

<a name="patch_script"></a>

## Als Shell-Skript

Die drei Schritte zusammengefasst, mit behandelten Exit-Codes:

```sh
#!/bin/sh
# mkpatch.sh -- create a patch that upgrades OLDDIR to the state of NEWDIR.
# usage: mkpatch.sh OLDDIR NEWDIR OUTDIR [ALGORITHM]
set -e
old=$1; new=$2; out=$3; algo=${4:-sha3-256}
[ -d "$old" ] && [ -d "$new" ] && [ -d "$out" ] || {
    echo "usage: $0 OLDDIR NEWDIR OUTDIR [ALGORITHM]" >&2; exit 2; }
out=$(cd "$out" && pwd)

# 1. fingerprint the new version
( cd "$new" && jacksum -a "$algo" -O "$out/new.list" . )

# 2. ask the old version which files differ or are missing.
#    Exit code 1 or 4 is the expected outcome here, so swallow it.
( cd "$old" && jacksum -a "$algo" -c "$out/new.list" --list-filter bad --list . \
      > "$out/files.list" 2>"$out/check.log" ) || true

if [ ! -s "$out/files.list" ]; then
    echo "$0: the two versions are identical, no patch needed."
    exit 0
fi

# 3. pack those files -- taken from the NEW version
( cd "$new" && tar czf "$out/patch.tar.gz" -T "$out/files.list" )

echo "$0: $(wc -l < "$out/files.list") file(s) packed into $out/patch.tar.gz"
```

```
sh mkpatch.sh ~/oldversion ~/newversion /tmp/out
mkpatch.sh: 3 file(s) packed into /tmp/out/patch.tar.gz
```

Das `|| true` bei Schritt 2 ist der Teil, den man leicht falsch macht. Mit `set -e` und ohne dieses
`|| true` stirbt das Skript genau an dem Kommando, das Unterschiede finden soll.

<a name="patch_ant"></a>

## Als Ant-Build-Datei

Girish Narang und Johann N. Löfflmann haben einen Ant-basierten Patch-Ersteller entwickelt, der
weiterhin als [build.xml](https://jacksum.net/downloads/build.xml) und
[build.properties](https://jacksum.net/downloads/build.properties) veröffentlicht ist. Diese Dateien
wurden für Jacksum 1.x geschrieben und **funktionieren mit Jacksum 4 nicht** — `-m`, `-p` und `-w .`
sind entfallen oder haben ihre Bedeutung geändert, und das nackte `-l` in Schritt 2 würde die
komplette Dateiliste statt nur der Unterschiede schreiben, sodass der "Patch" die vollständige
Distribution enthielte. Hier ist dieselbe Build-Datei, portiert:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<project default="create_jacksum_patch" name="Creating patches with Jacksum">
    <property file="build.properties"/>
    <target name="create_jacksum_patch">

        <!-- STEP 0: Init -->
        <tstamp>
            <format property="DSTAMP" pattern="-yyyyMMdd" />
            <format property="TSTAMP" pattern="-HHmmss" />
        </tstamp>
        <property name="patch.dir" value="${patch.dir.home}/patch${DSTAMP}${TSTAMP}/"/>
        <property name="patch.tar" value="patch.tar"/>
        <property name="patch.zip" value="patch.zip"/>

        <mkdir dir="${patch.dir}" />
        <echo>distro.old.dir is ${distro.old.dir}</echo>
        <echo>distro.new.dir is ${distro.new.dir}</echo>
        <echo>patch.dir is ${patch.dir}</echo>

        <!-- STEP 1: fingerprint the new version -->
        <exec executable="java" failonerror="true" dir="${distro.new.dir}"
              output="${patch.dir}/new.list">
            <arg line="-jar '${jacksum.jar.file}' -a sha3-256 --header -P / ."/>
        </exec>

        <!-- STEP 2: ask the old version which files differ or are missing.
             failonerror="false" is deliberate: Jacksum exits with 1 or 4 when it
             finds differences, and finding differences is the point of this step. -->
        <exec executable="java" failonerror="false" dir="${distro.old.dir}"
              output="${patch.dir}/files.list" error="${patch.dir}/check.log">
            <arg line="-jar '${jacksum.jar.file}' -a sha3-256 -c '${patch.dir}/new.list' --list-filter bad --list ."/>
        </exec>

        <!-- STEP 3: pack the differing files, taken from the new version -->
        <tar destfile="${patch.dir}/${patch.tar}"
             basedir="${distro.new.dir}"
             includesfile="${patch.dir}/files.list"/>

        <!-- STEP 4: the same as a zip -->
        <zip destfile="${patch.dir}/${patch.zip}"
             basedir="${distro.new.dir}"
             includesfile="${patch.dir}/files.list"
             encoding="UTF-8"/>

        <!-- STEP 5: compress patch.tar -->
        <gzip src="${patch.dir}/${patch.tar}" destfile="${patch.dir}/${patch.tar}.gz"/>
        <bzip2 src="${patch.dir}/${patch.tar}" destfile="${patch.dir}/${patch.tar}.bz2"/>

        <!-- STEP 6: remove temp files -->
        <delete file="${patch.dir}/files.list"/>

    </target>
</project>
```

mit `build.properties`:

```properties
distro.old.dir=/home/user/project/version1/
distro.new.dir=/home/user/project/version2/
patch.dir.home=/home/user/project/patch/
jacksum.jar.file=/usr/local/jacksum/jacksum-4.0.0.jar
```

Zwei Änderungen verdienen eine besondere Erwähnung. `--list-filter bad` in Schritt 2 ist nicht
optional — ohne es listet Jacksum 4 jede geprüfte Datei auf, auch die identischen. Und `new.list`
wird in Schritt 6 behalten statt gelöscht, weil Ihre Kunden genau diese Datei brauchen, um das
Ergebnis zu verifizieren.

Die obigen Jacksum-Aufrufe wurden einzeln gegen 4.0.0 verifiziert; das Ant-Target selbst wurde nicht
ausgeführt, weil auf dem für dieses Dokument verwendeten Rechner kein Ant installiert war.

<a name="ids"></a>

# 8. Erkennung von Einbrüchen (Intrusion Detection)

**Problem.** Sie wollen wissen, ob auf einem System hinter Ihrem Rücken etwas geändert, gelöscht oder
hinzugefügt wurde — Konfigurationsdateien, Programme, Web-Verzeichnisse — und die Antwort soll
Metadaten einbeziehen, weil eine Änderung, die Dateigröße und Zeitstempel erhält, genau das ist, was
jemand anstreben würde, der unentdeckt bleiben will.

Die Abschnitte 2 und 5 vergleichen Inhalte. Für diese Aufgabe verwenden Sie `--style full`, der den
Hashwert, den Zeitstempel, die Größe und den Namen festhält und — anders als ein selbst gebautes
`-F`-Format — ein echter Prüflisten-Stil ist, sodass Jacksum ihn mit `-c` wieder einlesen kann:

```
cd /etc
jacksum --style full -a sha3-256 -O /secure/baseline.txt .
```

```
#
# created by: Jacksum (https://jacksum.net, version: 4.0.0)
# invoked on JVM: OpenJDK 64-Bit Server VM (vendor: Eclipse Adoptium, version: 25.0.4+7-LTS)
# invoked on OS: Mac OS X (arch: aarch64, version: 26.6.2)
# invoked on date: 2026-08-24T22:54:10.338+02:00
#
# invoked from: /etc
# invocation args: --style full -a sha3-256 -O /secure/baseline.txt .
#________________________________________________________________________
c0e06825459ca99f6c8ec6e1d7640154ad685dc6acaf2e4ed06932f814e5711b 2026-08-24T22:54:10.270+02:00 14 ./sshd_config
423ed5f6208655c68319a9affcd2816f9d39076c236f29850d5483fb74a2ea0e 2026-08-24T22:54:10.270+02:00 16 ./hosts
7adc7b8d11576c0b7a07fd26120cba27a127b3a8684edf2da99f503938038f63 2026-08-24T22:54:10.270+02:00 11 ./passwd
```

Später, nachdem jemand `sshd_config` bearbeitet, den Zeitstempel von `hosts` ohne Inhaltsänderung
zurückgesetzt und eine `backdoor.conf` in das Verzeichnis gelegt hat:

```
jacksum --style full -a sha3-256 -c /secure/baseline.txt --no-header --list-filter bad,new -V nosummary .
Jacksum: Info: Option --compat/--style has been set, setting implicitly -a sha3-256 -E hex, stdin-name=<stdin>
      NEW  ./backdoor.conf
   FAILED  ./sshd_config
           [filesize expected: 14, actual: 15]
   FAILED  ./hosts
           [timestamp expected: 2026-08-24T22:54:10.270+02:00, actual: 2026-01-01T12:00:00.000+01:00]
```

Alle drei werden erkannt, und Jacksum sagt, *warum* jeder Fall fehlgeschlagen ist. Beachten Sie, dass
die Zeile zu `hosts` eine reine Metadatenänderung ist — der Inhalts-Hashwert passt weiterhin — und
eine rein inhaltsbezogene Prüfung sie als `OK` gemeldet hätte. Ob Sie das wollen, hängt vom System
ab: bei `/etc` ist ein rückwärts gewanderter Zeitstempel verdächtig, bei einem Build-Verzeichnis ist
er Lärm.

Details, die den Unterschied zwischen einem funktionierenden Stolperdraht und einem falschen
Sicherheitsgefühl machen:

- **`--list-filter bad,new` ist der Filter, den Sie wollen.** `bad` deckt `failed`, `missing` und
  `error` ab; `new` ergänzt Dateien, die aufgetaucht sind. `new` weglassen bedeutet, dass eine
  platzierte Datei unbemerkt bleibt.
- **Behalten Sie die Begründungszeilen.** Sie werden auf der Info-Verbositätsstufe ausgegeben,
  `-V noinfo` (oder `-V nosummary,noinfo`) blendet sie also aus. `-V nosummary` allein, wie oben,
  behält sie — um den Preis des Hinweises
  `Jacksum: Info: Option --compat/--style has been set ...`, der auf derselben Verbositätsstufe
  mitreist.
- **Die Basislinie ist das Kronjuwel.** Legen Sie sie außerhalb des überwachten Rechners ab, oder
  mindestens außerhalb des überwachten Baums und auf einem schreibgeschützten Medium. Wer die
  Basislinie umschreiben kann, kann die Prüfung für alles bestehen lassen.
- **Exit-Codes steuern die Alarmierung.** `0` heißt sauber, `1` heißt mindestens eine Abweichung, `4`
  heißt, etwas konnte nicht gelesen werden. In einem Cronjob ist `jacksum ... || alarm` die ganze
  Integration.
- **Für einen Audit ergänzen Sie `--check-strict`.** Das scheitert zusätzlich an fehlerhaft
  formatierten Zeilen in der Basislinie und endet mit `6`:

  ```
  jacksum --style full -a sha3-256 -c /secure/baseline.txt --no-header --check-strict -V nosummary,noinfo .
        NEW  ./backdoor.conf
     FAILED  ./sshd_config
     FAILED  ./hosts
         OK  ./passwd
  ```

  Auf einem unberührten Baum gibt dasselbe Kommando nur `OK`-Zeilen aus und endet mit `0`.
- **Zeitstempel tragen nur begrenzt weit.** Wer Schreibrechte hat, kann einen Zeitstempel genauso
  leicht wiederherstellen wie ändern; was nicht wiederherstellbar ist, ist der Inhalts-Hashwert. Die
  Metadaten sind die Bequemlichkeit, der Hashwert ist der Beweis.

Wenn Sie Änderungserkennung *ohne* Hashing wollen — auf großen Bäumen deutlich schneller, als Beweis
deutlich schwächer — dann liefert `--style without-hashes` denselben Arbeitsablauf allein auf
Zeitstempeln und Größen. Siehe
[Ein Verzeichnis als Momentaufnahme sichern und Änderungen später erkennen](JACKSUM_HACKS_de.md#snapshot).

<a name="web"></a>

# 9. Änderungserkennung bei Webseiten

**Problem.** Sie wollen benachrichtigt werden, wenn sich eine Seite ändert: die Sicherheitshinweise
eines Herstellers, eine Release-Notes-Seite, ein Lizenztext, die Preisliste eines Mitbewerbers.

Herunterladen und hashen in einer Pipe — keine temporäre Datei nötig:

```
curl -sSL https://example.org/page.html | jacksum -a sha3-256 -F "#HASH" -
b7cd39e7ec6c22c24e0938098d66516e1dcefe3a59457aed834717bc157a63d5
```

Bewahren Sie diesen Wert auf und vergleichen Sie von dann an dagegen:

```
curl -sSL https://example.org/page.html | jacksum -a sha3-256 -e b7cd39e7ec6c22c24e0938098d66516e1dcefe3a59457aed834717bc157a63d5 -
    MATCH  <stdin> (b7cd39e7ec6c22c24e0938098d66516e1dcefe3a59457aed834717bc157a63d5)

Jacksum: Expectation met.
Jacksum: 1 of the successfully read files matches the expected hash value.
```

Exit-Code `0` heißt unverändert, `6` heißt, die Erwartung wurde nicht erfüllt — ein Cronjob ist also
eine Zeile:

```sh
curl -sSL "$URL" | jacksum -a sha3-256 -e "$KNOWN" -V nosummary - \
    || mail -s "$URL changed" me@example.org < /dev/null
```

**Der Haken: die meisten Seiten ändern sich bei jedem Aufruf.** Ein Besucherzähler, eine rotierende
Werbeeinblendung, ein gerenderter Zeitstempel, ein CSRF-Token — jedes davon macht den Hashwert jedes
Mal anders und verwandelt Ihre Überwachung in einen Dauer-Alarm:

```
curl -sSL https://example.org/page.html | jacksum -a sha3-256 -e b7cd39e7ec6c22c24e0938098d66516e1dcefe3a59457aed834717bc157a63d5 -

Jacksum: Expectation not met.
Jacksum: 0 of the successfully read files match the expected hash value.
```

Dabei hat sich nichts geändert, was jemanden interessiert — der Zähler ist von 41234 auf 41235
gesprungen. Schneiden Sie den flüchtigen Teil *vor* dem Hashen heraus und nehmen Sie die Basislinie
von der gefilterten Form:

```
curl -sSL https://example.org/page.html | grep -v 'Visitor counter' | jacksum -a sha3-256 -F "#HASH" -
00efe6aedd332466d08f8836ebce0eed9c1986ffd999747f38f429621e2b05f7
```

Nun kann der Zähler tun, was er will:

```
curl -sSL https://example.org/page.html | grep -v 'Visitor counter' | jacksum -a sha3-256 -e 00efe6aedd332466d08f8836ebce0eed9c1986ffd999747f38f429621e2b05f7 -
    MATCH  <stdin> (00efe6aedd332466d08f8836ebce0eed9c1986ffd999747f38f429621e2b05f7)
```

während eine echte Änderung — aus "Version 4.0.0 is out" wird "Version 4.1.0 is out" — weiterhin
anspringt:

```
Jacksum: Expectation not met.
Jacksum: 0 of the successfully read files match the expected hash value.
```

`grep -v` ist die grobe Variante; `sed` auf einer Zeile oder eine Pipe durch einen Textextraktor
filtert genauer. Was Sie auch wählen: wenden Sie *denselben* Filter beim Erstellen der Basislinie und
beim Prüfen an, sonst weicht jeder Lauf ab.

**Mehrere Seiten gleichzeitig beobachten.** Speichern Sie die Downloads unter stabilen Namen und
führen Sie eine Liste, genau wie in Abschnitt 2:

```
jacksum -a sha3-256 --style linux -O watch.list a.html b.html
```

```
0073a1763a2d9b034ba9d7d0369758cf479111b49c5fcff4dce47739b6b5114c *a.html
ec62d3442f9275575499fabf10644ffa460121d518f0a3274bde02a110bde606 *b.html
```

Erneut herunterladen und prüfen; `--list-filter failed` meldet nur, was sich bewegt hat:

```
jacksum -a sha3-256 --style linux -c watch.list --list-filter failed -V nosummary,noinfo a.html b.html
   FAILED  b.html
```

Um den neuen Zustand als Basislinie zu übernehmen, führen Sie das `-O`-Kommando von oben erneut aus.

<a name="find"></a>

# 10. Dateien über ihren Fingerabdruck finden

**Problem.** Sie wissen, *was* Sie suchen, aber nicht *wo*: eine angreifbare Version einer
Bibliothek, die möglicherweise umbenannt wurde, jede Dublette eines Fotos, ein Schadsoftware-Muster —
oder umgekehrt jede Datei auf einem Server, die nicht auf Ihrer Freigabeliste steht.

Ein Hashwert identifiziert Inhalt unabhängig vom Dateinamen, was Jacksum zu einer Suchmaschine über
Inhalte macht. Bauen Sie die Liste der gesuchten Hashwerte aus Kopien der Artefakte selbst —
`--no-path` hält die Liste frei von den Pfaden, an denen sie zufällig lagen:

```
jacksum -a sha3-256 --style linux --no-path -O wanted.list lib/liba.jar
```

```
feb8f7188233235dedf318bba76c19501170eeaab8e06bf0fed385c87ab5af86 *liba.jar
```

Lassen Sie dann `--wanted-list` so viel von der Platte durchsuchen, wie Sie mögen:

```
jacksum -a sha3-256 --wanted-list wanted.list --style linux --threads-reading max -V nosummary,noinfo /opt /home
    MATCH  /opt/serverapp/lib/renamed.jar (liba.jar)
    MATCH  /home/dev/backup/lib/liba.jar (liba.jar)
```

Der Treffer wird mit dem Namen aus der *Wanted-Liste* in Klammern gemeldet, Sie erfahren also, welches
bekannte Artefakt Sie gefunden haben, egal in was es auf der Platte umbenannt wurde. Beachten Sie,
dass die Wanted-Liste vollständige Prüflisten-Zeilen braucht, keine nackten Hashwerte — eine Datei
mit blanken Hashwerten wird mit `not even one valid entry has been found` abgewiesen. Bauen Sie die
Liste also mit Jacksum und nicht von Hand.

Drehen Sie die Frage mit `--wanted-list-filter negative` um, um alles zu melden, was **nicht** auf der
Freigabeliste steht — so finden Sie die eine Datei in einem Deployment, die niemand erklären kann:

```
jacksum -a sha3-256 --wanted-list approved.list --wanted-list-filter negative --style linux -V nosummary,noinfo /opt/app
 NO MATCH  /opt/app/docs/changes.txt (cc2b01feca9e23a407f40303acd4d65c1720fdbf0e7c6aa9cb38a531dc1f1101)
 NO MATCH  /opt/app/readme.txt (e2f4ffbcc03afc3e53ff0685aa16a18f11977dd01a6176ca3c0ab7c17394f702)
```

Dubletten finden, Schadsoftware-Hashwerte nachschlagen und herausfinden, welcher Algorithmus einen
unbekannten Hashwert erzeugt hat, sind Varianten desselben Prinzips. Siehe
[Objekte finden](EXAMPLES_de.md#find) für diese Fälle, einschließlich des ausgearbeiteten
Log4j-/CVE-2021-44832-Beispiels.

<a name="generate"></a>

# 11. Reproduzierbare Passwörter und Zufallszahlen

**Problem.** Sie brauchen ein starkes, seitenspezifisches Passwort, das Sie nie speichern müssen,
oder eine große zufällig aussehende Zahl für Testdaten.

Eine Hashfunktion ist deterministisch, dieselbe Eingabe liefert also immer dieselbe Ausgabe — was sie
zu einem Werkzeug zur Passwort-*Ableitung* macht. Ein Hauptgeheimnis plus der Name der Seite ergibt
ein Passwort pro Seite, das Sie jederzeit neu berechnen und niemals aufschreiben müssen:

```
jacksum -a sha3-512 -E base64 -q txt:"my-master-secret:github.com"
J3NkKwWpP9/vTb34xSOHDB1fIGGqo1RL0Pruond/qyJTjGyv5EP634wwOro5YnNPwPgCotEJwgsMk0M3fbQ1lw==
```

Ändern Sie den Namen der Seite, und Sie erhalten einen völlig anderen Wert; verlieren Sie die Ausgabe,
dann erzeugen Sie sie aus denselben zwei Teilen neu. Die naheliegende Warnung gilt: das ist nur so
stark wie das Hauptgeheimnis, das Geheimnis landet in Ihrer Shell-History, wenn Sie nicht aufpassen,
und ein einfacher Hash ist keine dafür gebaute Schlüsselableitungsfunktion wie Argon2 oder scrypt —
für einen Passwort-*Tresor* nehmen Sie einen Tresor, für ein merkbares Ableitungsschema funktioniert
das hier.

Derselbe Mechanismus erzeugt große Pseudozufallszahlen, in der Basis, die Sie verlangen:

```
jacksum -a sha3-512 -E dec -q txt:seed42
12327966897560156648912595588607637832676508254087914026218880667581233408187643191090666642961416004716507085865029548101709907696105961020878718352947655
```

Details finden Sie unter [Mehr als Hashing](EXAMPLES_de.md#beyond), und die Kodierungs-
umwandlungen, auf denen das aufbaut, unter [Jacksum Hacks](JACKSUM_HACKS_de.md).

**Table of Contents**
 - [Before you start](#before)
   - [If you are on Windows](#windows)
 - [1. Verify a download or a file transfer](#transfer)
 - [2. Compare two directory trees or discs](#compare)
   - [Both trees at hand](#compare_both)
   - [Only one tree at a time](#compare_offline)
   - [One fingerprint for a whole tree](#compare_onehash)
 - [3. Long-term integrity of archived media](#archive)
 - [4. Is the medium still readable?](#readable)
 - [5. Unidirectional directory synchronisation](#sync)
 - [6. Incremental backups](#incremental)
 - [7. Create a patch for your customers](#patch)
   - [Step by step](#patch_manual)
   - [As a shell script](#patch_script)
   - [As an Ant build file](#patch_ant)
 - [8. Intrusion detection](#ids)
 - [9. Website content change detection](#web)
 - [10. Find files by their fingerprints](#find)
 - [11. Reproducible passwords and random numbers](#generate)

<a name="before"></a>

# Before you start

This document is a **cookbook**. Every section starts with a problem someone actually has, and ends
with the commands that solve it. If you are looking for what a particular option does, you want
[Examples](EXAMPLES.md) instead — that document is organized by feature, this one by goal.

All recipes below have been verified against **Jacksum 4.0.0** on three systems — Ubuntu 24.04 with
OpenJDK 25 and GNU tar 1.35, macOS 26 with OpenJDK 25 and bsdtar 3.5, and Windows 11 build 26200
with OpenJDK 25 and bsdtar 3.8 — and the program output shown is copied from those runs. Where a
value differs between platforms, the text says which platform it came from; the recipes themselves
work everywhere. The Ant build file in [7.3](#patch_ant) is the one thing that was never executed;
everything else, including the `mkpatch.cmd` of [7.2](#patch_script), was run and produced what it
claims. The only edit made after that run is the `-P /` in the batch file's step 1, a flag whose
effect was measured separately.

A few conventions used throughout:

- Commands are written as `jacksum ...`. If you have not put a launcher script on your `PATH`,
  replace `jacksum` with `java -jar jacksum-4.0.0.jar` everywhere.
- Most recipes use `sha3-256`, which is the default algorithm of Jacksum 4. `sha256` is used where
  a list has to be readable by `sha256sum`/`shasum` as well. Always name the algorithm explicitly in
  scripts, because the default may change in a future release.
- `-o <file>` writes the output to a file and **refuses** to overwrite an existing one; `-O <file>`
  overwrites. Recipes that are meant to be run repeatedly use `-O`.
- A trailing `.` means "the current directory and everything below it". Jacksum traverses
  recursively by default.
- Exit codes matter here more than anywhere else: `0` means everything was fine, `1` means at least
  one mismatch, and anything above `1` is an error (a missing file yields `4`, a failed
  `--check-strict` audit yields `6`). See
  [Exit codes](EXAMPLES.md#verify_exitcodes) for the full table.

Several recipes below started life as FAQs on <https://jacksum.net> and were written for Jacksum 1.x.
The commands have been **rewritten for Jacksum 4**, because a handful of options changed meaning in
between. If you are porting an old script of your own, this table is what bit us:

| Jacksum 1.x | Jacksum 4.0.0 |
|---|---|
| `-S` (one hash over a whole tree) | removed, see [2.3](#compare_onehash) |
| `-m` (meta info in the header) | `--header` |
| `-p` (print path info) | removed |
| `-w <dir>` (working directory) | **`--wanted-list <hash-file>`** — a completely different feature; use `cd` or a path argument |
| `-r` (recursive) | recursion is the default; `-r <depth>` now *requires* a depth argument |
| `-f` (regular files only) | `--dont-follow-symlinks-to-files` |
| `-l` (list the differences) | `--list` — but the default filter is `all`, so add `--list-filter bad` |
| `-P <char>` (path separator) | unchanged, and still useful |
| `-E <encoding>` | unchanged — do **not** drop it blindly, see the note below |

The default encoding depends on the algorithm: hexadecimal for `md5`, `sha*` and friends, but
decimal for CRCs and for the classic Unix checksums, so that Jacksum agrees with the native tools.

```
jacksum -a sum_bsd -F "#HASH" readme.txt
35080
jacksum -a sum_bsd -E hex -F "#HASH" readme.txt
8908
```

The first value is what BSD `sum` prints for the same file, and on GNU/Linux plain `sum` prints it
too, because its default *is* the BSD algorithm:

```
sum readme.txt
35080     1 readme.txt
sum -s readme.txt
1260 1 readme.txt
```

`sum -s` selects the System V algorithm, and `1260` is exactly what `jacksum -a sum_sysv` returns.
Use `-E hex` when you want hexadecimal from an algorithm whose native encoding is not hexadecimal,
and leave it out for `sha*`, where it is already the default.

Most examples run against this little tree:

    readme.txt              14 bytes
    version.properties      14 bytes
    docs/manual.txt         16 bytes
    docs/changes.txt        16 bytes
    lib/liba.jar            13 bytes
    lib/libb.jar            13 bytes

<a name="windows"></a>

## If you are on Windows

Jacksum itself behaves the same everywhere — same options, same output, same exit codes. What
differs is everything *around* it: the recipes below pack archives, filter text and count lines, and
the tools they use for that are not the ones Windows ships. Everything in the right-hand column
below is part of a stock Windows installation, so none of the recipes needs a single extra download.

| Task | Unix/macOS | Windows |
|---|---|---|
| archive from a file list | `tar cf x.tar -T list` | `tar -cf x.tar -T list` |
| zip from a file list | `zip -@ x.zip < list` | `tar -a -cf x.zip -T list` |
| gzip-compressed archive | `tar czf x.tar.gz -T list` | `tar -czf x.tar.gz -T list` |
| bzip2-compress | `bzip2 -9 x.tar` | `tar -cjf x.tar.bz2 -T list`, if `tar --version` lists `bz2lib` |
| unpack an archive | `bunzip2 -c x.tar.bz2 \| tar xf -` | `tar -xf x.zip`, `tar -xf x.tar.gz` |
| drop matching lines | `grep -v pattern` | `findstr /V pattern` |
| count lines | `wc -l < f` | `find /c /v "" < f` |
| sort lines | `sort` | `sort` (present) |
| discard stderr | `2>/dev/null` | `2>nul` |
| discard stdout | `> /dev/null` | `> nul` |
| exit code of the last command | `$?` | `%ERRORLEVEL%`, in PowerShell `$LASTEXITCODE` |
| temp / home directory | `/tmp`, `~` | `%TEMP%`, `%USERPROFILE%` |
| delete every file in a list | `while IFS= read -r f; do rm -- "$f"; done < l` | `for /f "usebackq delims=" %f in ("l") do del "%f"` |
| hash one file without Jacksum | `shasum -a 256 f` | `certutil -hashfile f SHA256` |
| verify a hash list without Jacksum | `sha256sum -c list` | nothing built in |
| fetch a URL | `curl -sSL url` | `curl.exe -sSL url` |

`tar.exe` and `curl.exe` have been part of Windows since Windows 10 build 17063; on anything older
you have to bring your own. Note that `tar -a -cf x.zip` in the right-hand column works because
Windows and macOS ship bsdtar — do **not** reach for it on GNU/Linux, where the same command
silently writes a tar archive under a `.zip` name (see [5](#sync)). The Windows blocks below are
written for `cmd`, and a `for` loop typed directly at the prompt uses `%f` where a `.cmd`
script needs `%%f`.

Four things that bite specifically on Windows:

**1. PowerShell pipes are not byte pipes.** When PowerShell pipes one native program into another it
does not forward bytes — it decodes and re-encodes text through `$OutputEncoding`, which is
`us-ascii` in PowerShell 5.1. Anything outside ASCII can be replaced, and a replaced byte gives you
a hash value that belongs to no file on your disk, without a single error message. On top of that,
`sort` in PowerShell is not `sort.exe` at all but an alias for `Sort-Object`, so a pipe you copy
from this document does something subtly different there.

This was measured on two Windows 11 systems, both PowerShell 5.1 with `us-ascii` output encoding,
and they did not agree with each other. On one, the `cmd` pipe and the PowerShell pipe gave the same
value. On the other they differed — and not only for a payload containing non-ASCII bytes but even
for a pipe carrying nothing but hexadecimal:

    via cmd pipe     9b2090e4a0403014750cacaddbea740c4fa3880c1e65f335e1f0fa457b9bf272
    via PowerShell   dec464c4b4683c145eed335ae09cf2073ef65f0c99cca84213867f65ae2515e3

One machine agreeing and another not is the worst possible outcome, because it means you cannot tell
by looking whether your shell is quietly changing the data. Keep to the safe form: run those pipes
in `cmd`, or write the intermediate data to a file and hash the file — going through a file
reproduced the `cmd` value exactly on both machines. Redirection to a file (`>`) is byte-exact in
both shells.

**2. Check what your `tar.exe` was linked against.** `tar --version` prints the compression
libraries the build carries, and that decides which archive formats work. On Windows 11 build 26200
it is generous:

```
tar --version
bsdtar 3.8.4 - libarchive 3.8.4 zlib/1.2.13.1-motley liblzma/5.8.1 bz2lib/1.0.8 libzstd/1.5.7 cng/2.0 libb2/bundled
```

With `bz2lib` present, `tar -cjf x.tar.bz2 -T list` works — so on a current Windows the bzip2 steps
of the Unix recipes need no substitute at all. Older builds shipped without it, so it is worth a
glance before you script around it. Independently of compression, `tar -a -cf` picks the *format*
from the file name extension and `tar -xf` detects the format of an existing archive on its own,
which is why no separate `bunzip2`/`unzip` step appears in the Windows blocks. Verified: `tar -a -cf
patch.zip` really does write a zip on Windows — the file starts with the `PK` magic.

**3. NTFS alternate data streams are invisible by default.** An ADS can be attached to any file or
directory, which makes it a natural hiding place. Jacksum skips them unless you ask for them:

```
jacksum -a sha3-256 --style linux .
5152c4efbbc6b48888a73e0c8ef28399468f482d22ae73b84066416911dc54bc *.\file.txt

jacksum -a sha3-256 --style linux --scan-ntfs-ads .
5152c4efbbc6b48888a73e0c8ef28399468f482d22ae73b84066416911dc54bc *.\file.txt
4318e51e31e286c1f86a15ea75f7652024bffde6d91e34175801ab05586e9df6 *.\file.txt:hidden:$DATA
```

The first run does not even hint that a stream is there. For a comparison that is merely informative
that may be acceptable; for an audit ([2](#compare)) or for intrusion detection ([8](#ids)) it is
not. Note the shape of the extra entry — a list containing `file.txt:hidden:$DATA` is no longer
portable to other platforms, so keep the option out of lists you intend to ship.

**4. Non-ASCII file names need the code page.** In `cmd`, switch to UTF-8 with `chcp 65001` and add
`--utf8` so Jacksum reads and writes the names in UTF-8. See
[File lists](EXAMPLES.md#input_filelist) for a worked example.

**5. Jacksum writes backslashes, this document prints forward slashes.** Every check list and every
`--list` output in the recipes below is shown with Unix paths, because that is what the Linux and
macOS runs produced. On Windows the same commands give you `.\docs\changes.txt` instead:

```
jacksum -a sha3-256 -O hashes.list .
type hashes.list
cc2b01feca9e23a407f40303acd4d65c1720fdbf0e7c6aa9cb38a531dc1f1101 .\docs\changes.txt
```

`-P /` normalises the separator in the list Jacksum **writes**, and that file is then byte-identical
to what this document prints. The Windows blocks below therefore create their lists with `-P /` — it
costs nothing, and the list becomes usable on a non-Windows machine into the bargain, which is the
normal case for a synchronisation or a patch you ship. Such a list verifies on Windows without
complaint.

What `-P /` does *not* change is what Jacksum prints to the screen. The check reports and the
`--list` output shown throughout this document come from the Linux and macOS runs and therefore
carry forward slashes; on Windows the very same commands print `.\readme.txt`. That is harmless —
`tar -T` and `del` accept either form — but do not be surprised when your terminal disagrees with
the page.

Finally, one convenience worth setting up once: put a `jacksum.bat` containing
`@java -jar C:\path\to\jacksum-4.0.0.jar %*` somewhere on your `PATH`, so that `jacksum` works as
written below.

<a name="transfer"></a>

# 1. Verify a download or a file transfer

**Problem.** You downloaded an installer, an `.iso` or a release tarball, and the vendor published a
hash value next to it. Did the file survive the transfer, and is it the file the vendor actually
built?

Paste the expected hash after `-e`:

```
jacksum -a sha256 -e 0df7a0f53c85a97b9e3e08e4ba148b6754d606416fc71a7d8ce853d55c0c6daf readme.txt
    MATCH  readme.txt (0df7a0f53c85a97b9e3e08e4ba148b6754d606416fc71a7d8ce853d55c0c6daf)

Jacksum: Expectation met.
Jacksum: 1 of the successfully read files matches the expected hash value.
```

If the file does not match, Jacksum says so and exits with `6`:

```
jacksum -a sha256 -e 0000000000000000000000000000000000000000000000000000000000000000 readme.txt

Jacksum: Expectation not met.
Jacksum: 0 of the successfully read files match the expected hash value.
```

If what the vendor published is a whole *line* rather than a bare hash, hand the line over verbatim
with `--check-line` and let Jacksum parse it:

```
jacksum -a sha256 --check-line "0df7a0f53c85a97b9e3e08e4ba148b6754d606416fc71a7d8ce853d55c0c6daf *readme.txt" -V nosummary readme.txt
       OK  readme.txt
```

Two things to keep in mind. First, a matching hash only proves that your copy equals *the file the
hash was computed from* — if an attacker can publish the hash, they can publish a matching file, so
the hash has to come over a channel you trust (a signed release page, a second mirror). Second, use
the algorithm the vendor used; if they only offer MD5 or SHA-1, the check still detects transfer
damage, but it is not evidence against a deliberate forgery.

On Windows you may be used to `certutil -hashfile <file> SHA256` or PowerShell's `Get-FileHash`.
Both compute a hash, but neither compares it — and `certutil` hands you the value wrapped in two
lines of prose, localised into the system language:

```
certutil -hashfile readme.txt SHA256
SHA256-Hash von readme.txt:
0df7a0f53c85a97b9e3e08e4ba148b6754d606416fc71a7d8ce853d55c0c6daf
CertUtil: -hashfile-Befehl wurde erfolgreich ausgefuehrt.
```

You still have to eyeball two long hex strings, which is exactly where mistakes happen. `-e` and
`--check-line` do the comparison for you and put the answer in the exit code.

More variants — pipes, BSD-style records, whole lists — are in
[Verify data integrity](EXAMPLES.md#verify).

<a name="compare"></a>

# 2. Compare two directory trees or discs

**Problem.** You copied a tree, burned a DVD, restored a backup, or rsynced a share, and now you
want to know whether the copy is really identical to the original — and if not, exactly which files
differ.

`diff -r` answers this when both trees are mounted at the same time. Jacksum answers it in that case
too, and in two cases `diff` cannot handle: when the two trees are never available simultaneously
(two DVDs, one drive) and when they live on two machines with no connection in between.

<a name="compare_both"></a>

## Both trees at hand

Fingerprint the reference tree, then check the other tree against that list:

```
cd dir1
jacksum -a sha3-256 -O /tmp/dir1.list .

cd ../dir2
jacksum -a sha3-256 -c /tmp/dir1.list .
```

Always `cd` into the tree and pass `.`, so the list contains *relative* paths. A list full of
`/Volumes/BACKUP/...` paths is useless on the next machine, where the same data sits somewhere else.

For a tree that is byte-for-byte identical you get nothing but `OK` lines and exit code `0`:

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

And this is a tree where one file was edited, one deleted and one added:

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

Read the four states as a comparison of two sets: `FAILED` = same name, different content,
`MISSING` = in the reference but not here, `NEW` = here but not in the reference, `OK` = identical.
This run exits with `4`, because a file named in the list could not be read.

If the list has to travel between Windows and Unix, add `-P /` when you create it. That normalizes
the path separator, so the same list works on both sides:

```
jacksum -a sha3-256 -P / -O /tmp/dir1.list .
```

Each platform has one option that decides whether the comparison is *complete*. On Windows, add
`--scan-ntfs-ads` to both commands — an alternate data stream is part of the file system but
invisible to Jacksum by default (see [If you are on Windows](#windows)). On GNU/Linux and other
Unix-like systems, add `--scan-all-unix-file-types`, because by default Jacksum reads regular files,
directories and symbolic links only, and reports anything else as an error rather than hashing it:

```
jacksum -a sha3-256 --style linux .
Jacksum: Error: ./queue.fifo: is not a regular file.
5152c4efbbc6b48888a73e0c8ef28399468f482d22ae73b84066416911dc54bc *./file.txt
```

Block devices, character devices, FIFOs, sockets and Solaris doors are covered by that option. **Be
careful with it on a live system:** reading a FIFO that has no writer blocks, and Jacksum will sit
there forever — which is exactly what you do not want in a cron job. Point it at a data tree, not at
`/dev` or a directory where services keep their sockets.

Whether you follow symbolic links is the other decision, and `/etc` is full of them. `-f` and `-d`
turn following off for links to files and to directories:

```
jacksum -a sha3-256 --style linux -f -d .
Jacksum: Info: Ignoring "./link.txt", because it is a symlink to a file.
Jacksum: Info: Ignoring "./linkdir", because it is a symlink to a dir.
5152c4efbbc6b48888a73e0c8ef28399468f482d22ae73b84066416911dc54bc *./file.txt
976297646d2ff90f920f00940f2b14927b1d50df3941d3db2da36c2bf793b786 *./sub/target.txt
```

Without them, a link into a directory is traversed and its contents appear a second time under the
link's path. Whichever you choose, use the *same* options on both sides — otherwise the two runs are
not comparable.

<a name="compare_offline"></a>

## Only one tree at a time

**Problem.** You want to compare two DVDs but only have one drive. Or the original is on a machine
you cannot reach from the copy.

Nothing changes except *when* things happen — the hash list is the portable stand-in for the tree it
describes. It is a small text file, so it fits on a USB stick, in an e-mail, or in a Git repository:

1. Insert the first disc (or sit down at the first machine) and write the list:

       cd /Volumes/DVD1
       jacksum -a sha3-256 -O ~/dvd1.list .

2. Eject the disc. Insert the second one and check it against the list:

       cd /Volumes/DVD2
       jacksum -a sha3-256 -c ~/dvd1.list .

The report is exactly the one from [2.1](#compare_both). Store the list next to your archive index,
not on the medium it describes — a list on the disc cannot tell you that the disc is unreadable, and
it also shows up in its own listing.

This offline pattern is the foundation of the next four recipes: write a list on the reference side,
carry it over, and let the target side tell you what differs.

<a name="compare_onehash"></a>

## One fingerprint for a whole tree

**Problem.** You do not want a list, you want *one* value you can write on the sleeve of a DVD or
paste into a ticket, and compare by eye.

Jacksum 1.5 had an option `-S` for this; it is gone. Build the value from a pipe instead — hash
every file, sort the hashes, hash the result:

```
jacksum -a sha3-256 --style hexhashes-only . | LC_ALL=C sort | jacksum -a sha3-256 -
258bebd7e2bdf4b72e6a6c422747e1b3c2c3ebe34d13846dfff74713fadcee4e <stdin>
```

The same tree at a different path gives the same value, which is the whole point:

```
cd /elsewhere/copy-of-the-tree
jacksum -a sha3-256 --style hexhashes-only . | LC_ALL=C sort | jacksum -a sha3-256 -
258bebd7e2bdf4b72e6a6c422747e1b3c2c3ebe34d13846dfff74713fadcee4e <stdin>
```

Change a single byte anywhere in the tree and the value changes completely:

```
dce61e54592e0ddef7b67c8aa0445f1c2bca3532d419634b712b32d230e3dd37 <stdin>
```

`sort` is not decoration. Jacksum does not promise an order in which it walks a tree, and the same
six files really do come back in a different order on different systems:

    macOS 26, APFS   docs/changes.txt docs/manual.txt version.properties lib/libb.jar lib/liba.jar readme.txt
    Ubuntu 24.04, ext4   readme.txt version.properties lib/liba.jar lib/libb.jar docs/changes.txt docs/manual.txt

Neither is alphabetical, neither is depth-first, and they are not the same. Without `sort` the two
machines would compute two different fingerprints for identical data.

`LC_ALL=C` is not decoration either, and this one is easy to miss. `sort` collates according to the
locale, so the *same* list can come out in a different order on two machines whose `LC_COLLATE`
differs. With `hexhashes-only` that is harmless in practice, because the lines are nothing but hex
digits. With the name-aware variant below it is not: two files with identical content produce the
same hash, so the file name breaks the tie, and locale collation orders case and punctuation
differently from byte order. Measured on Ubuntu 24.04 against a tree holding `Alpha.txt`,
`alpha.txt` and `ALPHA2.txt`, all with the same content:

    LC_ALL=C            738d840891fcc6424bd042e81fdf449d137ae59f5d493ea635ebd1ee30a9b842
    LC_ALL=en_US.UTF-8  48e147fe9e816152eb325f8e7ac9af8ca9c2046fc0fe5d4d4346a3d6151ef5d5

Two fingerprints for one tree — in a recipe whose entire purpose is comparing two machines.
`LC_ALL=C` pins the order to byte order, which is the same everywhere, and none of the values shown
in this section changes because of it.

A case-insensitive file system hides half of the problem. On macOS with APFS, or on Windows with
NTFS, `Alpha.txt` and `alpha.txt` are the *same* file, so that tree holds two files instead of three
and both values come out different from the ones above. The collation effect does not go away, it
just has fewer names left to disagree about — one more reason to pin the locale rather than to
reason about it.

**This fingerprint does not travel between Windows and Unix.** Windows `sort.exe` writes CRLF line
endings where Jacksum wrote LF, so the six sorted lines grow from 390 to 396 bytes and the hash over
them changes. The same tree therefore gives:

    Ubuntu 24.04, macOS 26   258bebd7e2bdf4b72e6a6c422747e1b3c2c3ebe34d13846dfff74713fadcee4e
    Windows 11               9b2090e4a0403014750cacaddbea740c4fa3880c1e65f335e1f0fa457b9bf272

Neither value is wrong — hashing the LF form gives the first, hashing the CRLF form gives the
second, and everything the recipe promises still holds *within* one platform: a changed byte and a
renamed file are detected exactly as described above. But a single value from a Windows machine
cannot be compared against one from a Linux machine. For that direction use the list of
[2.1](#compare_both) instead: it carries one line per file, and with `-P /` it comes out
byte-identical on all three systems.

`hexhashes-only` throws the file names away, so this variant is blind to renames: rename
`readme.txt` to `README.md` and the fingerprint stays `258bebd7...`. If names are part of what you
are comparing, use a style that carries them:

```
jacksum -a sha3-256 --style linux . | LC_ALL=C sort | jacksum -a sha3-256 -
61510463ec3b388476a553cbbaff82283cb1c4740fe662d41f2cab06cbf368a4 <stdin>
```

After the same rename this one *does* change, to `a77e39dc...`.

On Windows this pipe works in `cmd` — `sort` is there, and both `jacksum` invocations are the same
as above. Do **not** run it in PowerShell: the pipe between the two native programs re-encodes the
text and the resulting value is meaningless. If PowerShell is all you have, write the intermediate
list to a file and hash the file:

```
jacksum -a sha3-256 --style hexhashes-only . > ..\unsorted.txt
sort ..\unsorted.txt > ..\sorted.txt
jacksum -a sha3-256 -F "#HASH" ..\sorted.txt
258bebd7e2bdf4b72e6a6c422747e1b3c2c3ebe34d13846dfff74713fadcee4e
```

Redirection to a file is byte-exact in both shells; only the program-to-program pipe is not. Note
the `..\` — the intermediate files must land **outside** the tree, otherwise the first command
hashes its own output file and the value changes. That is the same trap as in [3](#archive), and it
is easy to walk into here because the pipe version has no file to misplace.

The trade-off against [2.1](#compare_both) is information: one value tells you *whether* two trees
differ, never *where*. Use it as a cheap tripwire, and keep the full list around for when it fires.

<a name="archive"></a>

# 3. Long-term integrity of archived media

**Problem.** You are burning data you intend to read in fifteen years. By then the operating system,
the tooling and possibly the hardware will have changed. Will you still be able to tell whether the
disc is intact?

The hash values are not the fragile part — the *file format* is. Write the list in a format that
tools other than Jacksum can read, and the check survives you dropping Jacksum entirely:

```
jacksum -a sha256 --style linux --header -O SHA256SUMS .
```

That produces a plain `sha256sum`-compatible list with a documentation block on top:

```
#
# created by: Jacksum (https://jacksum.net, version: 4.0.0)
# invoked on JVM: OpenJDK 64-Bit Server VM (vendor: Ubuntu, version: 25.0.3+9-2-24.04.2-Ubuntu)
# invoked on OS: Linux (arch: amd64, version: 7.0.0-30-generic)
# invoked on date: 2026-08-25T14:59:09.144+02:00
#
# invoked from: /mnt/archive-2026
# invocation args: -a sha256 --style linux --header -O SHA256SUMS .
#________________________________________________________________________
0df7a0f53c85a97b9e3e08e4ba148b6754d606416fc71a7d8ce853d55c0c6daf *./readme.txt
112773e2a370ee8a61667937e79f4f223ef5fe4db4504cb7ec1a5256060cf975 *./version.properties
213bb7ff99ae7fd27edfcd55ab5be34c2c8ab79264ac1bce46c50e060e837eee *./lib/liba.jar
4d8095c96f86709e5c5b9291ac6e2ca77488d0ccfeb3d4fbcac383d5eac5e527 *./lib/libb.jar
d0e2dc2e66b82a670659736963da9a56feeb25d78d79eda405bcbd84b37d711c *./docs/changes.txt
0b398916a560e8c357b8d7374bd93dd7865d0c528ed842abc47413d2cfb0bc70 *./docs/manual.txt
```

`--header` is what makes this future-proof: the algorithm, the Jacksum version, the platform and the
date are recorded in plain text, so whoever finds the disc knows what to do with the numbers. Lines
starting with `#` are comments, and every `sha256sum`-compatible tool skips them:

```
sha256sum -c SHA256SUMS         # GNU/Linux
shasum -a 256 -c SHA256SUMS     # macOS, *BSD
./readme.txt: OK
./version.properties: OK
./lib/liba.jar: OK
./lib/libb.jar: OK
./docs/changes.txt: OK
./docs/manual.txt: OK
```

Practical notes:

- **Pick a boring algorithm.** `sha256` is the safe bet for archives, precisely because it is
  implemented everywhere. `sha3-256` is the better hash function, but in 2041 you may be holding the
  disc and a machine that only has `sha256sum`. Nothing stops you from writing both lists.
- **On Windows, burn `jacksum.jar` onto the disc as well.** The argument above — pick a format that
  outlives the tool — only half applies on Windows, because Windows has never shipped a hash *list*
  verifier. `certutil -hashfile` and `Get-FileHash` handle one file at a time and cannot read a
  `SHA256SUMS` file at all, so on a bare Windows machine there is nothing to hand the list to.
  Jacksum is a single self-contained JAR; adding it to the medium costs a few megabytes and removes
  the problem. A `README.txt` next to it naming the command to run is worth the two lines.
- **Burn the list onto the disc *and* keep a copy elsewhere.** On the disc it travels with the data;
  off the disc it is still readable when the disc is not.
- **Use relative paths** (`cd` into the tree, pass `.`), and `-P /` if the disc will also be read on
  Windows.
- **Do not put the list inside the tree you are hashing** unless you accept that it will report
  itself as `NEW` on every check.

<a name="readable"></a>

# 4. Is the medium still readable?

**Problem.** An old backup drive, a decade-old DVD, a USB stick you found in a drawer. Before you
trust any of it, you want to know whether every byte can still be read at all — independently of
whether the content is *correct*.

`-a read` reads every byte of every file and discards it. Anything that goes wrong on the way — a
scratch, bit rot, a permission problem, a network share that dropped out — surfaces as an error:

```
jacksum -a read -V summary,errors -r max . > /dev/null
Jacksum: Error: ./locked.bin (Permission denied)

Jacksum: total files read successfully: 1
Jacksum: total bytes read: 3
Jacksum: total bytes read (human readable): 3 bytes
Jacksum: total file read errors: 1
```

Exit code `4` and `total file read errors` make it scriptable. Note that `-a none` will *not* find
those files, because it never opens them.

If you also have a hash list from section 3, run the check instead — that answers "readable *and*
unchanged" in one pass. Use `-a read` when there is no list, or when you want to survey a medium
before spending time on the comparison.

`-u <file>` collects the damaged files into a list you can act on. See
[Is everything on the medium still readable?](JACKSUM_HACKS.md#medium-readable) for the details.

<a name="sync"></a>

# 5. Unidirectional directory synchronisation

**Problem.** Two machines hold what should be the same directory. One of them is right (call it
`good`), the other has drifted (call it `bad`) — files were edited, deleted, or added by accident.
There is no network connection between the two, only the ability to carry a file across: a USB
stick, an e-mail attachment, an air-gapped transfer.

The trick is that you never need both trees at once. You need a hash list from `good`, a difference
list from `bad`, and an archive from `good`. Three files travel; nothing else does.

**Step 1 — on the good machine, fingerprint the reference tree.**

```
cd good
jacksum -a sha3-256 -O /tmp/hashes.list .
```

Carry `/tmp/hashes.list` to the faulty machine.

**Step 2 — on the faulty machine, ask what is wrong.**

```
cd bad
jacksum -a sha3-256 -c /tmp/hashes.list --list-filter bad --list . > /tmp/files.list 2>/dev/null
```

`--list-filter bad` narrows the report to `failed`, `missing` and `error`; `--list` reduces each
line to nothing but the path. What you get is a plain list of the files that need to be replaced:

```
./docs/changes.txt
./readme.txt
```

The command exits with `1` or `4` — that is the *expected* outcome here, not a failure, so do not
let `set -e` kill your script at this line. Errors go to standard error, which is why the redirect
matters: without `2>/dev/null` the `Jacksum: Error: ... does not exist.` lines would be interleaved
in your terminal (they would not end up in `files.list`, but they are noise).

Carry `/tmp/files.list` back to the good machine.

**Step 3 — on the good machine, pack exactly those files.**

```
cd good
tar cf /tmp/patch.tar -T /tmp/files.list      # GNU/Linux, macOS
bzip2 -9 /tmp/patch.tar
```

On Solaris and older BSD `tar`, the option for "read the file names from this file" is `-I` rather
than `-T`. Be careful: in GNU `tar`, `-I` means *use this compression program*, so the two are not
interchangeable. `tar` itself is on every system worth the name; `zip` and `bzip2` are **not** part
of a minimal GNU/Linux install (Debian netinst, Alpine, RHEL-minimal all leave them out), so on a
container or a freshly provisioned box `tar -czf` is the variant that just works. If you prefer a
zip archive:

```
cd good
zip -@ /tmp/patch.zip < /tmp/files.list
```

On Windows, do the whole step with the bundled `tar`, which writes zip archives directly — there is
no `zip` command to pipe into:

```
cd good
jacksum -a sha3-256 -P / -O %TEMP%\hashes.list .
tar -a -cf %TEMP%\patch.zip -T %TEMP%\files.list
```

The `-P /` in the first line is what makes `%TEMP%\hashes.list` look like the listing above. It does
not affect `files.list`, which comes from `--list` and keeps native separators — `tar -T` reads it
either way.

`-a` picks the format from the `.zip` extension. Use `.tar.gz` with `tar -czf` instead if you prefer
a tarball; `.tar.bz2` is likely unavailable, see [If you are on Windows](#windows).

**Step 4 — on the faulty machine, unpack over the tree.**

```
cd bad
bunzip2 -c /tmp/patch.tar.bz2 | tar xf -
```

On Windows, one command does it — `tar -xf` recognises zip, gzip and tar archives on its own, so
neither `bunzip2` nor `unzip` is needed:

```
cd bad
tar -xf %TEMP%\patch.zip
```

That works because Windows and macOS ship bsdtar. **GNU `tar` has no zip support at all**, so on a
GNU/Linux box the same command fails:

```
tar -xf patch.zip
tar: This does not look like a tar archive
tar: Exiting with failure status due to previous errors
```

Use `unzip patch.zip` there instead. Creating is worse, because it fails *silently*: `tar -a -cf
patch.zip -T files.list` writes a zip with bsdtar, but GNU `tar`'s `-a` only chooses between gzip,
bzip2 and xz — it does not know the zip format, so it hands you a plain tar archive that merely
happens to be called `.zip`, and it exits 0 without a word of warning. `file patch.zip` then says
`POSIX tar archive (GNU)`. If the two sides of your synchronisation run different operating systems,
`.tar.gz` is the format both understand without an extra package.

The archive contains the good versions of every modified file and every file that had gone missing,
with their relative paths, so extracting it in place repairs both cases at once.

**Step 5 — deal with the files that should not be there.**

This is the step the original FAQ never mentioned, and it matters: `--list-filter bad` covers
`failed`, `missing` and `error`, but **not** `new`. Files that exist only on the faulty machine are
invisible to steps 2–4 and survive the repair. Ask for them separately:

```
cd bad
jacksum -a sha3-256 -c /tmp/hashes.list --list-filter new --list . > /tmp/obsolete.list 2>/dev/null
```

```
./lib/obsolete.jar
```

Review that list before you act on it — this is the one destructive step in the recipe, and a
`--list-filter new` entry is also what a legitimate local file looks like. Then, if you really want
the two trees identical:

```
cd bad
while IFS= read -r f; do rm -- "$f"; done < /tmp/obsolete.list
```

On Windows:

```
cd bad
for /f "usebackq delims=" %f in ("%TEMP%\obsolete.list") do del "%f"
```

Double the percent signs (`%%f`) if you put that line into a `.cmd` file rather than typing it at
the prompt.

**Step 6 — confirm.**

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

Six `OK` lines and exit code `0`: the faulty machine has become a good machine.

Why "unidirectional"? Because `good` is the authority and `bad` is overwritten. Jacksum tells you
*that* two trees differ, never *which side is newer* — there are no modification-time heuristics and
no conflict resolution. If both sides hold changes you want to keep, this is the wrong tool; if one
side is the truth, it is a very small and very portable one.

<a name="incremental"></a>

# 6. Incremental backups

**Problem.** A full backup of your data takes hours. You want the daily run to archive only what
actually changed since the last one.

The usual approach is `find -newer`, which trusts modification times. Jacksum compares *content*,
which catches three things timestamps do not: files that were changed and then had their mtime
reset, files that were touched without being changed (and would be backed up for nothing), and
silent data corruption, where the bytes rot while the metadata stays pristine.

**Once — the full backup and the baseline.**

```
cd data
tar czf /backup/full.tar.gz .
jacksum -a sha3-256 -O /backup/base.list .
```

**Every run — ask what changed, archive that, then move the baseline forward.**

```
cd data
jacksum -a sha3-256 -c /backup/base.list --list-filter failed,new --list . > /backup/changed.list 2>/dev/null
```

`failed,new` is the right filter here: `failed` are the files whose content changed, `new` are the
files that did not exist at baseline time. `missing` is deliberately excluded — a deleted file is
nothing to put in an archive (if you need to replay deletions, capture that list separately with
`--list-filter missing --list`).

On a quiet day the list is empty and the command exits `0`:

```
wc -l < /backup/changed.list
       0
```

After editing `readme.txt` and adding `docs/notes.txt` it exits `1` and contains:

```
./readme.txt
./docs/notes.txt
```

Pack those, then update the baseline so tomorrow's run compares against today:

```
tar czf /backup/inc-$(date +%Y%m%d).tar.gz -T /backup/changed.list
jacksum -a sha3-256 -O /backup/base.list .
```

The Windows equivalent of the whole run, with `2>nul` in place of `2>/dev/null`:

```
cd data
jacksum -a sha3-256 -c C:\backup\base.list --list-filter failed,new --list . > C:\backup\changed.list 2>nul
for /f %d in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd"') do set STAMP=%d
tar -a -cf C:\backup\inc-%STAMP%.zip -T C:\backup\changed.list
jacksum -a sha3-256 -P / -O C:\backup\base.list .
```

The detour through PowerShell is there because `%DATE%` is formatted according to the machine's
locale. On a German Windows it comes out as `25.08.2026` — dots in a file name, and it sorts by day
rather than by year. `Get-Date -Format yyyyMMdd` gives `20260825` regardless of locale. To check
whether anything changed at all, `find /c /v "" < C:\backup\changed.list` replaces `wc -l`; it
prints `0` for an empty list and `2` for the two changed files above.

Note the `-O`: the baseline is rewritten on every run, so `-o` (which refuses to overwrite) would
fail on day two.

Order matters. Update the baseline **after** the archive was written successfully — if you update it
first and the `tar` then fails, those changes are recorded as backed up and will never be picked up
again. Keep the old baseline until the archive is verified, and store baselines outside the tree
they describe.

Restoring means unpacking the full backup and then every increment in order. Verifying a restore is
one command: `jacksum -a sha3-256 -c /backup/base.list .` against the restored tree.

<a name="patch"></a>

# 7. Create a patch for your customers

**Problem.** You are shipping version 4.1.0 of a product whose customers have 4.0.0 installed. The
full distribution is 400 MB; the actual change is 3 MB. You want to ship 3 MB.

This is section 5 with different words: the new version is the reference, the old version is the
target, and the "patch" is the archive of files that differ. Jacksum's part is deciding *which*
files those are — by content, so files that were rebuilt but did not change do not bloat the patch.

<a name="patch_manual"></a>

## Step by step

**1. Fingerprint the new version.**

```
cd ~/newversion
jacksum -a sha3-256 -O /tmp/new.list .
```

**2. Ask the old version what differs.**

```
cd ~/oldversion
jacksum -a sha3-256 -c /tmp/new.list --list-filter bad --list . > /tmp/files.list 2>/dev/null
```

```
./lib/libc.jar
./version.properties
./lib/libb.jar
```

`version.properties` and `lib/libb.jar` changed between the releases; `lib/libc.jar` is new in 4.1.0
and therefore reported as `missing` from the old version's point of view. All three belong in the
patch, which is exactly what `--list-filter bad` selects.

**3. Pack those files — from the new version.**

```
cd ~/newversion
tar cf /tmp/patch.tar -T /tmp/files.list      # GNU/Linux, macOS
bzip2 -9 /tmp/patch.tar
```

On Solaris and older BSD `tar`, use `-I` instead of `-T` (and see the warning in
[section 5](#sync) — GNU `tar` uses `-I` for something else entirely). For a zip archive:

```
cd ~/newversion
zip -@ /tmp/patch.zip < /tmp/files.list
```

On Windows, the same three steps are:

```
cd %USERPROFILE%\newversion
jacksum -a sha3-256 -P / -O %TEMP%\new.list .

cd %USERPROFILE%\oldversion
jacksum -a sha3-256 -c %TEMP%\new.list --list-filter bad --list . > %TEMP%\files.list 2>nul

cd %USERPROFILE%\newversion
tar -a -cf %TEMP%\patch.zip -T %TEMP%\files.list
```

A zip is the friendlier choice for customers on Windows anyway: Explorer opens it without any extra
software, and `tar -xf patch.zip` works from the command line. For customers on GNU/Linux ship
`.tar.gz` instead — GNU `tar` cannot read a zip, so a zip would force them to install `unzip`, while
`tar -xzf` needs nothing.

Your customers unpack that over their installation. What they should run afterwards is the list from
step 1, which you may as well ship inside the patch:

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

Seven `OK` lines: the 4.0.0 installation is now byte-for-byte a 4.1.0 installation.

One caveat, the same one as in section 5: files that 4.1.0 *removed* are not in the patch. If your
release deletes files, generate that list too and ship it as an uninstall step:

```
cd ~/oldversion
jacksum -a sha3-256 -c /tmp/new.list --list-filter new --list . > /tmp/remove.list 2>/dev/null
```

<a name="patch_script"></a>

## As a shell script

The three steps wrapped up, with the exit codes handled:

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

The `|| true` on step 2 is the part people get wrong. With `set -e` and without it, the script dies
on the very command that is supposed to find differences.

The same thing as a `.cmd` file for Windows:

```bat
@echo off
rem mkpatch.cmd -- create a patch that upgrades OLDDIR to the state of NEWDIR.
rem usage: mkpatch.cmd OLDDIR NEWDIR OUTDIR [ALGORITHM]
setlocal
set OLD=%~f1
set NEW=%~f2
set OUT=%~f3
set ALGO=%4
if "%ALGO%"=="" set ALGO=sha3-256
if not exist "%OLD%\" goto usage
if not exist "%NEW%\" goto usage
if not exist "%OUT%\" goto usage

rem 1. fingerprint the new version
del "%OUT%\new.list" 2>nul
pushd "%NEW%"
call jacksum -a %ALGO% -P / -O "%OUT%\new.list" .
popd
rem Do not trust the exit code alone: if the launcher itself fails -- missing jar, no java on the
rem PATH -- it exits with 1, which "if errorlevel 2" would wave through. Check the artefact instead.
if not exist "%OUT%\new.list" (
    echo %~nx0: could not fingerprint "%NEW%" -- is jacksum on the PATH? >&2
    exit /b 1
)

rem 2. ask the old version which files differ or are missing.
rem    Exit code 1 or 4 is the expected outcome here, so it is not checked.
pushd "%OLD%"
call jacksum -a %ALGO% -c "%OUT%\new.list" --list-filter bad --list . > "%OUT%\files.list" 2>"%OUT%\check.log"
popd

for %%F in ("%OUT%\files.list") do if %%~zF EQU 0 (
    echo %~nx0: the two versions are identical, no patch needed.
    exit /b 0
)

rem 3. pack those files -- taken from the NEW version
pushd "%NEW%"
tar -a -cf "%OUT%\patch.zip" -T "%OUT%\files.list"
popd
echo %~nx0: patch written to %OUT%\patch.zip
exit /b 0

:usage
echo usage: %~nx0 OLDDIR NEWDIR OUTDIR [ALGORITHM] >&2
exit /b 2
```

`cmd` has no `set -e`, so the error handling is explicit and inverted compared to the shell version:
step 1 is checked with `if errorlevel 2` (which is true for 2 and above, letting the harmless codes
through), while step 2 is deliberately left unchecked. The `%%~zF` trick reads the size of
`files.list` and stands in for `[ ! -s ... ]`.

Two details here were learned the hard way, and both produce a *silent* wrong result, which is the
worst kind.

**The `call` in front of both `jacksum` lines is not decoration.** If `jacksum` on your `PATH` is
the `jacksum.bat` launcher suggested in [If you are on Windows](#windows), then it is itself a batch
file — and a batch file that invokes another batch file *without* `call` hands over control and
never gets it back. The script ends at its first `jacksum` line: no file list, no archive, no
message, and an exit code of `0` because nothing failed. `tar` needs no `call`, being a real
executable.

**Checking `errorlevel` is not enough after step 1.** If the launcher itself cannot start — a jar
path that no longer resolves after `pushd`, or no `java` on the `PATH` — it exits with `1`, and
`if errorlevel 2` waves that through. The script then finds an empty `files.list`, concludes that
the two versions are identical and exits `0`, having done nothing. Testing for the artefact instead
of the exit code catches it, which is why step 1 ends with `if not exist "%OUT%\new.list"`. If you
write your own launcher, give it an **absolute** path to the jar: `pushd` changes the working
directory under it.

The `-P /` in step 1 is there for the same reason as in [7.1](#patch_manual): `new.list` travels
with the patch, and a customer on GNU/Linux cannot verify a list full of backslashes. It costs
nothing on Windows, where such a list verifies just as well.

<a name="patch_ant"></a>

## As an Ant build file

Girish Narang and Johann N. Löfflmann developed an Ant-based patch creator, still published as
[build.xml](https://jacksum.net/downloads/build.xml) and
[build.properties](https://jacksum.net/downloads/build.properties). Those files were written for
Jacksum 1.x and **do not work with Jacksum 4** — `-m`, `-p` and `-w .` have gone or changed meaning,
and step 2's bare `-l` would write the whole file list instead of just the differences, so the
"patch" would contain the complete distribution. Here is the same build file, ported:

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

with `build.properties`:

```properties
distro.old.dir=/home/user/project/version1/
distro.new.dir=/home/user/project/version2/
patch.dir.home=/home/user/project/patch/
jacksum.jar.file=/usr/local/jacksum/jacksum-4.0.0.jar
```

Two changes are worth calling out. `--list-filter bad` in step 2 is not optional — without it,
Jacksum 4 lists every checked file, including the identical ones. And `new.list` is kept rather than
deleted in step 6, because it is what your customers need in order to verify the result.

The Jacksum invocations above were verified individually against 4.0.0; the Ant target itself was
not run, as Ant was not installed on the machine used for this document.

<a name="ids"></a>

# 8. Intrusion detection

**Problem.** You want to know whether anything on a system was modified, deleted or added behind
your back — configuration files, binaries, web roots — and you want the answer to include metadata,
because a change that preserves file size and timestamp is exactly what someone trying to stay
hidden would aim for.

Sections 2 and 5 compare content. For this job, use `--style full`, which records the hash, the
timestamp, the size and the name, and — unlike a hand-rolled `-F` format — is a real check-list
style, so Jacksum can read it back with `-c`:

```
cd /etc
jacksum --style full -a sha3-256 -O /secure/baseline.txt .
```

```
#
# created by: Jacksum (https://jacksum.net, version: 4.0.0)
# invoked on JVM: OpenJDK 64-Bit Server VM (vendor: Ubuntu, version: 25.0.3+9-2-24.04.2-Ubuntu)
# invoked on OS: Linux (arch: amd64, version: 7.0.0-30-generic)
# invoked on date: 2026-08-24T22:54:10.338+02:00
#
# invoked from: /etc
# invocation args: --style full -a sha3-256 -O /secure/baseline.txt .
#________________________________________________________________________
c0e06825459ca99f6c8ec6e1d7640154ad685dc6acaf2e4ed06932f814e5711b 2026-08-24T22:54:10.270+02:00 14 ./sshd_config
423ed5f6208655c68319a9affcd2816f9d39076c236f29850d5483fb74a2ea0e 2026-08-24T22:54:10.270+02:00 16 ./hosts
7adc7b8d11576c0b7a07fd26120cba27a127b3a8684edf2da99f503938038f63 2026-08-24T22:54:10.270+02:00 11 ./passwd
```

Later, after someone edited `sshd_config`, reset the timestamp on `hosts` without changing its
content, and dropped a `backdoor.conf` into the directory:

```
jacksum --style full -a sha3-256 -c /secure/baseline.txt --no-header --list-filter bad,new -V nosummary .
Jacksum: Info: Option --compat/--style has been set, setting implicitly -a sha3-256 -E hex, stdin-name=<stdin>
      NEW  ./backdoor.conf
   FAILED  ./sshd_config
           [filesize expected: 14, actual: 15]
   FAILED  ./hosts
           [timestamp expected: 2026-08-24T22:54:10.270+02:00, actual: 2026-01-01T12:00:00.000+01:00]
```

All three are caught, and Jacksum says *why* each one failed. Note that the `hosts` line is a pure
metadata change — the content hash still matches — and a content-only check would have reported it
as `OK`. Whether you want that depends on the system: on `/etc` a timestamp that moved backwards is
suspicious, while on a build tree it is noise.

Details that make the difference between a working tripwire and a false sense of security:

- **`--list-filter bad,new` is the filter you want.** `bad` covers `failed`, `missing` and `error`;
  `new` adds files that appeared. Leaving out `new` means a planted file goes unreported.
- **Keep the reason lines.** They are emitted at info verbosity, so `-V noinfo` (or
  `-V nosummary,noinfo`) hides them. `-V nosummary` alone, as above, keeps them — at the price of
  the `Jacksum: Info: Option --compat/--style has been set ...` notice, which rides on the same
  verbosity level.
- **The baseline is the crown jewel.** Store it off the monitored machine, or at minimum outside the
  monitored tree and on read-only media. An attacker who can rewrite the baseline can make the check
  pass for anything.
- **On GNU/Linux, run it as `root` — or know what you are missing.** A tree like `/etc` holds files
  that only `root` may read (`shadow`, `sudoers`, `ssl/private`). As an ordinary user Jacksum
  reports the error and exits with `4`, but the more dangerous part is quiet: the unreadable file is
  simply **absent** from the baseline, so it is never monitored at all.

  ```
  jacksum --style full -a sha3-256 -O /secure/baseline.txt .
  Jacksum: Error: ./shadow (Permission denied)
  ```

  The baseline that this produced contains `passwd` and no `shadow`. Run the baseline and the check
  with the same privileges, and use `-u <file>` to collect what could not be read so that the gap is
  on record instead of invisible.
- **On GNU/Linux, decide about `--scan-all-unix-file-types` and `-f`/`-d`.** Both are explained in
  [2.1](#compare_both). For an audit the file types matter — a new FIFO or device node in a
  monitored directory is exactly the kind of thing you want to hear about — but mind the warning
  there about FIFOs blocking, and prefer `-f -d` on trees like `/etc` so that a symlink cannot pull
  half the file system into your baseline.
- **On Windows, add `--scan-ntfs-ads` to both the baseline and the check.** An alternate data stream
  can carry a payload while the file it hangs off looks untouched, and Jacksum does not look for
  them unless asked. For intrusion detection that is the difference between a tripwire and a
  decoration. Keep the option identical in both runs, or every stream shows up as `NEW` the first
  time you switch it on.
- **Exit codes drive the alerting.** `0` means clean, `1` means at least one mismatch, `4` means
  something could not be read. In a cron job, `jacksum ... || alert` is the whole integration; on a
  systemd distribution a timer unit plus `OnFailure=` gives you the same thing with logging in
  `journalctl`; on Windows, register the check with `schtasks /create` and let the wrapper `.cmd`
  test `%ERRORLEVEL%`.
- **For an audit, add `--check-strict`.** It also fails on malformed lines in the baseline, and
  exits with `6`:

  ```
  jacksum --style full -a sha3-256 -c /secure/baseline.txt --no-header --check-strict -V nosummary,noinfo .
        NEW  ./backdoor.conf
     FAILED  ./sshd_config
     FAILED  ./hosts
         OK  ./passwd
  ```

  On an untouched tree the same command prints only `OK` lines and exits `0`.
- **Timestamps only go so far.** Someone with write access can restore a timestamp as easily as they
  can change it; what they cannot do is restore the content hash. The metadata is a convenience, the
  hash is the evidence.
- **Know what this does not cover.** `--style full` records content, timestamp, size and name — and
  nothing else. Permissions, owner, group, ACLs and extended attributes are outside it, so
  `chmod 777 /etc/shadow` or a `chown` on a binary passes the check as `OK`, although either is a
  first-rate indicator of a compromise. On Linux that is the line between Jacksum and a dedicated
  host IDS: AIDE and Tripwire were built to watch those attributes and to keep their database
  signed. Jacksum's strength here is that it is one portable JAR with no database and no
  installation, which makes it excellent for an ad-hoc check, a locked-down box, or a second
  opinion — and a good companion to AIDE rather than a replacement. If you want the attributes as
  well, capture them separately (`stat`, `getfacl`) into a list of their own and compare that the
  same way.

If you want change detection *without* hashing — much faster on large trees, much weaker as
evidence — `--style without-hashes` gives you the same workflow on timestamps and sizes alone. See
[Snapshot a directory and detect changes later](JACKSUM_HACKS.md#snapshot).

<a name="web"></a>

# 9. Website content change detection

**Problem.** You want to be told when a page changes: a vendor's security advisories, a
release-notes page, a licence text, a competitor's pricing.

Fetch and hash in one pipe — no temporary file needed:

```
curl -sSL https://example.org/page.html | jacksum -a sha3-256 -F "#HASH" -
b7cd39e7ec6c22c24e0938098d66516e1dcefe3a59457aed834717bc157a63d5
```

Neither `curl` nor `wget` is guaranteed on a minimal GNU/Linux install or in a slim container image;
which of the two is present depends on the distribution and the image. A desktop Ubuntu has both.
`wget -qO- https://example.org/page.html` writes the same bytes to standard output as
`curl -sSL`, so whichever you find feeds the same pipe.

Store that value, and from then on compare against it:

```
curl -sSL https://example.org/page.html | jacksum -a sha3-256 -e b7cd39e7ec6c22c24e0938098d66516e1dcefe3a59457aed834717bc157a63d5 -
    MATCH  <stdin> (b7cd39e7ec6c22c24e0938098d66516e1dcefe3a59457aed834717bc157a63d5)

Jacksum: Expectation met.
Jacksum: 1 of the successfully read files matches the expected hash value.
```

Exit code `0` means unchanged, `6` means the expectation was not met — so a cron job is one line:

```sh
curl -sSL "$URL" | jacksum -a sha3-256 -e "$KNOWN" -V nosummary - \
    || mail -s "$URL changed" me@example.org < /dev/null
```

On Windows, download to a file instead of piping. `curl.exe` is there, but a pipe into another
native program is not byte-safe under PowerShell, and an HTML page is exactly the kind of input that
contains non-ASCII bytes:

```
curl.exe -sSL %URL% -o %TEMP%\page.html
jacksum -a sha3-256 -e %KNOWN% -V nosummary %TEMP%\page.html
if errorlevel 1 echo %URL% changed
```

Hashing the file rather than the stream is the more robust form on every platform, and it leaves you
with the changed page on disk so you can look at what actually moved.

**The catch: most pages change on every request.** A visitor counter, a rotating ad, a rendered
timestamp, a CSRF token — any of those makes the hash different every time and turns your monitor
into a permanent alarm:

```
curl -sSL https://example.org/page.html | jacksum -a sha3-256 -e b7cd39e7ec6c22c24e0938098d66516e1dcefe3a59457aed834717bc157a63d5 -

Jacksum: Expectation not met.
Jacksum: 0 of the successfully read files match the expected hash value.
```

Nothing anyone cares about changed there — the counter went from 41234 to 41235. Cut the volatile
part out *before* hashing, and take the baseline from the filtered form:

```
curl -sSL https://example.org/page.html | grep -v 'Visitor counter' | jacksum -a sha3-256 -F "#HASH" -
00efe6aedd332466d08f8836ebce0eed9c1986ffd999747f38f429621e2b05f7
```

Now the counter can do what it likes:

```
curl -sSL https://example.org/page.html | grep -v 'Visitor counter' | jacksum -a sha3-256 -e 00efe6aedd332466d08f8836ebce0eed9c1986ffd999747f38f429621e2b05f7 -
    MATCH  <stdin> (00efe6aedd332466d08f8836ebce0eed9c1986ffd999747f38f429621e2b05f7)
```

while a real edit — "Version 4.0.0 is out" becoming "Version 4.1.0 is out" — still trips it:

```
Jacksum: Expectation not met.
Jacksum: 0 of the successfully read files match the expected hash value.
```

`grep -v` is the crude version; `sed` on a line, or piping through a text extractor, filters more
precisely. Whatever you choose, apply the *same* filter when you take the baseline and when you
check, or every run will differ.

Windows has no `grep`, but `findstr /V` does the same job, and going through files keeps it
byte-safe:

```
curl.exe -sSL %URL% -o %TEMP%\page.html
findstr /V "Visitor counter" %TEMP%\page.html > %TEMP%\filtered.html
jacksum -a sha3-256 -e %KNOWN% -V nosummary %TEMP%\filtered.html
```

`findstr` matches literal text by default and `/V` inverts the match, so this drops every line
containing `Visitor counter`. Add `/R` for a regular expression, and `/I` to ignore case. Unlike
`sort.exe` in [2.3](#compare_onehash), `findstr` leaves the line endings alone: the filtered page
hashes to `00efe6ae...` on Windows too, the same value as on Linux and macOS.

**Watching several pages at once.** Save the downloads under stable names and keep one list, exactly
as in section 2:

```
jacksum -a sha3-256 --style linux -O watch.list a.html b.html
```

```
0073a1763a2d9b034ba9d7d0369758cf479111b49c5fcff4dce47739b6b5114c *a.html
ec62d3442f9275575499fabf10644ffa460121d518f0a3274bde02a110bde606 *b.html
```

Re-download and check; `--list-filter failed` reports only what moved:

```
jacksum -a sha3-256 --style linux -c watch.list --list-filter failed -V nosummary,noinfo a.html b.html
   FAILED  b.html
```

To adopt the new state as the baseline, re-run the `-O` command from above.

<a name="find"></a>

# 10. Find files by their fingerprints

**Problem.** You know *what* you are looking for but not *where* it is: a vulnerable version of a
library that may have been renamed, every duplicate of a photo, a malware sample, or conversely
every file on a server that is not on your approved list.

A hash identifies content regardless of the file name, which turns Jacksum into a search engine over
content. Build a list of the hashes you are hunting for from copies of the artifacts themselves —
`--no-path` keeps the list free of the paths they happened to sit at:

```
jacksum -a sha3-256 --style linux --no-path -O wanted.list lib/liba.jar
```

```
feb8f7188233235dedf318bba76c19501170eeaab8e06bf0fed385c87ab5af86 *liba.jar
```

Then let `--wanted-list` sweep as much of the disk as you like:

```
jacksum -a sha3-256 --wanted-list wanted.list --style linux --threads-reading max -V nosummary,noinfo /opt /home
    MATCH  /opt/serverapp/lib/renamed.jar (liba.jar)
    MATCH  /home/dev/backup/lib/liba.jar (liba.jar)
```

The match is reported with the name from the *wanted list* in parentheses, so you learn which known
artifact you found no matter what it has been renamed to on disk. Note that the wanted list needs
complete check-list lines, not bare hash values — a file of naked hashes is rejected with
`not even one valid entry has been found`, so build the list with Jacksum rather than by hand.

Turn the question around with `--wanted-list-filter negative` to report everything that is **not**
on the approved list — that is how you find the one file in a deployment nobody can account for:

```
jacksum -a sha3-256 --wanted-list approved.list --wanted-list-filter negative --style linux -V nosummary,noinfo /opt/app
 NO MATCH  /opt/app/docs/changes.txt (cc2b01feca9e23a407f40303acd4d65c1720fdbf0e7c6aa9cb38a531dc1f1101)
 NO MATCH  /opt/app/readme.txt (e2f4ffbcc03afc3e53ff0685aa16a18f11977dd01a6176ca3c0ab7c17394f702)
```

Finding duplicates, looking up malware hashes, and identifying which algorithm produced an unknown
hash are all variations on this. See [Find objects](EXAMPLES.md#find) for those, including the
worked Log4j/CVE-2021-44832 case.

<a name="generate"></a>

# 11. Reproducible passwords and random numbers

**Problem.** You need a strong, site-specific password that you never have to store, or a large
random-looking number for a test fixture.

A hash function is deterministic, so the same input always yields the same output — which makes it a
password *derivation* tool. One master secret plus the site name gives you a per-site password you
can always recompute and never need to write down:

```
jacksum -a sha3-512 -E base64 -q txt:"my-master-secret:github.com"
J3NkKwWpP9/vTb34xSOHDB1fIGGqo1RL0Pruond/qyJTjGyv5EP634wwOro5YnNPwPgCotEJwgsMk0M3fbQ1lw==
```

In `cmd` the quoting is different: there are no single quotes, so write
`jacksum -a sha3-512 -E base64 -q txt:"my-master-secret:github.com"` with double quotes. A `%` in
the secret survives a typed command line unchanged — measured, `-q txt:"a%b"` produces the hash of
the three bytes `a%b`. What you must not do is double it there: `-q txt:"a%%b"` hashes something
else entirely. The doubling rule belongs to `.cmd` **files**, where `%` introduces a parameter and
`%%` is how you write a literal one. Same secret, different result depending on where you typed it —
which is a good reason to keep `%` out of a secret you use on Windows at all.

Change the site name and you get an unrelated value; lose the output and you regenerate it from the
same two pieces. The obvious warning applies: this is only as strong as the master secret, the
secret ends up in your shell history unless you take care, and a plain hash is not a
purpose-built KDF like Argon2 or scrypt — for a password *vault* use a vault, for a memorable
derivation scheme this works.

The same mechanism produces large pseudo-random numbers, in whatever base you ask for:

```
jacksum -a sha3-512 -E dec -q txt:seed42
12327966897560156648912595588607637832676508254087914026218880667581233408187643191090666642961416004716507085865029548101709907696105961020878718352947655
```

See [Beyond hashing](EXAMPLES.md#beyond) for the details, and
[Jacksum Hacks](JACKSUM_HACKS.md) for the encoding conversions this builds on.

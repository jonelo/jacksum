**Table of Contents**
 - [Before you start](#before)
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

All recipes below have been verified against **Jacksum 4.0.0** on macOS with OpenJDK 25; the program
output shown is copied from those runs.

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

The first value is what BSD `sum` prints for the same file. Use `-E hex` when you want hexadecimal
from an algorithm whose native encoding is not hexadecimal — and leave it out for `sha*`, where it
is already the default.

Most examples run against this little tree:

    readme.txt              14 bytes
    version.properties      14 bytes
    docs/manual.txt         16 bytes
    docs/changes.txt        16 bytes
    lib/liba.jar            13 bytes
    lib/libb.jar            13 bytes

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

Jacksum 1.5 had an option `-S` for this; it is gone. Build the value from a pipe instead — hash every
file, sort the hashes, hash the result:

```
jacksum -a sha3-256 --style hexhashes-only . | sort | jacksum -a sha3-256 -
258bebd7e2bdf4b72e6a6c422747e1b3c2c3ebe34d13846dfff74713fadcee4e <stdin>
```

The same tree at a different path gives the same value, which is the whole point:

```
cd /elsewhere/copy-of-the-tree
jacksum -a sha3-256 --style hexhashes-only . | sort | jacksum -a sha3-256 -
258bebd7e2bdf4b72e6a6c422747e1b3c2c3ebe34d13846dfff74713fadcee4e <stdin>
```

Change a single byte anywhere in the tree and the value changes completely:

```
dce61e54592e0ddef7b67c8aa0445f1c2bca3532d419634b712b32d230e3dd37 <stdin>
```

`sort` is not decoration. Jacksum does not promise an order in which it walks a tree — in the runs
above the raw order was `docs/changes.txt`, `docs/manual.txt`, `version.properties`, `lib/libb.jar`,
`lib/liba.jar`, `readme.txt`, which is neither alphabetical nor depth-first. Without `sort` the same
data on a different filesystem can produce a different value.

`hexhashes-only` throws the file names away, so this variant is blind to renames: rename
`readme.txt` to `README.md` and the fingerprint stays `258bebd7...`. If names are part of what you
are comparing, use a style that carries them:

```
jacksum -a sha3-256 --style linux . | sort | jacksum -a sha3-256 -
61510463ec3b388476a553cbbaff82283cb1c4740fe662d41f2cab06cbf368a4 <stdin>
```

After the same rename this one *does* change, to `a77e39dc...`.

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

`--header` is what makes this future-proof: the algorithm, the Jacksum version, the platform and the
date are recorded in plain text, so whoever finds the disc knows what to do with the numbers. Lines
starting with `#` are comments, and every `sha256sum`-compatible tool skips them:

```
shasum -a 256 -c SHA256SUMS
./docs/changes.txt: OK
./docs/manual.txt: OK
./version.properties: OK
./lib/libb.jar: OK
./lib/liba.jar: OK
./readme.txt: OK
```

Practical notes:

- **Pick a boring algorithm.** `sha256` is the safe bet for archives, precisely because it is
  implemented everywhere. `sha3-256` is the better hash function, but in 2041 you may be holding the
  disc and a machine that only has `sha256sum`. Nothing stops you from writing both lists.
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
interchangeable. If you prefer a zip archive:

```
cd good
zip -@ /tmp/patch.zip < /tmp/files.list
```

On Windows, the same step reads:

```
cd good
type files.list | zip -@ patch.zip
```

**Step 4 — on the faulty machine, unpack over the tree.**

```
cd bad
bunzip2 -c /tmp/patch.tar.bz2 | tar xf -
```

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
which catches three things timestamps do not: files that were changed and then had their mtime reset,
files that were touched without being changed (and would be backed up for nothing), and silent data
corruption, where the bytes rot while the metadata stays pristine.

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
- **Exit codes drive the alerting.** `0` means clean, `1` means at least one mismatch, `4` means
  something could not be read. In a cron job, `jacksum ... || alert` is the whole integration.
- **For an audit, add `--check-strict`.** It also fails on malformed lines in the baseline, and exits
  with `6`:

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

If you want change detection *without* hashing — much faster on large trees, much weaker as
evidence — `--style without-hashes` gives you the same workflow on timestamps and sizes alone. See
[Snapshot a directory and detect changes later](JACKSUM_HACKS.md#snapshot).

<a name="web"></a>

# 9. Website content change detection

**Problem.** You want to be told when a page changes: a vendor's security advisories, a release-notes
page, a licence text, a competitor's pricing.

Fetch and hash in one pipe — no temporary file needed:

```
curl -sSL https://example.org/page.html | jacksum -a sha3-256 -F "#HASH" -
b7cd39e7ec6c22c24e0938098d66516e1dcefe3a59457aed834717bc157a63d5
```

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

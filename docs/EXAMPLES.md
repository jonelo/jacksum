**Table of Contents**
 - [Before you start](#before)
 - [1. Getting started](#getting_started)
   - [Hash one file](#gs_one_file)
   - [Hash many files and directory trees](#gs_many_files)
   - [Select the algorithm](#gs_algorithm)
   - [Select the encoding of the hash value](#gs_encoding)
 - [2. Input other than files](#input)
   - [Standard input and pipes](#input_stdin)
   - [Sequences on the command line](#input_sequences)
   - [File lists](#input_filelist)
   - [String lists](#input_stringlist)
 - [3. Output and formats](#output)
   - [Predefined styles](#output_styles)
   - [User defined formats](#output_formats)
   - [Timestamps, separators, paths, and grouping](#output_misc)
   - [Where the output goes](#output_files)
   - [Formats of other tools](#output_othertools)
 - [4. Verify data integrity](#verify)
   - [Verify one file against one known hash](#verify_one)
   - [Create a hash list and verify it later](#verify_list)
   - [OK, FAILED, MISSING, and NEW](#verify_states)
   - [Strict verification (audit)](#verify_strict)
   - [Filter the report](#verify_filter)
   - [Verify lists that were created by other tools](#verify_foreign)
   - [Exit codes](#verify_exitcodes)
 - [5. Find objects](#find)
   - [Find all duplicates of a file](#find_duplicates)
   - [Find a file by its hash](#find_byhash)
   - [Find files by a list of known hashes](#find_wantedlist)
   - [Negative matching](#find_negative)
   - [Find strings that match a hash](#find_strings)
   - [Find malware by hash values](#find_malware)
   - [Find the algorithm that generated a hash value](#find_algorithm)
 - [6. HMAC](#hmac)
   - [Calculate an HMAC](#hmac_calculate)
   - [Where the key comes from](#hmac_key)
   - [Truncated HMACs](#hmac_truncated)
   - [Verify an HMAC](#hmac_verify)
   - [Interoperability with other tools](#hmac_interop)
 - [7. Beyond hashing](#beyond)
   - [Reproducible, unique, secure passwords](#beyond_passwords)
   - [Large pseudo-random numbers](#beyond_random)
   - [Encoding conversions](#beyond_encodings)
 - [8. Customize CRCs](#crcs)
   - [6 parameters](#crcs_6)
   - [7 parameters](#crcs_7)
   - [8 parameters](#crcs_8)
 - [9. Performance and traversal control](#performance)
 - [10. Gather information](#info)
   - [About one algorithm](#info_one_algo)
   - [Investigate CRC parameters](#info_crc)
   - [About many algorithms](#info_many_algos)
   - [About styles and encodings](#info_styles)
   - [About the program](#info_program)
   - [Navigate the help](#info_help)

<a name="before"></a>

# Before you start

All examples below have been verified against **Jacksum 4.0.0**. They are ordered from easy to
hard: section 1 needs nothing but a file, section 10 is reference material you look up when you
need it.

A few conventions used throughout:

- Commands are written as `jacksum ...`. If you have not put a launcher script on your `PATH`,
  replace `jacksum` with `java -jar jacksum-4.0.0.jar` everywhere.
- **The default algorithm is SHA3-256.** It is used whenever `-a` is omitted. In scripts you
  should always name the algorithm explicitly, because the default may change in a future release.
- Short and long option names are equivalent: `-a`/`--algorithm`, `-c`/`--check-file`,
  `-e`/`--expect`, `-C`/`--compat`/`--style`, `-w`/`--wanted-list`. The long forms are preferred
  below because they read better.
- A trailing `.` means "the current directory and everything below it". Jacksum traverses
  recursively by default.
- Quoting differs between shells. In `bash`/`zsh` use single quotes to preserve the literal value
  of every character within the quotes; on the Windows `cmd` shell use double quotes.

See also the [Manpage](https://github.com/jonelo/jacksum/wiki/Manpage) for the authoritative
description of every option (`jacksum -h`), [Use Cases](USE_CASES.md) for complete recipes that
solve one problem end to end, and
[Jacksum Hacks](JACKSUM_HACKS.md) for the things Jacksum can do
that have nothing to do with hashing.

<a name="getting_started"></a>

# 1. Getting started

A hash function H maps a bit string m ∈ {0, 1}<sup>*</sup> of arbitrary length to a bit string
h ∈ {0, 1}<sup>n</sup> of fixed length n ∈ ℕ:

    h = H(m)

m is often called the message or the data and, depending on the design and the security strength
of the hash function H, h is called the checksum, CRC, hash, hash value, message digest, data's
fingerprint, or data's thumbprint.

Calculating hash values is usually the first step you take to be able to check data integrity at
all later on.

<a name="gs_one_file"></a>

## Hash one file

```
jacksum ubuntu-26.04-desktop-amd64.iso
```

Calculates the SHA3-256 hash (the default algorithm) of one file. The output is
`<hash> <filename>`.

```
jacksum -a sha3-256 ubuntu-26.04-desktop-amd64.iso
40822f93d646a7644f24be73f21b2998ee5ffd1ff16cb49a319dcd9d5538c508 ubuntu-26.04-desktop-amd64.iso
```

The same, with the algorithm named explicitly. This is what you want in scripts.

<a name="gs_many_files"></a>

## Hash many files and directory trees

```
jacksum -a crc32 -x *.txt
```

Calculates a 32 bit CRC of all text files in the current folder, printed in hexadecimal (`-x`).
Wildcard expansion is done by your shell; Jacksum also does it itself on shells that do not.

```
jacksum -a sha3-256 .
```

Traverses the current directory and everything below it. Directory traversal is recursive by
default; use `-r <depth>` to limit it.

```
jacksum -a cksum /mnt/share
```

Calculates a 32 bit CRC with the standard Unix `cksum` algorithm of all files under `/mnt/share`
and its subfolders.

<a name="gs_algorithm"></a>

## Select the algorithm

```
jacksum -a sha-256 .
```

Calculates a 256 bit hash with 5 rounds using the SHA-256 algorithm.

```
jacksum -a sha-256+sha3-256 .
```

Calculates the SHA-256 and SHA3-256 in **one pass over the data**. By default the individual hash values
are concatenated to one hash value, which makes integrity checks on a single value easy.

```
jacksum -a sha-256+sha3-256 -F "SHA-256=#HASH{0}  SHA3-256=#HASH{1} #FILENAME" .
```

The same two algorithms, but printed separately. `{0}` and `{1}` index the algorithms in the order
you named them.

```
jacksum -a sha-256+sha3-256 -F "SHA-256=#HASH{sha-256}  SHA3-256=#HASH{sha3-256} #FILENAME" .
```

The same, but with a string index `{sha-256}` and `{sha3-256} rather than integers.

```
jacksum -a all:sha -F "#ALGONAME{i}(#FILENAME) = #HASH{i}" .
```

Algorithm filtering: `all:<string>` selects every algorithm whose ID contains `<string>`, and `{i}`
iterates over all of them. Note that `all:sha` in Jacksum 4.0.0 also matches `ascon-hasha`,
`sha0`, `shabal*` and `shake*` — use `jacksum -a all:sha --list` first to see what you get.

```
jacksum -a sha-1+sha-224+sha-256+sha-384+sha-512+sha-512/224+sha-512/256+sha3-224+sha3-256+sha3-384+sha3-512 \
        -F "#ALGONAME{i}(#FILENAME) = #HASH{i}" .
```

If you want exactly the SHA-1, SHA-2 and SHA-3 family and nothing else, name them explicitly.
`jacksum -a all:sha --list` gives you the IDs to copy from.

<a name="gs_encoding"></a>

## Select the encoding of the hash value

By default the encoding depends on the algorithm: a classic checksum is usually printed in decimal,
a one-way hash function in hex. Pin it down explicitly whenever the output is going to be parsed
again later.

```
jacksum -a crc32 -x -q txt:123456789
jacksum -a crc32 -X -q txt:123456789
```

`-x` is lowercase hex, `-X` is uppercase hex.

```
jacksum -a sha3-256 -E base64 file.dat
```

`-E` accepts `bin`, `dec`, `oct`, `hex`, `hex-uppercase`, `base16`, `base32`, `base32-nopadding`,
`base32hex`, `base32hex-nopadding`, `base64`, `base64-nopadding`, `base64url`,
`base64url-nopadding`, `z-base-32`, `z85`, and `bb`/`bubblebabble`. `jacksum -h -E` documents each
of them.

<a name="input"></a>

# 2. Input other than files

Jacksum hashes almost any input: files, file trees, command line arguments, plain strings, encoded
strings, the console, standard input, NTFS ADS, pipes, sockets, doors, partitions, and disks.

<a name="input_stdin"></a>

## Standard input and pipes

A single hyphen as the file name means "read from standard input".

```
echo -n "Hello World" | jacksum -V summary -
```

Calculates a SHA3-256 hash from stdin. `-V summary` is worth enabling here, because it shows how
many bytes were actually read. Beware that `echo` behaves differently across platforms — the `-q`
option (below) is the platform independent way.

```
printf "Hello World\r\n" | jacksum -
```

`printf` is a shell builtin in GNU/Linux shells such as `bash` and `zsh` and behaves more
predictably than `echo`.

```
jacksum -a md5 -
```

Calculates the MD5 hash from input typed in the terminal. End the input with Ctrl+D on GNU/Linux
and macOS, Ctrl+Z on Windows.

```
cat fat.iso | jacksum -
```

Prints the SHA3-256 of a binary file on GNU/Linux and macOS. Use `type` rather than `cat` on
Microsoft Windows.

<a name="input_sequences"></a>

## Sequences on the command line

`-q` (`--quick`) processes one sequence and quits. The optional `<type>` prefix tells Jacksum how
to interpret it; without a prefix the sequence is expected to be hex.

```
jacksum -q txt:"The quick brown fox jumps over the lazy dog"
```

Unformatted text, interpreted in the platform's default character set. `\n` counts as two
characters (backslash and "n"), not as one.

```
jacksum -q txtf:"Hallo Welt\r\n"
```

Formatted text, always interpreted as UTF-8, with escape sequences `\t \n \r \" \' \\ \xHH`.

```
jacksum -a crc32 -q 48656C6C6F20576F726C6421
jacksum -a crc32 -q hex:48,65,6C,6C,6F,20,57,6F,72,6C,64,21
```

A hex sequence — here the bytes of `Hello World!`. Values may be separated by commas or spaces, or
not at all as long as the 8 bit boundaries are unambiguous.

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

Binary, decimal, octal, Base32, Base64, Base64url, z-base-32, and Z85 input.

```
jacksum -q file:myfile.img
```

Reads all bytes of one file into memory and hashes them. Limited to 128 MiB, because this mode also
feeds the algorithm finder (see [below](#find_algorithm)) which must avoid I/O. To hash large files,
pass them as normal program arguments instead.

```
jacksum -a crc64 -q txt:
```

Calculates a CRC-64 of an empty string. The result is the same as for a file with a size of 0 bytes.

```
jacksum -V summary -q txtf:abc\n -F "#ALGONAME(#SEQUENCE) = #HASH" -x
```

Because `#SEQUENCE` and `-V summary` are set, you see the input as hex **and** the number of bytes
that were read — useful when you are not sure what your shell handed over.

```
jacksum -q readline -F "#HASH msg=#MESSAGE msglen=#LENGTH hex=#SEQUENCE{hex}"
```

Reads one line from the console, echoing what you type. Handy for investigating strings and
encodings. `-q password` does the same without echoing (see
[Reproducible, unique, secure passwords](#beyond_passwords)).

<a name="input_filelist"></a>

## File lists

```
jacksum --file-list filelist.txt
```

For each file name in `filelist.txt`, read the file and calculate the hashes. Such lists are
produced by `find` on GNU/Linux and Unix, by `dir /b` in `cmd`, by `dir -n` in PowerShell — or by
Jacksum itself with `--style files-only`.

```
jacksum --file-list filelist.txt *.mp3 *.info myfolder
```

A file list and normal program arguments can be combined freely.

```
chcp 65001 & echo "a filename that contains unicode chars" | jacksum --utf8 --file-list - --file-list-format ssv
```

On the Windows `cmd` shell, switch the code page to UTF-8 and pipe file names into Jacksum.
`--file-list-format ssv` means space separated values (names containing spaces are enclosed in
double quotes), and `--utf8` makes Jacksum read the names in UTF-8.

<a name="input_stringlist"></a>

## String lists

```
jacksum -a sha3-256 --string-list words.txt
271878f8a927b4566ac951fc815b18dfad8d0302d61d11d80cbe15b7a3a056af alpha
f0277d92062bd9a41dd26cddbaf2c41d576cf7b0173cbe96c23d5f5a4f92cc8f beta
6dfbbc6ef6895dcd07e69effe2a7486bccd7a75609f39c08e7b3a55d399d3955 gamma
```

Hashes every **line** of `words.txt` as a string, rather than treating the lines as file names.
See also [Find strings that match a hash](#find_strings) and
[Large pseudo-random numbers](#beyond_random).

<a name="output"></a>

# 3. Output and formats

Jacksum supports 18 predefined styles for reading and writing check files, 17 encodings, 6
timestamp formats, and a fully customizable output format.

<a name="output_styles"></a>

## Predefined styles

`--style` (a.k.a. `-C`/`--compat`) sets the algorithm, the encoding and the layout in one go, and
it doubles as the **parser definition** when you read a list back in (see
[section 4](#verify)). All examples use the same file so that you can compare the shapes:

```
jacksum -a sha3-256 ubuntu-22.04-desktop-amd64.iso
c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43 ubuntu-22.04-desktop-amd64.iso
```

Default style.

```
jacksum -a sha3-256 --style linux ubuntu-22.04-desktop-amd64.iso
c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43 *ubuntu-22.04-desktop-amd64.iso
```

GNU/Linux style, as produced by `sha256sum` and friends. The `*` marks binary mode.

```
jacksum -a sha3-256 --style bsd ubuntu-22.04-desktop-amd64.iso
SHA3-256 (ubuntu-22.04-desktop-amd64.iso) = c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43
```

BSD style.

```
jacksum -a sha3-256 --style openssl ubuntu-22.04-desktop-amd64.iso
SHA3-256(ubuntu-22.04-desktop-amd64.iso)= c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43
```

OpenSSL style, as produced by `openssl dgst`.

```
jacksum -a sha3-256 --style solaris-digest ubuntu-22.04-desktop-amd64.iso
(ubuntu-22.04-desktop-amd64.iso) = c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43
```

Solaris `digest -v` style.

```
jacksum --style sfv -O list.sfv *
```

Simple File Verificator format — CRC-32 in uppercase hex, file name first.

```
jacksum -a sha3-256 --style hdb --no-path ./malware/
```

ClamAV `sigtool` hash database format (`hash:filesize:filename`).

The full set is `bsd`, `bsd-r`, `fciv`, `files-only`, `full`, `gnu-linux` (alias `linux`), `hdb`,
`hexhashes-only`, `openssl-dgst` (alias `openssl`), `openssl-dgst-r`, `sfv`, `sizes-and-names`,
`solaris-digest`, `solaris-digest-v`, `timestamps-and-names`, `without-hashes`, `without-sizes`,
`without-timestamps`. You can also write your own — see
[File Format of Styles](https://github.com/jonelo/jacksum/wiki/File-Format-of-Styles).

Styles and `-a` compose: if a style supports a user-specified algorithm, you can keep the layout
and swap the algorithm.

```
jacksum -a sha3-256 --style sfv .
```

SHA3-256 hashes in the legacy SFV layout.

<a name="output_formats"></a>

## User defined formats

`-F` (`--format`) gives you full control. The most important placeholders are `#HASH` (alias
`#CHECKSUM`, `#DIGEST`, `#FINGERPRINT`), `#HASHES`, `#ALGONAME`, `#ALGONAMES`, `#FILENAME`,
`#FILESIZE`, `#TIMESTAMP`, `#SEQUENCE`, `#LENGTH`, `#SEPARATOR`, and `#QUOTE`. `jacksum -h -F`
documents all of them.

Each placeholder takes modifiers in braces: an **index** (`{0}`, `{1}`, ... or `{i}` to iterate),
an **algorithm name** (`{sha1}`), an **encoding** (`{base64}`), and `{uppercase}` / `{name}`.

```
jacksum -a crc32c+sha-256+sha3-256 -F "#ALGONAME{i,uppercase} (#FILENAME) = #HASH{i,base64-nopadding}" .
CRC32C (./kali-linux-2023.1-installer-amd64.iso) = dUWxuQ
SHA-256 (./kali-linux-2023.1-installer-amd64.iso) = RuBXOaILKdtgyh//LpBoXqYyBxwxSpwkFtfEas7ye/A
SHA3-256 (./kali-linux-2023.1-installer-amd64.iso) = ffPkMr8uVPCO5GIHm8YpAbmaOVeBMaxvdLPI/N4NkbE

CRC32C (./ubuntu-22.04.2-desktop-amd64.iso) = hIXQsw
SHA-256 (./ubuntu-22.04.2-desktop-amd64.iso) = uY2slAqCsRDmJlynjRMg8fcQOGHpIqoaVOQgJobpu9M
SHA3-256 (./ubuntu-22.04.2-desktop-amd64.iso) = bvOhwtwckCQuzgm4LLEJoqPbjcbWSRSuNUOcyY4/1L0
```

`{i}` iterates: one line per algorithm, per file.

```
jacksum -a crc32c+sha-256+sha3-256 -F "#ALGONAMES{uppercase} (#FILENAME) = #HASHES{base64-nopadding}" .
CRC32C,SHA-256,SHA3-256 (./kali-linux-2023.1-installer-amd64.iso) = dUWxuQ,RuBXOaILKdtgyh//LpBoXqYyBxwxSpwkFtfEas7ye/A,ffPkMr8uVPCO5GIHm8YpAbmaOVeBMaxvdLPI/N4NkbE
```

The plural placeholders `#ALGONAMES`/`#HASHES` put everything on one line, comma separated.

```
jacksum -a crc32c+sha-256+sha3-256 -F "#ALGONAMES{uppercase} (#FILENAME) = #HASH{base64-nopadding}" .
CRC32C,SHA-256,SHA3-256 (./kali-linux-2023.1-installer-amd64.iso) = dUWxuUbgVzmiCynbYMof/y6QaF6mMgccMUqcJBbXxGrO8nvwffPkMr8uVPCO5GIHm8YpAbmaOVeBMaxvdLPI/N4NkbE
```

Singular `#HASH` without an index is the **concatenation** of all three hash values, encoded as one
value. That is what makes an integrity check on a single string possible.

```
jacksum -a sha1+sha1+sha3-256 -s \n \
        -F "#ALGONAME{0}/hex: #HASH{0,hex} #FILENAME{name}#SEPARATOR#ALGONAME{1}/base32: #HASH{1,base32} #FILENAME{name}#SEPARATOR#ALGONAME{2}/base64: #HASH{2,base64} #FILENAME{name}#SEPARATOR" \
        *.txt
```

You want hex **and** base32 for SHA-1, and base64 for SHA3-256? Name `sha1` twice. Each text file is
still read only once, and SHA-1's engine still runs only once per file.

```
jacksum -a blake2b+sha3-512 -q txtf:123456789\x0a -E hex -g 1 -F "$(cat template.txt)"
```

`-F` accepts a multi-line template. With `template.txt` containing

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

both the input and both hash values are printed in three encodings each.

<a name="output_misc"></a>

## Timestamps, separators, paths, and grouping

```
jacksum -a crc32 -t default .
909783072 6 20260816195904048 a.txt
```

`-t` adds the file's timestamp. Besides `default` there are `iso8601`, `unixtime`, and free-form
patterns.

```
jacksum -a sha1 -s "\t" -t "EEE, MMM d, yyyy 'at' h:mm a" .
```

A custom timestamp pattern, with the tabulator as the field separator (`-s`).

```
jacksum -a sha256 -P / -F "<a href=\"#FILENAME\">#HASH</a><br>" mp4s
```

`-P` forces the path separator, so you get forward slashes even on Windows.

```
jacksum -a none -q "txt:Hello World" -F "#SEQUENCE" -E hex -g 1
48 65 6c 6c 6f 20 57 6f 72 6c 64
```

`-g <count>` groups the encoded bytes; `-G <char>` changes the group separator.

<a name="output_files"></a>

## Where the output goes

```
jacksum -a sha3-256 -o hashes.list /data
```

Writes the output to `hashes.list`. Because the path started with `/`, the paths in the list are
absolute. `-o` refuses to overwrite an existing file; `-O` overwrites.

```
jacksum -a sha3-256 -o hashes.list data
```

The same, but the paths are stored relatively, because the path did not start with `/`.

```
jacksum -a blake3+ -E base64 -t iso8601 -O hashes.list data
```

The `+` suffix on the algorithm adds the file size to the output. Together with `-t iso8601` the
list carries hash, size and timestamp — which is what makes a later check able to distinguish a
changed file from a merely touched one.

```
jacksum --header -a sha3-256 --style linux -O log4j.hashes --no-path .
```

`--header` prepends a provenance comment block. `--no-path` stores bare file names.

<details>
<summary>Content of the generated file ...</summary>

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

Placeholders in the **output file name** are expanded, here to `hashes-sha3-256.txt`. Useful when
you loop over algorithms.

```
jacksum -a sha3-256 -U errors.log .
```

`-u`/`-U` redirect the error channel to a file, so that stdout stays a clean hash list.

<a name="output_othertools"></a>

## Formats of other tools

```
jacksum -a md5+sha1 -F "MD5-SHA1(#FILENAME)= #HASH{hex}" file
```

Produces the same output as `openssl dgst -md5-sha1 file`.

```
jacksum -a crc32c+sha-256+sha3-256 -F "#FILESIZE,#HASHES{hex},#FILENAME" .
3875536896,7545b1b9,46e05739a20b29db60ca1fff2e90685ea632071c314a9c2416d7c46acef27bf0,7df3e432bf2e54f08ee462079bc62901b99a39578131ac6f74b3c8fcde0d91b1,./kali-linux-2023.1-installer-amd64.iso
4927586304,8485d0b3,b98dac940a82b110e6265ca78d1320f1f7103861e922aa1a54e4202686e9bbd3,6ef3a1c2dc1c90242ece09b82cb109a2a3db8dc6d64914ae35439cc98e3fd4bd,./ubuntu-22.04.2-desktop-amd64.iso
```

The hashdeep format (`filesize,hash1,...,hashN,filename`), but with modern algorithms.

```
jacksum -a ed2k -F "ed2k://|file|#FILENAME{name}|#FILESIZE|#HASH{hex}|/" .
```

Produces ed2k links.

```
jacksum -a ed2k -P / -F "<a href=#QUOTEed2k://|file|#FILENAME|#FILESIZE|#HASH{hex}|#QUOTE>#FILENAME</a>" .
```

The same as HTML. `#QUOTE` inserts a double quote without fighting your shell over it.

```
jacksum -a tth+ed2k+sha1+md5 -F "magnet:?xl=#FILESIZE&dn=#FILENAME{name}&xt=urn:tree:tiger:#HASH{tth,base32}&xt=urn:ed2k:#HASH{ed2k,hex}&xt=urn:bitprint:#HASH{sha1,base32}.#HASH{tth,base32}&xt=urn:sha1:#HASH{sha1,base32}&xt=urn:md5:#HASH{md5,hex}" -
```

Produces magnet links. Note that the placeholders are indexed **by algorithm name** here, which is
more readable than `{0}`..`{3}` when there are four of them.

```
jacksum -a sum_sysv -E dec -t unixtime -F "1 i #FILENAME{name} #FILESIZE #HASH #TIMESTAMP" install/*
1 i a.txt 6 542 1786903144
```

Entries compatible with the syntax of a Solaris 10+ `pkgmap` file — useful if you want to patch a
Solaris patch.

```
jacksum -a tree:tiger -F "urn:#ALGONAME:#HASH" -q hex:
urn:tree:tiger:LWPNACQDBZRYXW3VHJVCJ64QBZNGHOHHHZWCLNQ
```

The root hash of a Tiger Tree Hash (a widely used form of the Merkle tree), here over an empty
input.

<a name="verify"></a>

# 4. Verify data integrity

Data integrity ensures that data items have not been changed, destroyed, or lost in an unauthorized
or accidental manner since they were created, transmitted, or stored.

As a file/data integrity tool, Jacksum can generate, store, and compare hash values in order to
detect changes made to files. It detects matching, non-matching, missing, and new files.

<a name="verify_one"></a>

## Verify one file against one known hash

From the Ubuntu website we know the SHA-256 hash of `ubuntu-22.04.1-desktop-amd64.iso`. We expect
that the file we downloaded is that file. There are three ways to say so.

### By `--expect` / `-e`

```
jacksum -a sha256 -e c396e956a9f52c418397867d1ea5c0cf1a99a49dcf648b086d2fb762330cc88d ubuntu-22.04.1-desktop-amd64.iso
    MATCH  ubuntu-22.04.1-desktop-amd64.iso (c396e956a9f52c418397867d1ea5c0cf1a99a49dcf648b086d2fb762330cc88d)

Jacksum: Expectation met.
Jacksum: 1 of the successfully read files matches the expected hash value.
```

The simplest form: you have a hash, you have a file. If you pass multiple files or directories,
Jacksum finds **all** files that match — see [section 5](#find).

### By `--check-line`

```
jacksum -a sha256 --check-line "c396e956a9f52c418397867d1ea5c0cf1a99a49dcf648b086d2fb762330cc88d *ubuntu-22.04.1-desktop-amd64.iso" -V nosummary
       OK  ubuntu-22.04.1-desktop-amd64.iso
```

If what you have is a whole hash/file **record** copied from a website, hand it over verbatim. You
do not need `--style linux` here, because the default parser understands the Linux format.

```
jacksum -a sha256 --style bsd --check-line "SHA-256 (ubuntu-22.04.1-desktop-amd64.iso) = c396e956a9f52c418397867d1ea5c0cf1a99a49dcf648b086d2fb762330cc88d" -V noinfo,nosummary
       OK  ubuntu-22.04.1-desktop-amd64.iso
```

The same record in BSD style — now `--style` is needed, because it selects the parser.

### By a pipe into `--check-file -`

```
echo c396e956a9f52c418397867d1ea5c0cf1a99a49dcf648b086d2fb762330cc88d *ubuntu-22.04.1-desktop-amd64.iso | jacksum -a sha256 --check-file - -V nosummary
       OK  ubuntu-22.04.1-desktop-amd64.iso
```

`-` as the check file means "read the list from standard input". Be aware that `echo` does not
behave the same on all platforms — on Windows you have to drop the quotes and the blanks, otherwise
`echo` passes those characters into the pipe. `--check-line` avoids the whole problem.

<a name="verify_list"></a>

## Create a hash list and verify it later

It is good practice to name the algorithm and the encoding explicitly, or to use a style which
pins down all three of algorithm, encoding and layout. A style also carries the regex information
the parser needs to read the list back in.

```
jacksum -a sha3-256 --style linux -O checkfile .
```

Create the list.

```
jacksum -a sha3-256 --style linux --check-file checkfile .
```

Verify it. Passing the same folder again is what enables Jacksum to also report files that were
**added** since the list was written.

```
jacksum -a blake3+ -E base64 -t iso8601 --check-file hashes.list data
```

If the list was created with `-a blake3+ -E base64 -t iso8601`, you have to specify the same
algorithm, encoding and timestamp format when you read it back.

<a name="verify_states"></a>

## OK, FAILED, MISSING, and NEW

```
jacksum -a sha3-256 --check-file file.hashes .
```

<details>
<summary>Result ...</summary>

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

Note that the check file itself shows up as `NEW` because it lives inside the directory being
checked. Store it outside the tree (`-O ../checkfile`) if that bothers you — for a strict check it
is mandatory.

<a name="verify_strict"></a>

## Strict verification (audit)

A strict check guarantees that no file has been changed, no file has been added, and no file has
been removed — and it makes the **exit code** reflect that verdict.

```
jacksum -a sha3-256 --check-file ../.SHA3 --check-strict data
Jacksum: strict check: PASSED
```

For the verdict to be meaningful, all of the following must hold:

- The files/directories must **not** be omitted, otherwise new files cannot be detected.
- They must be the same ones that were given when the list was created.
- The check file must **not** live inside them, otherwise it is itself reported as new or modified.
- `--list-filter` must be left at `all` (the default), because nothing may be filtered out.
- `-V` must include `summary` (the default for `--check-file`).
- On Windows, add `--scan-ntfs-ads`, because an alternate data stream could have been added that
  would otherwise go unnoticed.
- On GNU/Linux and Unix, add `--scan-all-unix-file-types`, to detect non-regular files that were
  added.

```
jacksum -a sha256 --check-file ../.SHA256 --check-strict --scan-ntfs-ads .
```

A full audit on Windows.

<a name="verify_filter"></a>

## Filter the report

```
jacksum -a sha3-256 --check-file my.hashes --list --list-filter bad -V nosummary .
```

Lists the file names of bad files (failed **or** missing) only — the shape you want in a cron job
that should stay silent when everything is fine.

```
jacksum -a sha3-256 --check-file my.hashes --list --list-filter new -V nosummary data
data/3.txt
```

Only files that were added since the list was written.

```
jacksum --check-file my.hashes --list-filter none,missing,new .
```

`--list-filter` takes a comma separated set, so you can combine exactly the states you care about.

<a name="verify_foreign"></a>

## Verify lists that were created by other tools

Jacksum reads not only its own output but also that of many other tools. `--style` selects the
parser; `--charset-check-file` the character set if the list is not UTF-8.

```
jacksum --check-file /var/lib/dpkg/info/sudo.md5sums --style linux -a md5 --path-relative-to /
```

Verifies a Debian package. Debian ships precalculated MD5 lists in `/var/lib/dpkg/info/` in which
the paths are stored relative to the root folder, so `--path-relative-to /` makes them absolute
again. To verify a package including all of its dependencies, install the `debsums` package and call
`rdebsums`.

```
jacksum -a sha3-256 --check-file list --ignore-timestamps --ignore-sizes .
```

Compare hashes only, even though the list also carries sizes and timestamps.

```
jacksum -a sha3-256 --check-file list --ignore-lines-starting-with-string ";" .
```

Skips comment lines in a foreign format (`-I` for short). `--ignore-empty-lines` and
`--ignore-hashes` exist for the same reason.

<a name="verify_exitcodes"></a>

## Exit codes

| Code | Meaning |
|---|---|
| `0` | everything is OK |
| `1` | at least one mismatch during verification, or `--exact -h <word>` found nothing |
| `>1` | parameter, `.jacksum` or I/O error (a failed `--check-strict` run exits with `6`) |

```
jacksum -a sha3-256 --check-file ../.SHA3 --check-strict -V nosummary data || echo "audit failed"
```

This is what makes Jacksum usable in scripts and cron jobs.

<a name="find"></a>

# 5. Find objects

Because a hash identifies content independently of the file name, Jacksum can be used as a search
engine over content: find duplicates, find a known file wherever it is hiding, find files that are
**not** on an approved list, or find the algorithm behind a hash.

<a name="find_duplicates"></a>

## Find all duplicates of a file

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

The same, but only the file names are printed — ready to be piped into `rm` or `ln`.

<a name="find_byhash"></a>

## Find a file by its hash

If you know the hash of a file, you can find the file even if you do not know its name. Let's search
for Satoshi Nakamoto's Bitcoin whitepaper on macOS:

```
jacksum -a sha256 -x -e b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553 --threads-reading max .
```

<details>
<summary>Result ...</summary>

```
    MATCH  /System/Library/Image Capture/Devices/VirtualScanner.app/Contents/Resources/simpledoc.pdf (b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553)

Jacksum: Expectation met.
Jacksum: 1 of the successfully read files matches the expected hash value.
```
</details>

`--threads-reading max` pays off on SSDs, where the bottleneck is not the disk.

<a name="find_wantedlist"></a>

## Find files by a list of known hashes

`--wanted-list` matches every file against a whole **set** of known hashes. The classic use case is
hunting for vulnerable libraries.

First, build the list from copies of the affected artifacts. SHA3-256 is a good choice here: a
modern, non-broken hash function minimizes collisions and therefore false positives.

```
jacksum --header -a sha3-256 --style linux -O log4j.hashes --no-path .
```

Then search the whole machine for anything matching:

```
jacksum --wanted-list log4j.hashes --style linux --threads-reading max -V summary,noinfo /
```

<details>
<summary>Result ...</summary>

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

Note the parenthesised name: the match is reported with the name from the **wanted list**, so you
learn *which* known artifact you just found, no matter what it has been renamed to on disk.

See also
[CVE-2021-44832: Find vulnerable .jar files using Jacksum](https://loefflmann.blogspot.com/2022/06/CVE-2021-44832%20Find%20vulnerable%20.jar%20files%20using%20Jacksum%203.4.0%20or%20later.html)

<a name="find_negative"></a>

## Negative matching

```
jacksum -a sha3-256 --wanted-list known.hashes --wanted-list-filter negative -V nosummary .
 NO MATCH  rogue.bin (e86c0e881ea1ba2245f051f6e18aa4aff92ec00784386a95cec08cc2a890fbf3)
```

Turns the question around: report everything that is **not** on the approved list. That is how you
find the one file in a deployment that nobody can account for. `--wanted-list-filter` (alias
`--match-filter`) accepts `match`/`positive` (the default) and `nomatch`/`negative`.

<a name="find_strings"></a>

## Find strings that match a hash

```
jacksum -a sha3-256 --string-list words.txt -e f0277d92062bd9a41dd26cddbaf2c41d576cf7b0173cbe96c23d5f5a4f92cc8f
f0277d92062bd9a41dd26cddbaf2c41d576cf7b0173cbe96c23d5f5a4f92cc8f beta
```

Combining `--string-list` with `-e` searches a word list rather than a file system — useful when
you know a hash was taken over a short, guessable string.

<a name="find_malware"></a>

## Find malware by hash values

To identify malware by hash values you first need hash values of malware.

**Get an existing database.** Scripts such as [dumahadaba](https://github.com/jonelo/dumahadaba)
transform a public malware database into a plain text file that Jacksum can process further.

**Or build your own.** With all the samples in `./malware`, store the hashes in the `hdb` format
used by [ClamAV's sigtool](https://docs.clamav.net/manual/Signatures.html#hash-based-signatures)
(`hash:filesize:filename`). `--no-path` keeps just the file names:

```
jacksum -a sha256 --style hdb --no-path -O malware.sha256.hdb ./malware/
```

Then hunt with it:

```
jacksum -a sha256 --style hdb --wanted-list malware.sha256.hdb .
```

<a name="find_algorithm"></a>

## Find the algorithm that generated a hash value

Since Jacksum supports so many algorithms, it can also work backwards: given the data and the hash,
it finds the algorithm, using a fast and smart brute force search.

```
jacksum -a unknown:16 -q hex:050000 -E hex -e d893
```

<details>
<summary>Result ...</summary>

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

Jacksum tested more than one million algorithms in about 5 seconds and found 21 that produce the
same value. Test with more and/or longer input/output pairs to narrow it down. The most likely
candidate is printed with a name if it is a well known CRC — here CRC-16/GENIBUS.

Once you have identified it, use the CRC definition that was found on your own data:

```
jacksum -a crc:16,1021,FFFF,false,false,FFFF -E hex -q txt:"Hello World"
```

`<bits>` in `unknown:<bits>` may be anything from 1 to 1024. See also
[Investigating Algorithms](https://github.com/jonelo/jacksum/wiki/Investigating-Algorithms).

<a name="hmac"></a>

# 6. HMAC

An HMAC (Keyed-Hash Message Authentication Code, RFC 2104) proves not only that data is unchanged
but that it comes from someone who knows a shared secret. Jacksum supports HMAC for 492 of its
586 algorithms — `jacksum --hmacs` lists them.

<a name="hmac_calculate"></a>

## Calculate an HMAC

```
jacksum -a hmac:sha256 -k txt:secret -q txt:"Hello World"
82ce0d2f821fa0ce5447b21306f214c99240fecc6387779d7515148bbdd0c415
```

The prefix `hmac:` (or `hmac-`) in front of any supported algorithm switches Jacksum into HMAC mode;
`-k` supplies the key.

```
jacksum -a hmac:sha3-256 -k file:key.txt message.txt
```

An HMAC over a file. Everything you know about `-a`, `-E`, `-F`, `--style` and directory traversal
keeps working.

<a name="hmac_key"></a>

## Where the key comes from

`-k` accepts exactly the same forms as `-q`: `txt:`, `txtf:`, `hex:`, `bin:`, `dec:`, `oct:`,
`base32:`, `base32hex:`, `base64:`, `base64url:`, `z-base-32:`, `z85:`, `file:`, `readline`, and
`password`.

```
jacksum -a hmac:sha256 -k file:key.txt message.txt
jacksum -a hmac:sha256 -k password message.txt
Key (echo off):
```

In multi-user environments only `file:<file>`, `readline` and `password` are advisable — every other
form leaves the secret in process lists and in your shell history. To avoid shoulder surfing,
prefer `file:` or `password` over `readline`.

<a name="hmac_truncated"></a>

## Truncated HMACs

```
jacksum -a hmac:sha3-256:160 -k txt:test --info
```

`hmac:<algo>:<bits>` truncates the result to `<bits>`. Truncation is a legitimate and common
practice — it limits how much of the underlying hash you reveal. `--info` shows you the resulting
parameters.

<a name="hmac_verify"></a>

## Verify an HMAC

```
jacksum -a hmac:sha256 -q password -k password -e 60273a1e778ed009a6fb32fa11dbb16f905148fc2ec84a67f8a3b3a6cabaa9b7
```

`-e` works in HMAC mode too, with both the key and the message read from the console.

<a name="hmac_interop"></a>

## Interoperability with other tools

The same HMAC, expressed in five ecosystems:

```
Jacksum:  jacksum -a hmac:<algo>[:<bits>] -k <key> <message>
OpenSSL:  openssl dgst -<algo> -mac hmac -macopt hexkey:<key> <message>
Python:   hmac.new(<key>, <message>, hashlib.<algo>).hexdigest()
PHP:      hash_hmac('<algo>', '<message>', '<key>');
```

`jacksum -h hmac:` prints the full compatibility list.

<a name="beyond"></a>

# 7. Beyond hashing

<a name="beyond_passwords"></a>

## Reproducible, unique, secure passwords

You can use Jacksum as a password generator that regenerates the password for a website on demand.
The advantage: you only have to remember **one** master password, yet every account gets a
different strong one. No password manager is involved, nothing is stored on disk, and if one site's
password leaks, the master password stays secret.

```
jacksum -a hmac:sha3-512:240 -8 -k password -q password -E base64
Key (echo off): <your master password>
Password: <the website address><your master password>
```

The recipe behind that command line:

1. Apply an **HMAC**, which makes precalculated rainbow tables useless.
2. Choose a cryptographic, non-broken hash function with a long output (SHA3-512).
3. **Truncate** it (`:240`) so the site never learns the full hash.
4. The HMAC key (`-k`) is your master password.
5. The message (`-q`) combines the site's address with the master password.
6. Encode with base64. 240 bits = 30 bytes is a multiple of 3, so there is no padding and you get a
   clean 40 character password with upper- and lowercase letters, digits, and sometimes a special
   character.

Both `-k password` and `-q password` read from the console with echo disabled. In that mode Jacksum
refuses to print the secret in clear text even if you ask for it with `-F`, refuses piping and
redirection (a console is required), and clears the password from memory afterwards. Use `-o`/`-O`
if you would rather not see the hash on screen either.

The full reasoning is at <https://bit.ly/secure-passwords-with-jacksum>.

<a name="beyond_random"></a>

## Large pseudo-random numbers

```
jacksum -h | sort | uniq > strings.txt
jacksum -a hmac:sha256:64 -k txt:run42 --string-list strings.txt -F "#HASH" -E dec
482543917333917802
2258785350139739212
2746638413740063416
```

A generator for many large random pseudo-numbers: take a set of unique strings, apply an HMAC,
truncate it to the bit width you want, and encode in decimal. The HMAC key acts as the **seed** for
the whole run, so the sequence is reproducible; each string acts as the individual seed for one
number.

<a name="beyond_encodings"></a>

## Encoding conversions

With `-a none` Jacksum stops hashing altogether and becomes a converter between hex, binary,
decimal, octal, Base32/64, Z85 and BubbleBabble:

```
jacksum -a none -q hex:CAFE -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex: #SEQUENCE{hex-uppercase}"
bin: 1100101011111110, dec: 51966, oct: 0145376, hex: CAFE
```

The full collection lives on the
[Jacksum Hacks](JACKSUM_HACKS.md) page.

<a name="crcs"></a>

# 8. Customize CRCs

Beyond the CRCs that ship with names, Jacksum lets you define your own from 1 to 64 bits wide.
See also [Working with CRCs](https://github.com/jonelo/jacksum/wiki/Working-with-CRCs).

<a name="crcs_6"></a>

## 6 parameters

Jacksum supports the quasi standard called "Rocksoft (tm) Model CRC Algorithm", which describes a
CRC by width, polynomial, init value, refIn, refOut, and xorOut.

```
jacksum -a crc:32,1EDC6F41,FFFFFFFF,true,true,FFFFFFFF -x -q txt:123456789
e3069283 9
```

The Castagnoli CRC-32, spelled out.

```
jacksum -a crc32c -x -q txt:123456789
e3069283 9
```

The same thing through its built-in alias.

<a name="crcs_7"></a>

## 7 parameters

An extended model with a 7th parameter defines CRCs that incorporate the **length** of the message.
If it is `true`, the most significant octet of the length is fed to the CRC's update method first;
if `false`, the least significant octet goes first.

```
jacksum -a crc:32,04C11DB7,0,false,false,FFFFFFFF,false -x -q txt:123456789
377a6011 9
```

The POSIX 1003.2 CRC algorithm.

```
jacksum -a cksum -x -q txt:123456789
377a6011 9
```

The same through its alias.

<a name="crcs_8"></a>

## 8 parameters

An 8th parameter XORs the length value before it is included in the CRC.

```
jacksum -a crc:32,04C11DB7,0,true,true,0,true,CC55CC55 -x -q txt:123456789
afcbb09a 9
```

The output of the `sum` command from [Plan 9](https://en.wikipedia.org/wiki/Plan_9_from_Bell_Labs).

```
jacksum -a sum_plan9 -x -q txt:123456789
afcbb09a 9
```

The same through its alias.

<a name="performance"></a>

# 9. Performance and traversal control

Jacksum has two independent concurrency subsystems: one that computes several algorithms over the
same data in parallel, and one that walks and reads many files in parallel. See
[Multi-Core Processor Support](https://github.com/jonelo/jacksum/wiki/Multi-Core-Processor-Support).

```
jacksum -a crc32c+md5+sha256 -V all -r 1 --threads-reading 4 --threads-hashing max --header -F "#HASHES #FILENAME" .
```

Three algorithms in one pass, one directory level deep, 4 reader threads, as many hashing threads
as there are cores.

```
jacksum -a sha3-256 --threads-reading max /
```

`max` is the right setting on SSDs and NVMe, where several concurrent reads are faster than one.
On spinning disks leave it at the default — seeking will cost you more than the parallelism gains.

```
jacksum -A -a md5 -V summary bigfile.iso
```

`-A` requests the alternative implementation of an algorithm where one exists, and `-V summary`
reports the elapsed time — that is how you compare the two.

```
jacksum -a sha3-256 -r 2 .
```

`-r <depth>` limits recursion depth.

```
jacksum -a sha3-256 -d -f .
```

`-d` does not follow symbolic links to directories, `-f` does not follow symbolic links to files.
Jacksum detects file system cycles either way.

```
jacksum -a sha3-256 --scan-all-unix-file-types .
jacksum -a sha3-256 --scan-ntfs-ads .
```

By default Jacksum reads regular files, directories and symbolic links. These options widen that to
all Unix file types resp. to NTFS alternate data streams — both relevant for a
[strict check](#verify_strict).

<a name="info"></a>

# 10. Gather information

<a name="info_one_algo"></a>

## About one algorithm

```
jacksum -h blake2b
```

Prints the BLAKE2b section of the manpage, including a compatibility list showing how to produce
the same value with other tools.

```
jacksum -a blake2b --info
```

Prints implementation details: hash length in bits and bytes, block size, HMAC compatibility,
whether the algorithm is considered broken, the avalanche effect, the relative speed rank, and
whether an alternative implementation exists.

```
jacksum -a md5 --info -V details
```

`-V details` adds the reasoning behind the `broken:` verdict.

<details>
<summary>Result (excerpt) ...</summary>

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

## Investigate CRC parameters

For a CRC, `--info` additionally returns the polynomial as a mathematical expression and in normal,
reversed and Koopman representation, the same for the reciprocal polynomial, and the Jacksum CRC
definition you can pass to `-a`.

```
jacksum -a crc32c --info
```

or, equivalently, by spelling all parameters out:

```
jacksum -a crc:32,1EDC6F41,FFFFFFFF,true,true,FFFFFFFF --info
```

<details>
<summary>Result ...</summary>

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

The same for CRC-64/xz.

<a name="info_many_algos"></a>

## About many algorithms

```
jacksum -a all --list
```

Prints every supported algorithm ID that can be passed to `-a`. Jacksum 4.0.0 supports **586**;
the annotated list lives on the
[Algorithms](ALGORITHMS.md) page.

```
jacksum -a all --list --verbose summary
```

Adds the count.

```
jacksum -a all:skein --list
jacksum -a all:128 --list
```

Filter by substring resp. by output width in bits.

```
jacksum -a all:8 --list --info
```

Every 8 bit algorithm, each with its full `--info` block.

```
jacksum --hmacs
```

Lists all algorithms for which an HMAC can be formed.

```
jacksum -a all:crc -F "#ALGONAME{i},#SEQUENCE,#HASH{i,hex}" -q txt:0123456789
```

Generates test vectors as CSV (`name,input as hex,hash as hex`) for a whole algorithm family at
once.

<details>
<summary>Result (Jacksum 4.0.0) ...</summary>

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

## About styles and encodings

```
jacksum --style bsd --info
```

Prints all properties of the compatibility definition for `bsd` — the starting point if you want to
write your own.

```
jacksum -h --style
jacksum -h -E
jacksum -h -F
jacksum -h parameters
```

The full documentation for styles, encodings, the format language, and program parameters.

<a name="info_program"></a>

## About the program

```
jacksum -v
jacksum --version
Jacksum 4.0.0
```

Only the program name and version in accordance with [Semantic Versioning](https://semver.org/).

```
jacksum --info
```

Version, primary IDs and descriptions of all supported algorithms, the number of algorithms,
supported character sets, system properties, available processors, and much more. Attach its output
to support requests.

```
jacksum --license
jacksum --copyright
```

The full license text, resp. the copyrights and license information for every portion of software
that Jacksum has licensed.

<a name="info_help"></a>

## Navigate the help

The manpage is built into the jar, and `-h` is a search over it.

```
jacksum -h
jacksum -h | more
```

The entire manpage, optionally page by page (`less` works on macOS and Linux, `more` works
everywhere).

```
jacksum -h examples
jacksum -h exa
```

A whole section. Section names may be abbreviated as long as they stay unambiguous — `jacksum -h ex`
prints both EXIT STATUS and EXAMPLES, because both start with `ex`.

```
jacksum -h synopsis
jacksum -h options
jacksum -h "operating modes"
jacksum -h "option "
```

More sections. The last one prints both OPTION TYPES and OPTION SUPPORT MATRIX, because both start
with `option `.

```
jacksum -h whirlpool
```

Information about every algorithm whose name starts with `whirlpool`.

```
jacksum -h -h
jacksum -h --path
jacksum -h -
```

Help on a single option, on every option starting with `--path`, and on all options (every option
starts with a minus sign).

```
jacksum --exact -h --path
```

`--exact` turns the prefix search into an exact match — this one prints nothing, because there is no
option called exactly `--path`.

---

**See also:**
[Manpage](https://github.com/jonelo/jacksum/wiki/Manpage) ·
[Algorithms](ALGORITHMS.md) ·
[Features](FEATURES.md) ·
[Working with CRCs](https://github.com/jonelo/jacksum/wiki/Working-with-CRCs) ·
[Investigating Algorithms](https://github.com/jonelo/jacksum/wiki/Investigating-Algorithms) ·
[Jacksum Hacks](JACKSUM_HACKS.md) ·
[File Format of Styles](https://github.com/jonelo/jacksum/wiki/File-Format-of-Styles) ·
[Multi-Core Processor Support](https://github.com/jonelo/jacksum/wiki/Multi-Core-Processor-Support)

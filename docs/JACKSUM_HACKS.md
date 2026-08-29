> [!TIP]
> Jacksum's primary purpose is to deal with hashes. However, since **Jacksum supports both many encodings and customized formatting** you get additional features that can be quite useful sometimes.
> **The Jacksum Hacks were not planned.** I was also quite surprised about this side effect which works pretty well.

Two options switch the hashing off, and the difference between them is the key to almost every hack
on this page:

| | reads the content of files | reports read errors | knows file size and timestamp |
|---|---|---|---|
| `-a none` | no  | no  | yes |
| `-a read` | yes | yes | yes |

So `-a none` turns Jacksum into a converter and a metadata tool, and `-a read` turns it into a tool
that touches every single byte without producing a hash value.

> [!NOTE]
> With `-a none` the option `-E` needs an explicit `-F`, otherwise Jacksum stops:
> `Jacksum: Parameter Error: -a none and -E without -F cannot go together.`
> Either write `-F "#SEQUENCE" -E hex` or use the short form `-F "#SEQUENCE{hex}"`.

See also [Examples](EXAMPLES.md) for what Jacksum does on purpose, and [Use Cases](USE_CASES.md)
for complete recipes — synchronising directories, building patches, intrusion detection — that
build on some of the hacks below.

**Table of contents**

- [Part 1 — Encodings and number bases](#part1)
  - [File dumps](#file-dumps)
  - [Working with strings ...](#strings)
  - [Binary to ...](#binary-to)
  - [Octal to ...](#octal-to)
  - [Decimal to ...](#decimal-to)
  - [Hex to ...](#hex-to)
  - [Base32 to ...](#base32-to)
  - [Base32hex to ...](#base32hex-to)
  - [Base64 to ...](#base64-to)
  - [Base64url to ...](#base64url-to)
  - [Z85 to ...](#z85-to)
  - [z-base-32 to ...](#z-base-32-to)
  - [BubbleBabble to ...](#bubblebabble-to)
  - [All 19 encodings at a glance](#all-encodings)
  - [Round trips](#round-trips)
- [Part 2 — Hacks that have nothing to do with hashing nor encoding](#part2)
  - [Is everything on the medium still readable?](#medium-readable)
  - [Find files (`find`)](#find-files)
  - [Sizes and timestamps (`stat`, `du`, `ls`)](#sizes-and-timestamps)
  - [Snapshot a directory and detect changes later](#snapshot)
  - [Path tool (`realpath`, `dirname`, `basename`, `cygpath`)](#path-tool)
  - [Timestamp tool (`date -r`)](#timestamp-tool)
  - [A template engine for file trees](#template-engine)
  - [Character set converter (`iconv`)](#charset-converter)
  - [Count the bytes of a pipe (`wc -c`)](#count-bytes)
  - [A searchable reference book](#reference-book)
  - [What am I running on?](#what-am-i-running-on)

<a name="part1"></a>

# Part 1 — Encodings and number bases

For all examples in this part we set `-a none`, because we are not interested in hashing at all.

The file `myfile.dat` used below contains the seven bytes `Jacksum`.

<a name="file-dumps"></a>

## File dumps

### Hex dump

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE" -E hex -g 1
    4a 61 63 6b 73 75 6d

### Hex dump, grouped

`-g <count>` groups the output in `<count>` bytes, `-G <char>` sets the separator.

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE" -E hex -g 2 -G :
    4a61:636b:7375:6d

### Base16 dump

Base16 (RFC 4648) is hexadecimal in uppercase.

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base16}"
    4A61636B73756D

### Base32 dump

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base32}"
    JJQWG23TOVWQ====

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base32-nopadding}"
    JJQWG23TOVWQ

### Base32hex dump

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base32hex}"
    99GM6QRJELMG====

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base32hex-nopadding}"
    99GM6QRJELMG

### Base64 dump

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base64}"
    SmFja3N1bQ==

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base64-nopadding}"
    SmFja3N1bQ

### Base64url dump

Safe for URLs and file names, because `+` and `/` are replaced by `-` and `_`.

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base64url}"
    SmFja3N1bQ==

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{base64url-nopadding}"
    SmFja3N1bQ

### Z85 dump

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{z85}"
    n)#jBB9hs

### z-base-32 dump

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{z-base-32}"
    jjosg45uqiso

Since 4.0.0 Jacksum also reads z-base-32 back in, see [z-base-32 to ...](#z-base-32-to).

### BubbleBabble dump

Pronounceable pseudowords, as used by OpenSSH and SSH2.

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{bubblebabble}"
    xidik-comuk-resyl-hyrix

Since 4.0.0 Jacksum also reads BubbleBabble back in, see [BubbleBabble to ...](#bubblebabble-to).

### Decimal, octal and binary dump

The whole file is treated as one big number, not as a list of byte values.

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{dec}"
    20936227908973933

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{oct}"
    1123026155334672555

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{bin}"
    01001010011000010110001101101011011100110111010101101101

### Size of a file

    $ jacksum -a none -q file:myfile.dat -F "#LENGTH"
    7

> [!NOTE]
> `-q file:` keeps the entire file in memory and is therefore limited to 128 MiB. For bigger files,
> pass the file as a normal parameter instead of using `-q`.


<a name="strings"></a>

## Working with strings ...

### Count characters of a string

    $ jacksum -a none -q "txt:Hello World" -F "#LENGTH"
    11

`#LENGTH` counts **bytes**, not characters. The five characters of `Grüße` are seven bytes in UTF-8:

    $ jacksum -a none -q "txt:Grüße" -F "#LENGTH bytes, hex: #SEQUENCE{hex}"
    7 bytes, hex: 4772c3bcc39f65

### String to hex

    $ jacksum -a none -q "txt:Hello World" -F "#SEQUENCE" -E hex -g 1
    48 65 6c 6c 6f 20 57 6f 72 6c 64

### Formatted string to hex

`txtf:` interprets `\t`, `\n`, `\r`, `\"`, `\'`, `\\` and `\xhh`.

    $ jacksum -a none -q "txtf:Hello World\n" -F "#SEQUENCE" -E hex -g 1
    48 65 6c 6c 6f 20 57 6f 72 6c 64 0a

    $ jacksum -a none -q "txtf:tab\there\n" -F "#SEQUENCE" -E hex -g 1
    74 61 62 09 68 65 72 65 0a


### String to base64, base32, z85 and BubbleBabble

    $ jacksum -a none -q "txt:Hello World" -F "#SEQUENCE{base64}"
    SGVsbG8gV29ybGQ=

    $ jacksum -a none -q "txt:Hello World" -F "#SEQUENCE{base32}"
    JBSWY3DPEBLW64TMMQ======

    $ jacksum -a none -q "txt:Hello World" -F "#SEQUENCE{z85}"
    nm=QNzY&b1A+]m

    $ jacksum -a none -q "txt:Hello World" -F "#SEQUENCE{bubblebabble}"
    xidak-hyryk-sored-buhok-zusuk-sunex

### String to everything

    $ jacksum -a none -q "txt:Hello World" -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 0100100001100101011011000110110001101111001000000101011101101111011100100110110001100100, dec: 87521618088882533792115812, oct: 044145330661571005355734466144, hex:48656C6C6F20576F726C64

### Convert many strings at once

`--string-list` reads one string per line. `#MESSAGE` is the string itself.

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

### An interactive converter

`-q readline` reads one line from the console and echoes it back through any format you like — handy
for investigating strings and encodings without leaving them in the shell history.

    $ jacksum -a none -q readline -F "msg=#MESSAGE len=#LENGTH hex=#SEQUENCE{hex} b64=#SEQUENCE{base64}"

Requires a real console; it cannot be fed through a pipe or a redirection.


<a name="binary-to"></a>

## Binary to ...

### Bin to dec

    $ jacksum -a none -q bin:1010101010101010 -F "#SEQUENCE{dec}"
    43690

### Bin to octal

    $ jacksum -a none -q bin:1010101010101010 -F "#SEQUENCE{oct}"
    125252

### Bin to hex

    $ jacksum -a none -q bin:1010101010101010 -F "#SEQUENCE{hex}"
    aaaa

### Bin to everything

    $ jacksum -a none -q bin:1010101010101010 -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 1010101010101010, dec: 43690, oct: 0125252, hex:AAAA

### Bin to everything in JSON

    $ jacksum -a none -q bin:1010101010101010 -F '{ "bin": "#SEQUENCE{bin}", "dec": "#SEQUENCE{dec}", "oct": "0#SEQUENCE{oct}", "hex": "0x#SEQUENCE{hex-uppercase}" }'
    { "bin": "1010101010101010", "dec": "43690", "oct": "0125252", "hex": "0xAAAA" }

### Bin to base64

    $ jacksum -a none -q bin:1010101010101010 -F "#SEQUENCE{base64}"
    qqo=

### Bin to z85

    $ jacksum -a none -q bin:1010101010101010 -F "#SEQUENCE{z85}"
    S&u


<a name="octal-to"></a>

## Octal to ...

Octal input is a list of **byte** values, separated by commas or spaces.

### Octal to binary

    $ jacksum -a none -q oct:312,376 -F "#SEQUENCE{bin}"
    1100101011111110

### Octal to decimal

    $ jacksum -a none -q oct:312,376 -F "#SEQUENCE{dec}"
    51966

### Octal to hex

    $ jacksum -a none -q oct:312,376 -F "#SEQUENCE{hex-uppercase}"
    CAFE

### Octal to everything

    $ jacksum -a none -q oct:312,376 -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 1100101011111110, dec: 51966, oct: 0145376, hex:CAFE

### Octal to everything in JSON

    $ jacksum -a none -q oct:312,376 -F '{ "bin": "#SEQUENCE{bin}", "dec": "#SEQUENCE{dec}", "oct": "0#SEQUENCE{oct}", "hex": "0x#SEQUENCE{hex-uppercase}" }'
    { "bin": "1100101011111110", "dec": "51966", "oct": "0145376", "hex": "0xCAFE" }

### Octal to base64

    $ jacksum -a none -q oct:312,376 -F "#SEQUENCE{base64}"
    yv4=

### Octal to z85

    $ jacksum -a none -q oct:312,376 -F "#SEQUENCE{z85}"
    +kI

### Octal to text

    $ jacksum -a none -q oct:"145 147 40 64 62 12" -F "#SEQUENCE{hex}"
    65672034320a


<a name="decimal-to"></a>

## Decimal to ...

### Decimal to binary

    $ jacksum -a none -q dec:42 -F "#SEQUENCE{bin}"
    00101010

### Decimal to hex

    $ jacksum -a none -q dec:42 -F "#SEQUENCE{hex-uppercase}"
    2A

### Decimal to octal

    $ jacksum -a none -q dec:42 -F "#SEQUENCE{oct}"
    52

### Decimal to everything

    $ jacksum -a none -q dec:42 -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 00101010, dec: 42, oct: 052, hex:2A

### Decimal to everything in JSON

    $ jacksum -a none -q dec:42 -F '{ "bin": "#SEQUENCE{bin}", "dec": "#SEQUENCE{dec}", "oct": "0#SEQUENCE{oct}", "hex": "0x#SEQUENCE{hex-uppercase}" }'
    { "bin": "00101010", "dec": "42", "oct": "052", "hex": "0x2A" }

### Decimal to base64

    $ jacksum -a none -q dec:42 -F "#SEQUENCE{base64}"
    Kg==

### Decimal to z85

    $ jacksum -a none -q dec:42 -F "#SEQUENCE{z85}"
    dG


<a name="hex-to"></a>

## Hex to ...

### Hex to binary

    $ jacksum -a none -q hex:cafe08 -F "#SEQUENCE{bin}"
    110010101111111000001000

### Hex to decimal

    $ jacksum -a none -q hex:7A -F "#SEQUENCE{dec}"
    122

### Hex to octal

    $ jacksum -a none -q hex:7A -F "#SEQUENCE{oct}"
    172

### Lowercase hex to uppercase hex

    $ jacksum -a none -q hex:cafe08 -F "#SEQUENCE{hex-uppercase}"
    CAFE08

### Uppercase hex to lowercase hex

    $ jacksum -a none -q hex:CAFE08 -F "#SEQUENCE{hex-lowercase}"
    cafe08

### Hex to everything

    $ jacksum -a none -q hex:CAFE -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 1100101011111110, dec: 51966, oct: 0145376, hex:CAFE

### Hex to everything in JSON

    $ jacksum -a none -q hex:CAFE -F '{ "bin": "#SEQUENCE{bin}", "dec": "#SEQUENCE{dec}", "oct": "0#SEQUENCE{oct}", "hex": "0x#SEQUENCE{hex-uppercase}" }'
    { "bin": "1100101011111110", "dec": "51966", "oct": "0145376", "hex": "0xCAFE" }

### Hex to base32

    $ jacksum -a none -q hex:C0DECAFE -F "#SEQUENCE{base32}"
    YDPMV7Q=

### Hex to base64

    $ jacksum -a none -q hex:C0DECAFE -F "#SEQUENCE{base64}"
    wN7K/g==

### Hex to base64url

    $ jacksum -a none -q hex:C0DECAFE -F "#SEQUENCE{base64url}"
    wN7K_g==

### Hex to z85

    $ jacksum -a none -q hex:C0DECAFE -F "#SEQUENCE{z85}"
    Z#0lk

### Hex to BubbleBabble

    $ jacksum -a none -q hex:C0DECAFE -F "#SEQUENCE{bubblebabble}"
    xubat-vidyz-vexox


<a name="base32-to"></a>

## Base32 to ...

### Decode Base32 and encode to hex

    $ jacksum -a none -q base32:YDPMV7Q= -F "#SEQUENCE{hex-uppercase}"
    C0DECAFE

### Decode Base32 and encode to base32hex

    $ jacksum -a none -q base32:YDPMV7Q= -F "#SEQUENCE{base32hex}"
    O3FCLVG=

### Decode Base32 and encode to base64

    $ jacksum -a none -q base32:YDPMV7Q= -F "#SEQUENCE{base64}"
    wN7K/g==

### Decode Base32 and encode to z85

    $ jacksum -a none -q base32:YDPMV7Q= -F "#SEQUENCE{z85}"
    Z#0lk

### Base32 to everything

    $ jacksum -a none -q base32:YDPMV7Q -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 11000000110111101100101011111110, dec: 3235826430, oct: 030067545376, hex:C0DECAFE


<a name="base32hex-to"></a>

## Base32hex to ...

### Decode Base32hex and encode to hex

    $ jacksum -a none -q base32hex:O3FCLVG= -F "#SEQUENCE{hex-uppercase}"
    C0DECAFE

### Decode Base32hex and encode to base32

    $ jacksum -a none -q base32hex:O3FCLVG= -F "#SEQUENCE{base32}"
    YDPMV7Q=

### Decode Base32hex and encode to base64

    $ jacksum -a none -q base32hex:O3FCLVG= -F "#SEQUENCE{base64}"
    wN7K/g==

### Base32hex to everything

    $ jacksum -a none -q base32hex:O3FCLVG= -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 11000000110111101100101011111110, dec: 3235826430, oct: 030067545376, hex:C0DECAFE


<a name="base64-to"></a>

## Base64 to ...

### Decode base64 and encode to hex

    $ jacksum -a none -q base64:wN7K/g== -F "#SEQUENCE{hex-uppercase}"
    C0DECAFE

### Decode base64 and encode to base64url

    $ jacksum -a none -q base64:wN7K/g== -F "#SEQUENCE{base64url}"
    wN7K_g==

### Decode base64 and encode to z85

    $ jacksum -a none -q base64:wN7K/g== -F "#SEQUENCE{z85}"
    Z#0lk

### Base64 without padding also works

    $ jacksum -a none -q base64:wN7K/g -F "#SEQUENCE{hex-uppercase}"
    C0DECAFE


<a name="base64url-to"></a>

## Base64url to ...

### Decode base64url and encode to hex

    $ jacksum -a none -q base64url:wN7K_g== -F "#SEQUENCE{hex-uppercase}"
    C0DECAFE

### Decode base64url and encode to standard base64

    $ jacksum -a none -q base64url:wN7K_g== -F "#SEQUENCE{base64}"
    wN7K/g==

### Decode base64url and encode to z85

    $ jacksum -a none -q base64url:wN7K_g== -F "#SEQUENCE{z85}"
    Z#0lk

### Base64url to everything

    $ jacksum -a none -q base64url:wN7K_g== -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 11000000110111101100101011111110, dec: 3235826430, oct: 030067545376, hex:C0DECAFE


<a name="z85-to"></a>

## Z85 to ...

### Decode Z85 and encode to hex

    $ jacksum -a none -q "z85:Z#0lk" -F "#SEQUENCE{hex}"
    c0decafe

### Decode Z85 and encode to base64

    $ jacksum -a none -q "z85:Z#0lk" -F "#SEQUENCE{base64}"
    wN7K/g==

### Decode Z85 and encode to base64url

    $ jacksum -a none -q "z85:Z#0lk" -F "#SEQUENCE{base64url}"
    wN7K_g==


<a name="z-base-32-to"></a>

## z-base-32 to ...

`-q z-base-32:` is new in Jacksum 4.0.0. Until then z-base-32 was an output encoding only, so a
z-base-32 string could be produced but not read back.

### Decode z-base-32 and encode to hex

    $ jacksum -a none -q z-base-32:adxci9o -F "#SEQUENCE{hex-uppercase}"
    C0DECAFE

### Decode z-base-32 and encode to base32

The same bytes, a different alphabet:

    $ jacksum -a none -q z-base-32:adxci9o -F "#SEQUENCE{base32}"
    YDPMV7Q=

### Decode z-base-32 and encode to base64

    $ jacksum -a none -q z-base-32:adxci9o -F "#SEQUENCE{base64}"
    wN7K/g==

### z-base-32 to everything

    $ jacksum -a none -q z-base-32:adxci9o -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 11000000110111101100101011111110, dec: 3235826430, oct: 030067545376, hex:C0DECAFE

### Read back a z-base-32 dump

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{z-base-32}"
    jjosg45uqiso

    $ jacksum -a none -q z-base-32:jjosg45uqiso -F "#SEQUENCE{hex}"
    4a61636b73756d

> [!NOTE]
> z-base-32 input is **lowercase only** and carries **no padding**. Both `ADXCI9O` and `adxci9o====`
> are rejected with `Invalid z-base32 input data.`, and the alphabet has no `l`, `v`, `0` or `2`.

The type is available for `-k` as well, because the secret key of an HMAC uses the same syntax as
`-q`:

    $ jacksum -a hmac:sha256 -k z-base-32:adxci9o myfile.dat


<a name="bubblebabble-to"></a>

## BubbleBabble to ...

`-q bubblebabble:` is new in Jacksum 4.0.0 as well. BubbleBabble is the only encoding on this page
that carries **redundancy**: every second tuple contains a checksum, and the string is framed by an
`x`. The decoder verifies all of that, so a mistyped string is rejected rather than being decoded to
arbitrary bytes.

### Decode BubbleBabble and encode to hex

    $ jacksum -a none -q bubblebabble:xubat-vidyz-vexox -F "#SEQUENCE{hex-uppercase}"
    C0DECAFE

### Decode BubbleBabble and encode to base32

    $ jacksum -a none -q bubblebabble:xubat-vidyz-vexox -F "#SEQUENCE{base32}"
    YDPMV7Q=

### Decode BubbleBabble and encode to base64

    $ jacksum -a none -q bubblebabble:xubat-vidyz-vexox -F "#SEQUENCE{base64}"
    wN7K/g==

### Decode BubbleBabble and encode to z85

    $ jacksum -a none -q bubblebabble:xubat-vidyz-vexox -F "#SEQUENCE{z85}"
    Z#0lk

### BubbleBabble to everything

    $ jacksum -a none -q bubblebabble:xubat-vidyz-vexox -F "bin: #SEQUENCE{bin}, dec: #SEQUENCE{dec}, oct: 0#SEQUENCE{oct}, hex:#SEQUENCE{hex-uppercase}"
    bin: 11000000110111101100101011111110, dec: 3235826430, oct: 030067545376, hex:C0DECAFE

### Read back a BubbleBabble dump

    $ jacksum -a none -q file:myfile.dat -F "#SEQUENCE{bubblebabble}"
    xidik-comuk-resyl-hyrix

    $ jacksum -a none -q bubblebabble:xidik-comuk-resyl-hyrix -F "#SEQUENCE{hex}"
    4a61636b73756d

### A typo detector

Three kinds of damage, three different messages, and never a silently wrong result (exit code 2 in
all three cases):

    $ jacksum -a none -q bubblebabble:xidik-comuk-resyl-hyrax -F "#SEQUENCE{hex}"
    BubbleBabble decoding error: the checksum of the tuple at index 19 does not match.

    $ jacksum -a none -q bubblebabble:xidqk-comuk-resyl-hyrix -F "#SEQUENCE{hex}"
    BubbleBabble decoding error: 'q' is not a valid vowel.

    $ jacksum -a none -q bubblebabble:idik-comuk-resyl-hyrix -F "#SEQUENCE{hex}"
    BubbleBabble decoding error: 22 is not a valid length for a BubbleBabble string.

That is what makes BubbleBabble the encoding of choice if a fingerprint has to be read out loud,
dictated over the phone, or typed in by a human.

### The classic test vectors

The three examples from the BubbleBabble specification, in both directions:

    $ jacksum -a none -q "txt:" -F "#SEQUENCE{bubblebabble}"
    xexax

    $ jacksum -a none -q "txt:1234567890" -F "#SEQUENCE{bubblebabble}"
    xesef-disof-gytuf-katof-movif-baxux

    $ jacksum -a none -q "txt:Pineapple" -F "#SEQUENCE{bubblebabble}"
    xigak-nyryk-humil-bosek-sonax

    $ jacksum -a none -q bubblebabble:xesef-disof-gytuf-katof-movif-baxux -F "#SEQUENCE{hex}"
    31323334353637383930

> [!NOTE]
> Unlike z-base-32, BubbleBabble input is **case insensitive**, so `XIDIK-COMUK-RESYL-HYRIX` is
> accepted as well. The empty input is `xexax`, not the empty string. For `-q` and `-k` the long
> form `bubblebabble:` is the only valid indicator; `bb:` is not a type and ends up in the hex
> fallback (`Not a hex number.`). Wherever an *encoding* is expected, `bb` is fine: `-E bb`,
> `#SEQUENCE{bb}`.

The type is available for `-k` as well, because the secret key of an HMAC uses the same syntax as
`-q`:

    $ jacksum -a hmac:sha256 -k bubblebabble:xubat-vidyz-vexox myfile.dat

And since Jacksum can turn a BubbleBabble string back into bytes, the option `-e` compares a
BubbleBabble encoded hash value byte-wise now, no matter how it was written:

    $ jacksum -a sha256 -E bb -e XIDOF-GANEG-HUFUV-VIBOM-PYZYG-FUNUL-GUFIB-BEHIG-KAKAS-GUVYK-NUVAH-NUZOH-ZIZUH-RIVEV-TORIB-TYSOP-GUXUX myfile.dat
        MATCH  myfile.dat (XIDOF-GANEG-HUFUV-VIBOM-PYZYG-FUNUL-GUFIB-BEHIG-KAKAS-GUVYK-NUVAH-NUZOH-ZIZUH-RIVEV-TORIB-TYSOP-GUXUX)

    Jacksum: Expectation met.
    Jacksum: 1 of the successfully read files matches the expected hash value.


<a name="all-encodings"></a>

## All 19 encodings at a glance

The same four bytes, `C0DECAFE`, through every encoding Jacksum knows:

    $ jacksum -a none -q hex:C0DECAFE -F "#SEQUENCE{<encoding>}"

| `<encoding>` | output |
|---|---|
| `bin`                 | `11000000110111101100101011111110` |
| `dec`                 | `3235826430` |
| `oct`                 | `30067545376` |
| `hex`                 | `c0decafe` |
| `hex-lowercase`       | `c0decafe` |
| `hex-uppercase`       | `C0DECAFE` |
| `hexup`               | `C0DECAFE` (deprecated since 3.0, use `hex-uppercase`) |
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

The same 19 values are valid for `-E`, and the input side (`-q <type>:`) understands
`bin`, `dec`, `oct`, `hex`, `base32`, `base32hex`, `base64`, `base64url`, `z85`, `z-base-32` and
`bubblebabble` (the last two are new in 4.0.0), `txt`, `txtf` and `file`.


<a name="round-trips"></a>

## Round trips

Anything that Jacksum can read with `-q <type>:` it can also write with `#SEQUENCE{<encoding>}`, so
you can chain conversions:

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
> Jacksum always writes *text*. It can read a binary file (`-q file:`) and print it in any encoding,
> but it cannot write raw bytes back to a file — so `base64 -d > file.bin` has no Jacksum equivalent.


<a name="part2"></a>

# Part 2 — Hacks that have nothing to do with hashing nor encoding

Everything below uses `-a none` or `-a read`, so no hash value is ever calculated. The examples run
against this little tree:

    myfile.dat            7 bytes
    readme.txt           12 bytes
    strings.txt          24 bytes
    photos/a.jpg          1 byte
    photos/2024/b.jpg     2 bytes

<a name="medium-readable"></a>

## Is everything on the medium still readable?

This is the one hack that has nothing to do with hashing at all: `-a read` reads every single byte
of every file and throws the bytes away. Anything that goes wrong on the way — a scratched DVD, a
dying USB stick, bit rot on an old backup drive, a network share that dropped out, a permission
problem — surfaces as a read error.

    $ jacksum -a read -V summary,errors -r max . > /dev/null

    Jacksum: total files read successfully: 5
    Jacksum: total bytes read: 46
    Jacksum: total bytes read (human readable): 46 bytes
    Jacksum: total file read errors: 0

The per-file output goes to standard output (and is thrown away by `> /dev/null`), while errors and
the summary go to standard error. With one broken file in the tree:

    $ jacksum -a read -V summary,errors -r max . > /dev/null
    Jacksum: Error: ./bad.bin (Permission denied)

    Jacksum: total files read successfully: 5
    Jacksum: total bytes read: 46
    Jacksum: total bytes read (human readable): 46 bytes
    Jacksum: total file read errors: 1

    $ echo $?
    4

`total file read errors` and the exit code 4 make this scriptable. Use `-u <file>` to collect the
list of damaged files:

    $ jacksum -a read -V summary,errors -u unreadable.log -r max /Volumes/BACKUP > /dev/null

Note that `-a none` will **not** find those files, because it never opens them:

    $ jacksum -a none -F "#FILESIZE #FILENAME" bad.bin
    1 bad.bin

That is exactly the difference: use `-a none` when you only want metadata, use `-a read` when you
want to know whether the data is still there.


<a name="find-files"></a>

## Find files (`find`)

`--style names-only` walks a directory tree and prints nothing but the paths.

    $ jacksum --style names-only .
    ./myfile.dat
    ./readme.txt
    ./photos/2024/b.jpg
    ./photos/a.jpg
    ./strings.txt

`-r <depth>` limits the recursion depth:

    $ jacksum --style names-only -r 1 .
    Jacksum: Info: "./photos" is a directory, but the maximum number of allowed directory levels (1) has been reached.
    ./myfile.dat
    ./readme.txt
    ./strings.txt

`-d` and `-f` stop Jacksum from following symbolic links to directories resp. to files:

    $ jacksum --style names-only -d -f .
    Jacksum: Info: Ignoring "./photolink", because it is a symlink to a directory.
    Jacksum: Info: Ignoring "./link.txt", because it is a symlink to a file.
    ./myfile.dat
    ./readme.txt
    ./photos/2024/b.jpg
    ./photos/a.jpg
    ./strings.txt

By default Jacksum only accepts regular files, and complains about everything else:

    $ jacksum --style names-only .
    Jacksum: Error: ./queue.fifo: is not a regular file.
    ./myfile.dat
    ./readme.txt
    ...

`--scan-all-unix-file-types` includes block devices, character devices, named pipes, sockets and
Solaris doors:

    $ jacksum --style names-only --scan-all-unix-file-types .
    ./myfile.dat
    ./queue.fifo
    ./readme.txt
    ./photos/2024/b.jpg
    ./photos/a.jpg
    ./strings.txt

On Microsoft Windows, `--scan-ntfs-ads` additionally lists NTFS alternate data streams — a place
where things like to hide.

The resulting list can be fed straight back into Jacksum with `--file-list`.


<a name="sizes-and-timestamps"></a>

## Sizes and timestamps (`stat`, `du`, `ls`)

Three ready-made styles produce hash-free lists. Do **not** combine them with `-a`, they bring their
own algorithm.

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

With `-F` you decide the layout yourself:

    $ jacksum -a none -F "#FILESIZE #FILENAME" .
    7 ./myfile.dat
    12 ./readme.txt
    2 ./photos/2024/b.jpg
    1 ./photos/a.jpg
    24 ./strings.txt

And `-V summary` counts files and bytes of a whole tree:

    $ jacksum -a read -V summary -r max . > /dev/null

    Jacksum: total files read successfully: 5
    Jacksum: total bytes read: 46
    Jacksum: total bytes read (human readable): 46 bytes
    Jacksum: total file read errors: 0


<a name="snapshot"></a>

## Snapshot a directory and detect changes later

`--style without-hashes` writes a list of timestamps, sizes and paths. Because it is a real check
list style, Jacksum can read it back with `-c` — you get a change detector without a single hash
value.

    $ jacksum --style without-hashes --no-header . > ../snapshot.txt

Store the snapshot **outside** the tree you are snapshotting, otherwise it shows up in its own list.

Later, after `readme.txt` grew, `notes.md` appeared and `photos/a.jpg` was deleted:

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

At the default verbosity Jacksum also tells you *why* a file failed:

       FAILED  ./readme.txt
               [filesize expected: 12, actual: 19]

`--list-filter` narrows the report down to what you care about:

    $ jacksum --style without-hashes -c ../snapshot.txt --no-header --list-filter new -V nosummary,noinfo,noerrors .
          NEW  ./notes.md

    $ jacksum --style without-hashes -c ../snapshot.txt --no-header --list-filter bad -V nosummary,noinfo .
    Jacksum: Error: ./photos/a.jpg: does not exist.
      MISSING  ./photos/a.jpg
       FAILED  ./readme.txt

Valid filters are `ok`, `failed`, `missing`, `new`, and the shortcuts `all`, `good`
(`none,ok,new`), `bad` (`none,failed,missing`) and `none`.

If you only care whether the files are still *there*, use `--style names-only` instead — that list
carries nothing but paths:

    $ jacksum --style names-only --no-header . > ../names.txt
    $ jacksum --style names-only -c ../names.txt --no-header --list-filter bad -V nosummary,noinfo .
    $ echo $?
    0

Silence plus exit code 0 means "nothing is missing". As soon as something disappears:

    $ jacksum --style names-only -c ../names.txt --no-header --list-filter bad -V nosummary,noinfo .
    Jacksum: Error: ./photos/a.jpg: does not exist.
      MISSING  ./photos/a.jpg
    $ echo $?
    4

Add `--check-strict` if malformed lines in the list should turn into a nonzero exit code too.


<a name="path-tool"></a>

## Path tool (`realpath`, `dirname`, `basename`, `cygpath`)

    $ jacksum -a none --path-absolute -F "#FILENAME" myfile.dat
    /private/tmp/jacksum-hacks-demo/myfile.dat

    $ jacksum -a none --path-relative-to /private/tmp -F "#FILENAME" .
    jacksum-hacks-demo/myfile.dat
    jacksum-hacks-demo/readme.txt
    jacksum-hacks-demo/photos/2024/b.jpg
    jacksum-hacks-demo/photos/a.jpg
    jacksum-hacks-demo/strings.txt

`#FILENAME{path}` and `#FILENAME{name}` are `dirname` and `basename`:

    $ jacksum -a none -F "#FILENAME{path} :: #FILENAME{name}" .
    . :: myfile.dat
    . :: readme.txt
    ./photos/2024 :: b.jpg
    ./photos :: a.jpg
    . :: strings.txt

`--no-path` drops the directory completely:

    $ jacksum -a none --no-path -F "#FILENAME" .
    myfile.dat
    readme.txt
    b.jpg
    a.jpg
    strings.txt

`-P <char>` swaps the path separator — turn Unix paths into Windows paths and back, or produce
forward slashes for HTML on Windows:

    $ jacksum -a none -P "\\" -F "#FILENAME" .
    .\myfile.dat
    .\readme.txt
    .\photos\2024\b.jpg
    .\photos\a.jpg
    .\strings.txt


<a name="timestamp-tool"></a>

## Timestamp tool (`date -r`)

`-t` formats the last modification time of a file in any format you like.

    $ jacksum -a none -t unixtime -F "#TIMESTAMP #FILENAME" .
    1772356500 ./myfile.dat
    1776843000 ./readme.txt
    1767595500 ./photos/2024/b.jpg
    1766595600 ./photos/a.jpg
    1771023600 ./strings.txt

Predefined formats are `default`, `default-utc`, `iso` (`iso8601`), `iso-utc` (`iso8601utc`),
`unixtime` and `unixtime-ms`:

    $ jacksum -a none -t iso-utc -F "#TIMESTAMP #FILENAME" myfile.dat
    2026-03-01T09:15:00.000Z myfile.dat

    $ jacksum -a none -t default -F "#TIMESTAMP #FILENAME" myfile.dat
    20260301101500000 myfile.dat

    $ jacksum -a none -t unixtime-ms -F "#TIMESTAMP #FILENAME" myfile.dat
    1772356500000 myfile.dat

Anything Java's `SimpleDateFormat` understands works as well:

    $ jacksum -a none -t "yyyy-MM-dd" -F "#TIMESTAMP #FILENAME" .
    2026-03-01 ./myfile.dat
    2026-04-22 ./readme.txt
    2026-01-05 ./photos/2024/b.jpg
    2025-12-24 ./photos/a.jpg
    2026-02-14 ./strings.txt


<a name="template-engine"></a>

## A template engine for file trees

`-F` is free text with a handful of tokens. Combined with `-a none` that makes Jacksum a small code
generator that walks a directory tree. `#QUOTE` inserts a `"`, `#SEPARATOR` inserts whatever `-s`
says.

An HTML link list:

    $ jacksum -a none -P / -F "<li><a href=#QUOTE#FILENAME#QUOTE>#FILENAME{name}</a> &mdash; #FILESIZE bytes</li>" .
    <li><a href="./myfile.dat">myfile.dat</a> &mdash; 7 bytes</li>
    <li><a href="./readme.txt">readme.txt</a> &mdash; 12 bytes</li>
    <li><a href="./photos/2024/b.jpg">b.jpg</a> &mdash; 2 bytes</li>
    <li><a href="./photos/a.jpg">a.jpg</a> &mdash; 1 bytes</li>
    <li><a href="./strings.txt">strings.txt</a> &mdash; 24 bytes</li>

JSON Lines, ready for `jq`:

    $ jacksum -a none -t iso -F '{"path":"#FILENAME","size":#FILESIZE,"mtime":"#TIMESTAMP"}' .
    {"path":"./myfile.dat","size":7,"mtime":"2026-03-01T10:15:00.000+01:00"}
    {"path":"./readme.txt","size":12,"mtime":"2026-04-22T09:30:00.000+02:00"}
    {"path":"./photos/2024/b.jpg","size":2,"mtime":"2026-01-05T07:45:00.000+01:00"}
    {"path":"./photos/a.jpg","size":1,"mtime":"2025-12-24T18:00:00.000+01:00"}
    {"path":"./strings.txt","size":24,"mtime":"2026-02-14T00:00:00.000+01:00"}

CSV, with the separator set by `-s`:

    $ jacksum -a none -s ";" -F "#FILENAME#SEPARATOR#FILESIZE" .
    ./myfile.dat;7
    ./readme.txt;12
    ./photos/2024/b.jpg;2
    ./photos/a.jpg;1
    ./strings.txt;24

A shell script:

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

Use `-o <file>` (or `-O <file>` to overwrite) if the result should go to a file instead of the
terminal.


<a name="charset-converter"></a>

## Character set converter (`iconv`)

`--string-list` reads the strings, `--charset-string-list` says how they are encoded, and
`--charset-stdout` says how they should be written. `#MESSAGE` passes the string through unchanged
— so the only thing that happens is a character set conversion.

    $ jacksum -a none --string-list strings.txt --charset-string-list UTF-8 --charset-stdout ISO-8859-1 -F "#MESSAGE" | xxd
    00000000: 4865 6c6c 6f20 576f 726c 640a 666f 6f0a  Hello World.foo.
    00000010: 4772 fcdf 650a                           Gr..e.

`Grüße` arrives as `47 72 fc df 65` — five ISO-8859-1 bytes instead of seven UTF-8 bytes. Add
`--bom` if the target charset should get a byte order mark. Jacksum knows 170+ character sets;
`--charset-output-file` does the same for `-o`.


<a name="count-bytes"></a>

## Count the bytes of a pipe (`wc -c`)

`-` reads standard input, `#LENGTH` prints how many bytes came through.

    $ printf 'Hello World' | jacksum -a read -F "#LENGTH" -
    11


<a name="reference-book"></a>

## A searchable reference book

The manpage is built into the binary, and `-h <word>` prints just the section that matches. That
makes Jacksum a decent lookup tool even when you are not hashing anything.

    $ jacksum -h -E              # all 19 encodings, with their alphabets
    $ jacksum -h --style         # every style, with a feature matrix
    $ jacksum -h -q              # all input types for -q
    $ jacksum -h algorithms      # all 586 algorithms
    $ jacksum -h examples        # the whole EXAMPLES section
    $ jacksum -h "exit status"   # the exit codes

`--exact` restricts the match to one option:

    $ jacksum --exact -h -g
        -g <count>
        --group-bytes <count>

                Group the hex output for the checksum in <count> bytes for
                better readability, only valid if encoding is hex or hexup.
                ...

If nothing matches, the exit code is 1 — so you can test for the existence of an option in a script:

    $ jacksum --exact -h --nonexistent > /dev/null 2>&1 ; echo $?
    1


<a name="what-am-i-running-on"></a>

## What am I running on?

`--header` prints a block of environment information in front of the actual output — JVM vendor and
version, operating system, architecture, the working directory, and the exact arguments Jacksum was
invoked with. Useful for reproducible reports and for bug reports.

    $ jacksum --header -q txt:
    #
    # created by: Jacksum (https://jacksum.net, version: 4.0.0)
    # invoked on JVM: OpenJDK 64-Bit Server VM (vendor: Eclipse Adoptium, version: 25.0.4+7-LTS)
    # invoked on OS: Mac OS X (arch: aarch64, version: 26.6.1)
    # invoked on date: 2026-08-16T22:35:01.152+02:00
    #
    # invoked from: /private/tmp/jacksum-hacks-demo
    # invocation args: --header -q txt:
    #___________________________________________________________________________________________
    a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a



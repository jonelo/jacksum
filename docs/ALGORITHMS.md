**Table of Contents**
 - [Algorithm Support](#algorithm_support)
 - [Standard Algorithms](#standard_algorithms)
   - [Standard Algorithms, sorted alphabetically](#standard_hash_functions_sorted_alphabetically)
   - [Standard Algorithms, sorted logically](#standard_hash_functions_sorted_logically)
     - [Cryptographic Hash Functions](#cryptographic_hash_functions)
     - [Hash Trees](#hash_trees)
     - [Non-Cryptographic Hash Functions](#non_cryptographic_hash_functions)
 - [Customized Algorithms](#customized_hash_functions)
   - [Concatenated Algorithms](#concatenated_algorithms)
   - [HMAC](#hmac)
   - [Truncated HMAC](#truncated_hmac)
   - [Customizable CRCs](#customizable_crcs)
 - [Getting Details about a Particular Algorithm](#getting_details)
 - [Footnotes](#footnotes)

<a name="algorithm_support"></a>

# Algorithm Support

Jacksum supports **586 standard algorithms**: cryptographic hash functions, hash trees,
non-cryptographic hash functions, CRCs, and classic checksums.

In addition to that, Jacksum also supports **customized algorithms**. That includes
concatenated algorithms, HMAC, truncated HMAC, and customizable CRCs.

The two sections that follow show the very same set of 586 algorithms twice: once
[sorted alphabetically](#standard_hash_functions_sorted_alphabetically) if you want to look
up a particular name, and once [sorted logically](#standard_hash_functions_sorted_logically)
if you want to understand where an algorithm comes from and how trustworthy it is.

See also

- [All available algorithms with details](https://github.com/jonelo/jacksum/wiki/Manpage#algorithms)
- Usage Cheat Sheet: [What algorithms are available](https://github.com/jonelo/jacksum/wiki/Cheat-Sheet#what-algorithms-are-available)
- [Features](FEATURES.md)


<a name="standard_algorithms"></a>
<a name="standard_hash_functions"></a>

# Standard Algorithms

<a name="standard_hash_functions_sorted_alphabetically"></a>

## Standard Algorithms, sorted alphabetically

Adler-32, ascon-hash, ascon-hasha, ascon-xof, ascon-xofa, AST strsum PRNG hash,
belt-hash (STB 34.101.31), BLAKE-[224,256,384,512], BLAKE2b-[8..512], BLAKE2s-[8..256],
BLAKE2bp, BLAKE2sp, BLAKE3, BlueMidnightWish-[224,256,384,512], cksum (Minix), cksum (Unix),
CubeHash-[224,256,384,512], CRC-8 (SMBus, FLAC), CRC-16 (ARC, LHA), CRC-16 (Minix), FCS-16,
CRC-24 (OpenPGP), CRC-32 (FCS-32), CRC-32 (MPEG-2), CRC-32 (bzip2),
CRC-32 (FDDI, a.k.a. sum on Plan 9), CRC-32 (UBICRC32, a.k.a. JAMCRC), CRC-32 (PHP's crc32),
CRC-32 (Go Koopman), CRC-32c (Castagnoli, iSCSI), CRC-64 <sup><a href="#iso3309">note</a></sup>,
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
TTH2, VSH-1024, Whirlpool-0, Whirlpool-1 (a.k.a. Whirlpool-T), Whirlpool, Xoodyak, xor8, and
XXH32.

<a name="standard_hash_functions_sorted_logically"></a>

## Standard Algorithms, sorted logically

<a name="cryptographic_hash_functions"></a>

### Cryptographic Hash Functions

- International and national standards of cryptographic hash functions (descending alphabetical sorting of the countries):

  - United States of America (USA)

    - SHA-1 <sup><a href="#broken">broken</a></sup>
    - SHA-2-family: SHA-[224,256,384,512], SHA-512/[224,256] (NIST FIPS 180-4)
    - SHA-3-family: SHA3-[224,256,384,512], SHAKE[128,256] (NIST FIPS 202)

  - Ukraine

    - Kupyna-[256,384,512] (DSTU 7564:2014)

  - Russian Federation

    - GOST R 34.11-94 <sup><a href="#broken">broken</a></sup>
    - GOST Crypto-Pro (GOST R 34.11-94) <sup><a href="#broken">broken</a></sup>
    - Streebog-256 (GOST R 34.11-2012)
    - Streebog-512 (GOST R 34.11-2012) <sup><a href="#broken">partly broken</a></sup>

  - Republic of Korea (ROK)

    - HAS-160 (KISA)
    - LSH-256-[224,256], LSH-512-[224,256,384,512] (KS X 3262)

  - People's Republic of China (PRC)

    - SM3

  - Republic of Belarus

    - belt-hash (STB 34.101.31)

- eXtendable Output Functions (XOF) as cryptographic hash functions with a fixed length:

  - KangarooTwelve
  - MarsupilamiFourteen
  - SHAKE128
  - SHAKE256

  Ascon-Xof and Ascon-XofA are XOFs as well; they are listed with the rest of the Ascon family below.

- Internationally accepted, modern strong cryptographic hash functions:

  - BLAKE2s-[8..256]
  - BLAKE2b-[8..512]
  - BLAKE2sp-256
  - BLAKE2bp-512
  - BLAKE3
  - RadioGatun[32,64]
  - PHP's variants on Tiger ("tiger192,4", "tiger160,4", and "tiger128,4")
  - RIPEMD-[128,160,256,320]
  - Tiger2
  - Tiger
  - Tiger/128
  - Tiger/160
  - Whirlpool
  - Whirlpool-0 <sup><a href="#superseded">superseded</a></sup>
  - Whirlpool-1, a.k.a. Whirlpool-T <sup><a href="#superseded">superseded</a></sup>

- All 5 finalists that support hashing of the NIST Lightweight Cryptography competition (2019–2023):

  - Ascon-Hash and Ascon-Hasha, Ascon-Xof and Ascon-XofA
  - Esch[256,384]
  - PHOTON-Beetle Hash
  - Romulus-H
  - Xoodyak

- All 5 finalists (round 3) of the NIST SHA-3 competition (2007–2012):

  - BLAKE-[224,256,384,512]
  - Groestl-[224,256,384,512]
  - JH[224,256,384,512]
  - Keccak[224,256,288,384,512]
  - Skein-256-[8..256], Skein-512-[8..512], Skein-1024-[8..1024]

- 8 of 9 candidates from round 2 of the NIST SHA-3 competition, excluding the finalists (2007–2012):

  - ECHO-[224,256,384,512]
  - Fugue-[224,256,384,512], and Fugue2-[224,256,384,512], the 2012 tweak of Fugue
  - Luffa-[224,256,384,512]
  - BlueMidnightWish-[224,256,384,512]
  - SIMD-[224,256,384,512]
  - CubeHash-[224,256,384,512]
  - Hamsi-[224,256,384,512] <sup><a href="#broken">partly broken</a></sup>
  - Shabal-[192,224,256,384,512]

- 2 of 37 candidates from round 1 of the NIST SHA-3 competition, excluding round 2 candidates and finalists (2007–2012):

  - Edon-R-[224,256,384,512] <sup><a href="#broken">partly broken</a></sup>
  - MD6-[8..512]

- Proposals from the 2005 NIST workshops before the SHA-3 competition:

  - DHA-256
  - FORK-256 <sup><a href="#broken">broken</a></sup>
  - VSH-1024 <sup><a href="#broken">partly broken</a></sup>

- Broken cryptographic hash functions for education and backwards compatibility purposes:

  - ed2k <sup><a href="#broken">broken</a></sup>
  - HAVAL-128-[3,4,5], HAVAL-[160,192,224,256]-[3,4,5] <sup><a href="#broken">broken</a></sup>
  - MD2 <sup><a href="#broken">partly broken</a></sup>
  - MD4 <sup><a href="#broken">broken</a></sup>
  - MD5 <sup><a href="#broken">broken</a></sup>
  - MDC2 <sup><a href="#broken">broken</a></sup>
  - PANAMA <sup><a href="#broken">broken</a></sup>
  - SHA-0 <sup><a href="#broken">broken</a></sup>
  - SHA-1 <sup><a href="#broken">broken</a></sup>

<a name="hash_trees"></a>

### Hash Trees

Merkle tree modes on top of a cryptographic hash function. The tree mode itself introduces no
weakness, so each one is exactly as strong as the underlying hash function.

- TTH, the Tiger Tree Hash (based on Tiger)
- TTH2 (based on Tiger2)

<a name="non-cryptographic_hash_functions"></a>
<a name="non_cryptographic_hash_functions"></a>

### Non-Cryptographic Hash Functions

- Standard Cyclic Redundancy Checks (CRCs)

  - CRC-8 (SMBus, FLAC)
  - CRC-16 (ARC, LHA), FCS-16
  - CRC-24 (OpenPGP, RFC 2440)
  - CRC-32 (FCS-32; ISO 3309, ISO/IEC 13239:2002, ITU-T V.42), CRC-32 (MPEG-2),
    CRC-32 (bzip2), CRC-32 (UBICRC32, a.k.a. JAMCRC), CRC-32 (PHP's crc32),
    CRC-32 (GO KOOPMAN), CRC-32c (Castagnoli, iSCSI, RFC 7143 section 13.1)
  - CRC-64 <sup><a href="#iso3309">note</a></sup>, CRC-64 (ECMA-182),
    CRC-64 (prog lang GO, const ISO), CRC-64 (.xz and prog lang GO, const ECMA),
    CRC-64 (NVM Express 64b CRC)
  - CRC-82 (DARC)

- CRCs that need more than the 6 standard Rocksoft (tm) Model parameters, because they
  incorporate the message length into the CRC

  - cksum (Unix, POSIX 1003.2)
  - CRC-32 (FDDI), which is the very same algorithm as sum on Plan 9

- Non-cryptographic hash functions

  - AST strsum PRNG hash
  - ELF (Unix)
  - FNV-0_[32,64,128,256,512,1024]
  - FNV-1_[32,64,128,256,512,1024]
  - FNV-1a_[32,64,128,256,512,1024]
  - joaat (Bob Jenkins' One-at-a-Time Hash)
  - PRNG hash (including parameters)
  - XXH32

- Classic Checksums

  - Adler-32
  - cksum (Minix) <sup><a href="#rocksoft">note</a></sup>
  - CRC-16 (Minix) <sup><a href="#rocksoft">note</a></sup>
  - Fletcher's Checksum
  - sum (BSD Unix)
  - sum (Minix)
  - sum (System V Unix)
  - sum [8,16,24,32,40,48,56,64]
  - xor8

<a name="customized_hash_functions"></a>
<a name="customized_algorithms"></a>

# Customized Algorithms

On top of the 586 standard algorithms, Jacksum lets you build your own. Customized algorithms
are not listed by `jacksum -a all -l`, but they can be selected and used just like any
standard algorithm.

<a name="concatenated_algorithms"></a>

## Concatenated Algorithms

Any number of algorithms can be chained with `+`. All files are read only once, no matter how
many algorithms have been selected, and each algorithm can run on its own thread.

    $ jacksum -a sha256+crc32 -q txt:"Hello World"

The filter syntax works here as well, so `-a all:32+all:64` selects every 32-bit and every
64-bit algorithm at once.

<a name="hmac"></a>

## HMAC

Jacksum supports **HMAC**, a mechanism for message authentication using an iterated
cryptographic hash function in combination with a secret shared key.

    $ jacksum -a hmac:sha256 -k txt:secret -q txt:"Hello World"

**492** of the 586 algorithms can be used as the underlying function of an HMAC; call
`jacksum --hmacs` for the full list, or `jacksum --hmacs -V summary` for the count.

The remaining 94 algorithms cannot be used, for one of the following reasons:

- They are not cryptographic hash functions at all: all CRCs, classic checksums, and
  non-cryptographic hash functions.
- Their block size is not larger than their digest size, which HMAC (RFC 2104 / FIPS 198-1)
  requires. This is why sponge-based and XOF-based constructions are excluded, among them the
  Ascon family, Esch, Xoodyak, PHOTON-Beetle, Romulus-H, RadioGatun, CubeHash, Fugue, Fugue2,
  Luffa, and Hamsi.
- They are not plain iterated hash functions: the tree hashes TTH and TTH2, and the parallel
  and tree-based modes BLAKE3 and BLAKE2bp.

Note that an HMAC built on a <a href="#broken">broken</a> hash function is not automatically
broken itself, and vice versa; see `--info --verbose details` for the specifics.

<a name="truncated_hmac"></a>

## Truncated HMAC

The HMAC output can be truncated to a given number of bits by appending `:<bits>`:

    $ jacksum -a hmac:sha256:128 -k txt:secret -q txt:"Hello World"

`jacksum --hmacs -V info` prints, for every algorithm, the recommended minimum length of the
key and the recommended minimum length of a truncated HMAC.

<a name="customizable_crcs"></a>

## Customizable CRCs

Jacksum implements the **"Rocksoft (tm) Model CRC Algorithm"** to describe CRCs, so an
additional 1.0399 * 10^267 customized CRCs can be used, with a width from 1 to 64 bits:

    crc:<width>,<poly>,<init>,<refIn>,<refOut>,<xorOut>[,<includeLen>[,<xorLen>]]

The two optional parameters `includeLen` and `xorLen` go beyond the classic 6-parameter model
and incorporate the message length into the CRC. For example, CRC-32 (FDDI) can be written as

    $ jacksum -a crc:32,04C11DB7,00000000,true,true,00000000,true,CC55CC55 -q txt:"Hello World"

Add `--info` to any `crc:` definition to see the polynomial in its normal, reversed and
Koopman representation, plus the reciprocal polynomial. `jacksum -h crc:` documents every
parameter in detail; see also the
[Manpage](https://github.com/jonelo/jacksum/wiki/Manpage#algorithms) for worked examples.

<a name="getting_details"></a>

# Getting Details about a Particular Algorithm

The manpage documents each algorithm with its name, length, type, year of publication,
website, standard, a comment, its security status ("broken"), a compatibility list for other
tools and programming languages, the Jacksum version it was added in, and the origin of the
implementation:

    $ jacksum -h sha3-256                     # documentation of one algorithm (or of a family)
    $ jacksum -a sha3-256 --info              # runtime information, including the security status
    $ jacksum -a sha3-256 --info -V details   # the same, with the reasoning behind the security status
    $ jacksum -a all --info                   # the same for every supported algorithm

To list algorithm IDs rather than documentation, and to narrow the list down by bit length or
by a substring of the ID:

    $ jacksum -a all -l                       # the IDs of all 586 algorithms
    $ jacksum -a all:256 -l                   # only the ones with a 256 bit output
    $ jacksum -a all:sha -l                   # only the ones whose ID contains "sha"
    $ jacksum --hmacs                         # only the ones that can be used for an HMAC

<a name="footnotes"></a>

# Footnotes

<a name="broken"></a>

**broken / partly broken** — to see the security section with the actual explanation, call

```
jacksum -a <algorithm> --info --verbose details
```

<a name="superseded"></a>

**superseded** — no attack targeting the algorithm itself has been published, but the design
was revised later on. Whirlpool-0 (2000) had its S-box replaced in Whirlpool-1 (2001), and
Whirlpool-1 still contains the flaw in the diffusion matrix that was corrected only in the
final Whirlpool (2003).

<a name="iso3309"></a>

**CRC-64** — this 64-bit CRC (generator polynomial x^64 + x^4 + x^3 + x + 1) is often wrongly
assumed to be a frame checking sequence defined in ISO 3309. It was used by the SWISS-PROT and
TrEMBL protein sequence data banks until 2009. Only the 32-bit CRC-32/FCS-32 is actually
specified in ISO 3309.

<a name="rocksoft"></a>

**cksum (Minix) and CRC-16 (Minix)** — despite their names, Jacksum classifies both as
checksums rather than CRCs, because neither can be described by the Rocksoft (tm) Model: they
use modified update methods that are kept for compatibility reasons. See
[minix-bug-151](https://jacksum.net/downloads/minix-bug-151.txt).

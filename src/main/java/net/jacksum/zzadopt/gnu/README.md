# Adopted GNU Crypto

`net.jacksum.zzadopt.gnu.crypto` is a copy of the hash functions of
[GNU Crypto](https://www.gnu.org/software/gnu-crypto/), relocated from `gnu.crypto`, and extended by
Jacksum. Jacksum uses it in `net.jacksum.algorithms.wrappers.MDgnu`, `net.jacksum.algorithms.md.Edonkey`
and in about 20 selectors, mostly as the alternate implementation (`-A`) of the algorithms the JDK provides.

## Origin

| | |
|---|---|
| Version | GNU Crypto **2.1.0** (October 2005), the final release. GNU Crypto has been merged into GNU Classpath afterwards and is not developed anymore, so there will be no further update |
| Source | <https://ftp.gnu.org/gnu/gnu-crypto/gnu-crypto-2.1.0.tar.bz2> |
| SHA-256 | `b2cb798e1ce2202ba9f4404b8fc4de4fe52c2be6a206a2721d2e37d00e1c24df` |
| Signature | `gnu-crypto-2.1.0.tar.bz2.sig` is made by RSA subkey `9A9877E909DA2030`. A current gpg with the GNU keyring (<https://ftp.gnu.org/gnu/gnu-keyring.gpg>) cannot verify it, because the subkey from 2005 has no cross-certification |
| License | GPL-2 or later with the GNU Classpath exception, see `src/main/resources/net/jacksum/legal/copyright.txt` |

Up to Jacksum 4.0.1 the copy was based on GNU Crypto 2.0.0/2.0.1 (identical for these files; `HashFactory`
even on CVS revision 1.11, older than 2.0.0). For Jacksum 4.0.2 it was updated to 2.1.0 by a three-way merge
per file (base 2.0.1, upstream 2.1.0, Jacksum's version). 2.1.0 changes the hash code only by the new method
`update(byte[])` in `IMessageDigest` and `BaseHash`. Apart from that it updates the address of the FSF and the
CVS keywords.

## Jacksum's modifications

Files written by Jacksum (not in GNU Crypto):

| File | Algorithm |
|---|---|
| `hash/Sha0.java` | SHA-0 |
| `hash/Sha224.java` | SHA-224 (derived from `Sha256`) |
| `hash/Has160.java` | HAS-160 |
| `hash/Tiger2.java` | Tiger2 (padding byte 0x80 instead of 0x01) |
| `hash/Tiger128.java`, `hash/Tiger160.java` | Tiger truncated to 128 and 160 bits |
| `hash/Whirlpool2000.java` | Whirlpool-0 (derived from GNU Crypto's `Whirlpool`), see below |
| `hash/Whirlpool2003.java` | Whirlpool, the final version (derived from GNU Crypto's `Whirlpool`), see below |

The three Whirlpool versions by Paulo S.L.M. Barreto and Vincent Rijmen:

| Jacksum algorithm | Class | Version |
|---|---|---|
| `whirlpool0`, `whirlpool-0` | `Whirlpool2000` | Whirlpool-0, the original specification of 2000 |
| `whirlpool1`, `whirlpool-1`, `whirlpool-t` | `Whirlpool2001` | Whirlpool-1, the first revision of 2001 with an improved S-box design; since March 2007 the authors call it Whirlpool-T. This is the version that GNU Crypto implements as `gnu.crypto.hash.Whirlpool` |
| `whirlpool`, `whirlpool2`, `whirlpool-2` | `Whirlpool2003` | Whirlpool, the second revision of May 24, 2003 with an improved diffusion matrix, after Shirai and Shibutani had found a flaw in the matrix of the previous versions (March 11, 2003) that made its branch number suboptimal. Adopted by ISO/IEC 10118-3:2004 |

Modified upstream files:

| File | Modification | Why |
|---|---|---|
| `hash/Whirlpool2001.java` | renamed from `Whirlpool`; hash size **64** instead of 20 bytes in the constructor; debug and timing code commented out | GNU Crypto's `Whirlpool` is Whirlpool-1 (2001), the name distinguishes it from the other two Whirlpool versions (see above). The hash size of 20 is an upstream bug (still in 2.1.0 and in GNU Classpath 0.99), `hashSize()` would report 160 bits |
| `hash/HashFactory.java` | `implements Registry`; creates Jacksum's algorithms (Whirlpool versions, SHA-224, Tiger2/160/128, all HAVAL variants, SHA-0, HAS-160); the `selfTest()` call in `getInstance()` is commented out ("selfTests only during test phases") | Jacksum's algorithms. Upstream runs the self test on every `getInstance()`, which costs time for each new instance |
| `Registry.java` | reduced to the names of the hash functions, plus the names of Jacksum's algorithms. Not updated to the constants that 2.1.0 adds (SASL, EAX, TLS/SSL padding, Fortuna, OMAC, ...) | Jacksum uses only the hash functions |
| `hash/Tiger.java` | `HASH_SIZE`, `BLOCK_SIZE`, `valid` and the registers `a`, `b`, `c` are `protected` instead of `private` | used by the subclasses `Tiger2`, `Tiger128`, `Tiger160` |
| all upstream files | `@Override`; `new Boolean(...)`/`booleanValue()` replaced by autoboxing; `private static final synchronized` becomes `private static synchronized`; `MD5.getResult()` returns directly; Javadoc fixes (`<tt>` becomes `<code>`, `@param`/`@return` added, broken `</a>` in `Tiger`, "SHA2-1" becomes "SHA2" in `Sha256`) | compiler warnings with current JDKs |
| all files | package `net.jacksum.zzadopt.gnu.crypto` instead of `gnu.crypto` | no clash with another GNU Crypto or GNU Classpath on the classpath |

## Why not GNU Classpath

GNU Classpath took over GNU Crypto in 2006 (`gnu.java.security.hash`, last release 0.99 in 2012). A comparison
of Classpath 0.99 with GNU Crypto 2.1.0 (October 2026) showed no reason to switch:

- the same algorithms (Haval, MD2, MD4, MD5, RipeMD128/160, Sha160/256/384/512, Tiger, Whirlpool), no
  additional ones, so all of Jacksum's own classes would still be needed;
- apart from Whirlpool, the code differs only cosmetically (e.g. `Boolean.valueOf()`, direct `return`),
  mostly the same modernizations Jacksum has made itself. There are no bug fixes in the hash algorithms;
- Classpath's `Whirlpool` is the final version of 2003, which Jacksum already has as `Whirlpool2003`
  (identical output). Whirlpool-1 would still be needed as Jacksum's own class, and the wrong hash size
  of 20 bytes is still in Classpath;
- the outputs of all common algorithms are identical (15 input lengths up to 100,003 bytes, fed in random
  pieces), and so is the speed;
- Classpath's code depends on `gnu.java.lang.CPStringBuilder` and `gnu.java.security.Configuration`
  (logging), which would have to be adapted again;
- Classpath is no longer developed either, and the license is the same.

## Removed in Jacksum 4.0.2

`net.jacksum.zzadopt.gnu.java.security` was a second copy of the same hash functions from GNU Classpath 0.99
(`gnu.java.security`), with small Jacksum changes (`StringBuilder` instead of `CPStringBuilder`, no logger in
`Whirlpool`, Javadoc). Jacksum did not use it, so it has been deleted.

## Verification of the update to 2.1.0

- `bouncycastle/verify/compare-jars.sh` (all algorithms, 17 input lengths, HMACs, `--info`) with the jar
  before and after the update: identical. The same with `-a all -A` (alternate implementations, i.e.
  the GNU Crypto classes where the JDK provides the primary one) over 21 input lengths: identical.
- The built-in `selfTest()` of all 40 hash names in `Registry` passes, and `update(byte[])` gives the same
  result as `update(byte[], int, int)`.
- md5, sha1, sha224, sha256, sha384, sha512, ripemd160, md4 and whirlpool (`-A`) match `openssl dgst`.

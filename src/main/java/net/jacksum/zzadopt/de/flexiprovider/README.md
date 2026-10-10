# Adopted FlexiProvider

`net.jacksum.zzadopt.de.flexiprovider` is a stripped copy of [FlexiProvider](https://web.archive.org/web/2016/http://www.flexiprovider.de/)
by The FlexiProvider Group, Technische Universität Darmstadt (Fachgebiet Kryptographie und Computeralgebra),
relocated from `de.flexiprovider`. It provides the hash functions that were proposed at the NIST crypto
workshops before the SHA-3 competition:

| Jacksum algorithm | Class |
|---|---|
| `dha256`, `dha-256` | `core/md/DHA256.java` |
| `fork256`, `fork-256` | `core/md/FORK256.java` |
| `vsh`, `vsh-1024` | `core/md/VSH.java` |

Jacksum uses them in `net.jacksum.algorithms.wrappers.MDflexiprovider` and in the selectors `DHA256_Selector`,
`FORK256_Selector` and `VSH_Selector`. They are part of Jacksum since 3.0.0.

## Origin

| | |
|---|---|
| Version | FlexiProvider **1.7p7** (sources of January 21, 2014; `FlexiCoreProvider` says `@version 1.7.6`), the final release. FlexiProvider is no longer developed |
| Source | `FlexiProvider-1.7p7_src.zip`. www.flexiprovider.de does not serve it anymore (HTTP 403), but the Wayback Machine has the original file: <https://web.archive.org/web/20160810001523/http://flexiprovider.de/download/FlexiProvider-1.7p7_src.zip> |
| SHA-256 | `b1e1a6a3de6ab14bcd3b8a3c9aa285fe61139d28060f597db328b84a92a6f3eb` |
| Mirror | <https://github.com/sharebookproject/AndroidPQCrypto> (`MCElieceProject/Flexiprovider`) contains the same sources, but it is not official and its umlauts are broken, so use it only as a secondary source |
| Encoding | the original files are ISO-8859-1, Jacksum's copy is UTF-8 (`project.build.sourceEncoding` in `pom.xml`) |

Only the 16 files that Jacksum needs are included. That is the transitive closure of the three hash
functions as `javac -implicit:class` determines it over Jacksum's own sources.

## License

The 16 files belong to the packages `api`, `common` and `core` of FlexiProvider. Their `COPYING` files
(`de/flexiprovider/core/COPYING`, `de/flexiprovider/common/COPYING`) say:

- the code can be licensed under either the **GNU Lesser General Public License, version 2.1 or (at your
  option) any later version**, or under an individual license for commercial projects. Jacksum uses the
  LGPL, which is compatible with Jacksum's GPL-3.0-or-later;
- copyright (c) 1998-2003 by The FlexiProvider Group, Technische Universitaet Darmstadt.

`api` has no `COPYING` file of its own. The file headers refer to "the file COPYING in the root directory of
this package", and the API is part of the core provider. Only the EC and NF providers (`de/flexiprovider/ec`,
`de/flexiprovider/nf`) are GPL licensed, and Jacksum uses neither.

The `LGPL.txt` that FlexiProvider ships next to `COPYING` contains the text of the GNU *Library* General
Public License version 2 (1991), although `COPYING` refers to version 2.1. This is an inconsistency upstream.

Up to Jacksum 4.0.1, `src/main/resources/net/jacksum/legal/copyright.txt` (`--copyright`) wrongly named
"GPL-2.0 or later" for this code. Since 4.0.2 it quotes the LGPL notice from `core/COPYING`, with the current
address of the FSF.

## Jacksum's modifications

Compared with FlexiProvider 1.7p7 (package name normalized, whitespace ignored).

Unchanged: `core/md/DHA256.java`, `common/math/FlexiBigInt.java`, `api/exceptions/DigestException.java`,
`api/exceptions/NoSuchAlgorithmException.java`.

| File | Modification | Why |
|---|---|---|
| `core/md/VSH.java` | **bug fix:** `digest()` pads the result with leading zero bytes to `VSH_DIGEST_LENGTH` (128 bytes) by the new method `extendByteArray()` (Dec 2020) | upstream returns `FlexiBigIntUtils.toMinimalByteArray(result)`, which drops leading zero bytes, so the hash is shorter than 1024 bits whenever the result has leading zero bytes. For short messages that is almost always the case, e.g. 7 bytes for the message "m0". Apart from the padding, the values are identical to upstream (checked with 3000 messages) |
| `core/md/VSH.java` | `@Override`, Javadoc, IntelliJ code formatting | |
| `core/md/FORK256.java` | `STEP()` renamed to `step()`; new debug methods `printIntermediateValues()` and `printOutputCompressionFunction()` (Dec 6, 2020), their calls are commented out; `@Override`, Javadoc, IntelliJ code formatting | debugging during the port; the computation is unchanged |
| `api/Registry.java` | reduced to message digests and secure randoms: the tables, imports, getters and registrations of ciphers, modes, paddings, MACs, signatures, keys and parameters are removed or commented out | everything else would pull in large parts of FlexiProvider |
| `api/MessageDigest.java`, `api/SecureRandom.java` | `@Override` on the JCA adapter methods, Javadoc reformatted | compiler warnings |
| `common/math/IntegerFunctions.java` | `log(double)`, `log(long)` and `logBKM(double)` marked `@Deprecated`; a direct `return`; Javadoc made acceptable to doclint (`<tt>` becomes `<code>`, `<`/`>` as entities) | compiler and Javadoc warnings |
| `common/util/BigEndianConversions.java`, `common/util/FlexiBigIntUtils.java`, `common/exceptions/ECException.java`, `common/exceptions/NoQuadraticResidueException.java` | Javadoc only: `<tt>` becomes `<code>`, `@see` to classes that are not included becomes plain "see" | Javadoc warnings |
| `api/Registry.java`, `api/exceptions/RegistrationException.java`, `common/util/DefaultPRNG.java`, `common/math/IntegerFunctions.java` | author names Martin Döring and Johannes Müller restored (4.0.2); a former encoding conversion had replaced the umlauts with U+FFFD | correct attribution |
| all files | package `net.jacksum.zzadopt.de.flexiprovider` instead of `de.flexiprovider` | no clash with a FlexiProvider on the classpath |

## Known issues

- The debug methods in `FORK256` are unused, but they import `net.loefflmann.sugar.util.ByteSequences`, so
  the adopted code depends on Jacksum's utility library.

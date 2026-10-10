# Adopted sphlib (fr.cryptohash)

`net.jacksum.zzadopt.fr.cryptohash` is a copy of the Java part of **sphlib** by Thomas Pornin
(Projet RNRT SAPHIR), relocated from `fr.cryptohash`. It provides SHA-3 candidates of the NIST SHA-3
competition (2007-2012) and two older hash functions:

| Algorithm family | Lengths | Classes |
|---|---|---|
| BLAKE | 224, 256, 384, 512 | `BLAKE*` (round 3 tweak) |
| Blue Midnight Wish | 224, 256, 384, 512 | `BlueMidnightWish*` (sphlib's `BMW*`) |
| CubeHash | 224, 256, 384, 512 | `CubeHash*` |
| ECHO | 224, 256, 384, 512 | `ECHO*` |
| Fugue | 224, 256, 384, 512 | `Fugue*` |
| Groestl | 224, 256, 384, 512 | `Groestl*` (round 3 tweak) |
| Hamsi | 224, 256, 384, 512 | `Hamsi*` |
| JH | 224, 256, 384, 512 | `JH*` (round 3 tweak) |
| Luffa | 224, 256, 384, 512 | `Luffa*` |
| Shabal | 192, 224, 256, 384, 512 | `Shabal*`, `ShabalGeneric` |
| SHAvite-3 | 224, 256, 384, 512 | `SHAvite*` |
| SIMD | 224, 256, 384, 512 | `SIMD*` |
| PANAMA | 256 | `PANAMA` |
| RadioGatun | 32, 64 (word size) | `RadioGatun32`, `RadioGatun64` |

Jacksum uses them in `net.jacksum.algorithms.wrappers.MDCryptohashSphlib3` (via `ext/HashFactory`) and in
the corresponding selectors in `net.jacksum.selectors`.

## Origin

| | |
|---|---|
| Version | sphlib **3.0**, the final release (SHA-3 round 3 tweaks of BLAKE, Groestl, JH, Keccak and Skein). sphlib is no longer developed |
| Author | Thomas Pornin, Projet RNRT SAPHIR |
| Source | `sphlib-3.0.zip`. The project site www.saphir2.com is no longer reachable, but the Wayback Machine has the original file: <https://web.archive.org/web/20120320213137/http://www.saphir2.com/sphlib/files/sphlib-3.0.zip> (`ext/HashFactory.java` refers to a 2016 snapshot of the same file) |
| SHA-256 | `99def13143639a38ad388702afa15ad657f5ae076831229261c67e06b3106f9d` |
| Java sources | `sphlib-3.0/java/fr/cryptohash` |

## License

MIT license, see `sphlib-3.0/LICENSE.txt`: "Copyright (c) 2007-2011 Projet RNRT SAPHIR". The license text is also
in the header of every file (with 2007-2010) and in `src/main/resources/net/jacksum/legal/copyright.txt`
(`--copyright`).

## Jacksum's modifications

Compared with sphlib 3.0 (package name normalized; whitespace, comments and `@Override` ignored, token by token):
47 files are identical, and so are `BlueMidnightWish224/256/384/512` apart from the class name. The only
change of the computation is the IV of Hamsi-224.

### Hamsi-224: initial value

| | IV (first four words; the remaining four are the same) | |
|---|---|---|
| sphlib 3.0 | `c3967a67 c3bc6c20 4bc3bcc3 a7c3bc6b` | the correct UTF-8 encoding of "Özgül Küçük, Katholieke Uni..." (the designer's name). sphlib marks the other IV as "Wrong IV, but compatible with test vectors submitted for round 2 of the SHA-3 competition" and, according to its `README.txt` and `c/hamsi.c`, anticipates a corrected Hamsi specification |
| Jacksum | `3c967a67 3cbc6c20 b4c343c3 a73cbc6b` | the IV of the Hamsi submission package for round 2, so Jacksum's hamsi224 is compatible with the official round 2 test vectors, not with sphlib 3.0. Jacksum's comment in `Hamsi224.java` calls the sphlib IV wrong ("there seems to be a typo at IV[3]") and refers to page 9 of the specification (<https://www.esat.kuleuven.be/cosic/publications/article-1203.pdf>) |

Hamsi-256, -384 and -512 are unchanged.

### Other modifications

| File | Modification | Why |
|---|---|---|
| `BlueMidnightWish224.java` ... `BlueMidnightWishSmallCore.java` | renamed from sphlib's `BMW224.java` ... `BMWSmallCore.java` (added in 2024) | readable class names |
| `BLAKE224/256/384/512`, `BLAKEBigCore`, `BLAKESmallCore`, `BlueMidnightWishBigCore`, `BlueMidnightWishSmallCore`, `CubeHashCore`, `ECHOBigCore`, `ECHOSmallCore`, `GroestlBigCore`, `GroestlSmallCore`, `JHCore`, `Luffa384`, `Luffa512`, `LuffaSmallCore`, `SHAviteBigCore`, `SHAviteSmallCore`, `SIMDBigCore`, `SIMDSmallCore` | Javadoc only (`@param`/`@return` added), `@Override`, code formatting (BLAKE) | compiler and Javadoc warnings |
| `DigestEngine.java` | Javadoc: removed a stray `</code>` | Javadoc error |
| `ext/HashFactory.java`, `ext/Registry.java` | written by Jacksum: creates the digests by name | used by `MDCryptohashSphlib3` |
| all files | package `net.jacksum.zzadopt.fr.cryptohash` instead of `fr.cryptohash` | no clash with another sphlib on the classpath |

## Not included

The other Java classes of sphlib 3.0 are not included, because Jacksum has other implementations of those
algorithms: HAVAL, HMAC, Keccak, MD2, MD4, MD5, RIPEMD, SHA-0, SHA-1, SHA-2, Skein, Tiger, Tiger2 and
Whirlpool (with their cores), as well as `MDHelper`, `package-info` and the `test` directory.

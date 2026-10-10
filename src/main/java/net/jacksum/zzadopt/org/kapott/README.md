# Adopted HBCI4Java (MDC2)

`net.jacksum.zzadopt.org.kapott.cryptalgs.MDC2` is a copy of the class `org.kapott.cryptalgs.MDC2` from
[HBCI4Java](https://github.com/hbci4j/hbci4java) by Stefan Palme, relocated from `org.kapott.cryptalgs`.
It implements MDC-2 (Modification Detection Code 2, ISO/IEC 10118-2, ANSI X9.31), a 128-bit hash function
built on DES; the DES operations come from the JCE (`javax.crypto.Cipher.getInstance("DES")`).

Jacksum uses it in `net.jacksum.selectors.MDC2_Selector` (algorithms `mdc2`, `mdc-2`), since Jacksum 3.0.0.

## Origin

| | |
|---|---|
| Project | HBCI4Java, <https://github.com/hbci4j/hbci4java> (still maintained; current release 4.1.17 on Maven Central as `com.github.hbci4j:hbci4j-core`) |
| File | `src/main/java/org/kapott/cryptalgs/MDC2.java` |
| Upstream state | unchanged since commit `07ba9fef5f` of December 22, 2019 ("Adopt all GPL license headers to LGPL"), the state of `master` in October 2026 |
| History | in HBCI4Java 2.x the class was `org.kapott.hbci.security.MDC2` (e.g. `hbci4java-2.5.0rc-1-src.zip` of 2005, GPL-2.0+), and an earlier development state of Jacksum used that one (see the commented-out code in `MDC2_Selector`, `net.jacksum.adapt.org.kapott.hbci.security.MDC2Provider`; MDC2 was released first with Jacksum 3.0.0). Later it moved to the sub-project CryptAlgs4Java as `org.kapott.cryptalgs.MDC2` (GPL-2.0+, `$Id` of 2011) and became part of HBCI4Java again (LGPL since 2019). Jacksum's current copy is derived from the current `org.kapott.cryptalgs.MDC2`: compared token by token, it differs from it only by the modifications below, but in 45 places from the version of 2005 |

## License

GNU Lesser General Public License, version 2.1 or (at your option) any later version, "Copyright (c)
2001-2010 Stefan Palme", see the file header and `LICENSE` of HBCI4Java. The license text is also in
`src/main/resources/net/jacksum/legal/copyright.txt` (`--copyright`).

Up to December 2019 the file was part of CryptAlgs4Java and licensed under the GPL, version 2 or later.
Jacksum's copy carries the LGPL header of the current version.

## Jacksum's modifications

Compared with the current upstream file (comments ignored, token by token), Jacksum's copy has exactly the
changes that its header lists:

| Modification | Why |
|---|---|
| `extends java.security.MessageDigest` instead of `java.security.MessageDigestSpi`, with `super("MDC2")` in the constructor | Jacksum uses the class directly as a `MessageDigest`, without registering a JCE provider |
| four chains of `catch` clauses (each `throw new RuntimeException(e)`) replaced by multi-catch | readability |
| code reformatted (e.g. braces around single statements) | readability |
| header comment: link to hbci4java, note on the patent (see below), list of these changes | documentation |
| package `net.jacksum.zzadopt.org.kapott.cryptalgs` instead of `org.kapott.cryptalgs` | no clash with HBCI4Java on the classpath |

The computation is unchanged.

## Patent

MDC-2 was covered by the U.S. Patent 4,908,861 (IBM), which expired on August 28, 2007:
<https://patents.google.com/patent/US4908861>. (The link in the file header points to the former USPTO
full text database `patft.uspto.gov`, which no longer exists.)

## Verification

MDC2("Now is the time for all ") = `42e50cd224baceba760bdd2bd409281a`, the test vector of OpenSSL's
`mdc2test` (checked in October 2026 with `jacksum -a mdc2 -q "txt:Now is the time for all "`).

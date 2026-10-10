# Adopted LSH (kr.re.nsr.crypto)

`net.jacksum.zzadopt.kr.re.nsr.crypto` is a copy of the Java reference implementation of **LSH**
(Lightweight Secure Hash) by the NSR (National Security Research Institute, Korea), as published by KISA
(Korea Internet & Security Agency), relocated from `kr.re.nsr.crypto`. LSH was designed in 2014 and is the
Korean national standard KS X 3262.

| Jacksum algorithm | Word length | Output |
|---|---|---|
| `lsh-256-224`, `lsh-256-256` | 32 bit (`hash/LSH256.java`) | 224, 256 bits |
| `lsh-512-224`, `lsh-512-256`, `lsh-512-384`, `lsh-512-512` | 64 bit (`hash/LSH512.java`) | 224, 256, 384, 512 bits |

Jacksum uses it in `net.jacksum.algorithms.wrappers.LSHWrapper` and `net.jacksum.selectors.LSH_Selector`,
since Jacksum 3.0.0.

## Origin

| | |
|---|---|
| Publisher | KISA, page "해시함수 LSH 소스코드" (hash function LSH source code): <https://seed.kisa.or.kr/kisa/Board/22/detailView.do>. The download needs a browser (a form with a CSRF token, sometimes a short survey). Navigation: seed.kisa.or.kr, 자료실 (downloads), 암호알고리즘 소스코드 (source code of cryptographic algorithms), 해시함수 LSH |
| Base of Jacksum's copy | "LSH_source code.zip (v1.0.1)", SHA3-256 `9bd824fa734635ecc9dbbfde737a7ee2c8f74010c96cda482180e780b9b54797` (recorded in `Hash.java`). KISA no longer offers v1.0.1 |
| Current version | `해시함수 LSH_소스코드(v1.0.2).zip` (files of February 16, 2021), SHA-256 `c5e8294870553a151bde3846649441dceb063a14da2e7df77ffb643472836992`, SHA3-256 `396f447868e4aec8d68e80ff27fbe16fe4bf77afed0863fb23167fb41caf461b`. Java sources in `lsh/source/java/LSH/src`. The zip contains no change log |
| Mirror | <https://github.com/sjnst217/KISA_LSH> (`KISA_LSH.zip`, repacked in 2023): its Java sources are identical to v1.0.2 apart from the line endings, so use it only as a secondary source |

## License

MIT license, "Copyright (c) 2016 NSR (National Security Research Institute)", see the `LICENSE` files in
`lsh/source/c/*` and `lsh/source/python` of the KISA zip; the Java directory has no `LICENSE` file of its own.
The license text is also in `src/main/resources/net/jacksum/legal/copyright.txt` (`--copyright`).

## Jacksum's modifications

Compared with v1.0.2 (package name normalized, comments ignored, token by token), `Mac.java`, `mac/HMac.java`
and `util/PackLE.java` are identical. Computationally, Jacksum's copy is identical to v1.0.2.

| File | Modification | Why |
|---|---|---|
| `hash/LSH256.java`, `hash/LSH512.java` | **bug fix in `update(byte[], int, int)`** (Jacksum 4.0.1): the free space of the block buffer is `BLOCKSIZE - idx` instead of v1.0.1's `BLOCKSIZE - rbytes` | v1.0.1 computed the gap from the length of the incoming data rather than from the fill mark of the buffer, so it went negative as soon as more than one block arrived at once while the buffer was already partially filled, and `update()` failed with an `ArrayIndexOutOfBoundsException`. KISA has fixed the same bug in the same way in v1.0.2 (`BLOCKSIZE - blkidx`) |
| `hash/LSH256.java`, `hash/LSH512.java` | the fill mark is called `idx` | the name of v1.0.1; v1.0.2 renamed it to `blkidx` together with the bug fix |
| `Hash.java` | `digest(int, int, byte[], int, int)` uses `switch` instead of `if`/`else if`/`else` | equivalent; v1.0.2 has `if`/`else`. Whether v1.0.1 already had the `switch` is unknown |
| all files | Korean comments and Javadoc translated to English (completed in 4.0.2); in `LSH256.update()` the length `lenbits` is documented in bits (the Korean original said bytes, although the parameter is in bits, as in `LSH512`) | readability |
| all files | package `net.jacksum.zzadopt.kr.re.nsr.crypto` instead of `kr.re.nsr.crypto` | no clash with another copy on the classpath |

## Not used by Jacksum

`Mac.java` and `mac/HMac.java` (HMAC-LSH) are kept, but Jacksum does not use them: Jacksum computes HMACs
with its own HMAC implementation. Jacksum's own code needs only `Hash`, `hash/LSH256`, `hash/LSH512` and
`util/PackLE`.

## Verification

The test program of the KISA zip (`lsh/source/java/LSH_testcode`: `LshTestMain`, `LSH256VS`, `LSH512VS`,
`HMACLSH256VS`, `HMACLSH512VS`) with the package name changed passes all 36 test vectors against Jacksum's
copy: LSH-256-256 and LSH-512-512 for 16 message lengths each (0 to 3073 bits and 0 to 6145 bits,
including lengths that are no multiple of 8), and 2 HMAC-LSH-256 and 2 HMAC-LSH-512 vectors. Checked in October 2026, before and after the
translation of the comments.

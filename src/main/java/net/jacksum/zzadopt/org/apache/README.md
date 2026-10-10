# Adopted Apache Commons Codec (XXHash32)

`net.jacksum.zzadopt.org.apache.commons.codec.digest.XXHash32` is a copy of the class
`org.apache.commons.codec.digest.XXHash32` from [Apache Commons Codec](https://commons.apache.org/proper/commons-codec/),
relocated from `org.apache.commons.codec.digest`. It implements xxHash32 by Yann Collet
(<https://xxhash.com/>). It is the only class Jacksum takes from Commons Codec.

Jacksum uses it in `net.jacksum.algorithms.wrappers.xxHash32` and `net.jacksum.selectors.xxHash32_Selector`
(algorithm `xxhash32`), since Jacksum 3.0.0.

## Origin

| | |
|---|---|
| Version | Apache Commons Codec **1.15** (released in 2020). Apart from the package name, Jacksum's file is byte-identical to `org/apache/commons/codec/digest/XXHash32.java` of `commons-codec-1.15-sources.jar` (1.14 differs only in its CRLF line endings) |
| Source | Maven Central: <https://repo1.maven.org/maven2/commons-codec/commons-codec/1.15/commons-codec-1.15-sources.jar> |
| Upstream history | Commons Codec copied the class from Apache Commons Compress 1.14 and has it since Commons Codec 1.11 |
| Current version | Commons Codec 1.22.1 (July 2026). Its `XXHash32` is computationally identical to 1.15 (see below), so there is no reason to update |

## License

Apache License 2.0, see the header of `XXHash32.java` and `src/main/resources/net/jacksum/legal/copyright.txt`
(`--copyright`). The `NOTICE.txt` of Commons Codec 1.15 says:

```
Apache Commons Codec
Copyright 2002-2020 The Apache Software Foundation

This product includes software developed at
The Apache Software Foundation (https://www.apache.org/).
```

(The rest of that `NOTICE.txt` concerns test data and the package `org.apache.commons.codec.language.bm`,
which Jacksum does not include.) As section 4(d) of the Apache License 2.0 requires, `copyright.txt` includes
this attribution since Jacksum 4.0.2.

## Jacksum's modifications

| File | Modification | Why |
|---|---|---|
| `commons/codec/digest/XXHash32.java` | package `net.jacksum.zzadopt.org.apache.commons.codec.digest` instead of `org.apache.commons.codec.digest` | no clash with a Commons Codec on the classpath |

There are no other modifications. Jacksum's own fix of 4.0.1 (`xxhash32` did not reset the number of
processed bytes, so `#FILESIZE` kept growing from one calculation to the next) is in the wrapper
`net.jacksum.algorithms.wrappers.xxHash32`, not in the adopted class.

## Comparison with later versions (October 2026)

From 1.16 to 1.22.1, Commons Codec changed `XXHash32` only cosmetically: the long literals use `L` instead
of `l` (1.16.0), the methods are in a different order and `getInt()` has fewer parentheses (1.16.1), and some
comments and Javadoc have changed. Compared token by token without comments and with the methods
reordered, the code is the same. A test with 4 seeds and 13 input lengths up to 65,537 bytes, fed in random
pieces and in one piece after `reset()`, gives identical values for Jacksum's copy and 1.22.1 (104
comparisons). Known values: xxh32("") = `02cc5d05`, xxh32("abc") = `32d153ff` (seed 0).

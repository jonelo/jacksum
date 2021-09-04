# Jacksum v3

## Overview

**Jacksum** (JAva ChecKSUM) is a free and cross platform data integrity
software tool to master checksums, CRCs, and message digests (hashes).

Jacksum is both a **command line tool**, and a **library**.

Jacksum runs on GNU/Linux, Microsoft Windows, and macOS. It takes advantage of modern **multi-processor/multi-core** environments for parallel computation and verification of hashes.

Jacksum supports **470 algorithms**, including
popular national- and international standards (e. g. SHA-2-family, SHA-3-family, GOST, Strebog, SM3, Kupyna, LSH, etc.),
cryptographic and non-cryptographic hash functions (e. g. BLAKE, BLAKE2b, BLAKE2s, BLAKE3, ed2k, Groestl, HAVAL, JH, Keccak, RadioGatun, RIPEMD, Skein, Tiger, Tiger2, TTH, Whirlpool2),
extendable output functions (e. g. SHAKE, KangarooTwelve, MarsupilamiFourteen),
checksums, and CRCs.
Also historic resp. broken algorithms are supported (e. g. DHA-256, ECHO, Fugue, FORK-256, HAS-160,
Luffa, MD2, MD4, MD5, MDC2, SHA-0, SHA-1, VSH-1024, PANAMA, Whirpool-0, Whirlpool-T).
Many of those algorithms are powered by many different, open source libraries or have been implemented for Jacksum.
Jacksum combines all algorithms in one tool/library and provides a feature-rich framework to use
all algorithms with simple, dedicated interfaces (CLI and API).

Go to [https://jacksum.net](https://jacksum.net) to learn more about Jacksum.

## Features

- select one, many, or all of the 470 algorithms for hash calculation 
  and verification
- select algorithms manually, filter them by name or message digest width
- process hash calculation and hash verification
  simultaneously in multi-processor/multi core environments
  (many files and many algorithms are supported)
- calculate hashes of files, stdin, file lists or command line argument values
- include not only the hash, but also the file size and/or file modification timestamp of files for integrity checks
- perform integrity checks, and detect ok, failed, missing, and new files
- specify the character set for both input and output
  (GB18030, UTF-8, UTF-16, UTF-16BE, UTF-16LE, UTF-32, UTF-32BE, UTF-32LE, etc.)
- specify one of the supported encodings for the hash values
  (bin, dec, oct, hex lowercase, hex uppercase, base16, base32, base32-nopadding, base32hex, base32hex-nopadding, base64, base64url, bubblebabble, etc.)  
- use comprehensive format options to get the output you need 
- use predefined compatibility files to read and write popular
  3rd party format styles (GNU/Linux, BSD, SFV, FCIV, openssl, etc.)
- create and use your own compatibility files
- create and use your own customized, user-defined output format
  (and create ed2k-links, magnet-links, or patch the Solaris pkgmap format for instance)
- use any of the predefined CRC or customize your own CRC, because Jacksum supports the
  "Rocksoft (tm) Model CRC Algorithm" quasi-standard, and an extended model of it
- find the algorithm that was used to calculate a checksum/CRC/hash
- find all files that match a given hash value
- process directories recursively, and limit the depth
- determine how to handle symbolic links on files and/or directories
- continue code flow in your own scripts dependent on the exit status of Jacksum
- specify your preferred level of verbosity of the tool
- obtain details for any of the 470 algorithms, including comprehensive compatibility lists
- use Jacksum the cross platform way, because it works the same on GNU/Linux, macOS, and Microsoft Windows
- etc.


## Documentation

### End users

* Jacksum [Homepage](https://jacksum.net) (https://jacksum.net)
* Jacksum [Dowload](https://github.com/jonelo/jacksum/releases) (precompiled)
* Jacksum [Manpage](https://github.com/jonelo/jacksum/blob/main/src/main/resources/net/jacksum/help/help_en.txt) (with many examples)
* Jacksum [Release Notes](https://github.com/jonelo/jacksum/blob/main/RELEASE-NOTES.txt) (2002-2021)

### Developers

* File [support requests, change requests, bug reports and feature requests on github](https://github.com/jonelo/jacksum/issues)
* Jacksum [Source Code](https://github.com/jonelo/jacksum) (on github, mavenized with an IntelliJ .idea config)
* Start your investigation journey by reading [net.jacksum.cli.Main](https://github.com/jonelo/jacksum/blob/main/src/main/java/net/jacksum/cli/Main.java) and [net.jacksum.JacksumAPI](https://github.com/jonelo/jacksum/blob/main/src/main/java/net/jacksum/JacksumAPI.java)
* Download and read the [jacksum-3.0.0-javadoc.jar](https://github.com/jonelo/jacksum/releases/download/v3.0.0/jacksum-3.0.0-javadoc.jar)

## License

The license that the project is offered under is the [GPL-3.0+](https://github.com/jonelo/jacksum/blob/main/LICENSE).


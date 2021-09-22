![GitHub issues](https://img.shields.io/github/issues-raw/jonelo/jacksum?color=blue)
![GitHub closed issues](https://img.shields.io/github/issues-closed-raw/jonelo/jacksum?color=blue)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/jonelo/jacksum?color=green)
![GitHub](https://img.shields.io/github/license/jonelo/jacksum?color=green)
![GitHub top language](https://img.shields.io/github/languages/top/jonelo/jacksum?color=green)

# Jacksum v3

## Abstract

**Jacksum** (JAva ChecKSUM) is a free and cross platform data integrity software tool.

It takes advantage of modern **multi-processor/multi-core** environments for **parallel computation and verification of hashes**, resp. checksums, CRCs, and message digests.

Jacksum supports tons of useful features and **470 algorithms**.

Jacksum is able to **find files that matches a hash value** and it can **find an algorithm to a hash value** using brute force.

Jacksum is both a **command line tool**, and a **library**. It can be integrated in your file browser.


## System Requirements

- GNU/Linux, Microsoft Windows, or macOS
- JDK 11 or later
- 2 MiB disk space

<details>
<summary>Details:</summary>

- To download the (Open)JDK 11 or later, you can go to any vendor that provides OpenJDK compatible builds, LTS (long term support) releases are recommended, examples are
  - https://adoptium.net
  - https://openjdk.java.net/
  - https://www.azul.com/downloads/?package=jdk
  - https://bell-sw.com/pages/downloads/
  - https://www.microsoft.com/openjdk
  - https://aws.amazon.com/de/corretto/
  - https://github.com/alibaba/dragonwell8
- Supported architectures are dependent on the OS and the JDK vendor:
  - x86 64 bit (x64)
  - x86 32 bit
  - ARM 64 bit (AArch64, resp. M1)
  - ARM 32 bit (AArch32)
  - PPC 64 bit
- a GitHub user have had success to run Jacksum without modification even on a smartphone running Android on ARM 64 bit, see also https://github.com/jonelo/jacksum/issues/7
- GNU/Linux is the correct term to refer to "Linux", see also https://www.gnu.org/gnu/linux-and-gnu.en.html
- actual RAM requirement is dependent on the OS, the architecture, the JDK, the JRE's (default) garbage collector settings and usage. Tests have shown that Jacksum feels pretty comfortable with 512 MiB Java heap on a x64 Windows 10 system for example while verifying millions of files of all sizes (0 bytes to several GiB).

</details>

## Quick Start

### How to configure and install it

Open a terminal (on Microsoft Windows, it is known as the "Command Prompt") and start Jacksum by typing

```
java -jar jacksum-3.0.0.jar
```

I recommend to adjust the Windows batch file (jacksum.bat) resp. the bash script (jacksum) for GNU/Linux and macOS, and add it to your PATH. You find more instructions in those files.

### Examples of how to use it

```
java -jar jacksum-3.0.0.jar -h
```

See also the [EXAMPLES section of the manpage](https://github.com/jonelo/jacksum/wiki/Manpage#examples).


## Features

- Jacksum supports 470 algorithms including
  - National standards of cryptographic hash functions: SHA-1, SHA-[224,256,384,512], SHA-512/[224,256] (USA, NIST FIPS 180-4); SHA3-[224,256,384,512], SHAKE[128,256] (USA (NIST FIPS 202); GOST, GOST Crypto-Pro (Russia, GOST R 34.11-94); Streebog-[256,512] (Russia, GOST R 34.11-2012); SM3 (China); Kupyna[256,384,512] (Ukraine, DSTU 7564:2014); LSH-256-[224,256], LSH-512-[224,256,384,512] (South Korea, KS X 3262); HAS-160 (KISA, South Korea)
  - All 5 candidates from round 3 the NIST SHA-3 competition (2007-2012): BLAKE-[224,256,348,512], Groestl-[224,256,384,512], JH[224,256,284,512], Keccak[224,256,384,512], Skein-256-[8..256], Skein-512-[8..512], Skein-1024-[8..1024]
  - 3 candidates from round 2 of the NIST SHA-3 competition (2007-2012): ECHO-[224,256,348,512], Fugue-[224,256,348,512], Luffa-[224,256,348,512]
  - Proposals from the 2005 NIST workshops before the SHA-3 competition: DHA-256, FORK-256, VSH-1024
  - International accepted, modern strong cryptographic hash functions: BLAKE2s-[8..256], BLAKE2b-[8..512], BLAKE3, ed2k, HAVAL-[160,192,224,256]-[3,4,5], RadioGatun[32,64], RIPEMD[160,256,320], Tiger2, Whirlpool
  - eXtendable Output Functions (XOF) as cryptographic hash functions with a fixed length: SHAKE128, SHAKE256, KangarooTwelve, MarsupilamiFourteen
  - Broken cryptographic hash functions for education and backwards compatibility purposes: HAVAL-128-[3,4,5], MD2, MD4, MD5, MDC2, PANAMA, RIPEMD-128, SHA-0, SHA-1, Tiger, Tiger/128, Tiger/160, Whirpool-0, Whirlpool-T
  - Checksums that can be found in software products and operating systems: Adler-32, cksum (Minix), cksum (Unix), ELF (Unix), Fletcher's Checksum, FNV-0_[32,64,128,256,512,1024], FNV-1_[32,64,128,256,512,1024], FNV-1a_[32,64,128,256,512,1024], joaat, sum (BSD Unix), sum (Minix), sum (System V Unix), sum [8,16,24,32,40,48,56], xor8, XXH32
  - CRCs that are being used in many software products and protocols: CRC-8 (FLAC), CRC-16 (LHA/ARC), CRC-16 (Minix), FCS-16, CRC-24 (OpenPGP), CRC-32 (FCS-32), CRC-32 (MPEG-2), CRC-32 (bzip2), CRC-32 (FDDI), CRC-32 (UBICRC32), CRC-32 (PHP's crc32), CRC-64 (ISO 3309), CRC-64 (ECMA-182), CRC-64 (prog lang GO, const ISO), CRC-64 (.xz and prog lang GO, const ECMA)
  - for a full detail list, go to https://github.com/jonelo/jacksum/wiki/Supported-Algorithms

- Multi-Core/Multi Processor Support
  - Process hash calculation and hash verification simultaneously in multi-processor/multi core environments
  - Works well if you have many cores, and many hundreds, thousands or millions of files to process and/or
  - Works well if you have selected more than one algorithm

- Algorithm Selection
  - Select one, many, or all algorithms for hash calculation and verification
  - Select algorithms manually, filter them by name or message digest width
  - Use any of the predefined CRC or customize your own CRC, because Jacksum supports the "Rocksoft (tm) Model CRC Algorithm", and an extended model of it
  - Algorithms can be concatenated in order to calculate many algorithms in one pass

- I/O Related Features
  - Calculate hashes from files, stdin, file lists or command line argument values
  - Process directories recursively, and limit the depth
  - Jacksum detects file system cycles for you and it bypasses those in order to avoid endless loops
  - It is aware of special files like FIFOs, and symlinks on Linux/Unix and system folders on Windows
  - Control how to handle symbolic links on files and/or directories
  - Specify the character set for both input and output: GB18030, UTF-8, UTF-16, UTF-16BE, UTF-16LE, UTF-32, UTF-32BE, UTF-32LE, etc.
  - It is large file aware, it can process filesizes up to 8 Exabytes (= 8,000,000,000 Gigabytes), presupposed your operating system respectively your file system is large file aware, too.

- Format Output Features
  - By default, the output is 100% compatible to Unix-standard tools such as sum, cksum, md5sum and sha1sum, b2sum, etc.
  - Use comprehensive format options to get the output you need
  - Specify one of the supported encodings for the hash values: bin, dec, oct, hex lowercase, hex uppercase, base16, base32, base32-nopadding, base32hex, base32hex-nopadding, base64, base64url, bubblebabble, etc.
  - Create and use your own customized, user-defined output format and create ed2k-links, magnet-links, or patch the Solaris pkgmap format for instance

- Integrity Verification Features
  - Include not only the hash, but also the file size and/or file modification timestamp of files for performing reliable integrity checks
  - Perform integrity checks, and detect ok, failed, missing, and new files
  - Use predefined compatibility files to read and write popular 3rd party format styles (GNU/Linux, BSD, SFV, FCIV, openssl, etc.)
  - Create and use your own compatibility files

- Find Objects with Jacksum
  - Find the algorithm that was used to calculate a checksum/CRC/hash
  - Find all files that match a given hash value (find duplicates of a file)

- Multi-Platform Support without recompilation
  - Operating Systems
    - Microsoft Windows (e.g. Microsoft Windows 10)
    - GNU/Linux (e.g. Ubuntu)
    - Unix (e.g. macOS)
  - Supported architectures are dependent on the JDK
    - x86 64 bit (x64)
    - x86 32 bit
    - ARM 64 bit (AArch64, resp. M1)
    - ARM 32 bit (AArch32)
    - PPC 64 bit
  - any other operating system or architecture with an OpenJDK compatible Java Runtime Environment (JRE) or Java Development Kit (JDK)

- Interaction with other Tools
  - Works with the SendTo-feature on many file browsers (e.g. macOS Finder, Microsoft Windows Explorer, Gnome Nautilus, KDE Konqueror, ROX Filer, etc.)
  - As it has a command line interface, Jacksum can be used in cronjobs and autostart environments
  - Jacksum returns an exit status which is dependent on the result of the calculation/verification process, so you can use Jacksum in scripts and batches and control the code flow in your own scripts
  - Use predefined compatibility files to read and write popular 3rd party format styles in order to interact with other tools (GNU/Linux, BSD, SFV, FCIV, openssl, etc.)

## Contribution

* File [support requests, change requests, bug reports and feature requests on GitHub](https://github.com/jonelo/jacksum/issues)

## Documentation

* Jacksum [Release Notes](https://github.com/jonelo/jacksum/blob/main/RELEASE-NOTES.txt) (2002-2021)
* Jacksum [Manpage](https://github.com/jonelo/jacksum/wiki/Manpage) (with many examples)
* Jacksum [Wiki](https://github.com/jonelo/jacksum/wiki)
* Jacksum [Source Code](https://github.com/jonelo/jacksum) (on github, mavenized with a pom.xml and an IntelliJ .idea config)
  * Start your investigation journey by reading [net.jacksum.cli.Main](https://github.com/jonelo/jacksum/blob/main/src/main/java/net/jacksum/cli/Main.java) and [net.jacksum.JacksumAPI](https://github.com/jonelo/jacksum/blob/main/src/main/java/net/jacksum/JacksumAPI.java)
  * Download and read the [jacksum-3.0.0-javadoc.jar](https://github.com/jonelo/jacksum/releases/download/v3.0.0/jacksum-3.0.0-javadoc.jar)
* [https://jacksum.net](https://jacksum.net)

## Integration

* [Jacksum File Browser Integration for GNU/Linux](https://github.com/jonelo/jacksum-fbi-linux)
* [Jacksum File Browser Integration for macOS](https://github.com/jonelo/jacksum-fbi-macos)

## Products that are powered by Jacksum

Create an [issue on github](https://github.com/jonelo/jacksum/issues) and let me know that you use Jacksum for your product if you want to be listed here.

- NumericalChameleon, see http://www.numericalchameleon.net

## License

The license that the project is offered under is the [GPL-3.0+](https://github.com/jonelo/jacksum/blob/main/LICENSE).

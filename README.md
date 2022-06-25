![GitHub issues](https://img.shields.io/github/issues-raw/jonelo/jacksum?color=blue)
![GitHub closed issues](https://img.shields.io/github/issues-closed-raw/jonelo/jacksum?color=blue)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/jonelo/jacksum?color=green)
![GitHub downloads latest](https://img.shields.io/github/downloads/jonelo/jacksum/v3.4.0/total?color=green)
![GitHub](https://img.shields.io/github/license/jonelo/jacksum?color=green)
![GitHub top language](https://img.shields.io/github/languages/top/jonelo/jacksum?color=green)
![GitHub downloads](https://img.shields.io/github/downloads/jonelo/jacksum/total?color=green)

* * *

<img width="128" height="128" align="left" src="https://raw.githubusercontent.com/jonelo/jacksum/main/docs/images/jacksum_logo_128x128.png" alt="Jacksum logo" style="vertical-align:top;margin:10px 10px" />

# Jacksum

**Jacksum** (**JAva ChecKSUM**) is a free, open source, cross-platform, feature-rich, multi-threaded command line tool for calculating hash values, verifying data integrity, finding files by their fingerprints, and finding algorithms to hash values.

Jacksum supports 472 hash functions, both cryptographic and non-cryptographic hash functions including CRCs and classic checksums:

Adler-32, BLAKE-[224,256,348,512], BLAKE2b-[8..512], BLAKE2s-[8..256], BLAKE3, cksum (Minix), cksum (Unix), CRC-8 (FLAC), CRC-16 (LHA/ARC), CRC-16 (Minix), FCS-16, CRC-24 (OpenPGP), CRC-32 (FCS-32), CRC-32 (MPEG-2), CRC-32 (bzip2), CRC-32 (FDDI), CRC-32 (UBICRC32), CRC-32 (PHP's crc32), CRC-64 (ISO 3309), CRC-64 (ECMA-182), CRC-64 (prog lang GO, const ISO), CRC-64 (.xz and prog lang GO, const ECMA), DHA-256, ECHO-[224,256,348,512], ed2k, ELF (Unix), Fletcher's Checksum, FNV-0_[32,64,128,256,512,1024], FNV-1_[32,64,128,256,512,1024], FNV-1a_[32,64,128,256,512,1024], FORK-256, Fugue-[224,256,348,512], GOST Crypto-Pro (GOST R 34.11-94), GOST R 34.11-94, Groestl-[224,256,384,512], HAS-160 (KISA), HAVAL-128-[3,4,5], HAVAL-[160,192,224,256]-[3,4,5], JH[224,256,284,512], joaat, KangarooTwelve, Keccak[224,256,384,512], Kupyna[256,384,512] (DSTU 7564:2014), LSH-256-[224,256], LSH-512-[224,256,384,512] (KS X 3262), Luffa-[224,256,348,512], MD2, MD4, MD5, MDC2, MarsupilamiFourteen, PANAMA, RIPEMD-128, RIPEMD[160,256,320], RadioGatun[32,64], SHA-0, SHA-1, SHA-[224,256,384,512], SHA-512/[224,256]  (NIST FIPS 180-4), SHA3-[224,256,384,512], SHAKE[128,256] (NIST FIPS 202), SM3, Skein-1024-[8..1024], Skein-256-[8..256], Skein-512-[8..512], Streebog-[256,512] (GOST R 34.11-2012), sum (BSD Unix), sum (Minix), sum (System V Unix), sum [8,16,24,32,40,48,56], Tiger, Tiger/128, Tiger/160, Tiger2, VSH-1024, Whirpool-0, Whirlpool-T, Whirlpool, xor8, and XXH32.

Jacksum comes with a bunch of [features](https://github.com/jonelo/jacksum/wiki/Features).

Jacksum is also a library. It is written entirely in **Java** ☕.

## Don't panic. It is a CLI, but there is also a GUI

Jacksum provides a command line interface (CLI). And if you are a skilled command line user (advanced user, sysadmin, computer scientist, cybersecurity engineer, penetration tester, forensics engineer, reverse engineer, ...) you could benefit from the power on the command line.

If you prefer a graphical user interface (GUI) and you just would like to calc and verify hashes with a graphical user interface, I recommend to download and use the Jacksum File Browser Integration (FBI) installer which comes with [HashGarten](https://github.com/jonelo/HashGarten) that is a GUI for Jacksum. The installer is available for many different file managers on Windows, Linux, and macOS. See also [Integrations](https://github.com/jonelo/jacksum#integrations).

## Screenshot

Stay tuned.

## System Requirements

- GNU/Linux, Microsoft Windows, or macOS
- JDK 11 or later
- 2 MiB disk space

<details>
<summary>Details ...</summary>

- To download the (Open)JDK 11 or later, you can go to any vendor that provides OpenJDK compatible builds, LTS (long term support) releases are recommended, examples are
  - https://adoptium.net
  - https://openjdk.java.net
  - https://www.azul.com/downloads/?package=jdk
  - https://bell-sw.com/pages/downloads/
  - https://www.microsoft.com/openjdk/
  - https://aws.amazon.com/de/corretto/
  - https://sapmachine.io
  - https://github.com/alibaba/dragonwell8
- Supported architectures are dependent on the OS and the JDK vendor:
  - x86 64 bit (x64)
  - x86 32 bit (x86)
  - ARM 64 bit (AArch64, resp. M1)
  - ARM 32 bit (AArch32)
  - PPC 64 bit (ppc64)
- a GitHub user have had success to run Jacksum without modification even on a smartphone running Android on ARM 64 bit, see also https://github.com/jonelo/jacksum/issues/7
- GNU/Linux is the correct term to refer to "Linux", see also https://www.gnu.org/gnu/linux-and-gnu.en.html
- actual RAM requirement is dependent on the OS, the architecture, the JDK, the JRE's (default) garbage collector settings and usage. Tests have shown that Jacksum feels pretty comfortable with 512 MiB Java heap on a x64 Windows 10 system for example while verifying millions of files of all sizes (0 bytes to several GiB).

</details>


## Where to download?

The latest released .jar file can be found at https://github.com/jonelo/jacksum/releases/latest
The .zip file also contains simple scripts to call Jacksum on Windows, Linux, and macOS from the command line.

## How to install and configure it

Download the .jar (or .zip) file as described above, open a terminal (on Microsoft Windows, it is known as the "Command Prompt") and start Jacksum by typing

```
java -jar jacksum-3.4.0.jar
```

I recommend to adjust the Windows batch file (jacksum.bat) resp. the bash script (jacksum) for GNU/Linux and Unix operating systems (e.g. macOS) and to put the script to a folder that is reachable by your PATH environment variable in order to launch jacksum easily just by typing

```
jacksum
```

<details>
<summary>Details ...</summary>

The following snippet could help you to setup Jacksum on GNU/Linux. In the example below, the launch script will be stored in `$HOME/bin/` which will also be added to your PATH. The jar file will be stored to `/opt/java/apps/jacksum/` so other users on the computer have access to it as well. 

```
$ echo 'export PATH="$PATH=$HOME/bin"' >> $HOME/.profile
$ export VERSION=3.4.0
$ unzip jacksum-$VERSION.zip
$ cp jacksum-$VERSION/unix/jacksum ~/bin && chmod +x ~/bin/jacksum
$ sudo mkdir -p /opt/java/apps/jacksum && cp jacksum-$VERSION/jacksum-$VERSION.jar /opt/java/apps/jacksum/
$ rm -R jacksum-$VERSION/
```

</details>

## Examples of how to use it

```
jacksum -h examples
```

See also the [EXAMPLES section of the manpage](https://github.com/jonelo/jacksum/wiki/Manpage#examples), and the [Cheat Sheet in the Wiki](https://github.com/jonelo/jacksum/wiki/Cheat-Sheet).


## Features

Jacksum supports **471 data fingerprinting algorithms**, including checksums, CRCs, XOFs,
cryptographic, and non-cryptographic hash functions.
Jacksum also supports the "Rocksoft (tm) Model CRC Algorithm" to
customize CRCs.

Jacksum can act as a classic **file integrity software** that generates,
stores, and compares message digests to detect changes made to the
files. Actually it can **detect matching, non-matching,
missing, and new files**.

Jacksum traverses file trees **recursively** with a **depth limit**.

Jacksum also allows you to **identify files** by their digital
fingerprints, find files that match a given hash value, find all
duplicates of a file, and even **find the algorithm** to a
checksum/CRC/hash by using a brute force algorithm.

Jacksum supports **multi-threading** on multi-processor and multi-core
computer systems. Jacksum can compute multiple **hashes simultaneously**,
and it can process multiple **files simultaneously**.

**Input** data can come from almost **any source**: files, disks, partitions,
standard input stream (stdin), and/or provided directly by command
line arguments. Also platform specific input such as NTFS Alternate
Data Streams (ADS) on Microsoft Windows, and block devices, character
devices, named pipes (FIFOs), and sockets (Unix-like OS only), and
doors (Solaris only) are supported and can be hashed.

**Output** can occur in **predefined standard formats** (BSD-, GNU/Linux-, or
Solaris style, SFV or FCIV) or in a **user-defined format** which is highly
customizable, including many encodings for representing hash values:
Hex (lower- and uppercase), Base16, Base32 (with and without padding),
Base32hex (with and without padding), Base64 (with and without padding),
Base64url (with and without padding), BubbleBabble, and z-base-32.
Paths can be omitted, printed absolutely or relative to a different path.

Jacksum supports **many charsets** for reading and writing files
properly, and it comes with full support for all common **Unicode** aware
charsets such as UTF-8, UTF-16, UTF-16BE, UTF-16LE, UTF-32, UTF-32LE,
UTF-32BE, and GB18030. A **Byte-Order Mark (BOM)** is supported for both
input and output, even if a BOM is optional for the selected charset.

Jacksum is a **command line tool**, and it can be integrated in your
**file browser**, see also [Integration](https://github.com/jonelo/jacksum#integration).

Jacksum can also be used as a **library** in your own projects by using its
**API**. Jacksum keeps the binary small, because it bundles only what it really needs to do the job.

For more information, see also the [comprehensive list of features](https://github.com/jonelo/jacksum/wiki/Features).


## Documentation

* [https://jacksum.net](https://jacksum.net) - Homepage
* [Features](https://github.com/jonelo/jacksum/wiki/Features)
* [Manpage](https://github.com/jonelo/jacksum/wiki/Manpage) (with many examples)
* [Wiki](https://github.com/jonelo/jacksum/wiki)
* [Release Notes](https://github.com/jonelo/jacksum/blob/main/RELEASE-NOTES.txt) - since 2002!
* [Developer Guide](https://github.com/jonelo/jacksum/wiki/Developer-Guide)
* [Source Code](https://github.com/jonelo/jacksum) (on GitHub, mavenized with a pom.xml and an IntelliJ .idea config)


## Integrations

* [Jacksum File Browser Integration for Microsoft Windows](https://github.com/jonelo/jacksum-fbi-windows)
* [Jacksum File Browser Integration for GNU/Linux](https://github.com/jonelo/jacksum-fbi-linux)
* [Jacksum File Browser Integration for macOS](https://github.com/jonelo/jacksum-fbi-macos)
* [Jacksum on Docker](https://hub.docker.com/r/jonelo/jacksum)
* [HashGarten](https://github.com/jonelo/HashGarten) - a GUI for Jacksum
* NumericalChameleon, see http://www.numericalchameleon.net

## History

* [Release Notes](https://github.com/jonelo/jacksum/blob/main/RELEASE-NOTES.txt) - since 2002!
* The first release of Jacksum was published in July 2002 on https://sourceforge.net/projects/jacksum/
* I made a pause in Jacksum development beteween 2007 and 2020
* Jacksum is actively developed again
* In September 2021 I moved the repo to GitHub

## Contribution

I appreciate feedback from users, bug hunters, and fresh ideas from open minded people. Feel free and file [support requests, change requests, bug reports and feature requests on GitHub](https://github.com/jonelo/jacksum/issues)

Spread the word, or give a star here on GitHub. 

## License

The license that the project is offered under is the [GPL-3.0+](https://github.com/jonelo/jacksum/blob/main/LICENSE).

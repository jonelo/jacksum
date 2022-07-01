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

The algorithms above are mainly powered by portions from large crypto libraries such as [Bouncy Castle](https://www.bouncycastle.org), [GNU Crypto](https://www.gnu.org/software/gnu-crypto/), Flexiprovider (abandoned), and Projet RNRT SAPHIR (abandoned). Libraries that have been abandoned are now supported by Jacksum (the hash part only). There are also algorithms that have been contributed to Jacksum by institutions, and individual developers, some have been translated to Java from C-sources or have been implemented from scratch. The Jacksum manpage clearly points out the origin for each algorithm. Any algorithm that is accepted by Jacksum will benefit from the framework that applies to all algorithms. For more information on that subject please go to the [copyright page](https://jacksum.net/en/legal/copyright.html).

Jacksum comes with a bunch of [features](https://github.com/jonelo/jacksum/wiki/Features).

Jacksum is also a library. It is written entirely in **Java** ☕.

## Don't panic. It is a CLI, but there is also a GUI

Jacksum provides a command line interface (CLI). And if you are a skilled command line user (advanced user, sysadmin, computer scientist, cybersecurity engineer, penetration tester, forensics engineer, reverse engineer, ...) you could benefit from the power on the command line.

If you prefer a graphical user interface (GUI) and you just would like to calc and verify hashes with a graphical user interface, I recommend to download and use the Jacksum File Browser Integration (FBI) installer which comes with [HashGarten](https://github.com/jonelo/HashGarten) that is a GUI for Jacksum. The installer is available for many different file managers on Windows, Linux, and macOS. See also [Integrations](https://github.com/jonelo/jacksum#integrations).

## Screenshot

<img width="100%" src="https://raw.githubusercontent.com/jonelo/jacksum/main/docs/images/screenshot-jacksum_on_ubuntu-cli_examples.png" alt="Jacksum on Ubuntu, CLI examples" style="vertical-align:top;margin:10px 10px" />

## Core Features

### Calculate hash values

A hash function maps a bit string m ∈ {0, 1}<sup>*</sup> of arbitrary length to a bit string h ∈ {0, 1}<sup>n</sup> of fixed length n ∈ ℕ:

    h = f(m)

m is often called the message or data. h is often called the checksum, hash, hash value, message digest, even (data's) finger- or thumbprint.

Examples:

    $ jacksum -a sha3-256 ubuntu-22.04-desktop-amd64.iso
    c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43 ubuntu-22.04-desktop-amd64.iso

    $ jacksum -a sha3-256 --style bsd ubuntu-22.04-desktop-amd64.iso
    SHA3-256 (ubuntu-22.04-desktop-amd64.iso) = c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43

    $ jacksum -a crc32c+sha-256+sha3-256 -E base64-nopadding --format "#ALGONAME{i} (#FILENAME) = #HASH{i}" .
    crc32c (./ubuntu-22.04-desktop-amd64.iso) = GBhNzg
    sha-256 (./ubuntu-22.04-desktop-amd64.iso) = uFKG2YVfVJ7ZiVdjUZ9qKVp2mPucXFNFgRs+7637bwc
    sha3-256 (./ubuntu-22.04-desktop-amd64.iso) = xeRkJqPKCuhI0pd0ftOEZFLMezPVtBivlh29Vd6N/0M

Jacksum also supports the "Rocksoft (tm) Model CRC Algorithm" to customize CRCs, and en extended model of it to define CRCs that incorporate the length of the message.

Example:

    $ jacksum -a crc:32,04C11DB7,FFFFFFFF,true,true,0 -x -q txt:123456789
    340bc6d9 9


### Verify data integrity

Data integrity ensures that data items have not been changed, destroyed, or lost in an unauthorized or accidental manner since they were created,
transmitted, or stored.

As a file integrity software Jacksum can generate, store, and compare hash values to detect changes made to files. Actually it can detect matching, non-matching, missing, and new files.

Example:

    $ jacksum -a sha3-256 -c file.hashes .

<details>
<summary>Result ...</summary>

```
Jacksum: Error: ./drei: does not exist.
  MISSING  ./drei
      NEW  ./file.hashes
      NEW  ./fünf
   FAILED  ./eins
       OK  ./zwei
       OK  ./vier

Jacksum: total lines in check file: 4
Jacksum: improperly formatted lines in check file: 0
Jacksum: properly formatted lines in check file: 4
Jacksum: ignored lines (empty lines and comments): 0
Jacksum: correctness of check file: 100.00 %

Jacksum: matches (OK): 2
Jacksum: mismatches (FAILED): 1
Jacksum: new files (NEW): 2
Jacksum: missing files (MISSING): 1

Jacksum: total files read: 3
Jacksum: total bytes read: 12
Jacksum: total bytes read (human readable): 12 bytes
Jacksum: total file read errors: 1

Jacksum: elapsed time: 151 ms
```
</details>


### Find files by their hashes

Jacksum also helps you to find all files that match any of the hashes in a given set of known hash values. Both positive and negative matching is supported.

    $ jacksum --wanted-list log4j.hashes --style linux --threads-reading 16 /

<details>
<summary>Result ...</summary>

```
    MATCH  /opt/serverapp/okmijnuhbzgv.jar (log4j-core-2.12.0.jar)

Jacksum: total lines in check file: 42
Jacksum: improperly formatted lines in check file: 0
Jacksum: properly formatted lines in check file: 33
Jacksum: ignored lines (empty lines and comments): 9
Jacksum: correctness of check file: 100.00 %

Jacksum: files read successfully: 252299
Jacksum: files read with errors: 0
Jacksum: total bytes read: 117670015750
Jacksum: total bytes read (human readable): 109 GiB, 602 MiB, 892 KiB, 774 bytes
Jacksum: files matching wanted hashes: 1
Jacksum: files not matching wanted hashes: 252266

Jacksum: elapsed time: 8 min, 38 s, 215 ms
```
</details>

See also [CVE-2021-44832: Find vulnerable .jar files using Jacksum](https://loefflmann.blogspot.com/2022/06/CVE-2021-44832%20Find%20vulnerable%20.jar%20files%20using%20Jacksum%203.4.0%20or%20later.html)

### Finding all duplicates by knowing a hash only

Jacksum also helps you find all duplicates of a file.

    $ jacksum -e c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43 .
    c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43 .\ubuntu-22.04-desktop-amd64.iso
    c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43 .\ubuntu-22.04-desktop-amd64 (1).iso

### Finding the algorithm to a hash

Since Jacksum supports so many algorithms, Jacksum also helps you to find the algorithm to a checksum/CRC/hash by calling a fast and smart brute force algorithm if the hash is unknown, but the data is known.

    $ jacksum -a unknown:16 -q hex:050000 -E hex -e d893
    
<details>
<summary>Result ...</summary>

```
Trying 13 algorithms with a width of 16 bits that are supported by Jacksum 3.0.0 ...

Trying 30 CRC algorithms with a width of 16 bits by testing against well known CRCs ...
crc:16,1021,FFFF,false,false,FFFF
    --> CRC-16/GENIBUS

Trying all CRC algorithms with a width of 16 bits by brute force (be patient!) ...
crc:16,1021,FFFF,false,false,FFFF
crc:16,37D2,FFFF,true,false,FFFF
crc:16,3E2D,0000,true,false,FFFF
crc:16,4175,FFFF,true,false,FFFF
crc:16,4A5B,FFFF,true,true,0000
crc:16,5A41,FFFF,true,false,FFFF
crc:16,5C63,FFFF,true,true,0000
crc:16,6287,FFFF,true,true,0000
crc:16,649C,0000,false,true,FFFF
crc:16,6D55,FFFF,true,true,0000
crc:16,75AC,FFFF,true,false,FFFF
crc:16,7D64,FFFF,false,false,FFFF
crc:16,81A6,FFFF,true,false,FFFF
crc:16,B9F9,FFFF,true,true,0000
crc:16,C3D6,FFFF,false,false,FFFF
crc:16,D436,0000,true,false,FFFF
crc:16,D6D2,0000,false,true,FFFF
crc:16,DA9C,FFFF,true,false,FFFF
crc:16,E03E,FFFF,false,false,FFFF
crc:16,F701,FFFF,true,false,FFFF


Jacksum: algorithms tested: 1048620
Jacksum: algorithms found: 21

Jacksum: elapsed time: 6 s, 460 ms
```

Means Jacksum has tested more than one million algorithms in about 7 seconds and it found 21 matching algorithms. Each of those returns the same CRC value. Test with more input/output sequences and/or longer input sequences in order to find the right algorithm. The most likely algorithm is printed with a name if it is a well known CRC. In this example it has been identified as the CRC-16/GENIBUS.

Once you have identified the correct algorithm, you can calculate your own input data using the CRC definitions that have been found.
```
jacksum -a crc:16,1021,FFFF,false,false,FFFF -E hex -q txt:"Hello World"
```
</details>
    
### More examples

    $ jacksum -h examples

See also the [EXAMPLES section of the manpage](https://github.com/jonelo/jacksum/wiki/Manpage#examples), and the [Cheat Sheet in the Wiki](https://github.com/jonelo/jacksum/wiki/Cheat-Sheet).


## More Features

Jacksum traverses file trees **recursively** with a **depth limit**.

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

Jacksum can also be used as a **library** in your own projects by using its
**API**. Jacksum keeps the binary small, because it bundles only what it really needs to do the job.

For more details, see also the [comprehensive list of features](https://github.com/jonelo/jacksum/wiki/Features).


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

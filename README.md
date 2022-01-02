![GitHub issues](https://img.shields.io/github/issues-raw/jonelo/jacksum?color=blue)
![GitHub closed issues](https://img.shields.io/github/issues-closed-raw/jonelo/jacksum?color=blue)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/jonelo/jacksum?color=green)
![GitHub](https://img.shields.io/github/license/jonelo/jacksum?color=green)
![GitHub top language](https://img.shields.io/github/languages/top/jonelo/jacksum?color=green)

* * *

# Jacksum

<img width="128" height="128" align="left" src="https://raw.githubusercontent.com/jonelo/jacksum/main/docs/images/jacksum_logo_128x128.png" alt="Jacksum logo" style="vertical-align:top;margin:10px 10px" />

**Jacksum** is is a free, powerful, cross-platform
command line utility, and lib which is all about checksums, CRCs,
and message digests (aka hashes, data/file fingerprints, thumbprints).

Jacksum supports **471 fingerprinting algorithms**, including checksums, CRCs, XOFs,
cryptographic, and non-cryptographic hash functions.
Jacksum also supports the "Rocksoft (tm) Model CRC Algorithm" to
customize CRCs.

Jacksum can act as a classic **file integrity software** that generates,
stores, and compares message digests to detect changes made to the
files. Actually it can **detect matching files, non-matching files,
missing files, and new files**.

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
Base32hex (with and without padding), Base64, Base64url, BubbleBabble,
and z-base-32.

Jacksum supports **many charsets** for reading and writing files
properly, and it comes with full support for all common **Unicode** aware
charsets such as UTF-8, UTF-16, UTF-16BE, UTF-16LE, UTF-32, UTF-32LE,
UTF-32BE, and GB18030. A **Byte-Order Mark (BOM)** is supported for both
input and output, even if a BOM is optional for the selected charset.

Jacksum is a **command line tool**, and it can be integrated in your
**file browser**, see also [Integration](https://github.com/jonelo/jacksum#integration).

Jacksum can also be used as a **library** in your own projects by using its
**API**. Jacksum keeps the binary small, because it bundles only what it really needs to do the job.

Jacksum is a synthetic word made of **JAva and ChecKSUM**, because Jacksum is written entirely in Java.

For more information, see also the [comprehensive list of features](https://github.com/jonelo/jacksum/wiki/Features).

## System Requirements

- GNU/Linux, Microsoft Windows, or macOS
- JDK 11 or later
- 2 MiB disk space

<details>
<summary>Details:</summary>

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

## Quick Start

### How to configure and install it

Open a terminal (on Microsoft Windows, it is known as the "Command Prompt") and start Jacksum by typing

```
java -jar jacksum-3.1.0.jar
```

I recommend to adjust the Windows batch file (jacksum.bat) resp. the bash script (jacksum) for GNU/Linux and Unix operating systems (e.g. macOS) and to put the script to a folder that is reachable by your PATH environment variable in order to launch jacksum easily just by typing

```
jacksum
```

### Examples of how to use it

```
jacksum -h examples
```

See also the [EXAMPLES section of the manpage](https://github.com/jonelo/jacksum/wiki/Manpage#examples).

## Contribution

We appreciate feedback from users, bug hunters, and fresh ideas from open minded people. Feel free and file [support requests, change requests, bug reports and feature requests on GitHub](https://github.com/jonelo/jacksum/issues)

Spread the word, or give a star here on GitHub. 

## Documentation

* Jacksum [Release Notes](https://github.com/jonelo/jacksum/blob/main/RELEASE-NOTES.txt) (2002-2021)
* Jacksum [Features](https://github.com/jonelo/jacksum/wiki/Features)
* Jacksum [Manpage](https://github.com/jonelo/jacksum/wiki/Manpage) (with many examples)
* Jacksum [Wiki](https://github.com/jonelo/jacksum/wiki)
* Jacksum [Source Code](https://github.com/jonelo/jacksum) (on GitHub, mavenized with a pom.xml and an IntelliJ .idea config)
  * Start your investigation journey by reading [net.jacksum.cli.Main](https://github.com/jonelo/jacksum/blob/main/src/main/java/net/jacksum/cli/Main.java) and [net.jacksum.JacksumAPI](https://github.com/jonelo/jacksum/blob/main/src/main/java/net/jacksum/JacksumAPI.java)
  * Download and read the [jacksum-3.1.0-javadoc.jar](https://github.com/jonelo/jacksum/releases/download/v3.1.0/jacksum-3.1.0-javadoc.jar)
* [https://jacksum.net](https://jacksum.net)

## Integration

* [Jacksum File Browser Integration for GNU/Linux](https://github.com/jonelo/jacksum-fbi-linux)
* [Jacksum File Browser Integration for macOS](https://github.com/jonelo/jacksum-fbi-macos)
* [Jacksum on Docker](https://hub.docker.com/r/jonelo/jacksum)

## Products that are powered by Jacksum

Create an [issue on github](https://github.com/jonelo/jacksum/issues) and let me know that you use Jacksum for your product if you want to be listed here.

- NumericalChameleon, see http://www.numericalchameleon.net

## History

Jacksum was published in July 2002 on https://sourceforge.net/projects/jacksum/.
In September 2021, starting with version 3.0.0, it moved its repo to GitHub.

## License

The license that the project is offered under is the [GPL-3.0+](https://github.com/jonelo/jacksum/blob/main/LICENSE).

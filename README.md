![GitHub issues](https://img.shields.io/github/issues-raw/jonelo/jacksum?color=blue)
![GitHub closed issues](https://img.shields.io/github/issues-closed-raw/jonelo/jacksum?color=blue)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/jonelo/jacksum?color=green)
![GitHub downloads latest](https://img.shields.io/github/downloads/jonelo/jacksum/v3.3.0/total?color=green)
![GitHub](https://img.shields.io/github/license/jonelo/jacksum?color=green)
![GitHub top language](https://img.shields.io/github/languages/top/jonelo/jacksum?color=green)
![GitHub downloads](https://img.shields.io/github/downloads/jonelo/jacksum/total?color=green)

* * *

# Jacksum

## What is it?

**Jacksum** (**JAva ChecKSUM**) is a free, open source, cross-platform, feature-rich, multi-threaded data integrity verification tool on the command line. It is also a lib, and it is all about checksums, CRCs, and message digests (aka hashes, data/file fingerprints, thumbprints). For a full feature list go to [Features](https://github.com/jonelo/jacksum/wiki/Features).

Jacksum is written entirely in **Java** ☕.

## Is this something for you?

If you are a command line user (advanced user, sysadmin, computer scientist, security engineer, forensics engineer, reverse engineer, ...) or a Java developer this .jar package is for you.

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

## How to download

The latest released .jar file can be found at https://github.com/jonelo/jacksum/releases/latest
The .zip file also contains simple scripts to call Jacksum on Windows, Linux, and macOS from the command line.


## How to clone/compile/package/install it (for developers)

Jacksum can be build by Maven. On the command line you can simply clone the source code by calling `git clone` and compile/package/install by calling `mvn install`. After installation, the .jar file can be found unter the target directory and in your $HOME/.m2/ directory structure. You should set JAVA_HOME properly so that the JDK tools such as javac and javadoc can be found. Example on Ubuntu 20.04.4 LTS:

```
$ export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
$ git clone https://github.com/jonelo/jacksum.git
$ cd jacksum
$ git tag -l
v3.0.0
v3.0.1
v3.1.0
v3.2.0
v3.3.0
$ git checkout tags/v3.3.0 -b three-three-zero
$ mvn install
```
Call `mvn -version` to check whether your maven would use at least Java 11. Alternatively use an IDE which supports both cloning from a GitHub repo and Maven.

## How to configure and install it (for users)

Download the .jar (or .zip) file as described above, open a terminal (on Microsoft Windows, it is known as the "Command Prompt") and start Jacksum by typing

```
java -jar jacksum-3.3.0.jar
```

I recommend to adjust the Windows batch file (jacksum.bat) resp. the bash script (jacksum) for GNU/Linux and Unix operating systems (e.g. macOS) and to put the script to a folder that is reachable by your PATH environment variable in order to launch jacksum easily just by typing

```
jacksum
```

## Features

<img width="128" height="128" align="left" src="https://raw.githubusercontent.com/jonelo/jacksum/main/docs/images/jacksum_logo_128x128.png" alt="Jacksum logo" style="vertical-align:top;margin:10px 10px" />

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


## Examples of how to use it

```
jacksum -h examples
```

See also the [EXAMPLES section of the manpage](https://github.com/jonelo/jacksum/wiki/Manpage#examples).


## Contribution

I appreciate feedback from users, bug hunters, and fresh ideas from open minded people. Feel free and file [support requests, change requests, bug reports and feature requests on GitHub](https://github.com/jonelo/jacksum/issues)

Spread the word, or give a star here on GitHub. 

## Documentation

* Jacksum [Release Notes](https://github.com/jonelo/jacksum/blob/main/RELEASE-NOTES.txt) (2002-2022)
* Jacksum [Features](https://github.com/jonelo/jacksum/wiki/Features)
* Jacksum [Manpage](https://github.com/jonelo/jacksum/wiki/Manpage) (with many examples)
* Jacksum [Wiki](https://github.com/jonelo/jacksum/wiki)
* Jacksum [Source Code](https://github.com/jonelo/jacksum) (on GitHub, mavenized with a pom.xml and an IntelliJ .idea config)
  * Start your investigation journey by reading [net.jacksum.cli.Main](https://github.com/jonelo/jacksum/blob/main/src/main/java/net/jacksum/cli/Main.java) and [net.jacksum.JacksumAPI](https://github.com/jonelo/jacksum/blob/main/src/main/java/net/jacksum/JacksumAPI.java)
  * Download and read the [jacksum-3.2.0-javadoc.jar](https://github.com/jonelo/jacksum/releases/download/v3.2.0/jacksum-3.2.0-javadoc.jar)
* [https://jacksum.net](https://jacksum.net)

## Integrations

* [Jacksum File Browser Integration for Microsoft Windows](https://github.com/jonelo/jacksum-fbi-windows)
* [Jacksum File Browser Integration for GNU/Linux](https://github.com/jonelo/jacksum-fbi-linux)
* [Jacksum File Browser Integration for macOS](https://github.com/jonelo/jacksum-fbi-macos)
* [Jacksum on Docker](https://hub.docker.com/r/jonelo/jacksum)
* [HashGarten](https://github.com/jonelo/HashGarten)
* NumericalChameleon, see http://www.numericalchameleon.net

## History

Jacksum version 1.0.0 was published in July 2002 on https://sourceforge.net/projects/jacksum/. Starting with Jacksum version 3.0.0 in September 2021, it moved its repo to GitHub.

## License

The license that the project is offered under is the [GPL-3.0+](https://github.com/jonelo/jacksum/blob/main/LICENSE).

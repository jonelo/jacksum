![GitHub issues](https://img.shields.io/github/issues-raw/jonelo/jacksum?color=blue)
![GitHub closed issues](https://img.shields.io/github/issues-closed-raw/jonelo/jacksum?color=blue)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/jonelo/jacksum?color=green)
![GitHub downloads latest](https://img.shields.io/github/downloads/jonelo/jacksum/v4.0.0/total?color=green)
![GitHub](https://img.shields.io/github/license/jonelo/jacksum?color=green)
![GitHub top language](https://img.shields.io/github/languages/top/jonelo/jacksum?color=green)
![GitHub downloads](https://img.shields.io/github/downloads/jonelo/jacksum/total?color=green)


# Jacksum

<img width="128" height="128" align="right" src="https://raw.githubusercontent.com/jonelo/jacksum/main/docs/images/jacksum_logo_128x128.png" alt="Jacksum logo" style="vertical-align:top;margin:10px 10px" />

**Jacksum** (**JAva ChecKSUM**) is a free, open source, cross-platform,
feature-rich, multi-threaded, command line utility that makes hash
functions available to you to solve particular tasks the smart way.

Jacksum is also a library. You can use it in your own projects. It is written entirely in
**Java** ☕.


## Use cases

Jacksum covers many types of use cases in which hash values make sense:

- Calculating hash values/fingerprints of almost any input
  (files, file trees, command line args, plain or encoded strings, console, standard input,
  NTFS ADS, pipes, sockets, doors, partitions, disks)
- Generating and persisting check lists and wanted lists
- Generating pseudo random numbers, and reproducible, unique, secure passwords
- Verifying data integrity: finding OK, failed, missing, or new files
- Performing a strict integrity verification, aka audit
- Finding files by their fingerprints (positive matching)
- Finding files that do not match certain fingerprints (negative matching)
- Finding all duplicates of a file by its hash value
- Finding the algorithm(s) that generated a certain hash value
- Gathering detailed hash algorithm information, including whether an algorithm is broken
- Investigating polynomials of CRCs and parameters of HMACs


## Key facts

- 586 algorithms (cryptographic hash functions, CRCs, and classic checksums)
- HMAC support for 490+ hash functions
- Customizable CRCs from 1 to 64 bit width
- 70 command line options to control Jacksum's behavior
- Cross-platform executability with identical behavior
- Multi-threading for parallel hash calculations and parallel data reads, in order to take
  advantage of multi-core processors and fast SSD storage
- Recursive traversal with depth control, policies to follow symbolic links on files and/or
  folders, and file system cycle detection
- 18 predefined standard styles for reading and writing check files, 15 of them for wanted lists
- 17 different encodings for representing hash values
- 6 predefined styles for representing file timestamps
- 170+ different character sets to be able to read and write hash files correctly
- Fully customizable output format
- 10000+ lines of documentation with descriptions, examples, and compatibility lists for all
  supported algorithms

For details, see the
[comprehensive list of features](docs/FEATURES.md).


## Audience

Jacksum is for users with security in mind, advanced users, sysadmins, students of informatics,
computer scientists, cybersecurity engineers, forensics engineers, penetration testers, white
hat hackers, reverse engineers, CRC researchers, etc. Jacksum is for professionals, but since
HashGarten bundles the Jacksum library, Jacksum is also available to users who don't know how to
open a terminal.


## System requirements

- GNU/Linux, Microsoft Windows, or macOS
- a JRE/JDK that is compatible with Java 21 LTS or later (OpenJDK 25 LTS or later is recommended)
- 2 MiB disk space

See [Install](https://github.com/jonelo/jacksum/wiki/Install) in the wiki for supported architectures and further details.


## Installation

Download the latest .jar (or .zip) file from
https://github.com/jonelo/jacksum/releases/latest, open a terminal, and start Jacksum by typing

```
java -jar jacksum-4.0.0.jar
```

The .zip file also contains simple scripts to call Jacksum on Windows, GNU/Linux, and macOS
just by typing `jacksum`. See [Install](https://github.com/jonelo/jacksum/wiki/Install)  for how to set that up, and
[Integrations](#integrations) for platform installers and the Docker image.


## Quick start

Calculate a hash value (the default style depends on the selected algorithm):

    $ jacksum -a sha3-256 ubuntu-22.04-desktop-amd64.iso
    c5e46426a3ca0ae848d297747ed3846452cc7b33d5b418af961dbd55de8dff43 ubuntu-22.04-desktop-amd64.iso

Calculate hash values of a file tree in the GNU/Linux style and store them in a check file:

    $ jacksum -a sha3-256 --style linux -O file.hashes .

Verify the file tree against that check file:

    $ jacksum -a sha3-256 --style linux -c file.hashes .

Verify a single file against a known hash value:

    $ jacksum -a sha256 -e c396e956a9f52c418397867d1ea5c0cf1a99a49dcf648b086d2fb762330cc88d ubuntu-22.04.1-desktop-amd64.iso
        MATCH  ubuntu-22.04.1-desktop-amd64.iso (c396e956a9f52c418397867d1ea5c0cf1a99a49dcf648b086d2fb762330cc88d)

    Jacksum: Expectation met.
    Jacksum: 1 of the successfully read files matches the expected hash value.

More examples: [Examples](docs/EXAMPLES.md), `jacksum -h examples`, and the
[Jacksum Hacks](docs/JACKSUM_HACKS.md).


## Algorithm support

Jacksum supports **586 algorithms**: cryptographic and non-cryptographic hash functions,
including CRCs and classic checksums. It supports **HMAC**, a mechanism for message
authentication using any iterated cryptographic hash function in combination with a secret
shared key. It also supports the **"Rocksoft (tm) Model CRC Algorithm"** to describe CRCs, so
additional 1.0399 * 10^267 customized CRCs can be used.

See [Algorithms](docs/ALGORITHMS.md) for the full list, and the
[Algorithm Selection Guide](docs/ALGORITHM_SELECTION_GUIDE.md) to decide which one fits your
use case.


## User interfaces

Jacksum provides a Command Line Interface (CLI) and an Application Programming Interface (API).
A Graphical User Interface (GUI) is provided by HashGarten, which is a subproject of the Jacksum
project. Also, there are File Browser Integrations (FBI) available.

![Architecture](https://github.com/jonelo/jacksum/assets/10409423/d0d0ce30-3698-4d7d-8d0e-e2c3d7f29bc3)

### Command Line Interface (CLI)

Command line users benefit from the power of the command line. You can call Jacksum in scripts,
cronjobs, AI agents, etc., in order to automate file integrity/verification tasks for example.

<img width="100%" src="https://raw.githubusercontent.com/jonelo/jacksum/main/docs/images/screenshot-jacksum_on_ubuntu-cli_examples.png" alt="Jacksum on Ubuntu, CLI examples" style="vertical-align:top;margin:10px 10px" />

See also the [manual of Jacksum](https://github.com/jonelo/jacksum/wiki/Manpage).

### Graphical User Interface (GUI)

If you prefer a GUI and you just would like to calculate and verify hashes with a graphical user
interface, I recommend downloading and using the Jacksum File Browser Integration (FBI)
installer, which comes with [HashGarten](https://github.com/jonelo/HashGarten), a GUI for
Jacksum. HashGarten also runs standalone and it supports Drag and Drop.

### File Browser Integration (FBI)

The File Browser Integration (FBI) installer can integrate both Jacksum and HashGarten into your
preferred file browser, such as Finder on macOS, Windows File Explorer on Microsoft Windows, and
Caja, Dolphin, elementary Files, GNOME Nautilus, Konqueror, Krusader, muCommander, Nemo,
ROX-Filer, SpaceFM, Thunar, Xfe, or zzzFM on GNU/Linux. See also [Integrations](#integrations).


## Documentation

* [https://jacksum.net](https://jacksum.net) - Homepage
* [Manpage](https://github.com/jonelo/jacksum/wiki/Manpage) (with many examples)
* [Features](docs/FEATURES.md)
* [Algorithms](docs/ALGORITHMS.md)
* [Algorithm Selection Guide](docs/ALGORITHM_SELECTION_GUIDE.md)
* [Examples](docs/EXAMPLES.md)
* [Jacksum Hacks](docs/JACKSUM_HACKS.md)
* [Wiki](https://github.com/jonelo/jacksum/wiki)
* [Release Notes](https://github.com/jonelo/jacksum/blob/main/RELEASE-NOTES.txt) - since 2002!
* [Developer Guide](https://github.com/jonelo/jacksum/wiki/Developer-Guide)
* [Source Code](https://github.com/jonelo/jacksum) (on GitHub, mavenized with a pom.xml and an IntelliJ .idea config)
* [References](https://github.com/jonelo/jacksum/wiki/References)


## Integrations

* [Jacksum for Windows](https://github.com/jonelo/jacksum-for-windows)
* [Jacksum for Linux](https://github.com/jonelo/jacksum-for-linux)
* [Jacksum for macOS](https://github.com/jonelo/jacksum-for-macos)
* [Jacksum on Docker](https://hub.docker.com/r/jonelo/jacksum)
* [HashGarten](https://github.com/jonelo/HashGarten) - a GUI for Jacksum
* NumericalChameleon, see http://www.numericalchameleon.net


## History

* [Release Notes](https://github.com/jonelo/jacksum/blob/main/RELEASE-NOTES.txt) - since 2002!
* The first release of Jacksum was published in July 2002 on https://sourceforge.net/projects/jacksum/
* I made a pause in Jacksum development between 2007 and 2020
* Jacksum is actively developed again
* In September 2021 I moved the repo to GitHub


## Contribution

I appreciate feedback from users, bug hunters, and fresh ideas from open minded people. Feel free
to file [support requests, change requests, bug reports and feature requests on GitHub](https://github.com/jonelo/jacksum/issues)

Spread the word, or give a star here on GitHub.


## Credits

Jacksum implements a lot of algorithms, but it doesn't reinvent the wheel if an algorithm is
already available in another mature crypto library. So Jacksum relies on
[Bouncy Castle](https://www.bouncycastle.org), [java-crc](https://github.com/snksoft/java-crc),
[GNU Crypto](https://www.gnu.org/software/gnu-crypto/) (abandoned), FlexiProvider (abandoned),
and Projet RNRT SAPHIR (abandoned). Libraries that have been abandoned are now supported by
Jacksum (the hash part only). The Jacksum manpage clearly points out the origin of each
algorithm. Any algorithm that is accepted by the Jacksum project will benefit from the framework
that applies to all algorithms. For more information on that subject please type
`jacksum --copyright` or go to the
[copyright page](https://jacksum.net/en/legal/copyright.html).


## License

The license that the project is offered under is the
[GPL-3.0+](https://github.com/jonelo/jacksum/blob/main/LICENSE).

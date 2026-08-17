**Table of Contents**
 - [At a Glance](#at_a_glance)
 - [Use Cases](#use_cases)
 - [Algorithms](#algorithms)
   - [Standard Algorithms](#standard_algorithms)
   - [Customized Algorithms](#customized_algorithms)
   - [Algorithm Selection](#algorithm_selection)
 - [Input](#input)
   - [Files, File Trees, and File Lists](#input_files)
   - [Strings, Sequences, and Streams](#input_sequences)
   - [Platform Specific Input](#input_platform)
   - [Character Sets, Unicode, and BOM](#input_charsets)
   - [Correctness of File Handling](#input_correctness)
 - [Output and Formats](#output)
   - [Predefined Styles](#output_styles)
   - [User Defined Formats](#output_formats)
   - [Encodings](#output_encodings)
   - [Timestamps](#output_timestamps)
   - [Paths and File Names](#output_paths)
   - [Where the Output Goes](#output_files)
   - [Character Sets, Unicode, and BOM](#output_charsets)
 - [Data Integrity Verification](#verification)
 - [Finding Objects](#finding)
 - [Performance](#performance)
 - [Platforms and Integration](#platforms)
   - [Operating Systems and Architectures](#platforms_os)
   - [Interaction with Other Tools](#platforms_tools)
   - [User Interfaces](#platforms_ui)
 - [Information, Investigation, and Learning](#information)
 - [Developer Support](#developer)
 - [Free Software, Mature and Stable](#free_software)

The features below describe **Jacksum 4.0.0**. For the authoritative description of every
option, see the [Manpage](https://github.com/jonelo/jacksum/wiki/Manpage) (`jacksum -h`).

<a name="at_a_glance"/>

# At a Glance

  - **586 algorithms**: cryptographic hash functions, hash trees, non-cryptographic hash
    functions, CRCs, and classic checksums
  - **HMAC** support for **492** of those algorithms, with optional truncation
  - **Customizable CRCs** from 1 to 64 bit width
  - **70 command line options** to control Jacksum's behavior
  - **Cross-platform executability with identical behavior**
  - **Multi-threading** for parallel hash calculations and for parallel data reads, in order to
    take advantage of multi-core processors and fast SSD storage
  - **Recursive traversal** with depth control, policies to follow symbolic links on files
    and/or folders, and file system cycle detection
  - **18 predefined styles** for reading and writing check files and wanted lists, plus
    user defined styles
  - **17 encodings** for representing hash values
  - **6 predefined formats** for representing file timestamps, plus freely definable formats
  - **170+ character sets** to read and write check files and wanted lists correctly
  - **Fully customizable output format** on demand
  - **10,000+ lines of manpage** with descriptions, examples, and compatibility lists for all
    supported algorithms

<a name="use_cases"/>

# Use Cases

Jacksum covers many types of use cases in which hash values make sense. Each of them is an
operating mode that Jacksum selects automatically, dependent on the options and parameters that
you set, see also the section OPERATING MODES of the
[Manpage](https://github.com/jonelo/jacksum/wiki/Manpage).

  - **Calculating hash values/fingerprints**
    - of almost any input: files, file trees, command line arguments, plain strings, encoded
      strings, console input, standard input, NTFS ADS, pipes, sockets, doors, partitions,
      and disks
    - generating and persisting check lists and wanted lists
    - generating large pseudo random numbers
    - generating reproducible, unique, secure passwords from a master password

  - **Verifying data integrity**
    - finding any OK, FAILED, MISSING, and NEW files
    - performing a strict integrity verification, aka an audit

  - **Finding objects**
    - finding strings that match a hash value
    - finding files by their fingerprints (positive matching)
    - finding files that do not match certain fingerprints (negative matching)
    - finding all duplicates of a file by its hash value
    - finding the algorithm(s) that generated a certain hash value

  - **Gathering information**
    - gathering detailed hash algorithm information, including whether an algorithm is broken
    - investigating CRC polynomials
    - investigating HMAC parameters

For worked examples of all of those modes, see
[Jacksum by Example](https://github.com/jonelo/jacksum/blob/main/docs/EXAMPLES.md),
`jacksum -h examples`, and the
[Cheat Sheet](https://github.com/jonelo/jacksum/wiki/Cheat-Sheet).

<a name="algorithms"/>

# Algorithms

<a name="standard_algorithms"/>

## Standard Algorithms

  - Jacksum supports **586 standard algorithms**: cryptographic hash functions, hash trees,
    non-cryptographic hash functions, CRCs, and classic checksums. That includes national and
    international standards, all candidates of the final round of the NIST SHA-3 competition,
    lightweight cryptography, eXtendable Output Functions (XOF), broken algorithms for
    educational and backwards compatibility purposes, and the checksums and CRCs that can be
    found in operating systems, software products, and protocols.
    For the complete list see [Algorithms](https://github.com/jonelo/jacksum/wiki/Algorithms).
  - Some algorithms have an **alternative, pure Java implementation** that can be selected with
    the option `-A`, while the default implementation is the one provided by the Java API,
    because it is usually optimized by the JVM vendor.
  - The pseudo algorithms `none` and `read` allow you to work with file metadata only: `none`
    does not even read the file content, `read` reads it, but neither of them computes a hash.

<a name="customized_algorithms"/>

## Customized Algorithms

  - **Concatenated algorithms**: algorithms can be concatenated with the plus character
    (e.g. `-a ascon-hash+sha256+crc32c`) in order to calculate many algorithms **in one pass**
    over the data. The result is returned either as one combined value or as separated values.
    This also allows you to use a combination of algorithms with a predefined standard style
    that was designed to support one algorithm only.
  - **HMAC**: the keyed-hash message authentication code is supported for 492 algorithms
    (e.g. `-a hmac:sha256`), the key is specified with `-k`, and can be read from a file, from
    the console, or hidden from the console like a password.
  - **Truncated HMAC**: the HMAC output can be truncated (e.g. `-a hmac:sha256:64`).
  - **Customized CRCs**: the "Rocksoft (tm) Model CRC Algorithm" (the 6 parameters `width`,
    `poly`, `init`, `refIn`, `refOut`, `xorOut`) is fully supported with a width from 1 to
    64 bits. An extended model with 7 or 8 parameters (`incLen`, `xorLen`) allows you to specify
    CRCs that incorporate the length of the input data.

<a name="algorithm_selection"/>

## Algorithm Selection

  - Select one, a few, many, or all algorithms for hash calculation, integrity verification, or
    information gathering.
  - Specify algorithms manually, or filter them by a message digest width (`-a all:256`) or by a
    substring of their name (`-a all:sha`). Filters can be combined with the plus character.
  - Let Jacksum find the algorithm for you if you know both the input and the hash value
    (`-a unknown:<bits>`).

<a name="input"/>

# Input

<a name="input_files"/>

## Files, File Trees, and File Lists

  - Processes directories **recursively**, and allows you to limit the depth (`-r`).
  - **Detects file system cycles** and avoids endless loops.
  - Allows you to control how **symbolic links** on files (`-f`) and/or on directories (`-d`)
    should be handled, on all operating systems.
  - **Wildcard support**, dependent on the shell being used.
  - Reads the files to be processed from a **file list** (`-L`), in either one-file-name-per-line
    format or as space separated values (`--file-list-format`). Such lists can be produced by
    `find`, `dir /b`, `dir -n`, or by Jacksum itself (`--style files-only`).

<a name="input_sequences"/>

## Strings, Sequences, and Streams

  - Hashes data given **directly on the command line** (`-q`). The sequence can be plain text
    (`txt:`), formatted text (`txtf:`), a binary, decimal, octal, or hexadecimal sequence
    (`bin:`, `dec:`, `oct:`, `hex:`), a Base32, Base32hex, Base64, Base64url, or z85 encoded
    string, the content of one file (`file:`, max. 128 MiB), or input typed on the console,
    either visible (`readline`) or hidden (`password`).
  - Hashes **strings stored in a text file**, one string per line (`--string-list`).
  - Reads from the **standard input stream** if the file name is a hyphen (`-`). The stream is
    named `<stdin>` in the output; `--legacy-stdin-name` restores the historical name `-`.

<a name="input_platform"/>

## Platform Specific Input

  - **Any operating system**: files, file trees, file lists, command line arguments, standard
    input, disks, and partitions.
  - **Unix-like operating systems** (including macOS, BSD, and GNU/Linux): block devices,
    character devices, named pipes (FIFOs), sockets, and sparse files. Use
    `--scan-all-unix-file-types` to include those file types during a recursive traversal.
  - **Solaris**: doors.
  - **Microsoft Windows**: partitions (`\\.\c:`), hidden partitions that are not mounted by
    default such as the recovery or the EFI partition
    (`\\?\Volume{...}\`), physical disks, CD-ROMs, DVDs, RAM disks, and
    **NTFS Alternate Data Streams** on files (`my-file.txt:secret:$DATA`). Use `--scan-ntfs-ads`
    to include NTFS ADS during a recursive traversal.

<a name="input_charsets"/>

## Character Sets, Unicode, and BOM

  - Full Unicode file name support for input files.
  - Allows you to specify the character set separately for check files
    (`--charset-check-file`), wanted lists (`--charset-wanted-list`), file lists
    (`--charset-file-list`), string lists (`--charset-string-list`), and the console
    (`--charset-console`). All charsets that the JDK provides are supported, including all
    common Unicode aware charsets such as UTF-8, UTF-16, UTF-16BE, UTF-16LE, UTF-32, UTF-32BE,
    UTF-32LE, and GB18030.
  - Ignores an optional **Byte-Order-Mark (BOM)** in the input if a BOM is allowed, but not
    required by the selected charset.

<a name="input_correctness"/>

## Correctness of File Handling

  - Handles special characters in file names correctly, e.g. if a file name on GNU/Linux ends
    with a space or if it contains backslashes or newline characters.
  - Handles the allowed maximum length of file names properly, e.g. 255 characters for a file
    name on Microsoft Windows NTFS file systems.
  - Handles the allowed maximum length of paths properly, e.g. 32,767 characters for the entire
    path on Microsoft Windows NTFS file systems.
  - It is **large file aware**: it can process file sizes up to 8 Exbibytes
    (= 8,000,000,000 Gibibytes), presupposed that your operating system respectively your file
    system is large file aware, too.

<a name="output"/>

# Output and Formats

<a name="output_styles"/>

## Predefined Styles

Jacksum can not only read and write its own output, it is also able to read output that was
produced by other tools, and to write output that other tools can read. 18 predefined styles are
available (option `--style`, aka `-C`, aka `--compat`):

| `<style>` | purpose |
| --- | --- |
| `bsd`, `bsd-r` | tagged and untagged BSD format, also produced by `md5sum --tag` etc. on GNU/Linux, and by `cksum` of GNU Core Utilities 9.0 and later |
| `gnu-linux` | the classic `md5sum`/`sha256sum` format |
| `openssl-dgst`, `openssl-dgst-r` | the format of `openssl dgst` |
| `sfv` | the Simple File Verification format |
| `fciv` | the format of Microsoft's "File Checksum Integrity Verifier" |
| `solaris-digest`, `solaris-digest-v` | the format of Solaris' `digest` command |
| `hdb` | the hash data base format that is used by ClamAV's `sigtool` |
| `full` | hash value, timestamp, file size, and file name |
| `without-hashes`, `without-sizes`, `without-timestamps` | full, minus one of the check values |
| `sizes-and-names`, `timestamps-and-names`, `files-only` | lightweight lists |
| `hexhashes-only` | hash values only, e.g. for wanted lists that came from a third party |

  - All styles except `hexhashes-only` can be used to write and to read **check files**
    (option `-c`), including the hash-free ones, which then check the existence, the size,
    and/or the timestamp of files.
  - All styles that carry a hash value can be used as **wanted lists** (option `-w`), which
    excludes `files-only`, `without-hashes`, `sizes-and-names`, and `timestamps-and-names`.
  - Every style works with **any algorithm** that Jacksum supports, as well as with concatenated
    algorithms. This allows you to print SHA3-256 hashes in the legacy SFV format, for example.
  - `jacksum --style <style> --info` prints all properties of a predefined style.
  - You can define **your own styles** in a small property file and use it both for writing and
    for parsing, see
    [File Format of Styles](https://github.com/jonelo/jacksum/wiki/File-Format-of-Styles).
    A custom style can also prepend a text of your choice to the header
    (`formatter.leadingHeader`).

<a name="output_formats"/>

## User Defined Formats

  - Use the comprehensive format option `-F`/`--format` to get exactly the output you need. More
    than 30 tokens are supported, among them `#ALGONAME`, `#HASH`, `#HASHES`, `#FILENAME`,
    `#FILESIZE`, `#TIMESTAMP`, `#SEQUENCE`, `#SEPARATOR`, and `#QUOTE`. Tokens can select a
    single algorithm out of a concatenation (`#HASH{2}`) and an explicit encoding
    (`#HASH{base64}`).
  - Create **ed2k links**, **magnet links**, and Solaris' **pkgmap** format.
  - A **header** with the metadata of the run can be printed (`--header`), and it can be
    suppressed for those styles that print one by default (`--no-header`). The comment character
    of the header follows `-I`.
  - The **separator** between the output fields is freely selectable (`-s`).
  - Bytes of a hash value can be **grouped** and separated for easier readability (`-g` and
    `-G`).

<a name="output_encodings"/>

## Encodings

17 encodings are available for representing hash values (option `-E`/`--encoding`):

`bin`, `dec`, `oct`, `hex` (lowercase), `hex-uppercase`, `base16`, `base32`, `base32-nopadding`,
`base32hex`, `base32hex-nopadding`, `base64`, `base64-nopadding`, `base64url`,
`base64url-nopadding`, `bubblebabble`, `z-base-32`, and `z85`.

The same encodings can be used to specify the input sequence of the option `-q`.

<a name="output_timestamps"/>

## Timestamps

  - 6 predefined timestamp formats are available (option `-t`/`--timestamp`): `default`,
    `default-utc`, `iso8601` (alias `iso`), `iso8601utc` (alias `iso-utc`), `unixtime`, and
    `unixtime-ms`.
  - In addition to those, any format that Java's `SimpleDateFormat` understands can be defined
    freely, including the tokens `#SEPARATOR` and `#QUOTE`.

<a name="output_paths"/>

## Paths and File Names

  - Paths can be omitted (`--no-path`), printed absolutely (`--path-absolute`), or relative to a
    different path (`--path-relative-to`) or to one of the parameters of the call
    (`--path-relative-to-entry`).
  - The path separator character can be replaced (`-P`), which is useful to produce output on
    Microsoft Windows that is readable on GNU/Linux and vice versa.
  - **GNU file name escaping** is supported, and it can be enabled or disabled per style
    (`--gnu-filename-escaping`).

<a name="output_files"/>

## Where the Output Goes

  - Regular output goes to standard output, or to a file (`-o`, resp. `-O` to overwrite an
    existing one). The output file is excluded from the calculation process automatically.
  - Error messages go to standard error, or to a file (`-u`, resp. `-U` to overwrite an existing
    one).
  - The name of the output file can contain tokens that are replaced at runtime
    (`--output-file-replace-tokens`), e.g. `-O /myisos/.#ALGONAME{uppercase}` writes a file
    called `.SHA-256`.
  - Jacksum returns an **exit code** that depends on the result of the calculation respectively
    the verification process, so that you can control the code flow in your own scripts.

<a name="output_charsets"/>

## Character Sets, Unicode, and BOM

  - Full Unicode file name support for output files.
  - Allows you to specify the character set separately for standard output
    (`--charset-stdout`), standard error (`--charset-stderr`), the output file
    (`--charset-output-file`), and the error file (`--charset-error-file`). `-8`/`--utf8` is a
    shortcut that sets UTF-8 for both stdout and stderr.
  - Adds an optional **Byte-Order-Mark (BOM)** to the output (`--bom`) if a BOM is allowed, but
    not required by the selected charset.

<a name="verification"/>

# Data Integrity Verification

  - Any algorithm that Jacksum supports can be used for integrity verification purposes,
    including concatenated algorithms and HMACs.
  - Verify against a **check file** (`-c`), against a **single line** of such a file
    (`--check-line`), or against a single **expected hash value** given on the command line
    (`-e`/`--expect`).
  - Detects **OK**, **FAILED**, **MISSING**, and **NEW** files.
  - **Strict check mode** aka an **audit** (`--check-strict`): the run is only successful if all
    files pass the verification and no files have been added or removed. The verification
    summary and the exit code follow that result.
  - Not only hash values, but also **file sizes** (`--filesize`) and **modification timestamps**
    (`-t`) can be part of a check file, so that metadata is verified as well.
  - Hash values, file sizes, and timestamps that are stored in a check file can selectively be
    **ignored** during a check (`--ignore-hashes`, `--ignore-sizes`, `--ignore-timestamps`).
  - The output can be **filtered** by result (`--list-filter ok,failed,missing,new`, and the
    shortcuts `all`, `none`, `good`, `bad`), which is also useful if you are interested in the
    summary only. If the filter selects neither `ok` nor `failed`, no hashing occurs at all.
  - Robust parsing: empty lines can be ignored (`--ignore-empty-lines`), and lines that start
    with a particular string are treated as comments (`-I`).

<a name="finding"/>

# Finding Objects

  - **Find all files that match a given hash value**, e.g. to find all duplicates of a file
    (`-e <hash>`).
  - **Find all files that match the hash values of a precalculated wanted list**
    (`-w <list>`), e.g. to find vulnerable, illegal, or malicious software on a computer,
    independent of the actual file names. Together with the `hdb` style, a ClamAV signature
    database can be used as the wanted list.
  - **Find all files that do not match** the hash values of a wanted list, by setting
    `--match-filter negative`. The filter also supports `match`, `nomatch`, `all`, `none`,
    and `positive`.
  - **Find strings that match a hash value** by combining `--string-list` with `-e`.
  - **Find the algorithm that was used** to calculate a given checksum, CRC, or hash, if both
    the input and the hash value are known (`-a unknown:<bits> -q <sequence> -e <hash>`).

<a name="performance"/>

# Performance

  - Jacksum supports **multi-threading** on both multi-processor and multi-core computer
    systems.
  - **Multiple algorithms**: Jacksum can calculate multiple hashes **simultaneously**
    (`--threads-hashing`), i.e. files are read only once and the calculation load is distributed
    over the available cores.
  - **Multiple files**: Jacksum can read multiple files **simultaneously**
    (`--threads-reading`), which pays off in particular on fast SATA SSDs and NVMe M.2 SSDs.
  - The user can **control the number of threads** for both.

<a name="platforms"/>

# Platforms and Integration

<a name="platforms_os"/>

## Operating Systems and Architectures

  - Microsoft Windows (e.g. Microsoft Windows 10 and 11)
  - GNU/Linux (e.g. Ubuntu)
  - Unix (e.g. BSD flavors, macOS, Solaris)
  - any other operating system or architecture with an OpenJDK compatible Java Runtime
    Environment (JRE) or Java Development Kit (JDK) that is compatible with **Java 21 LTS or
    later** (OpenJDK 25 LTS or later is recommended)
  - The supported hardware architectures depend on the OS and the JDK vendor. Usually those are
    x86 64 bit (x64), x86 32 bit (x86), ARM 64 bit (AArch64, resp. Apple silicon), ARM 32 bit
    (AArch32), and PPC 64 bit (ppc64).
  - **No recompilation required**: Jacksum is written entirely in Java, just execute the .jar
    file with your JRE or JDK.
  - **Identical behavior** on all supported platforms.
  - 2 MiB disk space.

<a name="platforms_tools"/>

## Interaction with Other Tools

  - Use the predefined styles to read and write popular third party formats (GNU/Linux, BSD,
    SFV, FCIV, openssl, Solaris, ClamAV, etc.), or define your own.
  - Jacksum can be used in **scripts**, **cronjobs**, **autostart environments**, and by
    **AI agents**, and it returns an exit code that reflects the result.
  - Works with the **SendTo feature** of many file browsers on all major operating systems.

<a name="platforms_ui"/>

## User Interfaces

  - **CLI**: the command line interface with 70 options.
  - **API**: Jacksum is also a library, see [Developer Support](#developer).
  - **GUI**: [HashGarten](https://github.com/jonelo/HashGarten), a subproject of Jacksum, is a
    graphical user interface that uses the Jacksum API. It also runs standalone and supports
    drag and drop.
  - **FBI**: the File Browser Integration installers integrate both Jacksum and HashGarten into
    your preferred file browser: Finder on macOS, File Explorer on Microsoft Windows, and Caja,
    Dolphin, elementary Files, GNOME Nautilus, Konqueror, Krusader, muCommander, Nemo,
    ROX-Filer, SpaceFM, Thunar, Xfe, or zzzFM on GNU/Linux. See
    [Jacksum for Windows](https://github.com/jonelo/jacksum-for-windows),
    [Jacksum for Linux](https://github.com/jonelo/jacksum-for-linux), and
    [Jacksum for macOS](https://github.com/jonelo/jacksum-for-macos).
  - **Docker**: [Jacksum on Docker](https://hub.docker.com/r/jonelo/jacksum).

<a name="information"/>

# Information, Investigation, and Learning

  - `--info` prints detailed information about the selected algorithm:
    - the hash length in bits, bytes, and nibbles
    - whether the algorithm is HMAC compatible, and, for an HMAC, its parameters: the underlying
      hash function, a specified truncation in both bits and bytes, the minimum recommended
      truncation, and whether the specified key follows the recommendation of the RFC
    - its **security status** (`broken:` — one of `yes`, `no`, `partly`, `depends`, or `n/a`),
      with an explanation of the verdict if `-V details` is set: which security property is
      affected, whether an attack is theoretical or practical, the year, and a reference
    - for CRCs: the **CRC parameters**, the Jacksum CRC definition code, and the polynomial in
      its mathematical, normal, reversed, and Koopman representation, for both the polynomial
      and its reciprocal
    - the **avalanche effect** (min, avg, and max), calculated over a sequence that you can
      specify with `-q`
    - the **relative speed rank** of the algorithm
    - whether an alternative implementation is available and would be used
  - `jacksum --info` without an algorithm prints the supported algorithms, the Java system
    properties, the number of available processors, and the **Java heap** situation, which is
    useful for troubleshooting.
  - `jacksum -a all -l` lists all algorithm IDs, `jacksum --hmacs` lists all algorithms that can
    form an HMAC.
  - The manpage documents **each algorithm** with a compatibility list that shows how to compute
    the same hash with other operating systems, tools, APIs, and programming languages.
  - The **help is searchable**: `jacksum -h <word>` finds all options, algorithms, and sections
    that match, and `--exact` restricts the search to an exact match, so that options such as
    `-` and `--` can be looked up at all.
  - The help is available in **English and German** (`jacksum -h de`).
  - Specify your preferred **level of verbosity** (`-V`): `info`, `warnings`, `errors`,
    `summary`, and `details` can be enabled and disabled individually, or by the shortcuts
    `all`, `default`, and `none`.

<a name="developer"/>

# Developer Support

  - The entire source code is open, hosted on GitHub, and accessible using git.
  - The project has been mavenized with a `pom.xml`, which makes it easy to work in your
    preferred IDE. An IntelliJ `.idea` config is part of the repo.
  - Jacksum provides a **Java API**, so you can incorporate Jacksum in your own projects. Among
    others, `JacksumAPI.getBrokenState()`, `JacksumAPI.getBrokenDescription()`, and
    `JacksumAPI.preloadBrokenStates()` allow your program to determine whether an algorithm is
    broken.
  - **Javadoc** is available.
  - Jacksum keeps compatibility with Java 21 LTS, but it takes all the advantages of later
    releases if available.
  - See the [Developer Guide](https://github.com/jonelo/jacksum/wiki/Developer-Guide).

<a name="free_software"/>

# Free Software, Mature and Stable

  - Jacksum is **Free Software**, released under the
    [GPL-3.0 or any later version](https://github.com/jonelo/jacksum/blob/main/LICENSE), it runs
    on entirely free platforms, and it is listed in the Free Software Foundation directory.
  - It is **OSI Certified Open Source software**.
  - It is **free of charge**, free of advertisement, free of expirations, and free of
    registration.
  - Jacksum is **mature and stable**: the first release was published in July 2002, and the
    project is actively developed, see the
    [Release Notes](https://github.com/jonelo/jacksum/blob/main/RELEASE-NOTES.txt).
  - [Testcases to test new releases and to find regressions](https://github.com/jonelo/jacksum-testcases)
    with the CLI are available.

See also

- [Algorithms](https://github.com/jonelo/jacksum/wiki/Algorithms)
- [Manpage](https://github.com/jonelo/jacksum/wiki/Manpage)
- [Cheat Sheet](https://github.com/jonelo/jacksum/wiki/Cheat-Sheet)
- [File Format of Styles](https://github.com/jonelo/jacksum/wiki/File-Format-of-Styles)
- [Jacksum Hacks](https://github.com/jonelo/jacksum/wiki/Jacksum-Hacks)

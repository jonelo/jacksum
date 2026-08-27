/*


  Jacksum 4.0.1 - a checksum/hash tool written in Java
  Copyright (c) 2001-2026 Dipl.-Inf. (FH) Johann N. Löfflmann,
  All Rights Reserved, <https://jacksum.net>.

  This program is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation, either version 3 of the License, or (at your option) any later
  version.

  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  details.

  You should have received a copy of the GNU General Public License along with
  this program. If not, see <https://www.gnu.org/licenses/>.

 */
package net.jacksum.compats.parsing;

import net.jacksum.JacksumAPI;
import net.jacksum.actions.io.verify.NotEvenOneEntryFoundException;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.compats.defs.CompatibilityProperties;
import net.jacksum.formats.Encoding;
import net.jacksum.formats.EncodingDecoding;
import net.jacksum.formats.FilenameFormatter;
import net.loefflmann.sugar.io.BOM;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.IntSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Parser {

    CompatibilityProperties props;
    private final ParserStatistics statistics;
    Pattern pattern;

    // the canonical name of the algorithm that has been selected, and the flag that keeps
    // the check of the algorithm name in the file a one time action, see checkAlgoname()
    private String expectedAlgoname;
    private boolean algonameChecked = false;

    // the number of characters that an encoded hash value of the selected algorithm consists of,
    // or 0 if the hash value should not be verified by its length, see determineHashLength()
    private int expectedHashLength = 0;

    public Parser(CompatibilityProperties props) throws InvalidParserParameterException {
        this.props = props;
        this.statistics = new ParserStatistics();

        // replacing the regex if a non-default algorithm (as defined in the parser file)
        // has been selected by the user on the command line
        int nibbles;
        if (props.getHashAlgorithmUserSelected()) {
            try {
                AbstractChecksum checksum = JacksumAPI.getChecksumInstance(props.getHashAlgorithm());
                nibbles = checksum.getSize() / 4;
            } catch (NoSuchAlgorithmException ex) {
                throw new InvalidParserParameterException(ex.getMessage());
            }
        } else {
            nibbles = props.getHashNibbles();
        }

        // the canonical name of the selected algorithm, in order to be able to compare it
        // with the algorithm name that is stored in a file of a tagged style, see also
        // checkAlgoname()
        try {
            expectedAlgoname = JacksumAPI.getChecksumInstance(props.getHashAlgorithm()).getName();
        } catch (NoSuchAlgorithmException | RuntimeException ex) {
            // e.g. an HMAC, which cannot be instantiated without a key; in that case
            // the algorithm name that is stored in a file is not checked at all
            expectedAlgoname = null;
        }

        if (props.isHashLengthCheckWanted()) {
            expectedHashLength = determineHashLength();
        }

        if (props.getRegexp() != null) {
            props.setRegexp(props.getRegexp().replace("#NIBBLES", Integer.toString(nibbles)));
            props.setRegexp(props.getRegexp().replace("#ALGONAME{uppercase}", props.getHashAlgorithm().toUpperCase(Locale.US)));
            props.setRegexp(props.getRegexp().replace("#ALGONAME{lowercase}", props.getHashAlgorithm().toLowerCase(Locale.US)));
            props.setRegexp(props.getRegexp().replace("#ALGONAME", props.getHashAlgorithm()));

            // parse the line using the regex
            try {
                pattern = Pattern.compile(props.getRegexp()); //, Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException pse) {
                throw new InvalidParserParameterException(pse.getMessage());
            }

            // a misconfigured parser must not end up in an exception while lines are
            // being parsed, so all group positions are checked before they are used
            int groups = pattern.matcher("").groupCount();
            checkGroupPosition("parser.regexp.hashPos", position("parser.regexp.hashPos", props::getRegexpHashPos), groups);
            checkGroupPosition("parser.regexp.filenamePos", position("parser.regexp.filenamePos", props::getRegexpFilenamePos), groups);
            checkGroupPosition("parser.regexp.algonamePos", position("parser.regexp.algonamePos", props::getRegexpAlgonamePos), groups);
            checkGroupPosition("parser.regexp.filesizePos", position("parser.regexp.filesizePos", props::getRegexpFilesizePos), groups);
            checkGroupPosition("parser.regexp.timestampPos", position("parser.regexp.timestampPos", props::getRegexpTimestampPos), groups);
            checkGroupPosition("parser.regexp.permissionsPos", position("parser.regexp.permissionsPos", props::getRegexpPermissionsPos), groups);
            checkGroupPosition("parser.regexp.gnuEscapingPos", position("parser.regexp.gnuEscapingPos", props::getRegexpGnuEscapingPos), groups);
        } else {
            throw new InvalidParserParameterException(String.format("Regular Expression expected in parser \"%s\"", props.getCompatName()));
        }
    }

    /**
     * Determines the number of characters that an encoded hash value of the selected algorithm
     * consists of. Two hash values of that algorithm's width are encoded for that purpose, one
     * that consists of 0x00 bytes only and one that consists of 0xFF bytes only. If both have the
     * same length, that length is the length of every encoded hash value of that algorithm; if
     * they don't (e.g. the encoding dec, where the length depends on the value), the length is
     * not deterministic and it must not be used to verify a hash value.
     *
     * @return the number of characters of an encoded hash value, or 0 if that number is not
     * deterministic resp. cannot be determined at all
     */
    private int determineHashLength() {
        try {
            Encoding encoding = Encoding.string2Encoding(props.getHashEncoding());
            int bytes = JacksumAPI.getChecksumInstance(props.getHashAlgorithm()).getByteArray().length;
            if (bytes < 1) {
                return 0;
            }
            byte[] allBitsSet = new byte[bytes];
            Arrays.fill(allBitsSet, (byte) 0xff);
            int length = EncodingDecoding.encodeBytes(new byte[bytes], encoding, 0, ' ').length();
            return length == EncodingDecoding.encodeBytes(allBitsSet, encoding, 0, ' ').length() ? length : 0;
        } catch (NoSuchAlgorithmException | RuntimeException ex) {
            // e.g. an HMAC, which cannot be instantiated without a key, or an encoding that the
            // style defines by a name that is unknown; in that case the length is not verified
            return 0;
        }
    }

    /**
     * Returns the value of a property that determines the position of a group
     * in the regular expression of the parser.
     *
     * @param property the name of the property
     * @param supplier the supplier that reads the value of the property
     * @return the value of the property
     * @throws InvalidParserParameterException if the value is not an integer
     */
    private int position(String property, IntSupplier supplier) throws InvalidParserParameterException {
        try {
            return supplier.getAsInt();
        } catch (NumberFormatException nfe) {
            throw new InvalidParserParameterException(String.format(
                    "The value of the property %s in parser \"%s\" must be an integer, but %s has been found.",
                    property, props.getCompatName(), nfe.getMessage().replace("For input string: ", "")));
        }
    }

    /**
     * Checks whether a group position is covered by the regular expression of
     * the parser.
     *
     * @param property the name of the property that determines the position
     * @param pos the position of the group
     * @param groups the number of groups in the regular expression
     * @throws InvalidParserParameterException if the position is out of range
     */
    private void checkGroupPosition(String property, int pos, int groups) throws InvalidParserParameterException {
        if (pos > groups) {
            throw new InvalidParserParameterException(String.format(
                    "The property %s in parser \"%s\" refers to the group %s, but the regular expression has %s group(s) only.",
                    property, props.getCompatName(), pos, groups));
        }
    }

    // controls how duplicate filenames should be handled
    // true: replace duplicate filenames (useful for --check-list)
    // false: don't replace duplicate "filenames", because filenames are comments (useful for --wanted-list)
    private boolean replaceDuplicateFilenames = true;


    public HashEntry parseOneLine(String line) {
        HashEntry hashEntry = null;
        int properlyFormattedLines = 0;
        int improperlyFormattedLines = 0;
        int ignoredLines = 0;
        try {
            hashEntry = parseLine(line);
            properlyFormattedLines++;
        } catch (IgnoredLineException ile) {
            // we want to silently ignore particular lines
            ignoredLines++;
        } catch (ImproperlyFormattedLineException ple) {
            improperlyFormattedLines++;
            System.err.printf("Jacksum: Warning: Improperly formatted line: %s%n", line);
        }
        // add to the statistics rather than replacing them, because a check line (--check-line)
        // can be specified in addition to a check file (-c), and the numbers of both must be
        // covered by the statistics and by the exit code, see also CheckAction
        getStatistics().setTotalLines(getStatistics().getTotalLines() + 1);
        getStatistics().setProperlyFormattedLines(getStatistics().getProperlyFormattedLines() + properlyFormattedLines);
        getStatistics().setImproperlyFormattedLines(getStatistics().getImproperlyFormattedLines() + improperlyFormattedLines);
        getStatistics().setIgnoredLines(getStatistics().getIgnoredLines() + ignoredLines);

        return hashEntry;
    }

    // fixes the file path if --path-relative-to <path> has been set
    // or if it is clear that we are on Windows and we read a Linux file
    private String fixPath(String line) {

        // concatenate the value of --path-relative-to and the path that has been parsed
        if (props.getPathRelativeTo() != null) {
            try {
                line = props.getPathRelativeTo().resolve(line).normalize().toString();
            } catch (InvalidPathException ipe) {
                // don't concatenate if it is a non-standard path, such as NTFS Data Streams
                // and in that case we also don't need to replace the separator char, we simply return
                return line;
            }
        }

        // Patch the path separator for line, if it is clear that it is from a foreign system.
        // Are we on Windows and do we read a Linux file?
        if (File.separatorChar == '\\' && line.contains("/")) {
            return line.replace('/', '\\');
        // Are we on Linux and do we read a Windows file?
        // Well, in this case we do NOTHING, because it is allowed to have a \ in the filename
        // real life example: /lib/systemd/system/system-systemd\x2dcryptsetup.slice
        // } else if (File.separatorChar == '/' && line.contains("\\")) {
        //    return line.replace('\\', '/');
        } else {
            return line;
        }
    }

    private HashEntry parseLine(String line) throws ImproperlyFormattedLineException, IgnoredLineException {
        if (props.isIgnoreEmptyLines() && line.trim().length() == 0) {
            throw new IgnoredLineException();
        }
        if (props.getIgnoreLinesStartingWithString() != null
                && line.startsWith(props.getIgnoreLinesStartingWithString())) {
            throw new IgnoredLineException();
        }

        HashEntry hashEntry = new HashEntry();

        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {

            if (props.getRegexpHashPos() > 0) {
                String hash = matcher.group(props.getRegexpHashPos());
                // a value that does not have the length of an encoded hash value is not a hash
                // value, so the line is not a line that stores a hash value at all
                if (expectedHashLength > 0 && hash.length() != expectedHashLength) {
                    throw new ImproperlyFormattedLineException();
                }
                hashEntry.setHash(hash);
            }

            if (props.getRegexpFilenamePos() > 0) {
                boolean gnuEscaping = props.getRegexpGnuEscapingPos() > 0 && matcher.group(props.getRegexpGnuEscapingPos()).equals("\\");
                String parsedFilename = matcher.group(props.getRegexpFilenamePos());
                String realFilename = gnuEscaping
                        ? FilenameFormatter.gnuUnescapeProblematicCharsInFilename(parsedFilename)
                        : parsedFilename;
                hashEntry.setFilename(fixPath(realFilename));
            }

            if (props.getRegexpFilesizePos() > 0) {
                try {
                    hashEntry.setFilesize(Long.parseLong(matcher.group(props.getRegexpFilesizePos())));
                } catch (NumberFormatException nfe) {
                    throw new ImproperlyFormattedLineException();
                }                
            }

            if (props.getRegexpTimestampPos() > 0) {
                hashEntry.setTimestamp(matcher.group(props.getRegexpTimestampPos()));
            }

            if (props.getRegexpPermissionsPos() > 0) {
                hashEntry.setPermissions(matcher.group(props.getRegexpPermissionsPos()));
            }

            if (!algonameChecked && props.getRegexpAlgonamePos() > 0) {
                algonameChecked = true;
                checkAlgoname(matcher.group(props.getRegexpAlgonamePos()));
            }

            return hashEntry;
        } else {
            throw new ImproperlyFormattedLineException();
        }

    }

    /**
     * Warns if the algorithm name that is stored in a file of a tagged style (e.g. bsd or
     * openssl-dgst) belongs to an algorithm other than the one that has been selected.
     *
     * A hash value of an algorithm with a different length is already rejected by the
     * regular expression of the parser, but an algorithm with the same length (e.g. sha256
     * vs. sha3-256) would only be reported as FAILED, as if the file had been altered.
     *
     * Only a name that Jacksum can resolve is taken into account, because the name in the
     * file is the name of the tool that has created it, and the very same algorithm can be
     * spelled differently by different tools, e.g. SHA256 (OpenSSL 1.1.1), SHA2-256
     * (OpenSSL 3.x), and sha256 (Solaris).
     *
     * @param algonameInFile the algorithm name that has been read from the file
     */
    private void checkAlgoname(String algonameInFile) {
        if (expectedAlgoname == null || algonameInFile == null) {
            return;
        }
        try {
            // algorithm names are lowercase in Jacksum, while the tools spell them in
            // various ways, e.g. SHA256, Skein512, and sha256
            String algonameResolved = JacksumAPI.getChecksumInstance(algonameInFile.toLowerCase(Locale.US)).getName();
            if (!algonameResolved.equals(expectedAlgoname)) {
                System.err.printf("Jacksum: Warning: The file has been created with the algorithm \"%s\", but the algorithm \"%s\" has been selected.%n",
                        algonameInFile, props.getHashAlgorithm());
            }
        } catch (NoSuchAlgorithmException | RuntimeException ex) {
            // Jacksum does not know that name, so it is a name that is specific to the tool
            // that has created the file, and we cannot tell whether it matches
        }
    }

    /**
     * Returns the key that is used to detect entries which refer to the same file, even if their
     * paths are spelled differently, e.g. "a.txt" and "./a.txt". It is the very same key that
     * MessageConsumerOnCheckedFiles uses to look the entries of the check file up, so that both
     * have the same understanding of what a duplicate is.
     *
     * @param filename the file name of an entry of the check file
     * @return the key for the duplicate detection
     */
    private String duplicateDetectionKey(String filename) {
        // the pseudo name for standard input (e.g. <stdin> or -) does not refer to a file on the
        // file system, so it must not be resolved against the current working directory
        if (filename == null || filename.equals(props.getStdinName())) {
            return filename;
        }
        try {
            return Paths.get(filename).toAbsolutePath().normalize().toString();
        } catch (InvalidPathException ipe) {
            return filename;
        }
    }

    /**
     * Determines whether two entries that refer to the same file store different properties, i.e.
     * whether the entry that is being replaced would produce a different verification result than
     * the entry that replaces it.
     *
     * @param one an entry of the check file
     * @param other another entry of the check file
     * @return true if the two entries don't store the same properties
     */
    private static boolean differs(HashEntry one, HashEntry other) {
        return !Objects.equals(one.getHash(), other.getHash())
                || one.getFilesize() != other.getFilesize()
                || !Objects.equals(one.getTimestamp(), other.getTimestamp());
    }

    /**
     * Counts a duplicate, and warns if the entry that is being replaced stores properties other
     * than the entry that replaces it, because such an entry must not be dropped silently.
     *
     * @param previous the entry that is being replaced
     * @param hashEntry the entry that replaces it
     * @param location where the entry that replaces it comes from
     */
    private void handleDuplicate(HashEntry previous, HashEntry hashEntry, String location) {
        getStatistics().setDuplicateEntriesCounted(true);
        getStatistics().setDuplicateEntries(getStatistics().getDuplicateEntries() + 1);
        if (differs(previous, hashEntry)) {
            System.err.printf("Jacksum: Warning: Duplicate entry in %s: \"%s\" and \"%s\" refer to the same file, but they don't store the same properties; the entry that has been read later is used.%n",
                    location, previous.getFilename(), hashEntry.getFilename());
        }
    }

    /**
     * Adds an entry to a list of entries that have been parsed already. If duplicate file names
     * are being replaced, see isReplaceDuplicateFilenames(), an entry that refers to the same file
     * is replaced rather than being added a second time, so that a file is verified exactly once.
     *
     * It is used for an entry that does not come from the check file itself, but from a check line
     * (--check-line), which can be specified in addition to a check file (-c).
     *
     * @param list the list of entries that have been parsed so far
     * @param hashEntry the entry that should be added
     */
    public void addEntry(List<HashEntry> list, HashEntry hashEntry) {
        if (replaceDuplicateFilenames) {
            String key = duplicateDetectionKey(hashEntry.getFilename());
            for (int i = 0; i < list.size(); i++) {
                HashEntry previous = list.get(i);
                if (Objects.equals(duplicateDetectionKey(previous.getFilename()), key)) {
                    // the entry that has been read later wins, and it keeps the position of the
                    // entry that it replaces, exactly as it is done in parseFile()
                    list.set(i, hashEntry);
                    handleDuplicate(previous, hashEntry, "the check line");
                    return;
                }
            }
        }
        list.add(hashEntry);
    }

    /**
     * Parses a file that contains entries with hashes that can be checked.
     *
     * @param filename the filename
     * @param charset the charset that should be used to read the file
     * @return a list of HashEntry objects.
     * @throws IOException if an I/O error occurs
     * @throws NotEvenOneEntryFoundException if only parse errors occur and not
     * even one entry is found
     */
    public List<HashEntry> parseFile(String filename, Charset charset) throws IOException, NotEvenOneEntryFoundException {
        List<HashEntry> list = new ArrayList<>();

        BufferedReader bufferedReader = null;
        FileReader fileReader = null;
        InputStreamReader inputStreamReader;

        boolean stdin = filename.equals("-");

        try {
            // don't use try-with-resources here, because we only want to close the BufferedReader (and FileReader),
            // but we don't want to close System.in
            if (stdin) {
                inputStreamReader = new InputStreamReader(System.in, charset);
                bufferedReader = new BufferedReader(inputStreamReader);
            } else {
                fileReader = new FileReader(filename, charset);
                bufferedReader = new BufferedReader(fileReader);
            }

            String line;
            int lineNumber = 0;
            int properlyFormattedLines = 0;
            int improperlyFormattedLines = 0;
            int ignoredLines = 0;
            Map<String, HashEntry> map = null;
            if (replaceDuplicateFilenames) {
                map = new LinkedHashMap<>();
            }
            while ((line = bufferedReader.readLine()) != null) {
                lineNumber++;
                try {

                    if (lineNumber == 1) {
                        line = BOM.cutBOM(line, charset);
                    }

                    HashEntry hashEntry = parseLine(line);
                    if (replaceDuplicateFilenames) {
                        // a LinkedHashMap keeps the position of an entry that is being replaced,
                        // so the order of the check file is preserved
                        HashEntry previous = map.put(duplicateDetectionKey(hashEntry.getFilename()), hashEntry);
                        if (previous != null) {
                            handleDuplicate(previous, hashEntry, String.format("line #%d in file \"%s\"", lineNumber, filename));
                        }
                    } else {
                        list.add(hashEntry);
                    }
                    properlyFormattedLines++;
                } catch (IgnoredLineException ile) {
                    // we want to silently ignore particular lines
                    ignoredLines++;
                } catch (ImproperlyFormattedLineException ple) {
                    improperlyFormattedLines++;
                    System.err.printf("Jacksum: Warning: Improperly formatted line in line #%d in file \"%s\": \"%s\"%n", lineNumber, filename, line);
                }
            }

            if (replaceDuplicateFilenames) {
                list.addAll(map.values());
                // the duplicates themselves have been counted by handleDuplicate() already
                getStatistics().setDuplicateEntriesCounted(true);
            }
            // add to the statistics rather than replacing them, see also parseOneLine()
            getStatistics().setTotalLines(getStatistics().getTotalLines() + lineNumber);
            getStatistics().setProperlyFormattedLines(getStatistics().getProperlyFormattedLines() + properlyFormattedLines);
            getStatistics().setImproperlyFormattedLines(getStatistics().getImproperlyFormattedLines() + improperlyFormattedLines);
            getStatistics().setIgnoredLines(getStatistics().getIgnoredLines() + ignoredLines);

            if (list.isEmpty()) {
                throw new NotEvenOneEntryFoundException(String.format("Jacksum: Error: not even one valid entry has been found in %s. Are you sure that you have specified the correct style?", filename));
            }

        } finally {
            if (!stdin) {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileReader != null) {
                    fileReader.close();
                }
            }

        }
        return list;
    }

    /**
     * @return the statistics
     */
    public ParserStatistics getStatistics() {
        return statistics;
    }

    public boolean isReplaceDuplicateFilenames() {
        return replaceDuplicateFilenames;
    }

    public void setReplaceDuplicateFilenames(boolean replaceDuplicateFilenames) {
        this.replaceDuplicateFilenames = replaceDuplicateFilenames;
    }
}

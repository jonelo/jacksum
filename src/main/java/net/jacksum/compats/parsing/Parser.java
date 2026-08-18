/*


  Jacksum 4.0.0 - a checksum/hash tool written in Java
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
import net.jacksum.formats.FilenameFormatter;
import net.loefflmann.sugar.io.BOM;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
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
        getStatistics().setTotalLines(1);
        getStatistics().setProperlyFormattedLines(properlyFormattedLines);
        getStatistics().setImproperlyFormattedLines(improperlyFormattedLines);
        getStatistics().setIgnoredLines(ignoredLines);

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
                hashEntry.setHash(matcher.group(props.getRegexpHashPos()));
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
                        if (map.containsKey(hashEntry.getFilename())) {
                            map.replace(hashEntry.getFilename(), hashEntry);
                        } else {
                            map.put(hashEntry.getFilename(), hashEntry);
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
            }
            getStatistics().setTotalLines(lineNumber);
            getStatistics().setProperlyFormattedLines(properlyFormattedLines);
            getStatistics().setImproperlyFormattedLines(improperlyFormattedLines);
            getStatistics().setIgnoredLines(ignoredLines);

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

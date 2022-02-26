/*


  Jacksum 3.2.0 - a checksum utility in Java
  Copyright (c) 2001-2022 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
import net.jacksum.actions.check.*;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.compats.defs.CompatibilityProperties;
import org.n16n.sugar.io.BOM;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Parser {

    CompatibilityProperties props;
    private final ParserStatistics statistics;
    Pattern pattern;

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
        } else {
            throw new InvalidParserParameterException(String.format("Regular Expression expected in parser \"%s\"", props.getCompatName()));
        }
    }

    public HashEntry parseOneLine(String line) {
        try {
            return parseLine(line);
        } catch (IgnoredLineException ile) {
            // we want to silently ignore particular lines
        } catch (ImproperlyFormattedLineException ple) {
            System.err.printf("Jacksum: Warning: Improperly formatted line: %s%n", line);
        }
        return null;
    }

    // fixes the file path if --path-relative-to <path> has been set
    // or if it is clear that we are on Windows and we read a Linux file
    private String fixPath(String line) {

        // concatenate the value of --path-relative-to and the path that has been parsed
        if (props.getPathRelativeTo() != null) {
            try {
                line = props.getPathRelativeTo().resolve(line).toString();
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
                hashEntry.setFilename(fixPath(matcher.group(props.getRegexpFilenamePos())));
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

            return hashEntry;
        } else {
            throw new ImproperlyFormattedLineException();
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

            boolean replaceDuplicateFilenames = true;
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
                throw new NotEvenOneEntryFoundException(String.format("Jacksum: Error: not even one valid entry has been found in %s", filename));
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

}

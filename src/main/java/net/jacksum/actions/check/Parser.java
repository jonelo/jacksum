/**
 *******************************************************************************
 *
 * Jacksum 3.0.0 - a checksum utility in Java
 * Copyright (c) 2001-2021 Dipl.-Inf. (FH) Johann N. Löfflmann,
 * All Rights Reserved, <https://jacksum.net>.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 *******************************************************************************
 */
package net.jacksum.actions.check;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.jacksum.JacksumAPI;
import net.jacksum.algorithms.AbstractChecksum;

public class Parser {

    CompatibilityProperties props;
    private ParserStatistics statistics;
    Pattern pattern;

    public Parser(CompatibilityProperties props) throws InvalidParserParameterException {
        this.props = props;
        this.statistics = new ParserStatistics();

        // replacing the regex if a non-default algorithm (as defined in the parser file) has been selected by the user on the command line
        int nibbles = 0;
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
            props.setRegexp(props.getRegexp().replace("#ALGONAME", props.getHashAlgorithm()));
            // parse the line using the regex
            try {
                pattern = Pattern.compile(props.getRegexp());
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
            System.err.println(String.format("Jacksum: Warning: Improperly formatted line: %s", line));
        }
        return null;
    }

    private String fixSeparator(String line) {
        // patch the path separator in the file if it is clear that it is from a foreign system
        // are we on Windows and do we read a Linux file?
        if (File.separatorChar == '\\' && line.contains("/")) {
            return line.replace('/', '\\');
            // are we on Linux and do we read a Windows file?
        } else if (File.separatorChar == '/' && line.contains("\\")) {
            return line.replace('\\', '/');
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
                hashEntry.setFilename(fixSeparator(matcher.group(props.getRegexpFilenamePos())));
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

    private String cutBOM(String line, Charset charset, byte[] BOM) {
        byte[] bytes = line.getBytes(charset);
        if (bytes != null && bytes.length >= BOM.length && Arrays.equals(Arrays.copyOf(bytes, BOM.length), BOM)) {
            return new String(Arrays.copyOfRange(bytes, BOM.length, bytes.length), charset);
        } else {
            return line;
        }
    }

    /**
     * Removes the Byte Order Mark (BOM) from the String if it is there.
     *
     * @param line a String that could contain a BOM.
     * @param charset the charset that determines the actual BOM.
     * @return a String without the BOM.
     */
    public String cutBOM(String line, Charset charset) {
        String lineWithoutBOM = line;
        if (charset.equals(Charset.forName("UTF-8"))) {
            lineWithoutBOM = cutBOM(line, charset, new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        } else if (charset.equals(Charset.forName("UTF-16BE"))) {
            lineWithoutBOM = cutBOM(line, charset, new byte[]{(byte) 0xFE, (byte) 0xFF});
        } else if (charset.equals(Charset.forName("UTF-16LE"))) {
            lineWithoutBOM = cutBOM(line, charset, new byte[]{(byte) 0xFF, (byte) 0xFE});
        } else if (charset.equals(Charset.forName("UTF-32BE"))) {
            lineWithoutBOM = cutBOM(line, charset, new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF});
        } else if (charset.equals(Charset.forName("UTF-32LE"))) {
            lineWithoutBOM = cutBOM(line, charset, new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00});
        } else if (charset.equals(Charset.forName("GB18030"))) {
            lineWithoutBOM = cutBOM(line, charset, new byte[]{(byte) 0x84, (byte) 0x31, (byte) 0x95, (byte) 0x33});
        }
        return lineWithoutBOM;
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

        boolean stdin = false;
        if (filename.equals("-")) {
            stdin = true;
        }

        try {
            // don't use try-with-resources here, because we only want to close the BufferedReader (and FileReader),
            // but we don't want to close System.in
            if (stdin) {
                inputStreamReader = new InputStreamReader(System.in, charset);
                bufferedReader = new BufferedReader(inputStreamReader);
            } else {
                fileReader = new FileReader(new File(filename), charset);
                bufferedReader = new BufferedReader(fileReader);
            }

            String line;
            int lineNumber = 0;
            int linesWithErrors = 0;
            while ((line = bufferedReader.readLine()) != null) {
                lineNumber++;
                try {

                    if (lineNumber == 1) {
                        line = cutBOM(line, charset);
                    }

                    HashEntry hashEntry = parseLine(line);
                    if (hashEntry != null) {
                        list.add(hashEntry);
                    }
                } catch (IgnoredLineException ile) {
                    // we want to silently ignore particular lines
                } catch (ImproperlyFormattedLineException ple) {
                    linesWithErrors++;
                    System.err.println(String.format("Jacksum: Warning: Improperly formatted line in line #%d in file \"%s\": \"%s\"", lineNumber, filename, line));
                }
            }

            getStatistics().setTotalLines(lineNumber);
            getStatistics().setImproperlyFormattedLines(linesWithErrors);

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

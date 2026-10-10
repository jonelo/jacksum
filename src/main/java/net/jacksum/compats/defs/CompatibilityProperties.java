/*

  Jacksum 4.0.2 - a checksum/hash tool written in Java
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
package net.jacksum.compats.defs;

import net.jacksum.JacksumAPI;
import net.jacksum.algorithms.AbstractChecksum;
import net.loefflmann.sugar.util.Support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;


/**
 * The properties of a compatibility file (a style) that define how lines of a hash
 * list are formatted and how they are parsed again, e.g. for the predefined styles
 * under {@code net/jacksum/compats/defs} or for a user-provided {@code .properties} file.
 */
public class CompatibilityProperties implements Serializable {

    private static final long serialVersionUID = 4442560626737130256L;

    /**
     * The properties as read from the compatibility file.
     */
    private final Properties props; // persistent Props
    private final static int CURRENT_COMPAT_SYNTAX_VERSION = 3;

    /**
     * Whether the verification should be strict (see {@link #isCheckStrict()}).
     */
    private boolean strictCheck = false;
    /**
     * Whether the user has selected a hash algorithm explicitly.
     */
    private boolean hashAlgorithmUserSelected = false;
    /**
     * Whether the user has selected a timestamp format explicitly.
     */
    private boolean timestampFormatUserSelected = false;

    private final static String COMPAT_SYNTAX_VERSION = "compat.syntaxVersion";
    private final static String COMPAT_NAME = "compat.name";
    private final static String COMPAT_AUTHORS = "compat.authors";
    private final static String COMPAT_VERSION = "compat.version";
    private final static String COMPAT_DESCRIPTION = "compat.description";
    //    private final static String PARSER_ENGINE_TYPE = "parser.engineType";
    private final static String LINES_IGNORE_LINES_STARTING_WITH_STRING = "parser.ignoreLinesStartingWithString";
    private final static String LINES_IGNORE_EMPTY_LINES = "parser.ignoreEmptyLines";
    private final static String LINES_REGEXP = "parser.regexp";
    private final static String REGEXP_HASH_POS = "parser.regexp.hashPos";
    private final static String REGEXP_ALGONAME_POS = "parser.regexp.algonamePos";
    private final static String REGEXP_FILENAME_POS = "parser.regexp.filenamePos";
    private final static String REGEXP_FILESIZE_POS = "parser.regexp.filesizePos";
    private final static String REGEXP_TIMESTAMP_POS = "parser.regexp.timestampPos";
    private final static String REGEXP_PERMISSIONS_POS = "parser.regexp.permissionsPos";
    private final static String REGEXP_GNU_ESCAPING_POS = "parser.regexp.gnuEscapingPos";
    private final static String HASH_NIBBLES = "parser.regexp.nibbles";
    private final static String HASH_LENGTH_CHECK = "parser.hashLengthCheck";
    private final static String FILESIZE_AS_BYTE_BLOCKS = "parser.filesizeAsByteBlocks";
    private final static String HASH_ALGORITHM = "algorithm.default";
    private final static String HASH_ALGORITHM_USER_SELECTABLE = "algorithm.userSelectable";
    private final static String TIMESTAMP_FORMAT = "timestampFormat.format";
    private final static String TIMESTAMP_FORMAT_USER_SELECTABLE = "timestampFormat.userSelectable";
    private final static String LINES_FORMAT = "formatter.format";
    private final static String HASH_ENCODING = "formatter.hash.encoding";
    private final static String STDIN_NAME = "formatter.stdinName";
    private final static String LINE_SEPARATOR = "formatter.lineSeparator";
    private final static String GNU_ESCAPING_USER_SELECTABLE = "formatter.gnuescaping.userSelectable";
    private final static String GNU_ESCAPING_ENABLED = "formatter.gnuescaping.enabled";
    private final static String ALGONAME_DEFAULT_REPLACEMENT = "formatter.ALGONAME.defaultReplacement";
    private final static String ALGONAME_EXCEPTION_MAPPINGS = "formatter.ALGONAME.exceptionMappings";
    private final static String HEADER = "formatter.header";
    private final static String LEADING_HEADER = "formatter.leadingHeader";


    /**
     * Creates an empty instance
     */
    public CompatibilityProperties() {
        props = new Properties();
        addRequiredPropertiesIfAbsent(AbstractChecksum.getStdinName());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<String> list = Support.sortPopertiesByKeys(props);
        for (String line : list) {
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Creates a ParserProperties object based on a properties file
     *
     * @param compatFilename the filename of the properties file.
     * @throws IOException if there is an I/O problem with the file.
     * @throws InvalidCompatibilityPropertiesException if the version of the compat file is incompatible with the expected version.
     */
    public CompatibilityProperties(String compatFilename) throws IOException, InvalidCompatibilityPropertiesException {
        this(compatFilename, AbstractChecksum.getStdinName());
    }

    /**
     * Reads compatibility properties.
     *
     * @param compatFilename the id of a predefined style, or the name of a file
     * @param defaultStdinName the name for the standard input stream that is used if the
     *                         style does not define one of its own
     * @throws IOException if the file exists, but it cannot be read
     * @throws InvalidCompatibilityPropertiesException if the value is neither a predefined style
     * nor an existing file, or if the file is not a valid style
     */
    public CompatibilityProperties(String compatFilename, String defaultStdinName) throws IOException, InvalidCompatibilityPropertiesException {
        String compatFilenameResolved = resolveAlias(compatFilename);
        if (isParserSupported(compatFilenameResolved)) {
            props = readFromJarFile(String.format("/net/jacksum/compats/defs/%s.properties", compatFilenameResolved));
        } else {
            // A value that is neither a predefined style nor an existing file is a mistake in the
            // parameters rather than an I/O problem, and a mistyped keyword must not be reported as
            // a file that is missing, because the user has not asked for a file at all.
            if (!new File(compatFilenameResolved).exists()) {
                throw new InvalidCompatibilityPropertiesException(String.format(
                        "\"%s\" is neither a predefined style nor a file that can be read.%nType \"jacksum -h --style\" to get a list of all predefined styles.",
                        compatFilename));
            }
            props = readFromLocalFile(compatFilenameResolved);
        }
        if (getCompatSyntaxVersion() != null) {
            // seems it is a valid property file
            int version;
            try {
                version = Integer.parseInt(getCompatSyntaxVersion().trim());
            } catch (NumberFormatException nfe) {
                throw new InvalidCompatibilityPropertiesException(String.format("The value of the property compat.syntaxVersion must be an integer, but \"%s\" has been found in \"%s\".", getCompatSyntaxVersion(), compatFilename));
            }
            // does it have the right version?
            if (version < CURRENT_COMPAT_SYNTAX_VERSION) {
                throw new InvalidCompatibilityPropertiesException(String.format("The value of the property compat.syntaxVersion must be %s or higher, but \"%s\" has been found in \"%s\".", CURRENT_COMPAT_SYNTAX_VERSION, getCompatSyntaxVersion(), compatFilename));
            }
        } else {
            // does not look like a valid property file
            throw new InvalidCompatibilityPropertiesException(String.format("The file \"%s\" does not look like a valid compatibility file. The property called compat.syntaxVersion is expected, but it hasn't been found in the file.", compatFilename));
        }
        addRequiredPropertiesIfAbsent(defaultStdinName);
    }

    private void addRequiredPropertiesIfAbsent(String defaultStdinName) {
        props.putIfAbsent(STDIN_NAME, defaultStdinName);
    }

    /**
     * Returns the underlying properties.
     *
     * @return the properties
     */
    public Properties getProperties() {
        return props;
    }

    /**
     * Returns the name of the compatibility file (property {@code compat.name}).
     *
     * @return the name, or null if not set
     */
    public String getCompatName() {
        return props.getProperty(COMPAT_NAME);
    }

    /**
     * Sets the name of the compatibility file (property {@code compat.name}).
     *
     * @param name the name
     */
    public void setCompatName(String name) {
        props.setProperty(COMPAT_NAME, name);
    }

    /**
     * Returns the syntax version of the compatibility file (property {@code compat.syntaxVersion}).
     *
     * @return the syntax version, or null if not set
     */
    public String getCompatSyntaxVersion() {
        return props.getProperty(COMPAT_SYNTAX_VERSION);
    }

    /**
     * Sets the syntax version of the compatibility file (property {@code compat.syntaxVersion}).
     *
     * @param version the syntax version
     */
    public void setCompatSyntaxVersion(String version) {
        props.setProperty(COMPAT_SYNTAX_VERSION, version);
    }

    /**
     * Returns the version of the compatibility file (property {@code compat.version}).
     *
     * @return the version, or null if not set
     */
    public String getCompatVersion() {
        return props.getProperty(COMPAT_VERSION);
    }

    /**
     * Sets the version of the compatibility file (property {@code compat.version}).
     *
     * @param version the version
     */
    public void setCompatVersion(String version) {
        props.setProperty(COMPAT_VERSION, version);
    }

    /**
     * Returns the description of the compatibility file (property {@code compat.description}).
     *
     * @return the description, or null if not set
     */
    public String getCompatDescription() {
        return props.getProperty(COMPAT_DESCRIPTION);
    }

    /**
     * Sets the description of the compatibility file (property {@code compat.description}).
     *
     * @param description the description
     */
    public void setCompatDescription(String description) {
        props.setProperty(COMPAT_DESCRIPTION, description);
    }

    /**
     * Returns the authors of the compatibility file (property {@code compat.authors}).
     *
     * @return the authors, or null if not set
     */
    public String getCompatAuthors() {
        return props.getProperty(COMPAT_AUTHORS);
    }

    /**
     * Sets the authors of the compatibility file (property {@code compat.authors}).
     *
     * @param authors the authors
     */
    public void setCompatAuthors(String authors) {
        props.setProperty(COMPAT_AUTHORS, authors);
    }


    /**
     * Returns the name that is used for the standard input stream (property
     * {@code formatter.stdinName}).
     *
     * @return the name for the standard input stream
     */
    public String getStdinName() {
        return props.getProperty(STDIN_NAME);
    }

    /**
     * Sets the name that is used for the standard input stream (property
     * {@code formatter.stdinName}).
     *
     * @param name the name for the standard input stream
     */
    public void setStdinName(String name) {
        props.setProperty(STDIN_NAME, name);
    }


//    public String getParserEngineType() {
//        return props.getProperty(PARSER_ENGINE_TYPE);
//    }

//    public void setParserEngineType(String engineType) {
//        props.setProperty(PARSER_ENGINE_TYPE, engineType);
//    }

    /**
     * Returns the prefix of lines that the parser should ignore (property
     * {@code parser.ignoreLinesStartingWithString}).
     *
     * @return the prefix, or null if not set
     */
    public String getIgnoreLinesStartingWithString() {
        return props.getProperty(LINES_IGNORE_LINES_STARTING_WITH_STRING);
    }

    /**
     * Sets the prefix of lines that the parser should ignore (property
     * {@code parser.ignoreLinesStartingWithString}).
     *
     * @param ignore the prefix
     */
    public void setIgnoreLinesStartingWithString(String ignore) {
        props.setProperty(LINES_IGNORE_LINES_STARTING_WITH_STRING, ignore);
    }

    /**
     * Determines whether the parser should ignore empty lines (property
     * {@code parser.ignoreEmptyLines}, false by default).
     *
     * @return true if empty lines should be ignored
     */
    public boolean isIgnoreEmptyLines() {
        return props.getProperty(LINES_IGNORE_EMPTY_LINES, "false").equals("true");
    }

    /**
     * Sets whether the parser should ignore empty lines (property {@code parser.ignoreEmptyLines}).
     *
     * @param ignore true if empty lines should be ignored
     */
    public void setIgnoreEmptyLines(boolean ignore) {
        props.setProperty(LINES_IGNORE_EMPTY_LINES, ignore ? "true" : "false");
    }

    /**
     * Determines whether the parser should verify that the hash value of a line has the length that
     * an encoded hash value of the selected algorithm has. It is required for a parser whose regular
     * expression does not constrain the hash value itself, which is the case for the parser that is
     * generated from the parameters, see DefaultCompatibilityProperties. The regular expression of a
     * predefined style constrains the hash value already, so the property is false by default.
     *
     * @return true if the length of the hash value should be verified
     */
    public boolean isHashLengthCheckWanted() {
        return props.getProperty(HASH_LENGTH_CHECK, "false").equals("true");
    }

    /**
     * Sets whether the parser should verify the length of the hash value of a line, see
     * {@link #isHashLengthCheckWanted()}.
     *
     * @param wanted whether the length of the hash value should be verified
     */
    public void setHashLengthCheckWanted(boolean wanted) {
        props.setProperty(HASH_LENGTH_CHECK, wanted ? "true" : "false");
    }

    /**
     * Returns the unit that the file size is stored in by the format that this parser reads: the
     * number of bytes that one block consists of, or -1 if the size is stored in bytes. Some
     * algorithms store the size as a number of blocks in their default format, e.g. sum_bsd,
     * sum_sysv, and sum_minix, so a verification has to compare the size in that very same unit,
     * see also SizeFormatter.lengthInUnitOfFormat().
     *
     * @return the number of bytes that one block consists of, or -1 for bytes
     */
    public long getFilesizeAsByteBlocks() {
        try {
            return Long.parseLong(props.getProperty(FILESIZE_AS_BYTE_BLOCKS, "-1"));
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    /**
     * Sets the unit that the file size is stored in, see {@link #getFilesizeAsByteBlocks()}.
     *
     * @param filesizeAsByteBlocks the number of bytes that one block consists of, or -1 for bytes
     */
    public void setFilesizeAsByteBlocks(long filesizeAsByteBlocks) {
        props.setProperty(FILESIZE_AS_BYTE_BLOCKS, Long.toString(filesizeAsByteBlocks));
    }

    /**
     * Sets whether GNU filename escaping can be selected by the user (property
     * {@code formatter.gnuescaping.userSelectable}).
     *
     * @param gnuEscaping true if GNU filename escaping is user selectable
     */
    public void setGnuEscapingUserSelectable(boolean gnuEscaping) {
        props.setProperty(GNU_ESCAPING_USER_SELECTABLE, gnuEscaping ? "true" : "false");
    }

    /**
     * Determines whether GNU filename escaping can be selected by the user (property
     * {@code formatter.gnuescaping.userSelectable}, false by default).
     *
     * @return true if GNU filename escaping is user selectable
     */
    public boolean isGnuEscapingUserSelectable() {
        return props.getProperty(GNU_ESCAPING_USER_SELECTABLE, "false").equals("true");
    }

    /**
     * Sets whether GNU filename escaping is enabled (property {@code formatter.gnuescaping.enabled}).
     *
     * @param gnuEscaping true if GNU filename escaping is enabled
     */
    public void setGnuEscapingEnabled(boolean gnuEscaping) {
        props.setProperty(GNU_ESCAPING_ENABLED, gnuEscaping ? "true" : "false");
    }

    /**
     * Determines whether GNU filename escaping is enabled (property
     * {@code formatter.gnuescaping.enabled}, false by default).
     *
     * @return true if GNU filename escaping is enabled
     */
    public boolean isGnuEscapingEnabled() {
        return props.getProperty(GNU_ESCAPING_ENABLED, "false").equals("true");
    }

    /**
     * Determines whether the style supports GNU filename escaping, i.e. whether the format
     * contains {@code #ESCAPETAG} and the regular expression has a position for it.
     *
     * @return true if GNU filename escaping is supported
     */
    public boolean isGnuEscapingSupported() {
        // formatter.format contains #ESCAPETAG and parser knows the position of the #ESCAPETAG
        return getFormat().contains("#ESCAPETAG") && getRegexpGnuEscapingPos() > 0;
    }

    /**
     * Determines whether the style supports filenames, i.e. whether the format contains
     * {@code #FILENAME} and the regular expression has a position for it.
     *
     * @return true if filenames are supported
     */
    public boolean isFilenameSupported() {
        // formatter.format contains #FILENAME and parser knows the position of the #FILENAME
        return getFormat().contains("#FILENAME") && getRegexpFilenamePos() > 0;
    }

    /**
     * Determines whether the style supports file sizes, i.e. whether the format contains
     * {@code #FILESIZE} and the regular expression has a position for it.
     *
     * @return true if file sizes are supported
     */
    public boolean isFilesizeSupported() {
        return getFormat().contains("#FILESIZE") && getRegexpFilesizePos() > 0;
    }

    /**
     * Determines whether the style supports timestamps, i.e. whether the format contains
     * {@code #TIMESTAMP} and the regular expression has a position for it.
     *
     * @return true if timestamps are supported
     */
    public boolean isTimestampSupported() {
        return getFormat().contains("#TIMESTAMP") && getRegexpTimestampPos() > 0;
    }


    /**
     * Returns the regular expression that the parser uses to parse a line (property
     * {@code parser.regexp}).
     *
     * @return the regular expression, or null if not set
     */
    public String getRegexp() {
        return props.getProperty(LINES_REGEXP);
    }

    /**
     * Sets the regular expression that the parser uses to parse a line (property
     * {@code parser.regexp}).
     *
     * @param regexp the regular expression
     */
    public void setRegexp(String regexp) {
        props.setProperty(LINES_REGEXP, regexp);
    }

    /**
     * Returns the position of the group in the regular expression that holds the hash value.
     *
     * @return the group position, or -1 if not set
     */
    public int getRegexpHashPos() {
        return Integer.parseInt(props.getProperty(REGEXP_HASH_POS, "-1"));
    }

    /**
     * Sets the position of the group in the regular expression that holds the hash value.
     *
     * @param pos the group position
     */
    public void setRegexHashPos(int pos) {
        props.setProperty(REGEXP_HASH_POS, String.valueOf(pos));
    }

    /**
     * Returns the position of the group in the regular expression that holds the filename.
     *
     * @return the group position, or -1 if not set
     */
    public int getRegexpFilenamePos() {
        return Integer.parseInt(props.getProperty(REGEXP_FILENAME_POS, "-1"));
    }

    /**
     * Sets the position of the group in the regular expression that holds the filename.
     *
     * @param pos the group position
     */
    public void setRegexpFilenamePos(int pos) {
        props.setProperty(REGEXP_FILENAME_POS, String.valueOf(pos));
    }

    /**
     * Returns the position of the group in the regular expression that holds the algorithm name.
     *
     * @return the group position, or -1 if not set
     */
    public int getRegexpAlgonamePos() {
        return Integer.parseInt(props.getProperty(REGEXP_ALGONAME_POS, "-1"));
    }

    /**
     * Sets the position of the group in the regular expression that holds the algorithm name.
     *
     * @param pos the group position
     */
    public void setRegexpAlgonamePos(int pos) {
        props.setProperty(REGEXP_ALGONAME_POS, String.valueOf(pos));
    }

    /**
     * Returns the position of the group in the regular expression that holds the file size.
     *
     * @return the group position, or -1 if not set
     */
    public int getRegexpFilesizePos() {
        return Integer.parseInt(props.getProperty(REGEXP_FILESIZE_POS, "-1"));
    }

    /**
     * Sets the position of the group in the regular expression that holds the file size.
     *
     * @param pos the group position
     */
    public void setRegexpFilesizePos(int pos) {
        props.setProperty(REGEXP_FILESIZE_POS, String.valueOf(pos));
    }

    /**
     * Returns the position of the group in the regular expression that holds the timestamp.
     *
     * @return the group position, or -1 if not set
     */
    public int getRegexpTimestampPos() {
        return Integer.parseInt(props.getProperty(REGEXP_TIMESTAMP_POS, "-1"));
    }

    /**
     * Sets the position of the group in the regular expression that holds the timestamp.
     *
     * @param pos the group position
     */
    public void setRegexpTimestampPos(int pos) {
        props.setProperty(REGEXP_TIMESTAMP_POS, String.valueOf(pos));
    }

    /**
     * Returns the position of the group in the regular expression that holds the file permissions.
     *
     * @return the group position, or -1 if not set
     */
    public int getRegexpPermissionsPos() {
        return Integer.parseInt(props.getProperty(REGEXP_PERMISSIONS_POS, "-1"));
    }

    /**
     * Sets the position of the group in the regular expression that holds the file permissions.
     *
     * @param pos the group position
     */
    public void setRegexpPermissionsPos(int pos) {
        props.setProperty(REGEXP_PERMISSIONS_POS, String.valueOf(pos));
    }

    /**
     * Returns the position of the group in the regular expression that holds the GNU escape tag.
     *
     * @return the group position, or -1 if not set
     */
    public int getRegexpGnuEscapingPos() {
        return Integer.parseInt(props.getProperty(REGEXP_GNU_ESCAPING_POS, "-1"));
    }

    /**
     * Sets the position of the group in the regular expression that holds the GNU escape tag.
     *
     * @param pos the group position
     */
    public void setRegexpGnuEscapingPos(int pos) {
        props.setProperty(REGEXP_GNU_ESCAPING_POS, String.valueOf(pos));
    }

    /**
     * Returns the line separator (property {@code formatter.lineSeparator}).
     *
     * @return the line separator, or the line separator of the system if not set
     */
    public String getLineSeparator() {
        return props.getProperty(LINE_SEPARATOR, System.lineSeparator());
    }

    /**
     * Sets the line separator (property {@code formatter.lineSeparator}).
     *
     * @param lineSeparator the line separator
     */
    public void setLineSeparator(String lineSeparator) {
        props.setProperty(LINE_SEPARATOR, lineSeparator);
    }

    /**
     * Returns the replacement for {@code #ALGONAME} that is used if no exception mapping
     * applies (property {@code formatter.ALGONAME.defaultReplacement}).
     *
     * @return the default replacement, or {@code #ALGONAME} if not set
     */
    public String getAlgonameDefaultReplacement() {
        return props.getProperty(ALGONAME_DEFAULT_REPLACEMENT, "#ALGONAME");
    }

    /**
     * Sets the replacement for {@code #ALGONAME} that is used if no exception mapping
     * applies (property {@code formatter.ALGONAME.defaultReplacement}).
     *
     * @param algonameDefaultReplacement the default replacement
     */
    public void setAlgonameDefaultReplacement(String algonameDefaultReplacement) {
        props.setProperty(ALGONAME_DEFAULT_REPLACEMENT, algonameDefaultReplacement);
    }

    /**
     * Returns the exception mappings for {@code #ALGONAME} (property
     * {@code formatter.ALGONAME.exceptionMappings}), a semicolon separated list of
     * {@code algorithmID=replacement} pairs.
     *
     * @return the exception mappings, or an empty string if not set
     */
    public String getAlgonameExceptionMappings() {
        return props.getProperty(ALGONAME_EXCEPTION_MAPPINGS, "");
    }

    /**
     * Sets the exception mappings for {@code #ALGONAME} (property
     * {@code formatter.ALGONAME.exceptionMappings}), a semicolon separated list of
     * {@code algorithmID=replacement} pairs.
     *
     * @param algonameExceptionMappings the exception mappings
     */
    public void setAlgonameExceptionMappings(String algonameExceptionMappings) {
        props.setProperty(ALGONAME_EXCEPTION_MAPPINGS, algonameExceptionMappings);
    }


    // hash algorithm

    /**
     * Returns the default hash algorithm of the style (property {@code algorithm.default}).
     *
     * @return the hash algorithm, or null if not set
     */
    public String getHashAlgorithm() {
        return props.getProperty(HASH_ALGORITHM);
    }

    /**
     * Sets the default hash algorithm of the style (property {@code algorithm.default}).
     *
     * @param hashAlgorithm the hash algorithm
     */
    public void setHashAlgorithm(String hashAlgorithm) {
        props.setProperty(HASH_ALGORITHM, hashAlgorithm);
    }


    /**
     * Determines whether the hash algorithm can be selected by the user (property
     * {@code algorithm.userSelectable}, false by default).
     *
     * @return true if the hash algorithm is user selectable
     */
    public boolean getHashAlgorithmUserSelectable() {
        return props.getProperty(HASH_ALGORITHM_USER_SELECTABLE, "false").equals("true");
    }

    /**
     * Sets whether the hash algorithm can be selected by the user (property
     * {@code algorithm.userSelectable}).
     *
     * @param selectable true if the hash algorithm is user selectable
     */
    public void setHashAlgorithmUserSelectable(boolean selectable) {
        props.setProperty(HASH_ALGORITHM_USER_SELECTABLE, selectable ? "true" : "false");
    }

    /**
     * Determines whether the user has selected a hash algorithm explicitly.
     *
     * @return the hashAlgorithmUserSelected
     */
    public boolean getHashAlgorithmUserSelected() {
        return hashAlgorithmUserSelected;
    }

    /**
     * Sets whether the user has selected a hash algorithm explicitly.
     *
     * @param hashAlgorithmUserSelected the hashAlgorithmUserSelected to set
     */
    public void setHashAlgorithmUserSelected(boolean hashAlgorithmUserSelected) {
        this.hashAlgorithmUserSelected = hashAlgorithmUserSelected;
    }

    // timestamp

    /**
     * Returns the timestamp format (property {@code timestampFormat.format}).
     *
     * @return the timestamp format, or null if not set
     */
    public String getTimestampFormat() {
        return props.getProperty(TIMESTAMP_FORMAT);
    }

    /**
     * Sets the timestamp format (property {@code timestampFormat.format}).
     *
     * @param timestampFormat the timestamp format
     */
    public void setTimestampFormat(String timestampFormat) {
        props.setProperty(TIMESTAMP_FORMAT, timestampFormat);
    }


    /**
     * Determines whether the timestamp format can be selected by the user (property
     * {@code timestampFormat.userSelectable}, false by default).
     *
     * @return true if the timestamp format is user selectable
     */
    public boolean isTimestampFormatUserSelectable() {
        return props.getProperty(TIMESTAMP_FORMAT_USER_SELECTABLE, "false").equals("true");
    }

    /**
     * Sets whether the timestamp format can be selected by the user (property
     * {@code timestampFormat.userSelectable}).
     *
     * @param selectable true if the timestamp format is user selectable
     */
    public void setTimestampFormatUserSelectable(boolean selectable) {
        props.setProperty(TIMESTAMP_FORMAT_USER_SELECTABLE, selectable ? "true": "false");
    }


    /**
     * Determines whether the user has selected a timestamp format explicitly.
     *
     * @return true if the user has selected a timestamp format
     */
    public boolean getTimestampFormatUserSelected() { return timestampFormatUserSelected; }

    /**
     * Sets whether the user has selected a timestamp format explicitly.
     *
     * @param timestampFormatUserSelected true if the user has selected a timestamp format
     */
    public void setTimestampFormatUserSelected(boolean timestampFormatUserSelected) {
        this.timestampFormatUserSelected = timestampFormatUserSelected;
    }


    /**
     * Returns the number of nibbles (hex digits) of the hash value (property
     * {@code parser.regexp.nibbles}).
     *
     * @return the number of nibbles, or -1 if not set
     */
    public int getHashNibbles() {
        return Integer.parseInt(props.getProperty(HASH_NIBBLES, "-1"));
    }

    /**
     * Sets the number of nibbles (hex digits) of the hash value (property
     * {@code parser.regexp.nibbles}).
     *
     * @param nibbles the number of nibbles
     */
    public void setHashNibbles(int nibbles) {
        props.setProperty(HASH_NIBBLES, String.valueOf(nibbles));
    }


    /**
     * Returns the encoding of the hash value (property {@code formatter.hash.encoding}).
     *
     * @return the encoding, or {@code hex} if not set
     */
    public String getHashEncoding() {
        return props.getProperty(HASH_ENCODING, "hex");
    }

    /**
     * Sets the encoding of the hash value (property {@code formatter.hash.encoding}).
     *
     * @param hashEncoding the encoding
     */
    public void setHashEncoding(String hashEncoding) {
        props.setProperty(HASH_ENCODING, hashEncoding);
    }


    /**
     * Determines whether a header should be printed (property {@code formatter.header},
     * false by default).
     *
     * @return true if a header is wanted
     */
    public boolean isHeaderWanted() {
        return props.getProperty(HEADER, "false").equals("true");
    }

    /**
     * Sets whether a header should be printed (property {@code formatter.header}).
     *
     * @param header true if a header is wanted
     */
    public void setHeaderWanted(boolean header) { props.setProperty(HEADER, header ? "true": "false"); }


    /**
     * Sets the leading header (property {@code formatter.leadingHeader}).
     *
     * @param leadingHeader the leading header
     */
    public void setLeadingHeader(String leadingHeader) {
         props.setProperty(LEADING_HEADER, leadingHeader);
    }

    /**
     * Returns the leading header (property {@code formatter.leadingHeader}).
     *
     * @return the leading header, or null if not set
     */
    public String getLeadingHeader() {
        return props.getProperty(LEADING_HEADER, null);
    }

    /**
     * The primary ID of the algorithm, used to look up the exception mappings for
     * {@code #ALGONAME}; initialized lazily.
     */
    private String primaryAlgoID = null;
    private void initPrimaryAlgoIDfrom(String algoID) {
        if (primaryAlgoID == null) {
            try {
                AbstractChecksum checksum = JacksumAPI.getChecksumInstance(algoID);
                primaryAlgoID = checksum.getName();
            } catch (NoSuchAlgorithmException ex) {
                primaryAlgoID = algoID;
            }
        }
    }

    /**
     * Returns the leading header with {@code #ALGONAMES} replaced by the algorithm IDs,
     * separated by commas.
     *
     * @param algoID the algorithm ID, with several IDs separated by {@code +}
     * @return the formatted leading header, or null if not set
     */
    public String getLeadingHeaderFormatted(String algoID) {
        if (props.getProperty(LEADING_HEADER) != null && props.getProperty(LEADING_HEADER).contains("#ALGONAMES")) {
            String header = props.getProperty(LEADING_HEADER);
            return header.replace("#ALGONAMES", algoID.replace("+",","));
        }
        return props.getProperty(LEADING_HEADER);
    }

    /**
     * Returns the line format with {@code #ALGONAME} replaced, either by an exception mapping
     * for the primary ID of the algorithm or by the default replacement.
     *
     * @param algoID the algorithm ID
     * @return the line format
     */
    public String getFormat(String algoID) {

        if (props.getProperty(LINES_FORMAT).contains("#ALGONAME")) {
            // what is the primary id?
            if (primaryAlgoID == null) {
                initPrimaryAlgoIDfrom(algoID);
            }

            String format = props.getProperty(LINES_FORMAT);

            // special algorithm names?
            if (!getAlgonameExceptionMappings().equals("")) {
                String[] tokens = getAlgonameExceptionMappings().split(";");
                for (String token : tokens) {
                    String[] tuple = token.split("=");
                    if (tuple[0].equals(primaryAlgoID)) {
                        format = format.replace("#ALGONAME", tuple[1]);
                    }
                }
            }

            // fallback if #ALGONAME is not already replaced
            return format.replace("#ALGONAME", getAlgonameDefaultReplacement());
        }

        return props.getProperty(LINES_FORMAT);
    }

    /**
     * Sets the line format (property {@code formatter.format}).
     *
     * @param format the line format
     */
    public void setFormat(String format) {
        props.setProperty(LINES_FORMAT, format);
    }

    /**
     * Returns the line format (property {@code formatter.format}) without any replacements.
     *
     * @return the line format, or an empty string if not set
     */
    public String getFormat() {
        return props.getProperty(LINES_FORMAT, "");
    }

    /**
     * Reads properties from a file in the file system.
     *
     * @param filename the name of the file
     * @return the properties
     * @throws IOException if the file cannot be read
     */
    public static Properties readFromLocalFile(String filename) throws IOException {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(filename)) {
            props.load(is);
        }
        return props;
    }

    /**
     * Reads properties from a resource in the jar file.
     *
     * @param filename the name of the resource
     * @return the properties
     * @throws IOException if the resource cannot be found or read
     */
    public static Properties readFromJarFile(String filename) throws IOException {
        Properties props = new Properties();
        try (InputStream is = CompatibilityProperties.class.getResourceAsStream(filename)) {
            if (is == null) {
                throw new IOException(String.format("%s not found.", filename));
            }
            props.load(is);
        }
        return props;
    }

    /**
     * Determines whether the verification should be strict.
     *
     * @return the strictCheck
     */
    public boolean isCheckStrict() {
        return strictCheck;
    }

    /**
     * Sets whether the verification should be strict.
     *
     * @param strictCheck the strictCheck to set
     */
    public void setCheckStrict(boolean strictCheck) {
        this.strictCheck = strictCheck;
    }


    private boolean isParserSupported(String parser) {
        switch (parser) {
            case "bsd":
            case "bsd-r":
            case "sfv":
            case "gnu-linux":
            case "fciv":
            case "openssl-dgst":
            case "openssl111-dgst":
            case "openssl-dgst-r":
            case "solaris-digest":
            case "solaris-digest-v":
            case "files-only":
            case "hdb":
            case "hexhashes-only":
            case "sizes-and-names":
            case "timestamps-and-names":
            case "full":
            case "without-hashes":
            case "without-timestamps":
            case "without-sizes":
            //case "hashdeep":
                return true;
            default:
                return false;
        }
    }

    private String resolveAlias(String parser) {
        switch (parser) {
            case "bsd-tagged":
            case "linux-tagged":
            case "gnu-linux-tagged":
                return "bsd";

            case "bsd-untagged":
            case "bsd-reversed":
                return "bsd-r";

            case "linux":
            case "linux-untagged":
            case "gnu-linux-untagged":
                return "gnu-linux";

            case "openssl":
            case "openssl-tagged":
                return "openssl-dgst";

            case "openssl111":
            case "openssl111-tagged":
                return "openssl111-dgst";

            case "openssl-untagged":
            case "openssl-r":
                return "openssl-dgst-r";

            case "solaris-tagged":
                return "solaris-digest-v";

            case "solaris-untagged":
                return "solaris-digest";

            case "filesonly":
            case "names-only":
                return "files-only";

            case "hexhashesonly":
                return "hexhashes-only";

            default:
                return parser;
        }
    }

    /**
     * The path that filenames should be relative to, or null.
     */
    private Path pathRelativeTo = null;
    /**
     * Sets the path that filenames should be relative to.
     *
     * @param pathRelativeTo the path, or null
     */
    public void setPathRelativeTo(Path pathRelativeTo) {
        this.pathRelativeTo = pathRelativeTo;
    }

    /**
     * Returns the path that filenames should be relative to.
     *
     * @return the path, or null if not set
     */
    public Path getPathRelativeTo() {
        return pathRelativeTo;
    }


}

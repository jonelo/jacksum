/*

  Jacksum 3.3.0 - a checksum utility in Java
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
package net.jacksum.compats.defs;

import net.jacksum.JacksumAPI;
import net.jacksum.algorithms.AbstractChecksum;
import net.loefflmann.sugar.util.Support;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;


public class CompatibilityProperties implements Serializable {

    private static final long serialVersionUID = 7076123016945520132L;
    private final Properties props; // persistent Props
    private final static int CURRENT_COMPAT_SYNTAX_VERSION = 3;

    private boolean strictCheck = false;
    private boolean hashAlgorithmUserSelected = false;

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
    private final static String HASH_ALGORITHM = "algorithm.default";
    private final static String HASH_ALGORITHM_USER_SELECTABLE = "algorithm.userSelectable";
    private final static String LINES_FORMAT = "formatter.format";
    private final static String HASH_ENCODING = "formatter.hash.encoding";
    private final static String STDIN_NAME = "formatter.stdinName";
    private final static String LINE_SEPARATOR = "formatter.lineSeparator";
    private final static String GNU_ESCAPING_USER_SELECTABLE = "formatter.gnuescaping.userSelectable";
    private final static String GNU_ESCAPING_ENABLED = "formatter.gnuescaping.enabled";
    private final static String ALGONAME_DEFAULT_REPLACEMENT = "formatter.ALGONAME.defaultReplacement";
    private final static String ALGONAME_EXCEPTION_MAPPINGS = "formatter.ALGONAME.exceptionMappings";


    /**
     * Creates an empty instance
     */
    public CompatibilityProperties() {
        props = new Properties();
        addRequiredPropertiesIfAbsent();
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
        String compatFilenameResolved = resolveAlias(compatFilename);
        if (isParserSupported(compatFilenameResolved)) {
            props = readFromJarFile(String.format("/net/jacksum/compats/defs/%s.properties", compatFilenameResolved));
        } else {
            props = readFromLocalFile(compatFilenameResolved);
        }
        if (getCompatSyntaxVersion() != null) {
            // seems it is a valid property file
            int version;
            try {
                version = Integer.parseInt(getCompatSyntaxVersion().trim());
            } catch (NumberFormatException nfe) {
                throw new InvalidCompatibilityPropertiesException(String.format("value of property compat.syntaxVersion must be an integer, but I found the value \"%s\" in \"%s\"", getCompatSyntaxVersion(), compatFilename));
            }
            // does it have the right version?
            if (version < CURRENT_COMPAT_SYNTAX_VERSION) {
                throw new InvalidCompatibilityPropertiesException(String.format("value of property compat.syntaxVersion must be %s or higher, but I found \"%s\" in \"%s\"", CURRENT_COMPAT_SYNTAX_VERSION, getCompatSyntaxVersion(), compatFilename));
            }
        } else {
            // does not look like a valid property file
            throw new InvalidCompatibilityPropertiesException(String.format("The file \"%s\" does not look like a valid compatibility file. The property called compat.syntaxVersion is expected, but it hasn't been found in the file.", compatFilename));
        }
        addRequiredPropertiesIfAbsent();
    }

    private void addRequiredPropertiesIfAbsent() {
        props.putIfAbsent(STDIN_NAME, AbstractChecksum.getStdinName());
    }

    public Properties getProperties() {
        return props;
    }

    public String getCompatName() {
        return props.getProperty(COMPAT_NAME);
    }

    public void setCompatName(String name) {
        props.setProperty(COMPAT_NAME, name);
    }

    public String getCompatSyntaxVersion() {
        return props.getProperty(COMPAT_SYNTAX_VERSION);
    }

    public void setCompatSyntaxVersion(String version) {
        props.setProperty(COMPAT_SYNTAX_VERSION, version);
    }

    public String getCompatVersion() {
        return props.getProperty(COMPAT_VERSION);
    }

    public void setCompatVersion(String version) {
        props.setProperty(COMPAT_VERSION, version);
    }

    public String getCompatDescription() {
        return props.getProperty(COMPAT_DESCRIPTION);
    }

    public void setCompatDescription(String description) {
        props.setProperty(COMPAT_DESCRIPTION, description);
    }

    public String getCompatAuthors() {
        return props.getProperty(COMPAT_AUTHORS);
    }

    public void setCompatAuthors(String authors) {
        props.setProperty(COMPAT_AUTHORS, authors);
    }


    public String getStdinName() {
        return props.getProperty(STDIN_NAME);
    }

    public void setStdinName(String name) {
        props.setProperty(STDIN_NAME, name);
    }


//    public String getParserEngineType() {
//        return props.getProperty(PARSER_ENGINE_TYPE);
//    }

//    public void setParserEngineType(String engineType) {
//        props.setProperty(PARSER_ENGINE_TYPE, engineType);
//    }

    public String getIgnoreLinesStartingWithString() {
        return props.getProperty(LINES_IGNORE_LINES_STARTING_WITH_STRING);
    }

    public void setIgnoreLinesStartingWithString(String ignore) {
        props.setProperty(LINES_IGNORE_LINES_STARTING_WITH_STRING, ignore);
    }

    public boolean isIgnoreEmptyLines() {
        return props.getProperty(LINES_IGNORE_EMPTY_LINES, "false").equals("true");
    }

    public void setIgnoreEmptyLines(boolean ignore) {
        props.setProperty(LINES_IGNORE_EMPTY_LINES, ignore ? "true" : "false");
    }

    public void setGnuEscapingUserSelectable(boolean gnuEscaping) {
        props.setProperty(GNU_ESCAPING_USER_SELECTABLE, gnuEscaping ? "true" : "false");
    }

    public boolean isGnuEscapingUserSelectable() {
        return props.getProperty(GNU_ESCAPING_USER_SELECTABLE, "false").equals("true");
    }

    public void setGnuEscapingEnabled(boolean gnuEscaping) {
        props.setProperty(GNU_ESCAPING_ENABLED, gnuEscaping ? "true" : "false");
    }

    public boolean isGnuEscapingEnabled() {
        return props.getProperty(GNU_ESCAPING_ENABLED, "false").equals("true");
    }

    public boolean isGnuEscapingSupported() {
        // formatter.format contains #ESCAPETAG and parser knows the position of the #ESCAPETAG
        return getFormat().contains("#ESCAPETAG") && getRegexpGnuEscapingPos() > 0;
    }

    public boolean isFilesizeSupported() {
        return getFormat().contains("#FILESIZE") && getRegexpFilesizePos() > 0;
    }

    public String getRegexp() {
        return props.getProperty(LINES_REGEXP);
    }

    public void setRegexp(String regexp) {
        props.setProperty(LINES_REGEXP, regexp);
    }

    public int getRegexpHashPos() {
        return Integer.parseInt(props.getProperty(REGEXP_HASH_POS, "-1"));
    }

    public void setRegexHashPos(int pos) {
        props.setProperty(REGEXP_HASH_POS, String.valueOf(pos));
    }

    public int getRegexpFilenamePos() {
        return Integer.parseInt(props.getProperty(REGEXP_FILENAME_POS, "-1"));
    }

    public void setRegexpFilenamePos(int pos) {
        props.setProperty(REGEXP_FILENAME_POS, String.valueOf(pos));
    }

    public int getRegexpAlgonamePos() {
        return Integer.parseInt(props.getProperty(REGEXP_ALGONAME_POS, "-1"));
    }

    public void setRegexpAlgonamePos(int pos) {
        props.setProperty(REGEXP_ALGONAME_POS, String.valueOf(pos));
    }

    public int getRegexpFilesizePos() {
        return Integer.parseInt(props.getProperty(REGEXP_FILESIZE_POS, "-1"));
    }

    public void setRegexpFilesizePos(int pos) {
        props.setProperty(REGEXP_FILESIZE_POS, String.valueOf(pos));
    }

    public int getRegexpTimestampPos() {
        return Integer.parseInt(props.getProperty(REGEXP_TIMESTAMP_POS, "-1"));
    }

    public void setRegexpTimestampPos(int pos) {
        props.setProperty(REGEXP_TIMESTAMP_POS, String.valueOf(pos));
    }

    public int getRegexpPermissionsPos() {
        return Integer.parseInt(props.getProperty(REGEXP_PERMISSIONS_POS, "-1"));
    }

    public void setRegexpPermissionsPos(int pos) {
        props.setProperty(REGEXP_PERMISSIONS_POS, String.valueOf(pos));
    }

    public int getRegexpGnuEscapingPos() {
        return Integer.parseInt(props.getProperty(REGEXP_GNU_ESCAPING_POS, "-1"));
    }

    public void setRegexpGnuEscapingPos(int pos) {
        props.setProperty(REGEXP_GNU_ESCAPING_POS, String.valueOf(pos));
    }

    public String getLineSeparator() {
        return props.getProperty(LINE_SEPARATOR, System.lineSeparator());
    }

    public void setLineSeparator(String lineSeparator) {
        props.setProperty(LINE_SEPARATOR, lineSeparator);
    }


    public String getAlgonameDefaultReplacement() {
        return props.getProperty(ALGONAME_DEFAULT_REPLACEMENT, "#ALGONAME");
    }

    public void setAlgonameDefaultReplacement(String algonameDefaultReplacement) {
        props.setProperty(ALGONAME_DEFAULT_REPLACEMENT, algonameDefaultReplacement);
    }

    public String getAlgonameExceptionMappings() {
        return props.getProperty(ALGONAME_EXCEPTION_MAPPINGS, "");
    }

    public void setAlgonameExceptionMappings(String algonameExceptionMappings) {
        props.setProperty(ALGONAME_EXCEPTION_MAPPINGS, algonameExceptionMappings);
    }


    public String getHashAlgorithm() {
        return props.getProperty(HASH_ALGORITHM);
    }

    public boolean getHashAlgorithmUserSelectable() {
        return props.getProperty(HASH_ALGORITHM_USER_SELECTABLE, "false").equals("true");
    }

    public void setHashAlgorithmUserSelectable(boolean selectable) {
        props.setProperty(HASH_ALGORITHM_USER_SELECTABLE, selectable ? "true" : "false");
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        props.setProperty(HASH_ALGORITHM, hashAlgorithm);
    }

    public String getHashEncoding() {
        return props.getProperty(HASH_ENCODING);
    }

    public int getHashNibbles() {
        return Integer.parseInt(props.getProperty(HASH_NIBBLES, "-1"));
    }

    public void setHashNibbles(int nibbles) {
        props.setProperty(HASH_NIBBLES, String.valueOf(nibbles));
    }


    public void setHashEncoding(String hashEncoding) {
        props.setProperty(HASH_ENCODING, hashEncoding);
    }

    public String getFormat(String algoid) {

        if (props.getProperty(LINES_FORMAT).contains("#ALGONAME")) {
            // what is the primary id?
            String primaryID;
            try {
                AbstractChecksum checksum = JacksumAPI.getChecksumInstance(algoid);
                primaryID = checksum.getName();
            } catch (NoSuchAlgorithmException ex) {
                primaryID = algoid;
            }

            String format = props.getProperty(LINES_FORMAT);

            // special algorithm names?
            if (!getAlgonameExceptionMappings().equals("")) {
                String[] tokens = getAlgonameExceptionMappings().split(";");
                for (String token : tokens) {
                    String[] tuple = token.split("=");
                    if (tuple[0].equals(primaryID)) {
                        format = format.replace("#ALGONAME", tuple[1]);
                    }
                }
            }
            return format.replace("#ALGONAME", getAlgonameDefaultReplacement());
        }

        return props.getProperty(LINES_FORMAT);
    }

    public void setFormat(String format) {
        props.setProperty(LINES_FORMAT, format);
    }

    public String getFormat() {
        return props.getProperty(LINES_FORMAT, "");
    }

    public static Properties readFromLocalFile(String filename) throws IOException {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(filename)) {
            props.load(is);
        }
        return props;
    }

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
     * @return the strictCheck
     */
    public boolean isCheckStrict() {
        return strictCheck;
    }

    /**
     * @param strictCheck the strictCheck to set
     */
    public void setCheckStrict(boolean strictCheck) {
        this.strictCheck = strictCheck;
    }

    /**
     * @return the hashAlgorithmUserSelected
     */
    public boolean getHashAlgorithmUserSelected() {
        return hashAlgorithmUserSelected;
    }

    /**
     * @param hashAlgorithmUserSelected the hashAlgorithmUserSelected to set
     */
    public void setHashAlgorithmUserSelected(boolean hashAlgorithmUserSelected) {
        this.hashAlgorithmUserSelected = hashAlgorithmUserSelected;
    }

    private boolean isParserSupported(String parser) {
        switch (parser) {
            case "bsd":
            case "bsd-r":
            case "sfv":
            case "gnu-linux":
            case "fciv":
            case "openssl-dgst":
            case "openssl-dgst-r":
            case "solaris-digest":
            case "solaris-digest-v":
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

            case "openssl-untagged":
            case "openssl-r":
                return "openssl-dgst-r";

            case "solaris-tagged":
                return "solaris-digest-v";

            case "solaris-untagged":
                return "solaris-digest";

            default:
                return parser;
        }
    }

    private Path pathRelativeTo = null;
    public void setPathRelativeTo(Path pathRelativeTo) {
        this.pathRelativeTo = pathRelativeTo;
    }

    public Path getPathRelativeTo() {
        return pathRelativeTo;
    }


}

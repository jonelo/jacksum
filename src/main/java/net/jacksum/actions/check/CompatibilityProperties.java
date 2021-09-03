/*

  Jacksum 3.0.0 - a checksum utility in Java
  Copyright (c) 2001-2021 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
package net.jacksum.actions.check;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import net.jacksum.algorithms.AbstractChecksum;
import org.n16n.sugar.util.Support;


public class CompatibilityProperties {
    private final Properties props; // persistent Props
    
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
    private final static String REGEXP_FILENAME_POS = "parser.regexp.filenamePos";
    private final static String REGEXP_FILESIZE_POS = "parser.regexp.filesizePos";
    private final static String REGEXP_TIMESTAMP_POS = "parser.regexp.timestampPos";
    private final static String REGEXP_PERMISSIONS_POS = "parser.regexp.permissionsPos";
    private final static String HASH_NIBBLES = "parser.regexp.nibbles";

    private final static String HASH_ALGORITHM = "algorithm.default";
    private final static String HASH_ALGORITHM_USER_SELECTABLE = "algorithm.userSelectable";
    private final static String LINES_FORMAT = "formatter.format";
    private final static String HASH_ENCODING = "formatter.hash.encoding";
    private final static String STDIN_NAME = "formatter.stdinName";


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
     * @param propertiesFile the filename of the properties file.
     * @throws java.io.IOException if there is an I/O problem with the file.
     */
    public CompatibilityProperties(String propertiesFile) throws IOException {
        if (isParserSupported(propertiesFile)) {
            props = readFromJarFile(String.format("/net/jacksum/actions/check/compatdefs/%s.properties", propertiesFile));
        } else {
            props = readFromLocalFile(propertiesFile);
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
    
    public String getFormat() {
        return props.getProperty(LINES_FORMAT);
    }

    public void setFormat(String format) {
        props.setProperty(LINES_FORMAT, format);
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
            case "sfv":
            case "linux":
            case "fciv":
            case "openssl":
                return true;
            default:
                return false;
        }
    }


}

/*


  Jacksum 3.8.0 - a checksum utility in Java
  Copyright (c) 2001-2023 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
package net.jacksum.cli;

import net.jacksum.actions.info.help.Help;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;
import net.jacksum.multicore.ThreadControl;
import net.jacksum.parameters.ParameterException;
import net.jacksum.parameters.Parameters;
import net.loefflmann.sugar.util.GeneralString;

import java.util.Locale;

/**
 * All parameters for the Command Line Interface (CLI)
 *
 * @author Johann N. Loefflmann
 */
public class CLIParameters {

    String[] args;

    public static final String __ALGORITHM = "--algorithm";
    public static final String _ALGORITHM = "-a";
    public static final String _ALTERNATIVE = "-A";
    public static final String __ALTERNATIVE = "--alternative";
    public static final String __PATH_ABSOLUTE = "--path-absolute";
    public static final String __ABSOLUTE = "--absolute";
    public static final String __PATH_RELATIVE_TO = "--path-relative-to";
    public static final String __RELATIVE_TO = "--relative-to";
    public static final String __PATH_RELATIVE_TO_ENTRY = "--path-relative-to-entry";
    public static final String __RELATIVE_TO_ENTRY = "--relative-to-entry";
    public static final String _UTF8 = "-8";
    public static final String __UTF8 = "--utf8";
    public static final String __COPYRIGHT = "--copyright";
    public static final String __LICENSE = "--license";
    public static final String __HEADER = "--header";
    public static final String __NO_HEADER = "--no-header";
    public static final String _CHECK_FILE = "-c";
    public static final String __CHECK_FILE = "--check-file";
    public static final String __CHECK_LINE = "--check-line";
    public static final String __CHECK_STRICT = "--check-strict";
    public static final String _COMPAT = "-C";
    public static final String __COMPAT = "--compat";
    public static final String __STYLE = "--style";
    public static final String __BOM = "--bom";
    public static final String _DONT_FOLLOW_SYMLINKS_TO_DIRECTORIES = "-d";
    public static final String __DONT_FOLLOW_SYMLINKS_TO_DIRECTORIES = "--dont-follow-symlinks-to-directories";
    public static final String _EXPECT_HASH = "-e";
    public static final String __EXPECT_HASH = "--expect-hash";
    public static final String __EXPECT = "--expect";
    public static final String _ENCODING = "-E";
    public static final String __ENCODING = "--encoding";
    public static final String _DONT_FOLLOW_SYMLINKS_TO_FILES = "-f";
    public static final String __DONT_FOLLOW_SYMLINKS_TO_FILES = "--dont-follow-symlinks-to-files";
    public static final String __FILESIZE = "--filesize";
    public static final String __GNU_FILENAME_ESCAPING = "--gnu-filename-escaping";
    public static final String _FORMAT = "-F";
    public static final String __FORMAT = "--format";
    public static final String _GROUP_BYTES = "-g";
    public static final String __GROUP_BYTES = "--group-bytes";
    public static final String _GROUP_BYTES_SEPARATOR = "-G";
    public static final String __GROUP_BYTES_SEPARATOR = "--group-bytes-separator";
    public static final String _HELP = "-h";
    public static final String __HELP = "--help";
    public static final String __HMACS = "--hmacs";
    public static final String __INFO = "--info";
    public static final String _IGNORE_LINES_STARTING_WITH_STRING = "-I";
    public static final String __IGNORE_LINES_STARTING_WITH_STRING = "--ignore-lines-starting-with-string";
    public static final String __IGNORE_EMPTY_LINES = "--ignore-empty-lines";
    public static final String __IGNORE_HASHES = "--ignore-hashes";
    public static final String __IGNORE_SIZES = "--ignore-sizes";
    public static final String __IGNORE_TIMESTAMPS = "--ignore-timestamps";
    public static final String _KEY = "-k";
    public static final String __KEY = "--key";
    public static final String _LIST = "-l";
    public static final String __LIST = "--list";
    public static final String __LIST_FILTER = "--list-filter";
    public static final String __WANTED_LIST_FILTER = "--wanted-list-filter";
    public static final String __MATCH_FILTER = "--match-filter";
    public static final String __LEGACY_STDIN_NAME = "--legacy-stdin-name";
    public static final String _FILE_LIST = "-L";
    public static final String __FILE_LIST = "--file-list";
    public static final String __FILE_LIST_FORMAT = "--file-list-format";
    public static final String __NO_PATH = "--no-path";
    public static final String _OUTPUT_FILE = "-o";
    public static final String __OUTPUT_FILE = "--output-file";
    public static final String _OUTPUT_FILE_OVERWRITE = "-O";
    public static final String __OUTPUT_FILE_OVERWRITE = "--output-file-overwrite";
    public static final String __OUTPUT_FILE_REPLACE_TOKENS = "--output-file-replace-tokens";
    public static final String _PATH_SEPARATOR = "-P";
    public static final String __PATH_SEPARATOR = "--path-separator";
    public static final String _QUICK = "-q";
    public static final String __QUICK = "--quick";
    public static final String _RECURSIVE = "-r";
    public static final String __RECURSIVE = "--recursive";
    public static final String __SCAN_ALL_UNIX_FILE_TYPES = "--scan-all-unix-file-types";
    public static final String __SCAN_NTFS_ADS = "--scan-ntfs-ads";
    public static final String _SEPARATOR = "-s";
    public static final String __SEPARATOR = "--separator";
    public static final String __STRING_LIST = "--string-list";
    public static final String __THREADS_HASHING = "--threads-hashing";
    public static final String __THREADS_READING =  "--threads-reading";
    public static final String _TIMESTAMP = "-t";
    public static final String __TIMESTAMP = "--timestamp";
    public static final String _ERROR_FILE = "-u";
    public static final String __ERROR_FILE = "--error-file";
    public static final String _ERROR_FILE_OVERWRITE = "-U";
    public static final String __ERROR_FILE_OVERWRITE = "--error-file-overwrite";
    public static final String _VERSION = "-v";
    public static final String __VERSION = "--version";
    public static final String _VERBOSE = "-V";
    public static final String __VERBOSE = "--verbose";
    public static final String _WANTED_LIST = "-w";
    public static final String __WANTED_LIST = "--wanted-list";
    public static final String _HEX_LOWERCASE = "-x";
    public static final String __HEX_LOWERCASE = "--hex-lowercase";
    public static final String _HEX_UPPERCASE = "-X";
    public static final String __HEX_UPPERCASE = "--hex-uppercase";
    public static final String __CHARSET_FILE_LIST = "--charset-file-list";
    public static final String __FILE_LIST_CHARSET = "--file-list-charset";
    public static final String __CHARSET_CHECK_FILE = "--charset-check-file";
    public static final String __CHECK_FILE_CHARSET = "--check-file-charset";
    public static final String __CHARSET_ERROR_FILE =  "--charset-error-file";
    public static final String __ERROR_FILE_CHARSET = "--error-file-charset";
    public static final String __CHARSET_OUTPUT_FILE = "--charset-output-file";
    public static final String __OUTPUT_FILE_CHARSET = "--output-file-charset";
    public static final String __CHARSET_WANTED_LIST = "--charset-wanted-list";
    public static final String __WANTED_LIST_CHARSET = "--wanted-list-charset";

    public static final String __CHARSET_STRING_LIST = "--charset-string-list";
    public static final String __STRING_LIST_CHARSET = "--string-list-charset";
    public static final String __CHARSET_CONSOLE = "--charset-console";
    public static final String __CONSOLE_CHARSET = "--console-charset";

    public static final String __CHARSET_STDOUT = "--charset-stdout";
    public static final String __STDOUT_CHARSET = "--stdout-charset";
    public static final String __CHARSET_STDERR = "--charset-stderr";
    public static final String __STDERR_CHARSET = "--stderr-charset";

    public static final String DASH = "-";
    public static final String DASHDASH = "--";
    public static final String HELP_DEFAULT_LANGUAGE = "en";


    public CLIParameters(String[] args) {
        this.args = args;
    }

    public String[] getArgs() {
        return args;
    }

    private void handleUserParamError(String userArg, String helpString) throws ParameterException {
        Help.printHelp("en", helpString);
        throw new ParameterException(String.format("Option %s requires a valid parameter.", userArg));
    }

    private void handleParamError(String helpString, String formattedMessage, String... values) throws ParameterException {
        Help.printHelp("en", helpString);
        throw new ParameterException(String.format("for option \"%s\": "+formattedMessage+ " For syntax on this option see above.", helpString, values));
    }


    /**
     * Parses the CLI Parameters and returns a Parameters object
     *
     * @return a Parameters objects
     * @throws ParameterException if parameters are invalid
     */
    public Parameters parse() throws ParameterException {
        Parameters parameters = new Parameters();
        parameters.setCLIParameters(args);

        boolean dashdash = false;
        String verboseControl = null;
        int firstfile = 0;
        String arg;
        if (args.length == 0) {
            parameters.setHelp(true);
        } else {
            while (firstfile < args.length && args[firstfile].startsWith(DASH) && !dashdash) {
                arg = args[firstfile++];

                // --algorithm
                if (arg.equals(_ALGORITHM) || arg.equals(__ALGORITHM)) {
                    if (firstfile < args.length) {
                        parameters.setAlgorithm(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __ALGORITHM);
                    }

                // --alternative
                } else if (arg.equals(_ALTERNATIVE) || arg.equals(__ALTERNATIVE)) {
                    parameters.setAlternateImplementationWanted(true);

                // --path-absolute
                } else if (arg.equals(__PATH_ABSOLUTE) || (arg.equals(__ABSOLUTE))) {
                    parameters.setPathAbsolute(true);

                // --path-relative-to
                } else if (arg.equals(__PATH_RELATIVE_TO) || arg.equals(__RELATIVE_TO)) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        parameters.setPathRelativeToAsString(arg);
                    } else {
                        handleUserParamError(arg, __PATH_RELATIVE_TO);
                    }

                } else if (arg.equals(__PATH_RELATIVE_TO_ENTRY) || arg.equals(__RELATIVE_TO_ENTRY)) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        try {
                            int number = Integer.parseInt(arg);
                            if (number < 1) {
                                throw new ParameterException("line number has to be > 0.");
                            }
                            parameters.setPathRelativeToEntry(number);
                        } catch (NumberFormatException nfe) {
                            throw new ParameterException(nfe.getMessage());
                        }
                    } else {
                        handleUserParamError(arg, __PATH_RELATIVE_TO_ENTRY);
                    }

                } else if (arg.equals(_UTF8) || arg.equals(__UTF8)) {
                    parameters.setUtf8(true);

                } else if (arg.equals(DASH)) {
                    parameters.setStdinForFilenamesFromArgs(true);

                } else if (arg.equals(DASHDASH)) {
                    dashdash = true;

                } else if (arg.equals(__COPYRIGHT)) {
                    parameters.setCopyrightWanted(true);

                } else if (arg.equals(__LICENSE)) {
                    parameters.setLicenseWanted(true);

                } else if (arg.equals(__HEADER)) {
                    parameters.setHeaderWanted(true);
                    parameters.setHeaderWantedExplicitlySet(true);

                } else if (arg.equals(__NO_HEADER)) {
                    parameters.setHeaderWanted(false);
                    parameters.setHeaderWantedExplicitlySet(true);

                } else if (arg.equals(_CHECK_FILE) || arg.equals(__CHECK_FILE)) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        parameters.setCheckFile(arg);
                        parameters.getVerbose().enableAll();
                    } else {
                        handleUserParamError(arg, __CHECK_FILE);
                    }

                } else if (arg.equals(__CHECK_LINE)) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        parameters.setCheckLine(arg);
                        parameters.getVerbose().enableAll();
                    } else {
                        handleUserParamError(arg, __CHECK_LINE);
                    }

                } else if (arg.equals(__CHECK_STRICT)) {
                    parameters.setCheckStrict(true);

                } else if (arg.equals(_COMPAT) || arg.equals(__COMPAT) || arg.equals(__STYLE)) {
                    if (firstfile < args.length) {
                        parameters.setCompatibilityID(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __COMPAT);
                    }

                } else if (arg.equals(__BOM)) {
                    parameters.setBom(true);

                } else if (arg.equals(_DONT_FOLLOW_SYMLINKS_TO_DIRECTORIES) || arg.equals(__DONT_FOLLOW_SYMLINKS_TO_DIRECTORIES)) {
                    parameters.setDontFollowSymlinksToDirectories(true);

                } else if (arg.equals(_EXPECT_HASH) || (arg.equals(__EXPECT_HASH)) || arg.equals(__EXPECT)) {
                    if (firstfile < args.length) {
                        parameters.setExpected(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __EXPECT_HASH);
                    }

                } else if (arg.equals(_ENCODING) || arg.equals(__ENCODING)) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        try {
                            parameters.setEncoding(arg);
                        } catch (IllegalArgumentException e) {
                            throw new ParameterException(e.getMessage());
                        }
                    } else {
                        handleUserParamError(arg, __ENCODING);
                    }

                } else if (arg.equals(_DONT_FOLLOW_SYMLINKS_TO_FILES) || arg.equals(__DONT_FOLLOW_SYMLINKS_TO_FILES)) {
                    parameters.setDontFollowSymlinksToFiles(true);

                } else if (arg.equals(__FILESIZE)) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];

                        try {
                            parameters.setFilesizeWanted(GeneralString.parseBoolean(arg));
                        } catch (IllegalArgumentException iae) {
                            handleParamError(__FILESIZE, iae.getMessage(), arg);
                        }
                    } else {
                        handleUserParamError(arg, __FILESIZE);
                    }

                } else if (arg.equals(__GNU_FILENAME_ESCAPING)) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];

                        try {
                            parameters.setGnuEscaping(GeneralString.parseBoolean(arg));
                        } catch (IllegalArgumentException iae) {
                            handleParamError(__GNU_FILENAME_ESCAPING, iae.getMessage(), arg);
                        }

                    } else {
                        handleUserParamError(arg, __GNU_FILENAME_ESCAPING);
                    }


                } else if (arg.equals(_FORMAT) || arg.equals(__FORMAT)) {
                    if (firstfile < args.length) {
                        parameters.setFormat(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __FORMAT);
                    }

                } else if (arg.equals(_GROUP_BYTES) || arg.equals(__GROUP_BYTES)) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        try {
                            parameters.setGrouping(arg);
                        } catch (IllegalArgumentException e) {
                            throw new ParameterException(String.format("%s is not a decimal number.", arg));
                        }
                    } else {
                        handleUserParamError(arg, __GROUP_BYTES);
                    }

                } else if (arg.equals(_GROUP_BYTES_SEPARATOR) || arg.equals(__GROUP_BYTES_SEPARATOR)) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        try {
                            parameters.setGroupChar(arg);
                        } catch (IllegalArgumentException e) {
                            throw new ParameterException(e.getMessage());
                        }
                    } else {
                        handleUserParamError(arg, __GROUP_BYTES_SEPARATOR);
                    }

                } else if (arg.equals(_HELP) || arg.equals(__HELP)) {
                    // default inits
                    parameters.setHelp(true);
                    parameters.setHelpLanguage(HELP_DEFAULT_LANGUAGE);
                    parameters.setHelpSearchString(null);
                    if (firstfile < args.length) {
                        String helpLanguageOrSearchString = args[firstfile++];

                        if (helpLanguageOrSearchString.equalsIgnoreCase(HELP_DEFAULT_LANGUAGE)) {
                            parameters.setHelpLanguage(helpLanguageOrSearchString);
                            if (firstfile < args.length) {
                                parameters.setHelpSearchString(args[firstfile++]);
                            }
                        } else {
                            parameters.setHelpSearchString(helpLanguageOrSearchString);
                        }
                    }

                } else if (arg.equals(__HMACS)) {
                    parameters.setHMACsWanted(true);

                } else if (arg.equals(__INFO)) {
                    parameters.setInfoMode(true);

                } else if (arg.equals(_IGNORE_LINES_STARTING_WITH_STRING) || arg.equals(__IGNORE_LINES_STARTING_WITH_STRING)) {
                    if (firstfile < args.length) {
                        parameters.setCommentChars(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __IGNORE_LINES_STARTING_WITH_STRING);
                    }

                } else if (arg.equals(__IGNORE_EMPTY_LINES)) {
                    parameters.setIgnoreEmptyLines(true);

                } else if (arg.equals(__IGNORE_HASHES)) {
                    parameters.setIgnoreHashes(true);

                } else if (arg.equals(__IGNORE_SIZES)) {
                    parameters.setIgnoreSizes(true);

                } else if (arg.equals(__IGNORE_TIMESTAMPS)) {
                    parameters.setIgnoreTimestamps(true);

                } else if (arg.equals(_KEY) || arg.equals(__KEY)) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        try {
                            parameters.setKey(arg);
                        } catch (IllegalArgumentException e) {
                            throw new ParameterException(e.getMessage());
                        }
                    } else {
                        handleUserParamError(arg, __KEY);
                    }

                } else if (arg.equals(_LIST) || arg.equals(__LIST)) {
                    parameters.setList(true);

                } else if (arg.equals(__LIST_FILTER)) {
                    if (firstfile < args.length) {
                        try {
                            parameters.getListFilter().setFilter(args[firstfile++]);
                        } catch (IllegalArgumentException e) {
                            Help.printHelp(HELP_DEFAULT_LANGUAGE, __LIST_FILTER);
                            throw new ParameterException(e.getMessage());
                        }
                    } else {
                        handleUserParamError(arg, __LIST_FILTER);
                    }

                } else if (arg.equals(__MATCH_FILTER) || arg.equals(__WANTED_LIST_FILTER)) {
                    if (firstfile < args.length) {
                        try {
                            parameters.getWantedListFilter().setFilter(args[firstfile++]);
                        } catch (IllegalArgumentException e) {
                            Help.printHelp(HELP_DEFAULT_LANGUAGE, __MATCH_FILTER);
                            throw new ParameterException(e.getMessage());
                        }
                    } else {
                        handleUserParamError(arg, __MATCH_FILTER);
                    }

                } else if (arg.equals(__LEGACY_STDIN_NAME)) {
                    parameters.setStdinName("-");
                    AbstractChecksum.setStdinName("-");

                } else if (arg.equals(_FILE_LIST) || arg.equals(__FILE_LIST)) {
                    if (firstfile < args.length) {
                        parameters.setFilelistFilename(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __FILE_LIST);
                    }

                } else if (arg.equals(__FILE_LIST_FORMAT)) {
                    if (firstfile < args.length) {
                        parameters.setFilelistFormat(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __FILE_LIST_FORMAT);
                    }

                } else if (arg.equals(__NO_PATH)) {
                    parameters.setNoPath(true);

                } else if (arg.equals(_OUTPUT_FILE) || arg.equals(__OUTPUT_FILE)) {
                    parameters.setOutputFileOverwrite(false);

                    if (firstfile < args.length) {
                        parameters.setOutputFile(args[firstfile++]);

                    } else {
                        handleUserParamError(arg, __OUTPUT_FILE);
                    }

                } else if (arg.equals(_OUTPUT_FILE_OVERWRITE) || arg.equals(__OUTPUT_FILE_OVERWRITE)) {
                    parameters.setOutputFileOverwrite(true);

                    if (firstfile < args.length) {
                        parameters.setOutputFile(args[firstfile++]);

                    } else {
                        handleUserParamError(arg, __OUTPUT_FILE_OVERWRITE);
                    }

                } else if (arg.equals(__OUTPUT_FILE_REPLACE_TOKENS)) {
                    parameters.setOutputFileReplaceTokens(true);

                } else if (arg.equals(_PATH_SEPARATOR) || arg.equals(__PATH_SEPARATOR)) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        try {
                            parameters.setPathChar(arg);
                        } catch (IllegalArgumentException e) {
                            throw new ParameterException(e.getMessage());
                        }
                    } else {
                        handleUserParamError(arg, __PATH_SEPARATOR);
                    }

                } else if (arg.equals(_QUICK) || arg.equals(__QUICK)) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        try {
                            parameters.setSequence(arg);
                        } catch (IllegalArgumentException e) {
                            throw new ParameterException(e.getMessage());
                        }
                    } else {
                        handleUserParamError(arg, __QUICK);
                    }

                } else if (arg.equals(_RECURSIVE) || arg.equals(__RECURSIVE)) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        if (arg.equals("max")) {
                            parameters.setDepth(Integer.MAX_VALUE);
                            parameters.setRecursive(true);
                        } else
                            try {
                                int depth = Integer.parseInt(arg);
                                if (depth < 1) {
                                    throw new ParameterException("depth value has to be > 0.");
                                }
                                parameters.setDepth(depth);
                                parameters.setRecursive(true);
                            } catch (NumberFormatException nfe) {
                                throw new ParameterException(nfe.getMessage());
                            }
                    } else {
                        handleUserParamError(arg, __RECURSIVE);
                    }

                } else if (arg.equals(__SCAN_ALL_UNIX_FILE_TYPES)) {
                    parameters.setScanAllUnixFileTypes(true);

                } else if (arg.equals(__SCAN_NTFS_ADS)) {
                    parameters.setScanNtfsAds(true);

                } else if (arg.equals(_SEPARATOR) || arg.equals(__SEPARATOR)) {
                    if (firstfile < args.length) {
                        parameters.setSeparator(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __SEPARATOR);
                    }

                } else if (arg.equals(__STRING_LIST)) {
                    if (firstfile < args.length) {
                        parameters.setStringList(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __STRING_LIST);
                    }

                } else if (arg.equals(__THREADS_HASHING)) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        if (arg.equals("max")) {
                            parameters.setThreadsHashing(ThreadControl.getThreadsMax());
                        } else {
                            try {
                                int value = Integer.parseInt(arg);
                                if (value < 1) {
                                    throw new ParameterException("threads value has to be > 0.");
                                }
                                parameters.setThreadsHashing(value);
                            } catch (NumberFormatException nfe) {
                                throw new ParameterException(nfe.getMessage());
                            }
                        }
                    } else {
                        handleUserParamError(arg, __THREADS_HASHING);
                    }

                } else if (arg.equals(__THREADS_READING)) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        if (arg.equals("max")) {
                            parameters.setThreadsReading(ThreadControl.getThreadsMax());
                        } else {
                            try {
                                int value = Integer.parseInt(arg);
                                if (value < 1) {
                                    throw new ParameterException("threads value has to be > 0.");
                                }
                                parameters.setThreadsReading(value);
                            } catch (NumberFormatException nfe) {
                                throw new ParameterException(nfe.getMessage());
                            }
                        }
                    } else {
                        handleUserParamError(arg, __THREADS_READING);
                    }

                } else if (arg.equals(_TIMESTAMP) || arg.equals(__TIMESTAMP)) {
                    if (firstfile < args.length) {
                        try {
                            parameters.setTimestampFormat(args[firstfile++]);
                        } catch (IllegalArgumentException e) {
                            throw new ParameterException(String.format("Option -t is wrong (\"%s\")", e.getMessage()));
                        }
                    } else {
                        handleUserParamError(arg, __TIMESTAMP);
                    }

                } else if (arg.equals(_ERROR_FILE) || arg.equals(__ERROR_FILE)) {
                    parameters.setErrorFileOverwrite(false);

                    if (firstfile < args.length) {
                        parameters.setErrorFile(args[firstfile++]);

                    } else {
                        handleUserParamError(arg, __ERROR_FILE);
                    }

                } else if (arg.equals(_ERROR_FILE_OVERWRITE) || arg.equals(__ERROR_FILE_OVERWRITE)) {
                    parameters.setErrorFileOverwrite(true);
                    if (firstfile < args.length) {
                        parameters.setErrorFile(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __ERROR_FILE_OVERWRITE);
                    }

                } else if (arg.equals(_VERSION) || arg.equals(__VERSION)) {
                    parameters.setVersionWanted(true);
                    parameters.getVerbose().setInfo(false); // no details if we don't want details explicitly

                } else if (arg.equals(_VERBOSE) || arg.equals(__VERBOSE)) {
                    if (firstfile < args.length) {
                        verboseControl = args[firstfile++];
                    } else {
                        handleUserParamError(arg, __VERBOSE);
                    }

                } else if (arg.equals(_WANTED_LIST) || arg.equals(__WANTED_LIST)) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        parameters.setWantedList(arg);
                        parameters.getVerbose().enableAll();
                    } else {
                        handleUserParamError(arg, __WANTED_LIST);
                    }

                } else if (arg.equals(_HEX_LOWERCASE) || arg.equals(__HEX_LOWERCASE)) {
                    parameters.setEncoding(Encoding.HEX);

                } else if (arg.equals(_HEX_UPPERCASE) || arg.equals(__HEX_UPPERCASE)) {
                    parameters.setEncoding(Encoding.HEX_UPPERCASE);

                } else if (arg.equals(__CHARSET_FILE_LIST) || arg.equals(__FILE_LIST_CHARSET)) {
                    if (firstfile < args.length) {
                        parameters.setCharsetFileList(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __CHARSET_FILE_LIST);
                    }

                } else if (arg.equals(__CHARSET_CHECK_FILE) || arg.equals(__CHECK_FILE_CHARSET)) {
                    if (firstfile < args.length) {
                        parameters.setCharsetCheckFile(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __CHARSET_CHECK_FILE);
                    }

                } else if (arg.equals(__CHARSET_WANTED_LIST) || arg.equals(__WANTED_LIST_CHARSET)) {
                    if (firstfile < args.length) {
                        parameters.setCharsetWantedList(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __CHARSET_WANTED_LIST);
                    }

                } else if (arg.equals(__CHARSET_ERROR_FILE) || arg.equals(__ERROR_FILE_CHARSET)) {
                    if (firstfile < args.length) {
                        parameters.setCharsetErrorFile(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __CHARSET_ERROR_FILE);
                    }

                } else if (arg.equals(__CHARSET_OUTPUT_FILE) || arg.equals(__OUTPUT_FILE_CHARSET)) {
                    if (firstfile < args.length) {
                        parameters.setCharsetOutputFile(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __CHARSET_OUTPUT_FILE);
                    }

                } else if (arg.equals(__CHARSET_STRING_LIST) || arg.equals(__STRING_LIST_CHARSET)) {
                    if (firstfile < args.length) {
                        parameters.setCharsetStringList(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __CHARSET_STRING_LIST);
                    }

                } else if (arg.equals(__CHARSET_CONSOLE) || arg.equals(__CONSOLE_CHARSET)) {
                    if (firstfile < args.length) {
                        parameters.setCharsetConsole(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __CHARSET_CONSOLE);
                    }

                } else if (arg.equals(__CHARSET_STDOUT) || arg.equals(__STDOUT_CHARSET)) {
                    if (firstfile < args.length) {
                        parameters.setCharsetStdout(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __CHARSET_STDOUT);
                    }

                } else if (arg.equals(__CHARSET_STDERR) || arg.equals(__STDERR_CHARSET)) {
                    if (firstfile < args.length) {
                        parameters.setCharsetStderr(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, __CHARSET_STDERR);
                    }

                } else {
                    throw new ParameterException(String.format("Unknown argument: %s. Use -h for help.", arg));
                }

            } // end while

            // if --info has been set, enable a potentially disabled verbose object again
            if (parameters.isInfoMode()) {
                parameters.getVerbose().setDefault();
                parameters.getVerbose().setInfo(true);
            }

            // parsing the verboseControl at the end of the while loop,
            // because some options change the default of the verbose object
            if (verboseControl != null) {
                try {
                    parameters.getVerbose().setVerbose(verboseControl);
                } catch (IllegalArgumentException e) {
                    Help.printHelp(HELP_DEFAULT_LANGUAGE, __VERBOSE);
                    throw new ParameterException(e.getMessage());
                }
            }

            // processing arguments file list            
            for (int i = firstfile; i < args.length; i++) {
                if (args[i].equals(DASH) && !dashdash) { // the dash (stdin) could come even between normal filenames
                    parameters.setStdinForFilenamesFromArgs(true);
                } else {
                    parameters.getFilenamesFromArgs().add(args[i]);
                }
            }

            // replace #ALGONAME, #ALGONAME{lowercase}, #ALGONAME{uppercase}
            if (parameters.getOutputFile() != null && parameters.isOutputFileReplaceTokens()) {
                parameters.setOutputFileRaw(parameters.getOutputFile());
                if (parameters.getAlgorithm() != null) {
                    parameters.setOutputFile(
                            parameters.getOutputFile().replaceAll(
                                    "#ALGONAME\\{uppercase\\}", parameters.getAlgorithm().toUpperCase(Locale.US).replace(':', '=')));
                    parameters.setOutputFile(
                            parameters.getOutputFile().replaceAll(
                                    "#ALGONAME\\{lowercase\\}", "#ALGONAME"));
                    parameters.setOutputFile(
                            parameters.getOutputFile().replaceAll(
                                    "#ALGONAME", parameters.getAlgorithm().toLowerCase(Locale.US).replace(':', '=')));
                }
            }

        } // end-if args.length > 0
        // end parsing arguments
        return parameters;
    }

}

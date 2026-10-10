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
package net.jacksum.cli;

import net.jacksum.actions.info.help.Help;
import net.jacksum.formats.Encoding;
import net.jacksum.multicore.ThreadControl;
import net.jacksum.parameters.ParameterException;
import net.jacksum.parameters.Parameters;
import net.jacksum.parameters.Sequence;
import net.loefflmann.sugar.util.GeneralString;

import java.util.Locale;
import java.util.Set;

/**
 * All parameters for the Command Line Interface (CLI)
 *
 * @author Johann N. Loefflmann
 */
public class CLIParameters {

    String[] args;

    /** Long form of the option that selects the algorithm(s). */
    public static final String __ALGORITHM = "--algorithm";
    /** Short form of the option that selects the algorithm(s). */
    public static final String _ALGORITHM = "-a";
    /** Short form of the option that selects the alternate implementation of an algorithm. */
    public static final String _ALTERNATIVE = "-A";
    /** Long form of the option that selects the alternate implementation of an algorithm. */
    public static final String __ALTERNATIVE = "--alternative";
    /** Option that prints absolute paths. */
    public static final String __PATH_ABSOLUTE = "--path-absolute";
    /** Alternative long form of the option that prints absolute paths. */
    public static final String __ABSOLUTE = "--absolute";
    /** Option that prints paths relative to a given path. */
    public static final String __PATH_RELATIVE_TO = "--path-relative-to";
    /** Alternative long form of the option that prints paths relative to a given path. */
    public static final String __RELATIVE_TO = "--relative-to";
    /** Option that prints paths relative to an entry of the file list. */
    public static final String __PATH_RELATIVE_TO_ENTRY = "--path-relative-to-entry";
    /** Alternative long form of the option that prints paths relative to an entry of the file list. */
    public static final String __RELATIVE_TO_ENTRY = "--relative-to-entry";
    /** Short form of the option that sets UTF-8 as the character set of both stdout and stderr. */
    public static final String _UTF8 = "-8";
    /** Long form of the option that sets UTF-8 as the character set of both stdout and stderr. */
    public static final String __UTF8 = "--utf8";
    /** Option that prints the copyright information. */
    public static final String __COPYRIGHT = "--copyright";
    /** Option that prints the license. */
    public static final String __LICENSE = "--license";
    /** Option that prints a header as a comment. */
    public static final String __HEADER = "--header";
    /** Option that suppresses the header. */
    public static final String __NO_HEADER = "--no-header";
    /** Short form of the option that verifies files against a check file. */
    public static final String _CHECK_FILE = "-c";
    /** Long form of the option that verifies files against a check file. */
    public static final String __CHECK_FILE = "--check-file";
    /** Option that verifies files against a check line given on the command line. */
    public static final String __CHECK_LINE = "--check-line";
    /** Option that exits with a non-zero status if the check file contains invalid lines. */
    public static final String __CHECK_STRICT = "--check-strict";
    /** Short form of the option that sets the compatibility style. */
    public static final String _COMPAT = "-C";
    /** Long form of the option that sets the compatibility style. */
    public static final String __COMPAT = "--compat";
    /** Alternative long form of the option that sets the compatibility style. */
    public static final String __STYLE = "--style";
    /** Option that adds a Byte-Order Mark (BOM) to the output. */
    public static final String __BOM = "--bom";
    /** Short form of the option that does not follow symbolic links to directories. */
    public static final String _DONT_FOLLOW_SYMLINKS_TO_DIRECTORIES = "-d";
    /** Long form of the option that does not follow symbolic links to directories. */
    public static final String __DONT_FOLLOW_SYMLINKS_TO_DIRECTORIES = "--dont-follow-symlinks-to-directories";
    /** Option that searches the help for an exact match rather than for a prefix. */
    public static final String __EXACT = "--exact";
    /** Short form of the option that sets the expected hash value. */
    public static final String _EXPECT_HASH = "-e";
    /** Long form of the option that sets the expected hash value. */
    public static final String __EXPECT_HASH = "--expect-hash";
    /** Alternative long form of the option that sets the expected hash value. */
    public static final String __EXPECT = "--expect";
    /** Short form of the option that sets the encoding of the hash value and of the sequence. */
    public static final String _ENCODING = "-E";
    /** Long form of the option that sets the encoding of the hash value and of the sequence. */
    public static final String __ENCODING = "--encoding";
    /** Short form of the option that does not follow symbolic links to files. */
    public static final String _DONT_FOLLOW_SYMLINKS_TO_FILES = "-f";
    /** Long form of the option that does not follow symbolic links to files. */
    public static final String __DONT_FOLLOW_SYMLINKS_TO_FILES = "--dont-follow-symlinks-to-files";
    /** Option that controls whether the file size is printed. */
    public static final String __FILESIZE = "--filesize";
    /** Option that controls the GNU filename escaping. */
    public static final String __GNU_FILENAME_ESCAPING = "--gnu-filename-escaping";
    /** Short form of the option that sets a customized output format. */
    public static final String _FORMAT = "-F";
    /** Long form of the option that sets a customized output format. */
    public static final String __FORMAT = "--format";
    /** Short form of the option that groups the bytes of the hex output. */
    public static final String _GROUP_BYTES = "-g";
    /** Long form of the option that groups the bytes of the hex output. */
    public static final String __GROUP_BYTES = "--group-bytes";
    /** Short form of the option that sets the separator for byte groups. */
    public static final String _GROUP_BYTES_SEPARATOR = "-G";
    /** Long form of the option that sets the separator for byte groups. */
    public static final String __GROUP_BYTES_SEPARATOR = "--group-bytes-separator";
    /** Short form of the option that prints the help. */
    public static final String _HELP = "-h";
    /** Long form of the option that prints the help. */
    public static final String __HELP = "--help";
    /** Option that prints all algorithms that support HMAC. */
    public static final String __HMACS = "--hmacs";
    /** Option that forces the information mode. */
    public static final String __INFO = "--info";
    /** Short form of the option that ignores lines of a list that start with a given string. */
    public static final String _IGNORE_LINES_STARTING_WITH_STRING = "-I";
    /** Long form of the option that ignores lines of a list that start with a given string. */
    public static final String __IGNORE_LINES_STARTING_WITH_STRING = "--ignore-lines-starting-with-string";
    /** Option that ignores empty lines of the string list. */
    public static final String __IGNORE_EMPTY_LINES = "--ignore-empty-lines";
    /** Option that ignores hash values during a check. */
    public static final String __IGNORE_HASHES = "--ignore-hashes";
    /** Option that ignores file sizes during a check. */
    public static final String __IGNORE_SIZES = "--ignore-sizes";
    /** Option that ignores timestamps during a check. */
    public static final String __IGNORE_TIMESTAMPS = "--ignore-timestamps";
    /** Short form of the option that sets the secret key for HMAC. */
    public static final String _KEY = "-k";
    /** Long form of the option that sets the secret key for HMAC. */
    public static final String __KEY = "--key";
    /** Short form of the option that enables the list view. */
    public static final String _LIST = "-l";
    /** Long form of the option that enables the list view. */
    public static final String __LIST = "--list";
    /** Option that filters the output of the check mode. */
    public static final String __LIST_FILTER = "--list-filter";
    /** Alternative long form of the option that filters the output of the options -w and -e. */
    public static final String __WANTED_LIST_FILTER = "--wanted-list-filter";
    /** Option that filters the output of the options -w and -e. */
    public static final String __MATCH_FILTER = "--match-filter";
    /** Option that names the standard input stream "-" rather than "&lt;stdin&gt;". */
    public static final String __LEGACY_STDIN_NAME = "--legacy-stdin-name";
    /** Short form of the option that processes the files that are listed in a file list. */
    public static final String _FILE_LIST = "-L";
    /** Long form of the option that processes the files that are listed in a file list. */
    public static final String __FILE_LIST = "--file-list";
    /** Option that sets the format of the file list. */
    public static final String __FILE_LIST_FORMAT = "--file-list-format";
    /** Option that prints file names without path information. */
    public static final String __NO_PATH = "--no-path";
    /** Short form of the option that writes the output to a file. */
    public static final String _OUTPUT_FILE = "-o";
    /** Long form of the option that writes the output to a file. */
    public static final String __OUTPUT_FILE = "--output-file";
    /** Short form of the option that writes the output to a file and overwrites an existing file. */
    public static final String _OUTPUT_FILE_OVERWRITE = "-O";
    /** Long form of the option that writes the output to a file and overwrites an existing file. */
    public static final String __OUTPUT_FILE_OVERWRITE = "--output-file-overwrite";
    /** Option that replaces tokens in the name of the output file. */
    public static final String __OUTPUT_FILE_REPLACE_TOKENS = "--output-file-replace-tokens";
    /** Short form of the option that sets the path separator character. */
    public static final String _PATH_SEPARATOR = "-P";
    /** Long form of the option that sets the path separator character. */
    public static final String __PATH_SEPARATOR = "--path-separator";
    /** Short form of the option that processes a sequence quickly and quits. */
    public static final String _QUICK = "-q";
    /** Long form of the option that processes a sequence quickly and quits. */
    public static final String __QUICK = "--quick";
    /** Short form of the option that processes subdirectories recursively. */
    public static final String _RECURSIVE = "-r";
    /** Long form of the option that processes subdirectories recursively. */
    public static final String __RECURSIVE = "--recursive";
    /** Option that scans all Unix file types, not only regular files. */
    public static final String __SCAN_ALL_UNIX_FILE_TYPES = "--scan-all-unix-file-types";
    /** Option that scans NTFS Alternate Data Streams. */
    public static final String __SCAN_NTFS_ADS = "--scan-ntfs-ads";
    /** Short form of the option that sets a custom separator string. */
    public static final String _SEPARATOR = "-s";
    /** Long form of the option that sets a custom separator string. */
    public static final String __SEPARATOR = "--separator";
    /** Option that activates the string list mode. */
    public static final String __STRING_LIST = "--string-list";
    // the special value that -r, --threads-hashing and --threads-reading accept
    // instead of a number; it is compared without regard to case, because no
    // number can be confused with it
    /** The special value that {@code -r}, {@code --threads-hashing} and {@code --threads-reading} accept instead of a number. */
    public static final String MAX = "max";
    /** Option that sets the number of threads for hashing. */
    public static final String __THREADS_HASHING = "--threads-hashing";
    /** Option that sets the number of threads for reading. */
    public static final String __THREADS_READING =  "--threads-reading";
    /** Short form of the option that sets the timestamp format. */
    public static final String _TIMESTAMP = "-t";
    /** Long form of the option that sets the timestamp format. */
    public static final String __TIMESTAMP = "--timestamp";
    /** Short form of the option that writes the error messages to a file. */
    public static final String _ERROR_FILE = "-u";
    /** Long form of the option that writes the error messages to a file. */
    public static final String __ERROR_FILE = "--error-file";
    /** Short form of the option that writes the error messages to a file and overwrites an existing file. */
    public static final String _ERROR_FILE_OVERWRITE = "-U";
    /** Long form of the option that writes the error messages to a file and overwrites an existing file. */
    public static final String __ERROR_FILE_OVERWRITE = "--error-file-overwrite";
    /** Short form of the option that prints the product name and version. */
    public static final String _VERSION = "-v";
    /** Long form of the option that prints the product name and version. */
    public static final String __VERSION = "--version";
    /** Short form of the option that sets the verbose controls. */
    public static final String _VERBOSE = "-V";
    /** Long form of the option that sets the verbose controls. */
    public static final String __VERBOSE = "--verbose";
    /** Short form of the option that sets the list of wanted hash values. */
    public static final String _WANTED_LIST = "-w";
    /** Long form of the option that sets the list of wanted hash values. */
    public static final String __WANTED_LIST = "--wanted-list";
    /** Short form of the option that selects lowercase hex encoding. */
    public static final String _HEX_LOWERCASE = "-x";
    /** Long form of the option that selects lowercase hex encoding. */
    public static final String __HEX_LOWERCASE = "--hex-lowercase";
    /** Short form of the option that selects uppercase hex encoding. */
    public static final String _HEX_UPPERCASE = "-X";
    /** Long form of the option that selects uppercase hex encoding. */
    public static final String __HEX_UPPERCASE = "--hex-uppercase";
    /** Option that sets the character set of the file list. */
    public static final String __CHARSET_FILE_LIST = "--charset-file-list";
    /** Alternative long form of the option that sets the character set of the file list. */
    public static final String __FILE_LIST_CHARSET = "--file-list-charset";
    /** Option that sets the character set of the check file. */
    public static final String __CHARSET_CHECK_FILE = "--charset-check-file";
    /** Alternative long form of the option that sets the character set of the check file. */
    public static final String __CHECK_FILE_CHARSET = "--check-file-charset";
    /** Option that sets the character set of the error file. */
    public static final String __CHARSET_ERROR_FILE =  "--charset-error-file";
    /** Alternative long form of the option that sets the character set of the error file. */
    public static final String __ERROR_FILE_CHARSET = "--error-file-charset";
    /** Option that sets the character set of the output file. */
    public static final String __CHARSET_OUTPUT_FILE = "--charset-output-file";
    /** Alternative long form of the option that sets the character set of the output file. */
    public static final String __OUTPUT_FILE_CHARSET = "--output-file-charset";
    /** Option that sets the character set of the wanted list. */
    public static final String __CHARSET_WANTED_LIST = "--charset-wanted-list";
    /** Alternative long form of the option that sets the character set of the wanted list. */
    public static final String __WANTED_LIST_CHARSET = "--wanted-list-charset";

    /** Option that sets the character set of the string list. */
    public static final String __CHARSET_STRING_LIST = "--charset-string-list";
    /** Alternative long form of the option that sets the character set of the string list. */
    public static final String __STRING_LIST_CHARSET = "--string-list-charset";
    /** Option that sets the character set of the console. */
    public static final String __CHARSET_CONSOLE = "--charset-console";
    /** Alternative long form of the option that sets the character set of the console. */
    public static final String __CONSOLE_CHARSET = "--console-charset";

    /** Option that sets the character set of the standard output stream. */
    public static final String __CHARSET_STDOUT = "--charset-stdout";
    /** Alternative long form of the option that sets the character set of the standard output stream. */
    public static final String __STDOUT_CHARSET = "--stdout-charset";
    /** Option that sets the character set of the standard error stream. */
    public static final String __CHARSET_STDERR = "--charset-stderr";
    /** Alternative long form of the option that sets the character set of the standard error stream. */
    public static final String __STDERR_CHARSET = "--stderr-charset";

    /** The single dash, which stands for the standard input stream if it is given as a file name. */
    public static final String DASH = "-";
    /** The double dash, which marks the end of the options. */
    public static final String DASHDASH = "--";
    /** The default language of the help. */
    public static final String HELP_DEFAULT_LANGUAGE = "en";
    /** The languages that the help is available in. */
    public static final Set<String> HELP_LANGUAGES = Set.of("en", "de");


    /**
     * Creates a new CLIParameters object for the given command line arguments.
     *
     * @param args the command line arguments
     */
    public CLIParameters(String[] args) {
        this.args = args;
    }

    /**
     * Returns the command line arguments.
     *
     * @return the command line arguments
     */
    public String[] getArgs() {
        return args;
    }

    private void handleUserParamError(String userArg, String helpString) throws ParameterException {
        Help.printHelp("en", helpString, true);
        throw new ParameterException(String.format("Option %s requires a valid parameter.", userArg));
    }

    /**
     * Prints the help for an option and throws a ParameterException that quotes the message
     * of the parser which has rejected the option's argument.
     *
     * The message is passed as an argument of String.format rather than being concatenated
     * into its format string, because it contains the argument the user has typed: a percent
     * sign in that argument would be interpreted as a conversion otherwise, which would
     * truncate, mangle, or even replace the whole message.
     *
     * @param helpString the option, in order to print its help and to name it in the message
     * @param message the message of the parser that has rejected the argument
     * @throws ParameterException always, that is the purpose of this method
     */
    private void handleParamError(String helpString, String message) throws ParameterException {
        Help.printHelp("en", helpString, true);
        throw new ParameterException(String.format(
                "For option \"%s\": %s For syntax on this option see above.", helpString, message));
    }


    /**
     * Parses the argument of an option that takes a number of threads.
     *
     * The value the user has typed is passed as an argument of String.format rather than
     * being concatenated into its format string, see handleParamError().
     *
     * @param value the argument the user has typed
     * @param option the option, in order to name it in the message
     * @return the number of threads, a value that ThreadControl accepts
     * @throws ParameterException if the argument is not a number, or if it is a number
     * that is out of range
     */
    private int parseThreads(String value, String option) throws ParameterException {
        if (value.equalsIgnoreCase(MAX)) {
            return ThreadControl.getThreadsMax();
        }
        int threads;
        try {
            threads = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            throw new ParameterException(String.format(
                    "Option %s: \"%s\" is not a valid number of threads. A value between %d and %d, or \"%s\", is required.",
                    option, value, ThreadControl.getThreadsMin(), ThreadControl.getThreadsLimit(), MAX));
        }
        // an out of range value would not just be pointless, it would stop the engine
        // from working at all, see net.jacksum.multicore.manyfiles.MessageWorker
        try {
            ThreadControl.checkRange(threads);
        } catch (IllegalArgumentException iae) {
            throw new ParameterException(String.format(
                    "Option %s requires a value between %d and %d, or the value \"%s\".",
                    option, ThreadControl.getThreadsMin(), ThreadControl.getThreadsLimit(), MAX));
        }
        return threads;
    }


    /**
     * Parses the CLI Parameters and returns a Parameters object
     *
     * @return a Parameters objects
     * @throws ParameterException if parameters are invalid
     */
    public Parameters parse() throws ParameterException {
        return parse(new Parameters());
    }

    /**
     * Parses the CLI Parameters into the Parameters object that is given, so that a
     * Parameters object can parse a command line in its own constructor.
     *
     * @param parameters the object that is filled, expected to be a new one
     * @return the object that has been given, filled with the parsed parameters
     * @throws ParameterException if parameters are invalid
     */
    public Parameters parse(Parameters parameters) throws ParameterException {
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
                                throw new ParameterException("The line number has to be > 0.");
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

                } else if (arg.equals(__EXACT)) {
                    parameters.setExact(true);

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
                            handleParamError(__FILESIZE, iae.getMessage());
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
                            handleParamError(__GNU_FILENAME_ESCAPING, iae.getMessage());
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
                            // pass the message on, it states the actual reason
                            throw new ParameterException(e.getMessage());
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

                        if (HELP_LANGUAGES.contains(helpLanguageOrSearchString.toLowerCase(Locale.US))) {
                            parameters.setHelpLanguage(helpLanguageOrSearchString.toLowerCase(Locale.US));
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
                            // the message of the exception may quote parts of the value,
                            // but the value of a key is a secret, so it must not be printed
                            throw new ParameterException(String.format(
                                    "Option %s: the key does not start with a known sequence type. Supported types are %s, as well as readline and password. The value is not shown, because it is a secret.",
                                    _KEY, Sequence.supportedTypes()));
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
                            Help.printHelp(HELP_DEFAULT_LANGUAGE, __LIST_FILTER, true);
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
                            Help.printHelp(HELP_DEFAULT_LANGUAGE, __MATCH_FILTER, true);
                            throw new ParameterException(e.getMessage());
                        }
                    } else {
                        handleUserParamError(arg, __MATCH_FILTER);
                    }

                } else if (arg.equals(__LEGACY_STDIN_NAME)) {
                    parameters.setStdinName("-");

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
                            throw new ParameterException(String.format("Option %s: %s", _QUICK, e.getMessage()));
                        }
                    } else {
                        handleUserParamError(arg, __QUICK);
                    }

                } else if (arg.equals(_RECURSIVE) || arg.equals(__RECURSIVE)) {
                    if (firstfile < args.length) {
                        // the option as the user has typed it, in order to name it in a message
                        String option = arg;
                        arg = args[firstfile++];
                        if (arg.equalsIgnoreCase(MAX)) {
                            parameters.setDepth(Integer.MAX_VALUE);
                            parameters.setRecursive(true);
                        } else {
                            int depth;
                            try {
                                depth = Integer.parseInt(arg);
                            } catch (NumberFormatException nfe) {
                                throw new ParameterException(String.format(
                                        "Option %s: \"%s\" is not a valid depth. A value of 1 or above, or \"%s\", is required.",
                                        option, arg, MAX));
                            }
                            if (depth < 1) {
                                throw new ParameterException(String.format(
                                        "Option %s requires a value of 1 or above, or the value \"%s\".", option, MAX));
                            }
                            parameters.setDepth(depth);
                            parameters.setRecursive(true);
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
                        parameters.setThreadsHashing(parseThreads(args[firstfile++], __THREADS_HASHING));
                    } else {
                        handleUserParamError(arg, __THREADS_HASHING);
                    }

                } else if (arg.equals(__THREADS_READING)) {
                    if (firstfile < args.length) {
                        parameters.setThreadsReading(parseThreads(args[firstfile++], __THREADS_READING));
                    } else {
                        handleUserParamError(arg, __THREADS_READING);
                    }

                } else if (arg.equals(_TIMESTAMP) || arg.equals(__TIMESTAMP)) {
                    if (firstfile < args.length) {
                        try {
                            parameters.setTimestampFormat(args[firstfile++]);
                        } catch (IllegalArgumentException e) {
                            throw new ParameterException(String.format("Option -t is wrong (\"%s\").", e.getMessage()));
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
                    Help.printHelp(HELP_DEFAULT_LANGUAGE, __VERBOSE, true);
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

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
package net.jacksum.parameters;

import net.jacksum.HashFunctionFactory;
import net.jacksum.actions.ActionType;
import net.jacksum.actions.info.algo.AlgoInfoActionParameters;
import net.jacksum.actions.info.app.AppInfoActionParameters;
import net.jacksum.actions.info.compat.CompatInfoActionParameters;
import net.jacksum.actions.info.help.Help;
import net.jacksum.actions.info.help.HelpActionParameters;
import net.jacksum.actions.info.hmacs.HMACsActionParameters;
import net.jacksum.actions.info.version.VersionActionParameters;
import net.jacksum.actions.io.compare.CompareActionInterface;
import net.jacksum.actions.io.quick.QuickActionParameters;
import net.jacksum.actions.io.strings.HashStringsActionParameters;
import net.jacksum.actions.io.verify.CheckActionParameters;
import net.jacksum.actions.io.verify.CheckConsumerParameters;
import net.jacksum.actions.io.verify.ListFilter;
import net.jacksum.actions.io.wanted.MatchFilter;
import net.jacksum.cli.CLIParameters;
import net.jacksum.cli.ExitCode;
import net.jacksum.cli.Messenger;
import net.jacksum.cli.Verbose;
import net.jacksum.compats.defs.CompatibilityProperties;
import net.jacksum.compats.defs.InvalidCompatibilityPropertiesException;
import net.jacksum.formats.Encoding;
import net.jacksum.formats.EncodingDecoding;
import net.jacksum.formats.FilenameFormatter;
import net.jacksum.formats.TimestampFormatter;
import net.jacksum.multicore.OSControl;
import net.jacksum.multicore.ThreadControl;
import net.jacksum.multicore.manyfiles.ProducerParameters;
import net.jacksum.parameters.base.*;
import net.jacksum.parameters.combined.FormatParameters;
import net.jacksum.parameters.combined.GatheringParameters;
import net.jacksum.parameters.combined.ProducerConsumerParameters;
import net.jacksum.parameters.combined.StatisticsParameters;
import net.loefflmann.sugar.io.BOM;
import net.loefflmann.sugar.io.GeneralIO;
import net.loefflmann.sugar.util.ExitException;
import net.loefflmann.sugar.util.GeneralString;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

import static net.jacksum.cli.CLIParameters.*;
import static net.jacksum.cli.Messenger.MsgType.INFO;
import static net.jacksum.cli.Messenger.MsgType.WARNING;

/**
 * The parameter cluster.
 */
public class Parameters implements
        // for Java serialization
        Serializable,

        // all action parameter interfaces
        ExpectationActionParameters, CheckActionParameters,
        AlgoInfoActionParameters, AppInfoActionParameters, CompatInfoActionParameters,
        VersionActionParameters, CompareActionInterface, HelpActionParameters, QuickActionParameters,
        HashStringsActionParameters, HMACsActionParameters,

        // all other parameter interfaces
        FormatParameters, AlgorithmParameters, CustomizedFormatParameters, StatisticsParameters,
        FileWalkerParameters, ProducerConsumerParameters, PathParameters,
        GatheringParameters, SequenceParameters, KeyParameters, ProducerParameters, CheckConsumerParameters,
        VerboseParameters, CompatibilityParameters, HeaderParameters, StringListParameters, ConsoleParameters {


    /** The algorithm identifier that is used if no algorithm has been specified. */
    public static final String ALGORITHM_IDENTIFIER_DEFAULT = "sha3-256";
    private static final long serialVersionUID = -4396148871815058931L;

    /** The lower case identifier of the algorithm. */
    private String algorithmIdentifier = ALGORITHM_IDENTIFIER_DEFAULT;
    /** The original command line arguments. */
    private String[] cliParameters;
    /** The name of the UTF-8 charset. */
    public static final String UTF_8 = "UTF-8";

    final transient private PrintStream stdOutBackup = System.out;
    final transient private PrintStream stdErrBackup = System.err;


    // -a
    /** The algorithm (-a). */
    private String algorithm = null;
    // -a unknown:
    /** Whether the algorithm should be found (-a unknown:). */
    private boolean findAlgorithm = false;
    // -A
    /** Whether the alternate implementation is wanted (-A). */
    private boolean alternate = false;
    // -c <file>
    /** The check file (-c). */
    private String checkFile = null;
    // --check-line <line>
    /** The line to be checked (--check-line). */
    private String checkLine = null;
    // --check-strict
    /** Whether the check is strict (--check-strict). */
    private boolean checkStrict = false;
    // -C <compatibility>
    /** The ID of the style (-C). */
    private String compatibilityID = null;
    /** The properties of the style (-C). */
    private CompatibilityProperties compatibilityProperties = null;

    // --charset-check-file <charset>
    /** The charset of the check file (--charset-check-file). */
    private String charsetCheckFile = UTF_8;
    // --charset-file-list <charset>
    /** The charset of the file list (--charset-file-list). */
    private String charsetFileList = UTF_8;
    // --charset-error-file <charset>
    /** The charset of the error file (--charset-error-file). */
    private String charsetErrorFile = UTF_8;
    // --charset-output-file <charset>
    /** The charset of the output file (--charset-output-file). */
    private String charsetOutputFile = UTF_8;
    // --charset-wanted-list <charset>
    /** The charset of the wanted list (--charset-wanted-list). */
    private String charsetWantedList = UTF_8;
    // --charset-string-list <list>
    /** The charset of the string list (--charset-string-list). */
    private String charsetStringList = UTF_8;
    // --charset-console <charset>
    /** The charset of the console (--charset-console). */
    private String charsetConsole = UTF_8;
    // --charset-stdout <charset>
    /** The charset of stdout (--charset-stdout). */
    private String charsetStdout = null;
    // --charset-stderr <charset>
    /** The charset of stderr (--charset-stderr). */
    private String charsetStderr = null;
    // -d
    /** Whether symbolic links to directories are not followed (-d). */
    private boolean dontFollowSymlinksToDirectories = false;
    // -e <text>
    /** The expected hash value (-e). */
    private String expected = null;
    /** The expected hash value decoded to bytes, cached by getExpectedBytes(). */
    byte[] expectedAsBytes = null;
    // -E default
    /** The encoding of the hash value (-E). */
    private Encoding encoding = null;
    // -f
    /** Whether symbolic links to files are not followed (-f). */
    private boolean dontFollowSymlinksToFiles = false;
    // -F
    /** The customized output format (-F). */
    private String format = null;
    // -g
    /** The number of characters in a group of the hash value (-g). */
    private int groupcount = 0;
    // -G
    /** The character that separates the groups (-G). */
    private Character groupingChar = null;
    // --exact
    /** Whether an exact match is required (--exact). */
    private boolean exact = false;
    // -h
    /** Whether the help is wanted (-h). */
    private boolean help = false;
    // -h lang
    /** The language of the help (-h lang). */
    private String helpLanguage = null;
    // -h [lang] search
    /** The string to search for in the help (-h [lang] search). */
    private String helpSearchString = null;
    // --hmacs
    /** Whether the list of HMACs is wanted (--hmacs). */
    private boolean HMACsWanted = false;
    // --info
    /** Whether the info mode is enabled (--info). */
    private boolean infoMode = false;
    // -I
    /** The characters that mark comment lines (-I). */
    private String commentChars = null;
    // -l, --list
    /** Whether the list mode is enabled (-l, --list). */
    private boolean list = false;
    // --list-filter
    /** The filter for the list output (--list-filter). */
    private ListFilter listFilter;
    // --list-filter-wanted
    /** The filter for the wanted list output (--list-filter-wanted). */
    private MatchFilter wantedListFilter;
    // --legacy-stdin-name
    /** The name of the standard input stream in the output. */
    private String stdinName = "<stdin>";
    // -L <file>
    /** The name of the file list (-L). */
    private String filelistFilename = null;
    // keeps all the filenames that have been specified by -L
    /** The filenames that have been specified by the file list (-L). */
    private List<String> filenamesFromFilelist = new ArrayList<>();
    // --file-list-format
    /** The format of the file list (--file-list-format). */
    private String filelistFormat = null;
    // -O/-o
    /** The output file (-O/-o). */
    private String outputFile = null;
    // --output-file-replace-tokens
    /** Whether tokens in the output filename are replaced (--output-file-replace-tokens). */
    private boolean outputFileReplaceTokens = false;
    /** The output filename before tokens have been replaced. */
    private String outputFileRaw = null;
    // -U/-u
    /** The error file (-U/-u). */
    private String errorFile = null;
    // -O
    /** Whether an existing output file may be overwritten (-O). */
    private boolean outputFileOverwrite = false;
    // -U
    /** Whether an existing error file may be overwritten (-U). */
    private boolean errorFileOverwrite = false;
    // -P
    /** The path separator character (-P). */
    private Character pathChar = File.separatorChar;
    // -q
    /** The sequence to be hashed (-q). */
    private Sequence sequence = null;
    // -k
    /** The key for the HMAC (-k). */
    private Sequence key = null;
    // -r
    /** Whether directories are processed recursively (-r). */
    private boolean recursive = false;
    // -r <depth>
    /** The maximum depth for recursion (-r). */
    private int depth = Integer.MAX_VALUE;
    // -s
    /** The separator with translated escape sequences (-s). */
    private String separator = null;
    // -t
    /** The timestamp format (-t). */
    private String timestampFormat = null;
    /** Whether the timestamp format has been set by the user. */
    private boolean timestampFormatSetByUser = false;
    // -v
    /** Whether the version is wanted (-v). */
    private boolean versionWanted = false;

    // -V
    /** The verbosity settings (-V). */
    private Verbose verbose;
    // -w
    /** The wanted list (-w). */
    private String wantedList = null;
    // -
    /** Whether the standard input stream is read (-). */
    private boolean stdin = false;
    // --utf8
    /** Whether UTF-8 is used for stdout and stderr (--utf8). */
    private boolean utf8 = false;
    // --line-separator
    /** The line separator (--line-separator). */
    private String lineSeparator = System.lineSeparator();

    // --license
    /** Whether the license is wanted (--license). */
    private boolean licenseWanted = false;

    // --copyright
    /** Whether the copyright is wanted (--copyright). */
    private boolean copyrightWanted = false;

    // --header
    /** Whether a header is written (--header). */
    private boolean headerWanted = false;
    /** Whether --header or --no-header has been specified explicitly. */
    private boolean headerWantedExplicitlySet = false;

    // only settable by a style
    /** The header line written before the regular header, only settable by a style. */
    private String leadingHeader = null;

    // --bom
    /** Whether a byte order mark is written (--bom). */
    private boolean bom = false;

    // --scan-all-unix-file-types
    /** Whether all Unix file types are scanned (--scan-all-unix-file-types). */
    private boolean scanAllUnixFileTypes = false;

    // --scan-ntfs-ads
    /** Whether NTFS alternate data streams are scanned (--scan-ntfs-ads). */
    private boolean scanNtfsAds = false;

    // keeps all the filenames that have been specified at the command line
    /** The filenames that have been specified at the command line. */
    private List<String> filenamesFromArgs;

    // --no-path
    /** Whether paths are omitted (--no-path). */
    private boolean noPath = false;

    // --path-absolute
    /** Whether absolute paths are written (--path-absolute). */
    private boolean pathAbsolute = false;

    // --path-relative-to
    /** The path that output paths are relative to (--path-relative-to). */
    private Path pathRelativeTo = null;
    /** The path that output paths are relative to, as a string. */
    private String pathRelativeToAsString = null;

    // --path-relative-to-entry
    /** The file list entry that sets the relative path (--path-relative-to-entry), 0 if not set. */
    private int pathRelativeToEntry = 0;
    // true if pathRelativeToAsString has been derived from pathRelativeToEntry by resolvePathRelativeTo()
    // and hence is not a path that has been set by the user resp. by the API
    /** Whether pathRelativeToAsString has been derived from pathRelativeToEntry. */
    private boolean pathRelativeToDerivedFromEntry = false;

    // --threads-hashing
    /** The number of hashing threads (--threads-hashing). */
    private int threadsHashing = ThreadControl.getThreadsHashing();
    // true if the value above has been set by the user resp. by the API rather than
    // being the default of ThreadControl; the value itself cannot answer that question,
    // because a user is free to ask for exactly the value that is the default anyway
    /** Whether the number of hashing threads has been set explicitly. */
    private boolean threadsHashingSet = false;

    // --threads-reading
    /** The number of reading threads (--threads-reading). */
    private int threadsReading = ThreadControl.getThreadsReading();
    // true if the value above has been set by the user resp. by the API, see threadsHashingSet
    /** Whether the number of reading threads has been set explicitly. */
    private boolean threadsReadingSet = false;

    /** The messenger for warnings and errors. */
    private final Messenger messenger;

    /** Whether the file size is wanted: 1 yes, 0 no, -1 not set. */
    private int filesizeWanted = -1;

    // --gnu-filename-escaping
    /** Whether GNU filename escaping is enabled (--gnu-filename-escaping). */
    private boolean gnuEscaping = !OSControl.isWindows(); // enabled by default on non-Windows systems
    /** Whether GNU filename escaping has been set by the user. */
    private boolean gnuEscapingSetByUser = false;

    // implicit parameters

    // keeps all the filenames that have been specified by -c
    /** The filenames that have been specified by the check file (-c). */
    private List<String> filenamesFromCheckFile = null;

    /** Whether this object has been modified by the API. */
    private boolean parameterModifiedByAPI = false;

    /** The string list (--string-list). */
    private String stringList = null;

    /** Whether empty lines are ignored. */
    private boolean ignoreEmptyLines = false;

    /** Whether hash values are ignored. */
    private boolean ignoreHashes = false;

    /** Whether file sizes are ignored. */
    private boolean ignoreSizes = false;

    /** Whether timestamps are ignored. */
    private boolean ignoreTimestamps = false;


    // ************************************** constructors *********************************************************

    /**
     * Parameterless Parameters Constructor.
     */
    public Parameters() {
        this.filenamesFromArgs = new ArrayList<>();
        verbose = new Verbose();
        messenger = new Messenger(verbose);
        listFilter = new ListFilter();
        wantedListFilter = new MatchFilter();
    }

    /**
     * Parameters Constructor. The arguments are parsed the way the command line
     * interface parses them, but they are not checked for combinations that make no
     * sense, call {@link #checked()} for that.
     *
     * @param args all arguments
     * @throws ParameterException if a parameter error occurs
     */
    public Parameters(String[] args) throws ParameterException {
        this();
        new CLIParameters(args).parse(this);
    }

    // ************************************** public methods *********************************************************

    /**
     * Return Parameters, but checked
     *
     * @return Parameters, but checked.
     * @throws ParameterException if parameter combinations are invalid
     * @throws ExitException      if an exit should happen.
     */
    public Parameters checked() throws ParameterException, ExitException {
        checkParameters(true);
        return this;
    }

    /**
     * Checks the parameters and returns this object.
     *
     * @param setupStreams true if the standard output and error streams should be set up as well
     * @return this Parameters object, but checked
     * @throws ParameterException if parameter combinations are invalid or if a parameter error occurs
     * @throws ExitException if an exit should happen
     */
    public Parameters checked(boolean setupStreams) throws ParameterException, ExitException {
        checkParameters(setupStreams);
        return this;
    }


    /**
     * Returns this object without checking the parameters.
     *
     * @return this Parameters object, unchecked
     */
    public Parameters unchecked() {
        return this;
    }

    /**
     * Sets up the standard output and error streams according to the charset options.
     *
     * @throws ParameterException if a charset is not supported
     * @throws ExitException if an exit should happen
     */
    public void setupStreams() throws ParameterException, ExitException {
        handleCharsetsAndSetupStreams();
    }

    /**
     * Returns the action type dependent on the parameters.
     *
     * @return the action type dependent on the parameters
     */
    public ActionType getActionType() {

        if (isVersionWanted()) {
            return ActionType.VERSION;

        } else if (isHelp()) {
            return ActionType.HELP;

        } else if (isLicenseWanted()) {
            return ActionType.LICENSE;

        } else if (isCopyrightWanted()) {
            return ActionType.COPYRIGHT;

        } else if (isHMACsWanted()) {
            return ActionType.HMACS;

        } else if (getCheckFile() != null || getCheckLine() != null) {
            return ActionType.CHECK;

        } else if (findAlgorithm) {
            return ActionType.FIND_ALGO;

        } else if (isSequence() && !isInfoMode()) {
            return ActionType.QUICK;

        } else if (isStringList()) {
            return ActionType.STRING_LIST;

        // must be the first check if isInfoMode is involved, because
        // the --compat option sets the algorithm implicitly
        } else if (isInfoMode() && getCompatibilityID() != null) {
            return ActionType.INFO_COMPAT;

        } else if (isInfoMode() && algorithm != null) {
            return ActionType.INFO_ALGO;

        } else if (isInfoMode() && algorithm == null) {
            return ActionType.INFO_APP;

        } else if (!getFilenamesFromArgs().isEmpty()
                || !getFilenamesFromFilelist().isEmpty()
                || stdin) {
            if (isWantedList() || isExpectation()) {
                return ActionType.WANTED_LIST;
            } else {
                return ActionType.HASH_FILES;
            }
        // must be the last check, because isList() can be enabled
        // by isWantedList() or by isExpectation()
        } else if (isList() && algorithm != null) {
            return ActionType.INFO_ALGO;

        } else { // default
            return ActionType.HELP;
        }
    }

    /**
     * Returns the format of the file list (option --file-list-format).
     *
     * @return the filelistFormat
     */
    public String getFilelistFormat() {
        return filelistFormat;
    }

    /**
     * Sets the format of the file list (option --file-list-format).
     *
     * @param filelistFormat the filelistFormat to set
     */
    public void setFilelistFormat(String filelistFormat) {
        this.filelistFormat = filelistFormat;
    }


    /**
     * Sets the original command line arguments.
     *
     * @param args the command line arguments
     */
    public void setCLIParameters(String[] args) {
        this.cliParameters = args;
    }

    @Override
    public String[] getCLIParameters() {
        return cliParameters;
    }

    /**
     * Sets whether this object has been modified by the API, in which case the command line arguments are rebuilt from the current values.
     *
     * @param parameterModifiedByAPI true if this object has been modified by the API
     */
    public void setParameterModifiedByAPI(boolean parameterModifiedByAPI) {
        this.parameterModifiedByAPI = parameterModifiedByAPI;
    }

    public String[] getCLIParametersWithQuotes() {
        List<String> list = new ArrayList<>();
        // if the parameter object has been modified by the API (e.g. by an GUI),
        // the original args passed to the app aren't valid anymore, we need to
        // build the args by the values of the current parameter object values.
        String[] source = parameterModifiedByAPI ? this.toStringArrayList().toArray(new String[0]) : cliParameters;
        String previous = null;
        for (String param : source) {
            // the key of an HMAC is a secret, it must not end up in the header, so it is
            // replaced by "password", which is how a key entered at the console is shown,
            // and which makes Jacksum ask for the key if the line is run again;
            // the name of a key file is not a secret and is kept
            if ((_KEY.equals(previous) || __KEY.equals(previous))
                    && !param.toLowerCase(Locale.US).startsWith(Sequence.Type.FILE.getCode() + ":")) {
                param = Sequence.Type.PASSWORD.getCode();
            }
            previous = param;
            // the hash-sign acts as a comment on many GNU/Linux shells, it needs to be quoted
            if (param.contains(" ") || param.startsWith("#")) {
                list.add(String.format("\"%s\"", param));
            } else {
                list.add(param);
            }
        }
        return list.toArray(new String[0]);
    }


    @Override
    public boolean isHelpSearchString() {
        return (helpSearchString != null);
    }

    @Override
    public List<String> getFilenamesFromArgs() {
        return filenamesFromArgs;
    }

    /**
     * Sets the filenames that have been specified at the command line.
     *
     * @param filenamesFromArgs the filenames
     */
    public void setFilenamesFromArgs(List<String> filenamesFromArgs) {
        this.filenamesFromArgs = filenamesFromArgs;
    }

    /**
     * Sets the characters that mark comment lines (option -I).
     *
     * @param commentChars the characters that mark comment lines
     */
    public void setCommentChars(String commentChars) {
        this.commentChars = commentChars;
    }

    public String getCommentChars() {
        return commentChars;
    }

    /**
     * Sets the verbosity settings.
     *
     * @param verbose the verbosity settings
     */
    public void setVerbose(Verbose verbose) {
        this.verbose = verbose;
    }

    @Override
    public Verbose getVerbose() {
        return verbose;
    }

    // -s
    @Override
    public String getSeparator() {
        return separator;
    }

    /** The separator as it has been specified (-s). */
    private String separatorRaw = null;

    private String getSeparatorRaw() {
        return separatorRaw;
    }

    /**
     * Sets the separator (option -s); escape sequences are translated.
     *
     * @param separator the separator, may contain escape sequences
     */
    public void setSeparator(String separator) {
        this.separatorRaw = separator;
        this.separator = net.loefflmann.sugar.util.GeneralString.translateEscapeSequences(separator);
    }

    @Override
    public boolean isSeparatorSet() {
        return separator != null;
    }

    // -f
    @Override
    public boolean isDontFollowSymlinksToFiles() {
        return dontFollowSymlinksToFiles;
    }

    // -f
    /**
     * Sets whether symbolic links to files are not followed (option -f).
     *
     * @param dontFollowSymlinksToFiles true if symbolic links to files should not be followed
     */
    public void setDontFollowSymlinksToFiles(boolean dontFollowSymlinksToFiles) {
        this.dontFollowSymlinksToFiles = dontFollowSymlinksToFiles;
    }

    // -r
    @Override
    public boolean isRecursive() {
        return recursive;
    }

    // -r
    /**
     * Sets whether directories are processed recursively (option -r).
     *
     * @param recursive true if directories should be processed recursively
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    @Override
    public boolean isStdinForFilenamesFromArgs() {
        return stdin;
    }

    /**
     * Sets whether the standard input stream is read (filename -).
     *
     * @param stdin true if the standard input stream should be read
     */
    public void setStdinForFilenamesFromArgs(boolean stdin) {
        this.stdin = stdin;
    }

    // -x, -X, -E <encoding>
    @Override
    public Encoding getEncoding() {
        return encoding;
    }

    // -x, -X, -E <encoding>
    /**
     * Sets the encoding of the hash value (options -x, -X, -E).
     *
     * @param encoding the encoding
     */
    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets the encoding of the hash value by its name (option -E).
     *
     * @param encoding the name of the encoding
     * @throws IllegalArgumentException if the encoding is not supported
     */
    public void setEncoding(String encoding) throws IllegalArgumentException {
        this.encoding = Encoding.string2Encoding(encoding);
    }

    @Override
    public boolean isEncodingSet() {
        return encoding != null;
    }

    // -q
    public Sequence getSequence() {
        return sequence;
    }

    /**
     * Sets the sequence to be hashed (option -q).
     *
     * @param string the sequence string
     */
    public void setSequence(String string) {
        sequence = new Sequence(string);
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    /**
     * Sets the key for the HMAC (option -k).
     *
     * @param key the key as a sequence string
     */
    public void setKey(String key) {
        this.key = new Sequence(key);
    }

    public void setKey(Sequence key) { this.key = key; }

    public boolean isKey() { return key != null; }

    public Sequence getKey() { return key; }

    public boolean isSequence() {
        return sequence != null;
    }

    // -g
    @Override
    public int getGrouping() {
        return groupcount;
    }

    // -g
    /**
     * Sets the number of characters in a group of the hash value (option -g).
     *
     * @param grouping the number of characters in a group, 0 disables grouping
     * @throws IllegalArgumentException if the value is negative
     */
    public void setGrouping(int grouping) throws IllegalArgumentException {
        if (grouping < 0) {
            throw new IllegalArgumentException("Grouping is out of range. A value >= 0 is expected.");
        }
        this.groupcount = grouping;
    }

    /**
     * Sets the number of characters in a group of the hash value (option -g).
     *
     * @param grouping the number of characters in a group as a decimal number
     * @throws IllegalArgumentException if the value is not a decimal number or if it is negative
     */
    public void setGrouping(String grouping) throws IllegalArgumentException {
        try {
            setGrouping(Integer.parseInt(grouping));
        } catch (NumberFormatException e) {
            // only a value that is not a number at all ends up here; a value that is a
            // number but out of range is rejected by setGrouping(int) with its own message
            throw new IllegalArgumentException(String.format("%s is not a decimal number.", grouping));
        }
    }

    @Override
    public boolean isGroupingSet() {
        return groupcount > 0;
    }

    /**
     * Sets the character that separates the groups of the hash value (option -G).
     *
     * @param string a string that consists of exactly one character
     * @throws IllegalArgumentException if the string does not consist of exactly one character
     */
    public void setGroupChar(String string) throws IllegalArgumentException {
        if (string.length() != 1) {
            throw new IllegalArgumentException(String.format(
                    "Exactly one character is required, but \"%s\" has been given.", string));
        } else {
            setGroupChar(string.charAt(0));
        }
    }

    /**
     * Sets the character that separates the groups of the hash value (option -G).
     *
     * @param c the group character
     */
    public void setGroupChar(char c) {
        groupingChar = c;
    }

    @Override
    public Character getGroupChar() {
        return groupingChar;
    }

    @Override
    public boolean isGroupCharSet() {
        return groupingChar != null;
    }

    @Override
    public boolean isPathCharSet() {
        return !pathChar.equals(File.separatorChar);
    }

    @Override
    public boolean isNoPath() {
        return noPath;
    }

    /**
     * Sets the path separator character (option -P).
     *
     * @param arg a string that consists of exactly one character, either / or \
     * @throws IllegalArgumentException if the string does not consist of exactly one character, or if the character is neither / nor \
     */
    public void setPathChar(String arg) throws IllegalArgumentException {
        if (arg.length() != 1) {
            throw new IllegalArgumentException("Exactly one character is required.");
        } else {
            setPathChar(arg.charAt(0));
        }
    }

    /**
     * Sets the path separator character (option -P).
     *
     * @param arg the path separator, either / or \
     * @throws IllegalArgumentException if the character is neither / nor \
     */
    public void setPathChar(Character arg) throws IllegalArgumentException {
        if (arg == '/' || arg == '\\') {
            pathChar = arg;
        } else {
            throw new IllegalArgumentException("Option -P requires \"/\" or \"\\\".");
        }
    }

    @Override
    public Character getPathChar() {
        return pathChar;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public boolean isFormatWanted() {
        return format != null;
    }

    /**
     * Sets the customized output format (option -F).
     *
     * @param format the format
     */
    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String getCheckFile() {
        return checkFile;
    }

    /**
     * Sets the check file (option -c).
     *
     * @param checkFile the name of the check file
     */
    public void setCheckFile(String checkFile) {
        this.checkFile = checkFile;
    }

    @Override
    public String getExpectedString() {
        return expected;
    }

    @Override
    public byte[] getExpectedBytes() throws UnsupportedOperationException {
        if (expectedAsBytes == null) {
            if (isEncodingSet() && getEncoding().equals(Encoding.HEX) && !isGroupingSet()) {
                expectedAsBytes = EncodingDecoding.sequence2bytes(Sequence.Type.HEX, expected);
            } else if (isEncodingSet() && getEncoding().equals(Encoding.BUBBLEBABBLE)) {
                // grouping is not supported by the BubbleBabble encoding, so it can be ignored here
                try {
                    expectedAsBytes = EncodingDecoding.sequence2bytes(Sequence.Type.BUBBLEBABBLE, expected);
                } catch (IllegalArgumentException iae) {
                    // the expected value is not a valid BubbleBabble string, let the caller
                    // fall back to the comparison of the strings which returns NO MATCH
                    throw new UnsupportedOperationException(iae.getMessage());
                }
            } else {
                throw new UnsupportedOperationException();
                // TODO: transform Hex with grouping chars to bytes
                // TODO: transform dec, oct, base32, base64, etc. to bytes
            }
        }
        return expectedAsBytes;
    }

    /**
     * Sets the expected hash value (option -e) and discards any previously decoded expected bytes.
     *
     * @param expected the expected hash value
     */
    public void setExpected(String expected) {
        this.expected = expected;
        // the decoded value belongs to the old expectation, see getExpectedBytes()
        this.expectedAsBytes = null;
    }

    @Override
    public boolean isExpectation() {
        return getExpectedString() != null;
    }

    @Override
    public String getTimestampFormat() {
        return timestampFormat;
    }

    /**
     * Sets the timestamp format (option -t); a non-null value marks it as set by the user.
     *
     * @param timestampFormat the timestamp format, or null to discard it
     */
    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
        // null discards the format and with it the fact that the user has set it
        this.timestampFormatSetByUser = timestampFormat != null;
    }

    /**
     * Determines whether the timestamp format has been set by the user (option -t) rather than by a
     * style (option -C/--style), because a style sets the field directly, see also
     * handleCompatibility().
     *
     * @return true if the timestamp format has been set by the user.
     */
    public boolean isTimestampFormatSetByUser() {
        return timestampFormatSetByUser;
    }

    @Override
    public boolean isTimestampWanted() {
        return timestampFormat != null;
    }

    @Override
    public String getHelpLanguage() {
        return helpLanguage;
    }

    @Override
    public boolean isHelpLanguage() {
        return (helpLanguage != null);
    }

    /**
     * Sets the language of the help (option -h [lang]).
     *
     * @param helpLanguage the language of the help
     */
    public void setHelpLanguage(String helpLanguage) {
        this.helpLanguage = helpLanguage;
    }

    @Override
    public String getHelpSearchString() {
        return helpSearchString;
    }

    /**
     * Sets the string to search for in the help (option -h [lang] search).
     *
     * @param helpSearchString the search string
     */
    public void setHelpSearchString(String helpSearchString) {
        this.helpSearchString = helpSearchString;
    }

    @Override
    public boolean isAlternateImplementationWanted() {
        return alternate;
    }

    /**
     * Sets whether the alternate implementation of the algorithm is wanted (option -A).
     *
     * @param alternate true if the alternate implementation is wanted
     */
    public void setAlternateImplementationWanted(boolean alternate) {
        this.alternate = alternate;
    }

    @Override
    public String getAlgorithmIdentifier() {
        return algorithmIdentifier;
    }

    /**
     * Determines whether the algorithm has been set by the user (option -a).
     *
     * @return true if the algorithm has been set
     */
    public boolean isAlgorithmSetByUser() {
        return algorithm != null;
    }

    /**
     * Returns the algorithm as it has been specified (option -a).
     *
     * @return the algorithm, or null if it has not been set
     */
    public String getAlgorithm() {
        return algorithm;
    }


    /**
     * Sets the algorithm (option -a). A null value selects the default algorithm, and an algorithm starting with {@code unknown:} enables finding the algorithm and all verbose output.
     *
     * @param algorithm the algorithm, or null
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        if (algorithm == null) {
            algorithmIdentifier = ALGORITHM_IDENTIFIER_DEFAULT;
        } else {
            this.algorithmIdentifier = algorithm.toLowerCase(Locale.US);
        }

        if (algorithmIdentifier.startsWith("unknown:")) {
            findAlgorithm = true;
            verbose.enableAll();
        }
    }

    @Override
    public boolean isList() {
        return list;
    }

    /**
     * Sets whether the list mode is enabled (option -l resp. --list).
     *
     * @param list true if the list mode should be enabled
     */
    public void setList(boolean list) {
        this.list = list;
    }

    @Override
    public boolean isDontFollowSymlinksToDirectories() {
        return dontFollowSymlinksToDirectories;
    }

    /**
     * Sets whether symbolic links to directories are not followed (option -d).
     *
     * @param dontFollowSymlinksToDirectories true if symbolic links to directories should not be followed
     */
    public void setDontFollowSymlinksToDirectories(boolean dontFollowSymlinksToDirectories) {
        this.dontFollowSymlinksToDirectories = dontFollowSymlinksToDirectories;
    }

    public String getOutputFile() {
        return outputFile;
    }

    /**
     * Sets the output file (option -o resp. -O).
     *
     * @param outputFile the name of the output file
     */
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public String getErrorFile() {
        return errorFile;
    }

    @Override
    public boolean scanAllUnixFileTypes() {
        return scanAllUnixFileTypes;
    }

    @Override
    public boolean isScanNtfsAds() {
        return scanNtfsAds;
    }

    /**
     * Sets whether NTFS alternate data streams are scanned (option --scan-ntfs-ads).
     *
     * @param scanNtfsAds true if NTFS alternate data streams should be scanned
     */
    public void setScanNtfsAds(boolean scanNtfsAds) {
        this.scanNtfsAds = scanNtfsAds;
    }

    /**
     * Sets whether all Unix file types are scanned (option --scan-all-unix-file-types).
     *
     * @param scanAllUnixFileTypes true if all Unix file types should be scanned
     */
    public void setScanAllUnixFileTypes(boolean scanAllUnixFileTypes) {
        this.scanAllUnixFileTypes = scanAllUnixFileTypes;
    }

    /**
     * Sets the error file (option -u resp. -U).
     *
     * @param errorFile the name of the error file
     */
    public void setErrorFile(String errorFile) {
        this.errorFile = errorFile;
    }

    /**
     * Sets the maximum depth for the recursive processing of directories (option -r).
     *
     * @param depth the maximum depth
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Sets the line separator (option --line-separator).
     *
     * @param lineSeparator the line separator
     */
    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    /**
     * Returns whether a byte order mark is written (option --bom).
     *
     * @return true if a byte order mark is written
     */
    public boolean isBom() {
        return bom;
    }

    /**
     * Sets whether a byte order mark is written (option --bom).
     *
     * @param bom true if a byte order mark should be written
     */
    public void setBom(boolean bom) {
        this.bom = bom;
    }

    public boolean isHeaderWanted() {
        return headerWanted;
    }

    /**
     * Sets whether a header is written (option --header).
     *
     * @param headerWanted true if a header should be written
     */
    public void setHeaderWanted(boolean headerWanted) {
        this.headerWanted = headerWanted;
    }

    // by default the flag headerWantedExplicitlySet is false,
    // it becomes true if the user explicitly set --header or --no-header
    /**
     * Sets whether the user has specified --header or --no-header explicitly.
     *
     * @param headerWantedExplicitlySet true if --header or --no-header has been specified explicitly
     */
    public void setHeaderWantedExplicitlySet(boolean headerWantedExplicitlySet) {
        this.headerWantedExplicitlySet = headerWantedExplicitlySet;
    }

    // did the user specify explicitly --header or --no-header ?
    /**
     * Determines whether the user has specified --header or --no-header explicitly.
     *
     * @return true if --header or --no-header has been specified explicitly
     */
    public boolean isHeaderWantedExplicitlySet() {
        return headerWantedExplicitlySet;
    }

    public boolean isPathAbsolute() {
        return pathAbsolute;
    }

    /**
     * Sets whether absolute paths are written (option --path-absolute).
     *
     * @param pathAbsolute true if absolute paths should be written
     */
    public void setPathAbsolute(boolean pathAbsolute) {
        this.pathAbsolute = pathAbsolute;
    }

    /**
     * Sets whether paths are omitted in the output (option --no-path).
     *
     * @param noPath true if paths should be omitted
     */
    public void setNoPath(boolean noPath) {
        this.noPath = noPath;
    }

    /**
     * Returns the path that the output paths are relative to (option --path-relative-to).
     *
     * @return the path as a string, or null if it has not been set
     */
    public String getPathRelativeToAsString() {
        return pathRelativeToAsString;
    }

    /**
     * Sets the path that the output paths are relative to (option --path-relative-to); the path is not regarded as derived from a file list entry.
     *
     * @param pathRelativeToAsString the path as a string
     */
    public void setPathRelativeToAsString(String pathRelativeToAsString) {
        this.pathRelativeToAsString = pathRelativeToAsString;
        // a path that is set explicitly is not a derived one
        this.pathRelativeToDerivedFromEntry = false;
    }

    public Path getPathRelativeTo() {
        return pathRelativeTo;
    }

    /**
     * Sets the path that the output paths are relative to.
     *
     * @param pathRelativeTo the path
     */
    public void setPathRelativeTo(Path pathRelativeTo) {
        this.pathRelativeTo = pathRelativeTo;
    }

    public int getThreadsHashing() {
        return threadsHashing;
    }

    /**
     * Returns whether the number of hashing threads has been set explicitly.
     *
     * @return true if the number of hashing threads has been set by the user resp. by the API
     */
    public boolean isThreadsHashingSet() {
        return threadsHashingSet;
    }

    /**
     * Sets the number of hashing threads for this run.
     *
     * @param threadsHashing the number of threads
     * @throws IllegalArgumentException if the value is not in the range that
     * {@link ThreadControl#getThreadsMin()} and {@link ThreadControl#getThreadsLimit()} describe
     */
    public void setThreadsHashing(int threadsHashing) {
        ThreadControl.checkRange(threadsHashing);
        this.threadsHashingSet = true;
        // Deliberately no write back to ThreadControl: the static field is the default
        // for a new Parameters object only, this object is the authority for this run,
        // and the classes that need the value get it from here.
        this.threadsHashing = threadsHashing;
    }

    public int getThreadsReading() {
        return threadsReading;
    }

    /**
     * Returns whether the number of reading threads has been set explicitly.
     *
     * @return true if the number of reading threads has been set by the user resp. by the API
     */
    public boolean isThreadsReadingSet() {
        return threadsReadingSet;
    }

    /**
     * Sets the number of reading threads for this run.
     *
     * @param threadsReading the number of threads
     * @throws IllegalArgumentException if the value is not in the range that
     * {@link ThreadControl#getThreadsMin()} and {@link ThreadControl#getThreadsLimit()} describe
     */
    public void setThreadsReading(int threadsReading) {
        ThreadControl.checkRange(threadsReading);
        this.threadsReadingSet = true;
        // Deliberately no write back to ThreadControl, see setThreadsHashing().
        this.threadsReading = threadsReading;
    }

    /**
     * Returns the number of the file list entry that sets the path the output paths are relative to (option --path-relative-to-entry).
     *
     * @return the number of the entry, starting with 1, or 0 if it has not been set
     */
    public int getPathRelativeToEntry() {
        return pathRelativeToEntry;
    }

    /**
     * Determines whether a file list entry sets the path the output paths are relative to (option --path-relative-to-entry).
     *
     * @return true if an entry number greater than 0 has been set
     */
    public boolean isPathRelativeToEntry() {
        return pathRelativeToEntry > 0;
    }

    /**
     * Sets the number of the file list entry that sets the path the output paths are relative to (option --path-relative-to-entry).
     *
     * @param number the number of the entry, starting with 1, or 0 to unset it
     */
    public void setPathRelativeToEntry(int number) {
        this.pathRelativeToEntry = number;
        if (number == 0) {
            // without an entry a path that has been derived from it stands on its own
            this.pathRelativeToDerivedFromEntry = false;
        }
    }


    /**
     * Sets whether the file size is wanted (option --filesize).
     *
     * @param filesizeWanted 1 if the file size is wanted, 0 if it is not wanted, -1 if it has not been set
     */
    public void setFilesizeWanted(int filesizeWanted) {
        this.filesizeWanted = filesizeWanted;
    }

    /**
     * Returns whether the file size is wanted (option --filesize).
     *
     * @return 1 if the file size is wanted, 0 if it is not wanted, -1 if it has not been set
     */
    public int getFilesizeWanted() {
        return filesizeWanted;
    }

    /**
     * Sets whether the file size is wanted (option --filesize).
     *
     * @param filesizeWanted true if the file size is wanted
     */
    public void setFilesizeWanted(boolean filesizeWanted) {
        this.filesizeWanted = filesizeWanted ? 1 : 0;
    }

    /**
     * Resets the file size setting to not set.
     */
    public void unsetFilesizeWanted() {
        this.filesizeWanted = -1;
    }

    public boolean isFilesizeWantedSet() {
        return filesizeWanted != -1;
    }

    public boolean isFilesizeWanted() {
        return filesizeWanted == 1;
    }

    public boolean isGnuEscaping() {
        return gnuEscaping;
    }

    public boolean isGnuEscapingSetByUser() {
        return gnuEscapingSetByUser;
    }

    /**
     * Sets whether GNU filename escaping is enabled (option --gnu-filename-escaping) and marks it as set by the user.
     *
     * @param gnuEscaping true if GNU filename escaping should be enabled
     */
    public void setGnuEscaping(boolean gnuEscaping) {
        this.gnuEscaping = gnuEscaping;
        this.gnuEscapingSetByUser = true;
    }

    /**
     * Resets the GNU filename escaping to the platform default (enabled on non-Windows systems) and marks it as not set by the user.
     */
    public void setGnuEscapingToDefault() {
        this.gnuEscaping = !OSControl.isWindows();
        this.gnuEscapingSetByUser = false;
    }

    /**
     * Returns the wanted list (option -w resp. --wanted-list).
     *
     * @return the wanted list, or null if it has not been set
     */
    public String getWantedList() {
        return wantedList;
    }

    /**
     * Determines whether a wanted list has been set (option -w resp. --wanted-list).
     *
     * @return true if a wanted list has been set
     */
    public boolean isWantedList() {
        return wantedList != null;
    }

    /**
     * Sets the wanted list (option -w resp. --wanted-list).
     *
     * @param wantedList the wanted list
     */
    public void setWantedList(String wantedList) {
        this.wantedList = wantedList;
    }

    /**
     * Returns the charset of the wanted list (option --charset-wanted-list).
     *
     * @return the charset of the wanted list
     */
    public String getCharsetWantedList() {
        return charsetWantedList;
    }

    /**
     * Sets the charset of the wanted list (option --charset-wanted-list).
     *
     * @param charsetWantedList the charset of the wanted list
     */
    public void setCharsetWantedList(String charsetWantedList) {
        this.charsetWantedList = charsetWantedList;
    }

    /**
     * Returns the filter for the wanted list output (option --list-filter-wanted).
     *
     * @return the filter for the wanted list output
     */
    public MatchFilter getWantedListFilter() {
        return wantedListFilter;
    }

    /**
     * Sets the filter for the wanted list output (option --list-filter-wanted).
     *
     * @param wantedListFilter the filter for the wanted list output
     */
    public void setWantedListFilter(MatchFilter wantedListFilter) {
        this.wantedListFilter = wantedListFilter;
    }

    public String getStringList() {
        return stringList;
    }

    public void setStringList(String stringList) {
        this.stringList = stringList;
    }

    public boolean isStringList() {
        return stringList != null;
    }

    public String getCharsetStringList() {
        return charsetStringList;
    }

    /**
     * Sets the charset of the string list (option --charset-string-list).
     *
     * @param charsetStringList the charset of the string list
     */
    public void setCharsetStringList(String charsetStringList) {
        this.charsetStringList = charsetStringList;
    }

    public boolean isIgnoreEmptyLines() {
        return ignoreEmptyLines;
    }

    /**
     * Sets whether empty lines are ignored (option --ignore-empty-lines).
     *
     * @param ignoreEmptyLines true if empty lines should be ignored
     */
    public void setIgnoreEmptyLines(boolean ignoreEmptyLines) {
        this.ignoreEmptyLines = ignoreEmptyLines;
    }

    public boolean isIgnoreHashes() { return ignoreHashes; }

    /**
     * Sets whether hash values are ignored.
     *
     * @param ignoreHashes true if hash values should be ignored
     */
    public void setIgnoreHashes(boolean ignoreHashes) { this.ignoreHashes = ignoreHashes; }

    public boolean isIgnoreSizes() { return ignoreSizes; }

    /**
     * Sets whether file sizes are ignored.
     *
     * @param ignoreSizes true if file sizes should be ignored
     */
    public void setIgnoreSizes(boolean ignoreSizes) { this.ignoreSizes = ignoreSizes; }

    public boolean isIgnoreTimestamps() { return ignoreTimestamps; }

    /**
     * Sets whether timestamps are ignored.
     *
     * @param ignoreTimestamps true if timestamps should be ignored
     */
    public void setIgnoreTimestamps(boolean ignoreTimestamps) { this.ignoreTimestamps = ignoreTimestamps; }


    public String getCharsetConsole() {
        return charsetConsole;
    }

    public void setCharsetConsole(String charsetConsole) {
        this.charsetConsole = charsetConsole;
    }


    public boolean isOutputFile() {
        return outputFile != null;
    }

    /**
     * Returns whether an existing output file may be overwritten (option -O).
     *
     * @return true if an existing output file may be overwritten
     */
    public boolean isOutputFileOverwrite() {
        return outputFileOverwrite;
    }

    public boolean isErrorFile() {
        return errorFile != null;
    }

    /**
     * Returns whether an existing error file may be overwritten (option -U).
     *
     * @return true if an existing error file may be overwritten
     */
    public boolean isErrorFileOverwrite() {
        return errorFileOverwrite;
    }

    /**
     * Returns whether the algorithm should be found for a given hash value (option -a unknown:...).
     *
     * @return true if the algorithm should be found
     */
    public boolean isFindAlgorithm() {
        return findAlgorithm;
    }

    /**
     * Returns the name of the file list (option -L resp. --file-list).
     *
     * @return the name of the file list, or null if it has not been set
     */
    public String getFilelistFilename() {
        return filelistFilename;
    }

    /**
     * Sets the name of the file list (option -L resp. --file-list).
     *
     * @param filelistFilename the filelistFilename to set
     */
    public void setFilelistFilename(String filelistFilename) {
        this.filelistFilename = filelistFilename;
    }

    /**
     * @return the filelistByFile
     */
    @Override
    public List<String> getFilenamesFromFilelist() {
        return filenamesFromFilelist;
    }

    /**
     * Sets the filenames that have been read from the file list (option -L resp. --file-list).
     *
     * @param filenamesFromFilelist the filelistByFile to set
     */
    public void setFilenamesFromFilelist(List<String> filenamesFromFilelist) {
        this.filenamesFromFilelist = filenamesFromFilelist;
    }

    /**
     * @return the compatibilityID
     */
    @Override
    public String getCompatibilityID() {
        return compatibilityID;
    }

    /**
     * Sets the ID of the style resp. compatibility (option -C resp. --style).
     *
     * @param compatibilityID the compatibilityID to set
     */
    public void setCompatibilityID(String compatibilityID) {
        this.compatibilityID = compatibilityID;
    }

    /**
     * @return the charsetCheckFile
     */
    @Override
    public String getCharsetCheckFile() {
        return charsetCheckFile;
    }

    /**
     * Sets the charset of the check file (option --charset-check-file).
     *
     * @param charsetCheckFile the charsetCheckFile to set
     */
    public void setCharsetCheckFile(String charsetCheckFile) {
        this.charsetCheckFile = charsetCheckFile;
    }

    /**
     * Returns the charset of the file list (option --charset-file-list).
     *
     * @return the charsetListFile
     */
    public String getCharsetFileList() {
        return charsetFileList;
    }

    /**
     * Sets the charset of the file list (option --charset-file-list).
     *
     * @param charsetFileList the charsetListFile to set
     */
    public void setCharsetFileList(String charsetFileList) {
        this.charsetFileList = charsetFileList;
    }

    /**
     * @return the filelistByCheck
     */
    @Override
    public List<String> getFilenamesFromCheckFile() {
        return filenamesFromCheckFile;
    }

    /**
     * @param filenamesFromCheckFile the filelistByCheck to set
     */
    public void setFilenamesFromCheckFile(List<String> filenamesFromCheckFile) {
        this.filenamesFromCheckFile = filenamesFromCheckFile;
    }

    /**
     * Returns the charset of the error file (option --charset-error-file).
     *
     * @return the charsetErrorFile
     */
    public String getCharsetErrorFile() {
        return charsetErrorFile;
    }

    /**
     * Sets the charset of the error file (option --charset-error-file).
     *
     * @param charsetErrorFile the charsetErrorFile to set
     */
    public void setCharsetErrorFile(String charsetErrorFile) {
        this.charsetErrorFile = charsetErrorFile;
    }

    /**
     * Returns the charset of the output file (option --charset-output-file).
     *
     * @return the charsetOutputFile
     */
    public String getCharsetOutputFile() {
        return charsetOutputFile;
    }

    /**
     * Sets the charset of the output file (option --charset-output-file).
     *
     * @param charsetOutputFile the charsetOutputFile to set
     */
    public void setCharsetOutputFile(String charsetOutputFile) {
        this.charsetOutputFile = charsetOutputFile;
    }

    /**
     * Returns the charset of the standard output stream (option --charset-stdout).
     *
     * @return the charsetStdout
     */
    public String getCharsetStdout() {
        return charsetStdout;
    }

    /**
     * Sets the charset of the standard output stream (option --charset-stdout).
     *
     * @param charsetStdout the charsetStdout to set
     */
    public void setCharsetStdout(String charsetStdout) {
        this.charsetStdout = charsetStdout;
    }

    /**
     * Returns the charset of the standard error stream (option --charset-stderr).
     *
     * @return the charsetStderr
     */
    public String getCharsetStderr() {
        return charsetStderr;
    }

    /**
     * Sets the charset of the standard error stream (option --charset-stderr).
     *
     * @param charsetStderr the charsetStderr to set
     */
    public void setCharsetStderr(String charsetStderr) {
        this.charsetStderr = charsetStderr;
    }


    /**
     * Returns whether the version is wanted (option -v).
     *
     * @return the versionWanted
     */
    public boolean isVersionWanted() {
        return versionWanted;
    }

    /**
     * Sets whether the version is wanted (option -v).
     *
     * @param versionWanted the versionWanted to set
     */
    public void setVersionWanted(boolean versionWanted) {
        this.versionWanted = versionWanted;
    }

    /**
     * Returns whether the list of supported HMACs is wanted (option --hmacs).
     *
     * @return true if the list of supported HMACs is wanted
     */
    public boolean isHMACsWanted() {
        return HMACsWanted;
    }

    /**
     * Sets whether the list of supported HMACs is wanted (option --hmacs).
     *
     * @param HMACsWanted true if the list of supported HMACs is wanted
     */
    public void setHMACsWanted(boolean HMACsWanted) {
        this.HMACsWanted = HMACsWanted;
    }

    /**
     * Sets whether an existing error file may be overwritten (option -U).
     *
     * @param errorFileOverwrite the errorFileOverwrite to set
     */
    public void setErrorFileOverwrite(boolean errorFileOverwrite) {
        this.errorFileOverwrite = errorFileOverwrite;
    }

    /**
     * Sets whether an existing output file may be overwritten (option -O).
     *
     * @param outputFileOverwrite the outputFileOverwrite to set
     */
    public void setOutputFileOverwrite(boolean outputFileOverwrite) {
        this.outputFileOverwrite = outputFileOverwrite;
    }

    /**
     * Returns whether UTF-8 is used for both stdout and stderr (option --utf8).
     *
     * @return the utf8
     */
    public boolean isUtf8() {
        return utf8;
    }

    /**
     * Sets whether UTF-8 is used for both stdout and stderr (option --utf8).
     *
     * @param utf8 the utf8 to set
     */
    public void setUtf8(boolean utf8) {
        this.utf8 = utf8;
    }

    /**
     * @return the exact
     */
    @Override
    public boolean isExact() {
        return exact;
    }

    /**
     * Sets whether the exact match is required (option --exact).
     *
     * @param exact the exact to set
     */
    public void setExact(boolean exact) {
        this.exact = exact;
    }

    /**
     * Returns whether the help is wanted (option -h).
     *
     * @return the help
     */
    public boolean isHelp() {
        return help;
    }

    /**
     * Sets whether the help is wanted (option -h).
     *
     * @param help the help to set
     */
    public void setHelp(boolean help) {
        this.help = help;
    }

    /**
     * @return the infoMode
     */
    public boolean isInfoMode() {
        return infoMode;
    }

    /**
     * Sets whether the info mode is enabled (option --info).
     *
     * @param infoMode the infoMode to set
     */
    public void setInfoMode(boolean infoMode) {
        this.infoMode = infoMode;
    }



    @Override
    public long getFilesizeAsByteBlocks() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public String getFilesizeWithPrintfFormatted() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    /**
     * @return the checkStrict
     */
    public boolean isCheckStrict() {
        return checkStrict;
    }

    /**
     * Sets whether the check is strict (option --check-strict).
     *
     * @param checkStrict the checkStrict to set
     */
    public void setCheckStrict(boolean checkStrict) {
        this.checkStrict = checkStrict;
    }


    /**
     * @return the compatibilityProperties
     */
    @Override
    public CompatibilityProperties getCompatibilityProperties() {
        return compatibilityProperties;
    }

    /**
     * Sets the properties of the style resp. compatibility (option -C resp. --style).
     *
     * @param compatibilityProperties the compatibilityProperties to set
     */
    public void setCompatibilityProperties(CompatibilityProperties compatibilityProperties) {
        this.compatibilityProperties = compatibilityProperties;
    }


    /**
     * @return the listFilter
     */
    @Override
    public ListFilter getListFilter() {
        return listFilter;
    }

    /**
     * Sets the filter for the list output (option --list-filter).
     *
     * @param listFilter the listFilter to set
     */
    public void setListFilter(ListFilter listFilter) {
        this.listFilter = listFilter;
    }

    /**
     * Returns the line that should be checked (option --check-line).
     *
     * @return the checkLine
     */
    public String getCheckLine() {
        return checkLine;
    }

    /**
     * Sets the line that should be checked (option --check-line).
     *
     * @param checkLine the checkLine to set
     */
    public void setCheckLine(String checkLine) {
        this.checkLine = checkLine;
    }

    /**
     * @return the stdinNameForOutput
     */
    @Override
    public String getStdinName() {
        return stdinName;
    }

    /**
     * Sets the name that is used for the standard input stream in the output.
     *
     * @param stdinName the stdinNameForOutput to set
     */
    public void setStdinName(String stdinName) {
        this.stdinName = stdinName;
    }

    /**
     * Returns whether the license is wanted (option --license).
     *
     * @return true if the license is wanted
     */
    public boolean isLicenseWanted() {
        return licenseWanted;
    }

    /**
     * Sets whether the license is wanted (option --license).
     *
     * @param licenseWanted true if the license is wanted
     */
    public void setLicenseWanted(boolean licenseWanted) {
        this.licenseWanted = licenseWanted;
    }

    /**
     * Returns whether the copyright is wanted (option --copyright).
     *
     * @return true if the copyright is wanted
     */
    public boolean isCopyrightWanted() {
        return copyrightWanted;
    }

    /**
     * Sets whether the copyright is wanted (option --copyright).
     *
     * @param copyrightWanted true if the copyright is wanted
     */
    public void setCopyrightWanted(boolean copyrightWanted) {
        this.copyrightWanted = copyrightWanted;
    }

    /**
     * Updates this parameters instance with values from the newParameters object.
     * @param newParameters the parameters object with new values
     */
    public void update(Parameters newParameters) {
        if (newParameters.getAlgorithm() != null) {
            this.setAlgorithm(newParameters.getAlgorithm());
        }
        if (newParameters.isAlternateImplementationWanted()) {
            this.setAlternateImplementationWanted(true);
        }
        if (newParameters.isUtf8()) {
            this.setUtf8(true);
        }
        if (newParameters.isCheckStrict()) {
            this.setCheckStrict(true);
        }
        if (newParameters.isCopyrightWanted()) {
            this.setCopyrightWanted(true);
        }
        if (newParameters.isLicenseWanted()) {
            this.setLicenseWanted(true);
        }
        this.setHeaderWanted(newParameters.isHeaderWanted());
        this.setHeaderWantedExplicitlySet(newParameters.isHeaderWantedExplicitlySet());

        if (newParameters.getCheckFile() != null) {
            this.setCheckFile(newParameters.getCheckFile());
        }
        if (newParameters.getCheckLine() != null) {
            this.setCheckLine(newParameters.getCheckLine());
        }
        if (newParameters.isCheckStrict()) {
            this.setCheckStrict(true);
        }
        if (newParameters.isIgnoreHashes()) {
            this.setIgnoreHashes(true);
        }
        if (newParameters.isIgnoreSizes()) {
            this.setIgnoreSizes(true);
        }
        if (newParameters.isIgnoreTimestamps()) {
            this.setIgnoreTimestamps(true);
        }
        if (newParameters.isStringList()) {
            this.setStringList(newParameters.getStringList());
            if (!newParameters.getCharsetStringList().equalsIgnoreCase(UTF_8)) {
                this.setCharsetStringList(newParameters.getCharsetStringList());
            }
            if (newParameters.getCommentChars() != null) {
                this.setCommentChars(newParameters.getCommentChars());
            }
            if (newParameters.isIgnoreEmptyLines()) {
                this.setIgnoreEmptyLines(newParameters.isIgnoreEmptyLines());
            }
        }
        if (newParameters.getCompatibilityID() != null) {
            this.setCompatibilityID(newParameters.getCompatibilityID());
        } else {
            // all properties here that are being set implicitly by a compat file
            if (newParameters.getEncoding() != null) {
                this.setEncoding(newParameters.getEncoding());
            }
            if (newParameters.getFormat() != null) {
                this.setFormat(newParameters.getFormat());
            }
            if (newParameters.getCommentChars() != null) {
                this.setCommentChars(newParameters.getCommentChars());
            }
            if (newParameters.getStdinName().equals("-")) {
                this.setStdinName("-");
            }
        }
        if (newParameters.isBom()) {
            this.setBom(true);
        }
        if (newParameters.isDontFollowSymlinksToDirectories()) {
            this.setDontFollowSymlinksToDirectories(true);
        }
        if (newParameters.isDontFollowSymlinksToFiles()) {
            this.setDontFollowSymlinksToFiles(true);
        }
        if (newParameters.getExpectedString() != null) {
            this.setExpected(newParameters.getExpectedString());
        }
        if (newParameters.isGroupingSet()) {
            this.setGrouping(newParameters.getGrouping());
        }
        if (newParameters.isGroupCharSet()) {
            this.setGroupChar(newParameters.getGroupChar());
        }
        if (newParameters.isHelp()) {
            this.setHelp(true);
            if (newParameters.isHelpLanguage()) {
                this.setHelpLanguage(newParameters.getHelpLanguage());
                if (newParameters.isHelpSearchString()) {
                    this.setHelpSearchString(newParameters.getHelpSearchString());
                }
            } else {
                if (newParameters.isHelpSearchString()) {
                    this.setHelpSearchString(newParameters.getHelpSearchString());
                }
            }
        }
        if (newParameters.isInfoMode()) {
            this.setInfoMode(true);
        }
        if (newParameters.isList()) {
            this.setList(true);
        }
        if (newParameters.getListFilter().isFilterHasBeenSet()) {
            this.setListFilter(newParameters.getListFilter());
        }
        if (newParameters.getWantedListFilter().isFilterHasBeenSet()) {
            this.setWantedListFilter(newParameters.getWantedListFilter());
        }
        if (newParameters.getFilelistFilename() != null) {
            this.setFilelistFilename(newParameters.getFilelistFilename());
            this.setFilenamesFromFilelist(new ArrayList<>());
        }
        if (newParameters.getFilelistFormat() != null) {
            this.setFilelistFormat(newParameters.getFilelistFormat());
        }
        if (newParameters.isPathAbsolute()) {
            this.setPathAbsolute(true); //
            this.setPathRelativeToEntry(0);
            this.setPathRelativeToAsString(null);
            this.setNoPath(false);
        } else
        if (newParameters.isPathRelativeToEntry()) {
            this.setPathAbsolute(false);
            this.setPathRelativeToEntry(newParameters.getPathRelativeToEntry()); //
            this.setPathRelativeToAsString(null);
            this.setNoPath(false);
        } else
        if (newParameters.getPathRelativeToAsString() != null) {
            this.setPathAbsolute(false);
            this.setPathRelativeToEntry(0);
            this.setPathRelativeToAsString(newParameters.getPathRelativeToAsString()); //
            this.setNoPath(false);
        } else
        if (newParameters.isNoPath()) {
            this.setPathAbsolute(false);
            this.setPathRelativeToEntry(0);
            this.setPathRelativeToAsString(null);
            this.setNoPath(true); //
        }
        if (newParameters.getOutputFile() != null) {
            if (newParameters.isOutputFileOverwrite()) {
                this.setOutputFileOverwrite(true);
            } else {
                this.setOutputFileOverwrite(false);
            }
            this.setOutputFile(newParameters.getOutputFile());
        }
        if (newParameters.getErrorFile() != null) {
            if (newParameters.isErrorFileOverwrite()) {
                this.setErrorFileOverwrite(true);
            } else {
                this.setErrorFileOverwrite(false);
            }
            this.setErrorFile(newParameters.getErrorFile());
        }
        if (newParameters.isPathCharSet()) {
            this.setPathChar(newParameters.getPathChar());
        }
        if (newParameters.isSequence()) {
            this.setSequence(newParameters.getSequence());
        }
        if (newParameters.isKey()) {
            this.setKey(newParameters.getKey());
        }
        if (newParameters.isRecursive()) {
            this.setRecursive(true);
            this.setDepth(newParameters.getDepth());
        }
        if (newParameters.scanAllUnixFileTypes()) {
            this.setScanAllUnixFileTypes(true);
        }
        if (newParameters.isScanNtfsAds()) {
            this.setScanNtfsAds(true);
        }
        if (newParameters.isSeparatorSet()) {
            this.setSeparator(newParameters.getSeparatorRaw());
        }
        if (newParameters.isThreadsHashingSet()) {
            this.setThreadsHashing(newParameters.getThreadsHashing());
        }
        if (newParameters.isThreadsReadingSet()) {
            this.setThreadsReading(newParameters.getThreadsReading());
        }
        if (newParameters.isFilesizeWantedSet()) {
            this.setFilesizeWanted(newParameters.isFilesizeWanted());
        }
        if (newParameters.isTimestampWanted()) {
            this.setTimestampFormat(newParameters.getTimestampFormat());
        }
        if (newParameters.isVersionWanted()) {
            this.setVersionWanted(true);
        }
        if (!newParameters.getVerbose().isDefault()) {
            this.setVerbose(newParameters.getVerbose());
        }
        if (!newParameters.getCharsetFileList().equalsIgnoreCase(UTF_8)) {
            this.setCharsetFileList(newParameters.getCharsetFileList());
        }
        if (!newParameters.getCharsetCheckFile().equalsIgnoreCase(UTF_8)) {
            this.setCharsetCheckFile(newParameters.getCharsetCheckFile());
        }
        if (!newParameters.getCharsetErrorFile().equalsIgnoreCase(UTF_8)) {
            this.setCharsetErrorFile(newParameters.getCharsetErrorFile());
        }
        if (!newParameters.getCharsetOutputFile().equalsIgnoreCase(UTF_8)) {
            this.setCharsetOutputFile(newParameters.getCharsetOutputFile());
        }
        if (newParameters.getCharsetStdout() != null) {
            this.setCharsetStdout(newParameters.getCharsetStdout());
        }
        if (newParameters.getCharsetStderr() != null) {
            this.setCharsetStderr(newParameters.getCharsetStderr());
        }
        if (newParameters.isStdinForFilenamesFromArgs()) {
            this.setStdinForFilenamesFromArgs(true);
        }
        if (newParameters.getFilenamesFromArgs() != null) {
            this.setFilenamesFromArgs(newParameters.getFilenamesFromArgs());
        }
    }

    /**
     * Builds the command line arguments from the current values of this object.
     *
     * @return the command line arguments
     */
    public List<String> toStringArrayList() {
        List<String> list = new ArrayList<>();
        if (algorithm != null) {
            list.add(_ALGORITHM);
            list.add(algorithm);
        }
        if (alternate) {
            list.add(_ALTERNATIVE);
        }
        if (utf8) {
            list.add(__UTF8);
        }
        if (checkStrict) {
            list.add(__CHECK_STRICT);
        }
        if (copyrightWanted) {
            list.add(__COPYRIGHT);
        }
        if (licenseWanted) {
            list.add(__LICENSE);
        }
        if (headerWanted && headerWantedExplicitlySet) {
            list.add(__HEADER);
        }
        if (!headerWanted && headerWantedExplicitlySet) {
            list.add(__NO_HEADER);
        }
        if (checkFile != null) {
            list.add(_CHECK_FILE);
            list.add(checkFile);
        }
        if (checkLine != null) {
            list.add(__CHECK_LINE);
            list.add(checkLine);
        }
        if (ignoreHashes) {
            list.add(__IGNORE_HASHES);
        }
        if (ignoreSizes) {
            list.add(__IGNORE_SIZES);
        }
        if (ignoreTimestamps) {
            list.add(__IGNORE_TIMESTAMPS);
        }
        if (stringList != null) {
            list.add(__STRING_LIST);
            list.add(stringList);

            if (!charsetStringList.equalsIgnoreCase(UTF_8)) {
                list.add(__CHARSET_STRING_LIST);
                list.add(charsetStringList);
            }
            if (getCommentChars() != null) {
                list.add(_IGNORE_LINES_STARTING_WITH_STRING);
                list.add(getCommentChars());
            }
        }
        if (ignoreEmptyLines) {
            list.add(__IGNORE_EMPTY_LINES);
        }

        if (compatibilityID != null) {
            list.add(__STYLE);
            list.add(compatibilityID);
        } else {
            // all properties here that are being set implicitly by a compat file
            if (encoding != null) {
                list.add(_ENCODING);
                list.add(Encoding.encoding2String(encoding));
            }
            if (format != null) {
                list.add(_FORMAT);
                list.add(format);
            }
            if (getCommentChars() != null) {
                list.add(_IGNORE_LINES_STARTING_WITH_STRING);
                list.add(getCommentChars());
            }
            if (stdinName.equals(DASH)) {
                list.add(__LEGACY_STDIN_NAME);
            }
        }
        if (bom) {
            list.add(__BOM);
        }
        if (dontFollowSymlinksToDirectories) {
            list.add(_DONT_FOLLOW_SYMLINKS_TO_DIRECTORIES);
        }
        if (dontFollowSymlinksToFiles) {
            list.add(_DONT_FOLLOW_SYMLINKS_TO_FILES);
        }
        if (expected != null) {
            list.add(_EXPECT_HASH);
            list.add(expected);
        }
        if (isGroupingSet()) {
            list.add(_GROUP_BYTES);
            list.add(String.valueOf(getGrouping()));
        }
        if (isGroupCharSet()) {
            list.add(_GROUP_BYTES_SEPARATOR);
            list.add(String.valueOf(getGroupChar()));
        }
        if (isHelp()) {
            list.add(_HELP);
            if (isHelpLanguage()) {
                list.add(getHelpLanguage());
                if (isHelpSearchString()) {
                    list.add(getHelpSearchString());
                }
            } else {
                if (isHelpSearchString()) {
                    list.add(getHelpSearchString());
                }
            }
            // --exact has to be added after the search string, because -h would
            // consume --exact as its search string otherwise
            if (isExact()) {
                list.add(__EXACT);
            }
        }
        if (isInfoMode()) {
            list.add(__INFO);
        }
        if (isList()) {
            list.add(_LIST);
        }
        if (listFilter.isFilterHasBeenSet()) {
            list.add(__LIST_FILTER);
            list.add(listFilter.toString());
        }
        if (wantedList != null) {
            list.add(_WANTED_LIST);
            list.add(wantedList);
        }
        if (wantedListFilter.isFilterHasBeenSet()) {
            list.add(__WANTED_LIST_FILTER);
            list.add(wantedListFilter.toString());
        }
        if (isHMACsWanted()) {
            list.add(__HMACS);
        }
        if (isGnuEscapingSetByUser()) {
            list.add(__GNU_FILENAME_ESCAPING);
            list.add(isGnuEscaping() ? "true" : "false");
        }
        if (isOutputFileReplaceTokens()) {
            list.add(__OUTPUT_FILE_REPLACE_TOKENS);
        }
        if (filelistFilename != null) {
            list.add(__FILE_LIST);
            list.add(filelistFilename);
        }
        if (filelistFormat != null) {
            list.add(__FILE_LIST_FORMAT);
            list.add(filelistFormat);
        }
        if (isFilesizeWantedSet()) {
            list.add(__FILESIZE);
            list.add(isFilesizeWanted() ? "true": "false");
        }
        if (pathAbsolute) {
            list.add(__PATH_ABSOLUTE);
        } else
        if (isPathRelativeToEntry()) {
            list.add(__PATH_RELATIVE_TO_ENTRY);
            list.add(String.valueOf(getPathRelativeToEntry()));
        } else
        if (pathRelativeToAsString != null) {
            list.add(__PATH_RELATIVE_TO);
            list.add(pathRelativeToAsString);
        } else
        if (noPath) {
            list.add(__NO_PATH);
        }
        if (outputFile != null) {
            if (outputFileOverwrite) {
                list.add(__OUTPUT_FILE_OVERWRITE);
            } else {
                list.add(__OUTPUT_FILE);
            }
            list.add(outputFile);
        }
        if (errorFile != null) {
            if (errorFileOverwrite) {
                list.add(__ERROR_FILE_OVERWRITE);
            } else {
                list.add(__ERROR_FILE);
            }
            list.add(errorFile);
        }
        if (isPathCharSet()) {
            list.add(_PATH_SEPARATOR);
            list.add(String.valueOf(getPathChar()));
        }
        if (isSequence()) {
            list.add(_QUICK);
            list.add(getSequence().asString());
        }
        if (isKey()) {
            list.add(_KEY);
            list.add(getKey().asString());
        }
        if (isRecursive()) {
            list.add(_RECURSIVE);
            if (getDepth() == Integer.MAX_VALUE) {
                list.add("max");
            } else {
                list.add(String.valueOf(getDepth()));
            }
        }
        if (scanAllUnixFileTypes) {
            list.add(__SCAN_ALL_UNIX_FILE_TYPES);
        }
        if (scanNtfsAds) {
            list.add(__SCAN_NTFS_ADS);
        }
        if (isSeparatorSet()) {
            list.add(_SEPARATOR);
            list.add(getSeparatorRaw());
        }
        if (isThreadsHashingSet()) {
            list.add(__THREADS_HASHING);
            list.add(String.valueOf(getThreadsHashing()));
        }
        if (isThreadsReadingSet()) {
            list.add(__THREADS_READING);
            list.add(String.valueOf(getThreadsReading()));
        }
        if (isTimestampFormatSetByUser()) {
            list.add(_TIMESTAMP);
            list.add(getTimestampFormat());
        }
        if (isVersionWanted()) {
            list.add(_VERSION);
        }
        if (!verbose.isDefault()) {
            list.add(_VERBOSE);
            list.add(verbose.toString());
        }
        if (!charsetFileList.equalsIgnoreCase(UTF_8)) {
            list.add(__CHARSET_FILE_LIST);
            list.add(charsetFileList);
        }
        if (!charsetCheckFile.equalsIgnoreCase(UTF_8)) {
            list.add(__CHARSET_CHECK_FILE);
            list.add(charsetCheckFile);
        }
        if (!charsetErrorFile.equalsIgnoreCase(UTF_8)) {
            list.add(__CHARSET_ERROR_FILE);
            list.add(charsetErrorFile);
        }
        if (!charsetOutputFile.equalsIgnoreCase(UTF_8)) {
            list.add(__CHARSET_OUTPUT_FILE);
            list.add(charsetOutputFile);
        }
        if (!charsetWantedList.equalsIgnoreCase(UTF_8)) {
            list.add(__CHARSET_WANTED_LIST);
            list.add(charsetWantedList);
        }
        if (!charsetConsole.equalsIgnoreCase(UTF_8)) {
            list.add(__CHARSET_CONSOLE);
            list.add(charsetConsole);
        }
        if (charsetStdout != null) {
            list.add(__CHARSET_STDOUT);
            list.add(charsetStdout);
        }
        if (charsetStderr != null) {
            list.add(__CHARSET_STDERR);
            list.add(charsetStderr);
        }

        if (stdin) {
            list.add(DASH);
        }

        List<String> filenames = getFilenamesFromArgs();
        if (filenames.size() > 0 && filenames.get(0).startsWith(DASH)) {
            list.add(DASHDASH);
        }
        for (String filename : filenames) {
            list.add(filename);
        }

        return list;
    }

    // setupStreams - if streams should be setup as well
    /**
     * Checks the parameters, resolves the file list, validates sequence, key, style and algorithm, and applies implicit settings.
     *
     * @param setupStreams true if the standard output and error streams should be set up as well
     * @throws ParameterException if parameter combinations are invalid or if a parameter error occurs
     * @throws ExitException if an exit should happen
     */
    public void checkParameters(boolean setupStreams) throws ParameterException, ExitException {
        resolveFileList(); // --file-list <list> --file-list-charset <charset>
        if (setupStreams) {
            handleCharsetsAndSetupStreams();
        }
        checkForNonsenseParameterCombinations();
        validateSequence(); // -q
        handleKey();
        handleCompatibility();
        validateAlgorithm(); // --algorithm
        resolvePathRelativeTo(); // --path-relative-to-entry <number> --path-relative-to <path> --file-list <list>
        handleWarningsAndImplicitSettings();
    }

    // ignore/disable unsupported/unsuitable/incompatible parameters
    /**
     * Checks the parameters and sets up the standard output and error streams.
     *
     * @throws ParameterException if parameter combinations are invalid or if a parameter error occurs
     * @throws ExitException if an exit should happen
     */
    public void checkParameters() throws ParameterException, ExitException {
        checkParameters(true);
    }

    /**
     * Process list that has been specified by -L resp. --file-list
     * @throws ParameterException if file list does not exist, if it is not readable or if file list format or file list charset is unsupported
     */
    public void resolveFileList() throws ParameterException {

        if (this.getFilelistFilename() != null) {
            this.getFilenamesFromFilelist().clear();
            try {
                if (this.getFilelistFilename().equals("-")) { // stdin

                    if (this.getFilelistFormat() == null || this.getFilelistFormat().equals("list")) {

                        this.getFilenamesFromFilelist().addAll(
                                GeneralIO.readLinesFromStdin(
                                        Charset.forName(this.getCharsetFileList()),
                                        true,
                                        this.getCommentChars(), false));
                    } else if (this.getFilelistFormat().equals("ssv")) { // space separated values
                        this.getFilenamesFromFilelist().addAll(
                                GeneralIO.readLinesFromStdin(
                                        Charset.forName(this.getCharsetFileList()),
                                        true,
                                        this.getCommentChars(), true));
                    } else {
                        Help.printHelp(HELP_DEFAULT_LANGUAGE, __FILE_LIST_FORMAT, true);
                        throw new ParameterException(String.format("File list format \"%s\" is unsupported.", this.getFilelistFormat()));
                    }
                } else {
                    if (this.getFilelistFormat() == null || this.getFilelistFormat().equals("list")) {

                        this.getFilenamesFromFilelist().addAll(
                                GeneralIO.readLinesFromTextFile(
                                        this.getFilelistFilename(),
                                        Charset.forName(this.getCharsetFileList()),
                                        true,
                                        this.getCommentChars(), false));
                    } else if (this.getFilelistFormat().equals("ssv")) { // space separated values
                        this.getFilenamesFromFilelist().addAll(
                                GeneralIO.readLinesFromTextFile(
                                        this.getFilelistFilename(),
                                        Charset.forName(this.getCharsetFileList()),
                                        true,
                                        this.getCommentChars(), true));

                    } else {
                        Help.printHelp(HELP_DEFAULT_LANGUAGE, __FILE_LIST_FORMAT, true);
                        throw new ParameterException(String.format("File list format \"%s\" is unsupported.", this.getFilelistFormat()));
                    }
                }


                int ndx = 0;
                while (ndx < this.getFilenamesFromFilelist().size()) {

                    String theString = this.getFilenamesFromFilelist().get(ndx);
                    if (isGnuEscaping() && theString.startsWith("\\")) {
                        this.getFilenamesFromFilelist().set(ndx,
                                FilenameFormatter.gnuUnescapeProblematicCharsInFilename(theString.substring(1)));
                    }
                    ndx++ ;
                }

            } catch (UnsupportedCharsetException uce) {
                throw new ParameterException(String.format("Charset \"%s\" is unsupported. Check the supported character sets with jacksum --info.", this.getCharsetFileList()));
            } catch (IOException ex) {
                throw new ParameterException(String.format("File %s not found or cannot be read.", this.getFilelistFilename()));
            }
        }
    }


    // ************************************** private methods *********************************************************

    private void validateAlgorithm() throws ParameterException {
        // validity check for --algorithm
        if (algorithm != null) {
            if (algorithm.startsWith("+")) {
                throw new ParameterException(String.format("The algorithm %s must not start with a + sign, but it can end with one.", algorithm));
            }
            if (algorithm.contains("++")) {
                throw new ParameterException(String.format("The algorithm %s must not contain ++.", algorithm));
            }
            if (algorithm.contains("hmac:") && !isKey()) {
                throw new ParameterException(String.format("The HMAC algorithm %s requires a key. It is specified with the -k option.", algorithm));
            }
        }
    }

    private void resolvePathRelativeTo() throws ParameterException {
        // validity check for --path-relative-to-entry
        // an empty file list means that the option is ignored, as documented
        if (isPathRelativeToEntry() && getFilenamesFromFilelist().size() > 0) {
            // the entry has to exist, otherwise the access below would end up in an
            // IndexOutOfBoundsException rather than in a parameter error
            if (getPathRelativeToEntry() > getFilenamesFromFilelist().size()) {
                throw new ParameterException(String.format(
                        "Option %s %s is out of range. The file list that has been specified by %s contains %s entries only.",
                        __PATH_RELATIVE_TO_ENTRY, getPathRelativeToEntry(), __FILE_LIST,
                        getFilenamesFromFilelist().size()));
            }
            setPathRelativeToAsString(getFilenamesFromFilelist().get(getPathRelativeToEntry()-1));
            // remember that this path has been derived, and not been set by the user resp. by the API,
            // so that a subsequent call of checked() does not report a conflict of path options
            pathRelativeToDerivedFromEntry = true;
        }

        // validity check for --path-relative-to, and set the value for the Path called pathRelativeTo
        if (pathRelativeToAsString != null) {
            try {
                Path path = Paths.get(pathRelativeToAsString);
                if (Files.exists(path)) {
                    if (Files.isDirectory(path)) {
                        pathRelativeTo = path.toAbsolutePath().normalize();
                    } else {
                        pathRelativeTo = path.toAbsolutePath().normalize().getParent();
                    }
                } else {
                    throw new ParameterException(String.format("%s does not exist.%n", pathRelativeToAsString));
                }
            } catch (InvalidPathException ipe) {
                throw new ParameterException(String.format("%s is an invalid path.%n", pathRelativeToAsString));
            }
        }
    }

    /**
     * Decodes the sequence of option -q once, so that a malformed sequence is reported as
     * a parameter error before any work is done.
     *
     * <p>The sequence is decoded lazily by {@link Sequence#asBytes()}, which is called
     * from several places while the run is already under way. An exception from there
     * would travel to Main and would be printed without the prefix that every other
     * message of Jacksum carries, and without naming the option that is at fault. The
     * decoded bytes are kept by the Sequence object, so nothing is decoded twice.</p>
     *
     * @throws ParameterException if the sequence cannot be decoded
     */
    private void validateSequence() throws ParameterException {
        if (isSequence()
                && !getSequence().getType().equals(Sequence.Type.READLINE)
                && !getSequence().getType().equals(Sequence.Type.PASSWORD)) {
            // readline and password are read from the console later, see QuickAction
            try {
                getSequence().asBytes();
            } catch (IllegalArgumentException e) {
                throw new ParameterException(String.format("Option %s: %s", _QUICK, e.getMessage()));
            }
        }
    }

    // if --key is set to readline or password, input is read from the console to set the key for the parameter object
    private void handleKey() throws ParameterException, ExitException {
        if (isKey()) {
            if (getKey().getType().equals(Sequence.Type.PASSWORD)) {
                char[] passwd = net.loefflmann.sugar.io.Console.readPassword("Key (echo off): ");
                if (passwd != null) {
                    try {
                        setKey(new Sequence(Sequence.Type.PASSWORD, new String(passwd).getBytes(getCharsetConsole())));
                    } catch (UnsupportedEncodingException e) {
                        throw new ParameterException(e.getMessage());
                    } finally {
                        java.util.Arrays.fill(passwd, ' ');
                    }
                }
            } else
            if (getKey().getType().equals(Sequence.Type.READLINE)) {
                String line = net.loefflmann.sugar.io.Console.readLine("Key (echo on): ");
                if (line != null) {
                    try {
                        setKey(new Sequence(Sequence.Type.READLINE, line.getBytes(getCharsetConsole())));
                    } catch (UnsupportedEncodingException e) {
                        throw new ParameterException(e.getMessage());
                    }
                }
            }
            try {
                HashFunctionFactory.setKey(getKey().asBytes());
            } catch (IllegalArgumentException e) {
                // The sequence of the key is decoded here for the first time, so this is
                // the place where a malformed one has to be reported. Without this, the
                // exception would travel to Main and would be printed without the prefix
                // that every other message of Jacksum carries.
                // The message of the exception may quote parts of the key, so it is passed on
                // only for the type file, whose messages are about the file and not the key.
                if (getKey().getType().equals(Sequence.Type.FILE)) {
                    throw new ParameterException(String.format("Option %s: %s", _KEY, e.getMessage()));
                }
                throw new ParameterException(String.format(
                        "Option %s: the key is not a valid sequence of the type %s. The value is not shown, because it is a secret.",
                        _KEY, getKey().getType().getCode()));
            }
        } else {
            // This run has no key, so it must not use the key of an earlier run in the
            // same JVM: without this, an HMAC would silently be initialized with a secret
            // that has never been given for this run.
            HashFunctionFactory.wipeKey();
        }
    }
    private static String decodeQuote(String format) {
        return GeneralString.replaceAllStrings(format, "#QUOTE", "\"");
    }

    private static String decodeSeparator(String format, String separator) {
        if (separator != null) {
            format = GeneralString.replaceAllStrings(format, "#SEPARATOR", separator);
        }
        return format;
    }

    /**
     * Restores the standard output stream that was active when this object was created.
     */
    public void restoreStdOut() {
        System.setOut(stdOutBackup);
    }

    /**
     * Restores the standard error stream that was active when this object was created.
     */
    public void restoreStdErr() {
        System.setErr(stdErrBackup);
    }

    /**
     * Restores both the standard output and the standard error stream that were active when this object was created.
     */
    public void restoreStreams() {
        restoreStdOut();
        restoreStdErr();
    }

    /**
     * Checks whether the output file (option -o) resp. the error file (option -u) already
     * exists, before any of them is opened.
     *
     * The check has to happen before opening, because opening a file for writing truncates
     * it: if both options point to the same file, the shared stream used to be created
     * before the check, so the file was already emptied when the check aborted the run; and
     * if only the error file existed, the output file had already been created before the
     * run was aborted.
     *
     * @throws ExitException if a file exists while overwriting it has not been allowed
     */
    private void checkOutputTargets() throws ExitException {
        if (isOutputFile() && !isOutputFileOverwrite()) {
            File file = new File(getOutputFile());
            if (file.exists()) {
                throw new ExitException(String.format(
                        "Jacksum: Error: The file %s already exists. Specify the file by -O to overwrite it.", file),
                        ExitCode.IO_ERROR);
            }
        }
        if (isErrorFile() && !isErrorFileOverwrite()) {
            File file = new File(getErrorFile());
            if (file.exists()) {
                throw new ExitException(String.format(
                        "Jacksum: Error: The file %s already exists. Specify the file by -U to overwrite it.", file),
                        ExitCode.IO_ERROR);
            }
        }
    }

    /**
     * Determines whether two character set names denote the same character set. The names
     * are case insensitive, and a character set can be named by an alias as well, so
     * "utf-8", "UTF-8" and "utf8" all denote the same one.
     *
     * @param one a character set name
     * @param other another character set name
     * @return true if both names denote the same character set
     */
    private static boolean isSameCharset(String one, String other) {
        if (one.equalsIgnoreCase(other)) {
            return true;
        }
        try {
            return Charset.forName(one).equals(Charset.forName(other));
        } catch (IllegalArgumentException iae) {
            // an unsupported or an illegal name is reported later, when the stream is set up
            return false;
        }
    }

    private void handleCharsetsAndSetupStreams() throws ParameterException, ExitException {
        if (isUtf8()) {
            setCharsetStdout(UTF_8);
            setCharsetStderr(UTF_8);
        }

        if (getCharsetStdout() != null) {
            // change stdout
            try {
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, getCharsetStdout()));
            } catch (UnsupportedEncodingException e) {
                throw new ExitException(String.format("Jacksum: Error: Encoding %s for stdout is not supported by your JVM or OS.", getCharsetStdout()), ExitCode.IO_ERROR);
            }
        }

        if (getCharsetStderr() != null) {
            // change stderr
            try {
                System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, getCharsetStderr()));
            } catch (UnsupportedEncodingException e) {
                throw new ExitException(String.format("Jacksum: Error: Encoding %s for stderr is not supported by your JVM or OS.", getCharsetStderr()), ExitCode.IO_ERROR);
            }
        }

        boolean outputFileAndErrorFileAreEqual = false;

        if (isOutputFile() && isErrorFile()) {
            Path outputFilePath = Paths.get(getOutputFile()).toAbsolutePath().normalize();
            Path errorFilePath = Paths.get(getErrorFile()).toAbsolutePath().normalize();
            outputFileAndErrorFileAreEqual = outputFilePath.equals(errorFilePath);
        }

        if (outputFileAndErrorFileAreEqual && !isSameCharset(charsetOutputFile, charsetErrorFile)) {
            throw new ParameterException("Output and error file are the same, but character sets for output and error file have been set differently.");
        }

        // both files have to be checked before any of them is opened for writing,
        // because opening truncates, see checkOutputTargets()
        checkOutputTargets();

        PrintStream streamShared = null;
        boolean isShared = false;
        if (outputFileAndErrorFileAreEqual) {
            try {
                streamShared = new PrintStream(new FileOutputStream(getOutputFile()), true, getCharsetOutputFile());
                isShared = true;
            } catch (UnsupportedEncodingException e) {
                throw new ExitException(String.format(
                        "Jacksum: Error: Encoding %s for the output file is not supported by your JVM or OS.",
                        getCharsetOutputFile()), ExitCode.IO_ERROR);
            } catch (FileNotFoundException e) {
                // the message of the exception already names the file and the reason
                throw new ExitException(String.format("Jacksum: Error: %s", e.getMessage()), ExitCode.IO_ERROR);
            }
        }

        if (isOutputFile()) {
            try {
                if (isShared) {
                    System.setOut(streamShared);
                } else {
                    PrintStream out;
                    out = new PrintStream(new FileOutputStream(getOutputFile()), true, getCharsetOutputFile());
                    //PrintStream tee = new TeeStream(System.out, out);
                    System.setOut(out);
                }
            } catch (UnsupportedEncodingException e) {
                throw new ExitException(String.format(
                        "Jacksum: Error: Encoding %s for the output file is not supported by your JVM or OS.",
                        getCharsetOutputFile()), ExitCode.IO_ERROR);
            } catch (FileNotFoundException e) {
                // the message of the exception already names the file and the reason
                throw new ExitException(String.format("Jacksum: Error: %s", e.getMessage()), ExitCode.IO_ERROR);
            }
        }

        if (isErrorFile()) {
            try {
                if (isShared) {
                    System.setErr(streamShared);
                } else {
                    PrintStream err;
                    err = new PrintStream(new FileOutputStream(getErrorFile()), true, getCharsetErrorFile());
                    //PrintStream tee = new TeeStream(System.out, err);
                    System.setErr(err);
                }
            } catch (UnsupportedEncodingException e) {
                throw new ExitException(String.format(
                        "Jacksum: Error: Encoding %s for the error file is not supported by your JVM or OS.",
                        getCharsetErrorFile()), ExitCode.IO_ERROR);
            } catch (FileNotFoundException e) {
                // the message of the exception already names the file and the reason
                throw new ExitException(String.format("Jacksum: Error: %s", e.getMessage()), ExitCode.IO_ERROR);
            }
        }

        // add BOM, dependent on the charset, if desired
        String bomCharset = null;
        if (bom && isOutputFile()) {
            bomCharset = getCharsetOutputFile();
        } else if (bom && getCharsetStdout() != null) {
            bomCharset = getCharsetStdout();
        }
        if (bomCharset != null) {
            byte[] actualBOM = BOM.getBOM(bomCharset);
            if (actualBOM != null && actualBOM.length > 0) {
                BOM.writeBOM(actualBOM);
            }
        }

    }


    private void checkAlgorithmIsNone() throws ParameterException {
        // exit if selected parameters make no sense
        if (getAlgorithmIdentifier().equals("none")) {
            // there is no hash value for algorithm "none"
            // we cannot encode a non-existing hash value
            if (isEncodingSet() && !isFormatWanted()) {
                throw new ParameterException("-a none and -E without -F cannot go together.");
            }
            // we should not expect a hash value from the algorithm
            if (expected != null) {
                throw new ParameterException("-a none and -e cannot go together.");
            }
        }
    }

    private void checkAlgorithmIsRead() throws ParameterException {
        if (getAlgorithmIdentifier().equals("read")) {
            if (isEncodingSet() && !isFormatWanted()) {
                throw new ParameterException("-a read and -E without -F cannot go together.");
            }
            if (expected != null) {
                throw new ParameterException("-a read and -e cannot go together.");
            }
        }
    }

    private void checkForNonsenseParameterCombinations() throws ParameterException, ExitException {
        checkAlgorithmIsNone();
        checkAlgorithmIsRead();

        if (stdin && isSequence()) {
            throw new ParameterException("Cannot read from both standard input and -q.");
        }

        if (exact && !(help && isHelpSearchString())) {
            throw new ParameterException(String.format("Option %s is only supported in combination with %s resp. %s and a search string.", __EXACT, _HELP, __HELP));
        }

        if (findAlgorithm) {
            if (sequence == null) {
                throw new ParameterException("Option -a unknown:<width> requires option -q.");
            }
            if (expected == null) {
                throw new ParameterException("Option -a unknown:<width> requires option -e.");
            }
            if (encoding == null) {
                throw new ParameterException("Option -a unknown:<width> requires option -E.");
            }
        }


        // grouped hash values cannot be parsed, so they cannot be verified, see also
        // the parser that is generated by DefaultCompatibilityProperties
        if (isGroupingSet() || isGroupCharSet()) {
            if (getCheckFile() != null) {
                throw new ParameterException(String.format("Options %s and %s cannot be combined with %s, because grouped hash values cannot be parsed.",
                        _GROUP_BYTES, _GROUP_BYTES_SEPARATOR, __CHECK_FILE));
            }
            if (isWantedList()) {
                throw new ParameterException(String.format("Options %s and %s cannot be combined with %s, because grouped hash values cannot be parsed.",
                        _GROUP_BYTES, _GROUP_BYTES_SEPARATOR, __WANTED_LIST));
            }
        }

        // verifying files against a list of hashes and searching files by wanted hashes are two
        // different jobs, and getActionType() can only perform one of them: it would silently
        // ignore the wanted list resp. the expectation, including its exit code
        if (isVerifying()) {
            String verifyOption = getCheckFile() != null ? __CHECK_FILE : __CHECK_LINE;
            if (isWantedList()) {
                throw new ParameterException(String.format("Options %s and %s cannot be combined, because they ask for two different jobs.",
                        __WANTED_LIST, verifyOption));
            }
            if (isExpectation()) {
                throw new ParameterException(String.format("Options %s and %s cannot be combined, because they ask for two different jobs.",
                        _EXPECT_HASH, verifyOption));
            }
        }

        // a strict check has to detect all statuses, but a filter can prevent hashing which
        // would prevent a reliable detection, see also ListFilter.isHashingRequired();
        // both options have no effect at all if nothing is being verified, so they are only
        // rejected in verification mode
        if (isVerifying() && isCheckStrict() && !getListFilter().isAll()) {
            throw new ParameterException(String.format("Option %s requires %s all, because a filter could prevent hashing, which could prevent a reliable detection, but %s %s has been set.",
                    __CHECK_STRICT, __LIST_FILTER, __LIST_FILTER, getListFilter()));
        }

        // a timestamp that contains a space cannot be told apart from the fields that
        // follow it, so such a timestamp cannot be parsed, see also the parsers of the
        // styles that support timestamps and DefaultCompatibilityProperties
        if (isTimestampWanted() && getTimestampFormat().contains(" ")
                && (getCheckFile() != null || isWantedList())) {
            throw new ParameterException(String.format("The timestamp format that has been set by %s must not contain a space if %s resp. %s is used, because such a timestamp cannot be parsed.",
                    _TIMESTAMP, __CHECK_FILE, __WANTED_LIST));
        }

        // a check list and a wanted list are both read by the same parser, so both of them are
        // checked the same way, before the run starts
        checkListArgument(_CHECK_FILE, getCheckFile());
        checkListArgument(_WANTED_LIST, getWantedList());

        int pathOptions = 0;
        if (isPathAbsolute()) {
            pathOptions++;
        }
        // a path that resolvePathRelativeTo() has derived from --path-relative-to-entry does not count,
        // otherwise calling checked() more than once on the same object would fail
        if (getPathRelativeToAsString() != null && !pathRelativeToDerivedFromEntry) {
            pathOptions++;
        }
        if (isNoPath()) {
            pathOptions++;
        }
        if (pathRelativeToEntry > 0) {
            pathOptions++;
        }
        if (pathOptions > 1) {
            throw new ParameterException(String.format("Only one of the following options is allowed: %s, %s, %s, or %s.",
                    __NO_PATH, __PATH_ABSOLUTE, __PATH_RELATIVE_TO, __PATH_RELATIVE_TO_ENTRY));
        }

        try {
            if (timestampFormat != null && !TimestampFormatter.isFormatASupportedKeyword(timestampFormat)) {
                    // #QUOTE and #SEPARATOR should be replaced
                    timestampFormat = decodeQuote(timestampFormat);
                    timestampFormat = decodeSeparator(timestampFormat, separator);
                    // test, if the timestampformat is valid
                    Format timestampFormatter = new SimpleDateFormat(timestampFormat);
                    // ... ignore the return value, just force an IllegalArgumentException if format is invalid
                    timestampFormatter.format(new Date());
            }
        } catch (IllegalArgumentException e) {
            // a ParameterException prefixes the message with "Jacksum: Parameter Error: ",
            // and Main answers it with the very same exit code, see also the -t branch of
            // CLIParameters, which words the same error that way already
            throw new ParameterException(String.format("Option %s is wrong (\"%s\").", _TIMESTAMP, e.getMessage()));
        }

        if (isPathRelativeToEntry() && getFilelistFilename() == null) {
            throw new ParameterException(String.format("Option %s requires option %s.", __PATH_RELATIVE_TO_ENTRY, __FILE_LIST));
        }

    }


    /**
     * Determines whether Jacksum verifies data against a check file resp. a check line
     * (options -c and --check-line).
     *
     * In that mode the timestamp format is not only used to format the output, it is also
     * required to parse and to compare the timestamps that are stored in the check file, so
     * it must not be discarded, see also handleWarningsAndImplicitSettings().
     *
     * @return true if a check file resp. a check line has been specified
     */
    private boolean isVerifying() {
        return checkFile != null || checkLine != null;
    }

    private void handleWarningsAndImplicitSettings() {

        // warnings
        if (groupcount > 0 && encoding == null) {
            setEncoding(Encoding.HEX);
            messenger.print(WARNING, "-g has been set, but -E has not been set. Setting -E hex implicitly.");
        }

        if ((groupcount > 0) && (encoding != null && !encoding.equals(Encoding.HEX) && !encoding.equals(Encoding.HEX_UPPERCASE))) {
            messenger.print(WARNING, String.format("-g requires -E hex or -E hex-uppercase, but -E %s has been set. Ignoring -g.", encoding));
        }

        if (groupingChar != null && encoding == null) {
            setEncoding(Encoding.HEX);
            messenger.print(WARNING, "-G has been set, but -E has not been set. Setting -E hex implicitly.");
        }

        if ((groupingChar != null) && (encoding != null && !encoding.equals(Encoding.HEX) && !encoding.equals(Encoding.HEX_UPPERCASE))) {
            messenger.print(WARNING, String.format("-G requires -E hex or -E hex-uppercase, but -E %s has been set. Ignoring -G.", encoding));
        }

        // ignoring flags
        if (checkFile != null && format != null) {
            messenger.print(WARNING, "Option -F will be ignored, because option -c is used.");
            setFormat(null);
        }

        // both timestamp and sequence have been specified
        if (timestampFormat != null && sequence != null && !isVerifying()) {
            // only warn if the user has asked for a timestamp, the format could also come from a style
            if (isTimestampFormatSetByUser()) {
                messenger.print(WARNING, "A sequence (-q) has been specified, timestamp (-t) will be ignored.");
            }
            setTimestampFormat(null);
        }

        // verification mode, no compat file has been given, and no algorithm id
        if (checkFile != null && compatibilityID == null && algorithm == null) {
            setAlgorithm(ALGORITHM_IDENTIFIER_DEFAULT);
            messenger.print(INFO, String.format("Option -a has not been given, it is set implicitly to %s. Alternatively set -C <compatibility>.", ALGORITHM_IDENTIFIER_DEFAULT));
        }

        if (stdin && timestampFormat != null && !isVerifying()) {
            // only warn if the user has asked for a timestamp, the format could also come from a style
            if (isTimestampFormatSetByUser()) {
                messenger.print(WARNING, String.format("Option %s has been ignored, because standard input is used.", __TIMESTAMP));
            }
            setTimestampFormat(null);
        }

        if (this.isGnuEscapingSetByUser() && this.isGnuEscaping() && OSControl.isWindows()) {
            gnuEscaping = false;
            gnuEscapingSetByUser = false;
            messenger.print(WARNING, String.format("Ignoring option %s, because GNU file name escaping is not supported on Microsoft Windows.", __GNU_FILENAME_ESCAPING));
        }

        // implicit settings
        if (isRecursive() && getFilenamesFromArgs().isEmpty() && getFilenamesFromFilelist().isEmpty()) {
            messenger.print(WARNING, "Option -r has been set, but no files have been given, reading files recursively, starting with the current working directory ...");
            filenamesFromArgs.add(".");
        }

        if (!isHelp()
                && !isLicenseWanted()
                && !isCopyrightWanted()
                && !isHMACsWanted()
                && sequence == null
                && getFilenamesFromArgs().isEmpty()
                && getFilenamesFromFilelist().isEmpty()
                && checkFile == null
                && checkLine == null
                && !isRecursive()
                && !stdin
                && !infoMode
                && stringList == null
                && !list
                && !versionWanted) {
            messenger.print(WARNING, "No files have been specified, reading from the standard input stream (stdin) ...");
            stdin = true;
        }

    }

    /**
     * Checks the argument of an option that expects the name of a list that Jacksum reads, such as
     * -c resp. -w. A list that is a directory is a mistake in the parameters rather than an I/O
     * problem, and a list that does not exist is worth a message that names it, because the parser
     * would only pass the message of a Java exception on, without any context.
     *
     * @param option the option the list belongs to, used for the error message
     * @param filename the argument of that option, null if the option has not been set
     * @throws ParameterException if the argument is a directory
     * @throws ExitException if the argument does not exist
     */
    private void checkListArgument(String option, String filename) throws ParameterException, ExitException {
        if (filename == null || filename.equals("-")) { // the - means: read from stdin
            return;
        }
        File f = new File(filename);
        if (f.isDirectory()) {
            throw new ParameterException(String.format("Parameter %s %s is a directory, but a file name is expected.", option, filename));
        }
        if (!f.exists()) {
            throw new ExitException(String.format("Jacksum: Error: %s: No such file or directory.", filename), ExitCode.IO_ERROR);
        }
    }

    private void handleCompatibility() throws ParameterException, ExitException {

        if (this.getCompatibilityID() != null) {
            try {
                compatibilityProperties = new CompatibilityProperties(this.getCompatibilityID(), this.getStdinName());

                if (this.algorithm != null) {
                    // if the style allows overwriting the algorithm and the algorithm has been set using -a ...
                    if (compatibilityProperties.getHashAlgorithmUserSelectable()) {
                        // ... we overwrite the default in the compatibilityProperties object ...
                        compatibilityProperties.setHashAlgorithm(this.algorithm);
                        // ... and flag that change by setting setHashAlgorithmUserSelected(true);
                        compatibilityProperties.setHashAlgorithmUserSelected(true);
                    } else {
                        messenger.print(WARNING, String.format("Ignoring option %s, because the style \"%s\" only supports a hardcoded algorithm.", __ALGORITHM, compatibilityID));
                    }
                }

                // the style determines the encoding of the hash value, either by the
                // property formatter.hash.encoding or by the encoding that is part of
                // the format (e.g. #HASH{hex}), so an encoding that has been set by the
                // user cannot have an effect. Note: this must happen before
                // handleWarningsAndImplicitSettings(), because option -g sets -E hex
                // implicitly and would cause a bogus warning here.
                if (this.encoding != null) {
                    Encoding encodingOfStyle = null;
                    try {
                        encodingOfStyle = Encoding.string2Encoding(compatibilityProperties.getHashEncoding());
                    } catch (IllegalArgumentException iae) {
                        // an invalid encoding in a style file is reported later, when it is being set
                    }
                    if (!this.encoding.equals(encodingOfStyle)) {
                        messenger.print(WARNING, String.format("Ignoring option %s, because the style \"%s\" defines the encoding %s.",
                                _ENCODING, compatibilityID, compatibilityProperties.getHashEncoding()));
                    }
                }

                // a style that stores hash values cannot store one if there is no hash value;
                // in check mode the combination is fine, it verifies the properties that are
                // left (e.g. the existence of the files), see also --ignore-hashes
                if (checkFile == null
                        && "none".equals(compatibilityProperties.getHashAlgorithm())
                        && compatibilityProperties.getRegexpHashPos() > 0) {
                    messenger.print(WARNING, String.format("The style \"%s\" is defined for hash values, but the algorithm \"%s\" does not produce one, so the hash value stays empty.",
                            compatibilityID, "none"));
                }

                if (this.isHeaderWantedExplicitlySet()) {
                    compatibilityProperties.setHeaderWanted(this.isHeaderWanted());
                }

                if (this.isGnuEscapingSetByUser()) {
                    // if the style allows overwriting GnuEscaping and GnuEscaping has been set using --gnu-filename-escaping ...
                    if (compatibilityProperties.isGnuEscapingSupported() && compatibilityProperties.isGnuEscapingUserSelectable()) {
                        // ... we overwrite the default in the compatibilityProperties object ...
                        compatibilityProperties.setGnuEscapingEnabled(this.isGnuEscaping());
                    } else {
                        messenger.print(WARNING, String.format("Ignoring option %s, because the style \"%s\" does not support GNU escaping or does not allow it to be enabled.", __GNU_FILENAME_ESCAPING, compatibilityID));
                    }
                }

                if (this.isFilesizeWantedSet()) {
                    if (!compatibilityProperties.isFilesizeSupported()) {
                        messenger.print(WARNING, String.format("Ignoring option %s, because the style \"%s\" does not support file sizes.", __FILESIZE, compatibilityID));
                    }
                }

                if (this.isTimestampFormatSetByUser()) {
                    // if the style allows overwriting the timestamp format and the timestamp format has been set using -t ...
                    if (compatibilityProperties.isTimestampSupported() && compatibilityProperties.isTimestampFormatUserSelectable()) {
                        // ... we overwrite the default in the compatibilityProperties object ...
                        compatibilityProperties.setTimestampFormat(this.getTimestampFormat());
                        // ... and flag that change by setting setTimestampFormatUserSelected(true);
                        compatibilityProperties.setTimestampFormatUserSelected(true);
                    } else {
                        messenger.print(WARNING, String.format("Ignoring option %s, because the style \"%s\" does not support file timestamps.", __TIMESTAMP, compatibilityID));
                    }
                }


                // patch this parameters object explicitly, because now the parameters
                // come from the compatibilityID object (the compatibilityProperties)
                if (!infoMode) { // we didn't specify both -C and --info

                    if (checkFile != null) { // we are in check mode
                        messenger.print(INFO, String.format("Option --compat/--style has been set, setting implicitly -a %s -E %s, stdin-name=%s",
                                compatibilityProperties.getHashAlgorithm(),
                                compatibilityProperties.getHashEncoding(),
                                compatibilityProperties.getStdinName()));
                    } else { // we are in normal calculation/print mode
                        String fmt = compatibilityProperties.getFormat(this.getAlgorithmIdentifier());
                        messenger.print(INFO, String.format("Option --compat/--style has been set, setting implicitly -a %s -E %s -F \"%s\", stdin-name=%s",
                                compatibilityProperties.getHashAlgorithm(),
                                compatibilityProperties.getHashEncoding(),
                                fmt,
                                compatibilityProperties.getStdinName()));

                        this.setFormat(fmt);
                        this.setLeadingHeader(compatibilityProperties.getLeadingHeaderFormatted(this.getAlgorithmIdentifier()));
                    }
                }


                this.setAlgorithm(compatibilityProperties.getHashAlgorithm());
                this.setEncoding(compatibilityProperties.getHashEncoding());
                this.setHeaderWanted(compatibilityProperties.isHeaderWanted());
                this.setStdinName(compatibilityProperties.getStdinName());
                this.setLineSeparator(compatibilityProperties.getLineSeparator());
                gnuEscaping = compatibilityProperties.isGnuEscapingEnabled();
                if (this.getCommentChars() == null && compatibilityProperties.getIgnoreLinesStartingWithString() != null) {
                    this.setCommentChars(compatibilityProperties.getIgnoreLinesStartingWithString());
                }
                // assign the field directly, so that the style does not mark the timestamp format
                // as being set by the user, see also gnuEscaping a few lines above
                timestampFormat = compatibilityProperties.getTimestampFormat();

            } catch (InvalidCompatibilityPropertiesException ex) {
                // the value of the option is wrong, which is no I/O problem at all
                throw new ParameterException(ex.getMessage());
            } catch (IOException ex) {
                // the file exists, but it cannot be read; the message of the exception has no
                // context, so it needs the prefix that every other error of the application carries
                throw new ExitException(String.format("Jacksum: Error: %s", ex.getMessage()), ExitCode.IO_ERROR);
            }
        } else { // -C hasn't been set, we want to use the default output formatter

            // on Linux and Unix: enable GNU filename escaping for the default output formatter
            // if the user didn't make a selection explicitly on GNU filename escaping.
            // Assign the field directly, so that the platform default does not mark GNU file
            // name escaping as being set by the user, see also timestampFormat above.
            if (!OSControl.isWindows() && !this.isGnuEscapingSetByUser()) {
                gnuEscaping = true;
            }
        }
    }



    /**
     * Returns whether tokens in the output filename are replaced (option --output-file-replace-tokens).
     *
     * @return true if tokens in the output filename are replaced
     */
    public boolean isOutputFileReplaceTokens() {
        return outputFileReplaceTokens;
    }

    /**
     * Sets whether tokens in the output filename are replaced (option --output-file-replace-tokens).
     *
     * @param outputFileReplaceTokens true if tokens should be replaced
     */
    public void setOutputFileReplaceTokens(boolean outputFileReplaceTokens) {
        this.outputFileReplaceTokens = outputFileReplaceTokens;
    }

    /**
     * Returns the output filename as it has been specified, before any tokens have been replaced (option --output-file-replace-tokens).
     *
     * @return the raw output filename, or null if it has not been set
     */
    public String getOutputFileRaw() {
        return outputFileRaw;
    }

    /**
     * Sets the output filename as it has been specified, before any tokens have been replaced (option --output-file-replace-tokens).
     *
     * @param outputFileRaw the raw output filename
     */
    public void setOutputFileRaw(String outputFileRaw) {
        this.outputFileRaw = outputFileRaw;
    }

    public String getLeadingHeader() {
        return leadingHeader;
    }

    /**
     * Sets the header line that is written before the regular header; only settable by a style.
     *
     * @param leadingHeader the leading header
     */
    public void setLeadingHeader(String leadingHeader) {
        this.leadingHeader = leadingHeader;
    }
}

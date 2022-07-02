/*


  Jacksum 3.4.0 - a checksum utility in Java
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
package net.jacksum.parameters;

import net.jacksum.actions.ActionType;
import net.jacksum.actions.check.*;
import net.jacksum.actions.compare.CompareActionInterface;
import net.jacksum.actions.help.Help;
import net.jacksum.actions.help.HelpActionParameters;
import net.jacksum.actions.infoalgo.AlgoInfoActionParameters;
import net.jacksum.actions.infoapp.AppInfoActionParameters;
import net.jacksum.actions.infocompat.CompatInfoActionParameters;
import net.jacksum.actions.quick.QuickActionParameters;
import net.jacksum.actions.version.VersionActionParameters;
import net.jacksum.actions.wanted.WantedListFilter;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.cli.ExitCode;
import net.jacksum.cli.Messenger;
import net.jacksum.cli.Verbose;
import net.jacksum.compats.defs.CompatibilityProperties;
import net.jacksum.compats.defs.InvalidCompatibilityPropertiesException;
import net.jacksum.formats.Encoding;
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
import net.loefflmann.sugar.util.ByteSequences;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

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

        // all other parameter interfaces
        VersionActionParameters, CompareActionInterface, HelpActionParameters, QuickActionParameters,
        FormatParameters, AlgorithmParameters, CustomizedFormatParameters, StatisticsParameters,
        FileWalkerParameters, ProducerConsumerParameters, PathParameters,
        GatheringParameters, SequenceParameters, ProducerParameters, CheckConsumerParameters,
        VerboseParameters, CompatibilityParameters, HeaderParameters {


    public static final String ALGORITHM_IDENTIFIER_DEFAULT = "sha3-256";
    private static final long serialVersionUID = 2705696599336345354L;
    private String algorithmIdentifier = ALGORITHM_IDENTIFIER_DEFAULT;
    private String[] cliParameters;
    public static final String UTF_8 = "UTF-8";

    transient private PrintStream stdOutBackup = System.out;
    transient private PrintStream stdErrBackup = System.err;


    // -a
    private String algorithm = null;
    // -a unknown:
    private boolean findAlgorithm = false;
    // -A
    private boolean alternate = false;
    // -c <file>
    private String checkFile = null;
    // --check-line <line>
    private String checkLine = null;
    // --check-strict
    private boolean checkStrict = false;
    // -C <compatibility>
    private String compatibilityID = null;
    private CompatibilityProperties compatibilityProperties = null;

    // --charset-check-file <charset>
    private String charsetCheckFile = UTF_8;
    // --charset-file-list <charset>
    private String charsetFileList = UTF_8;
    // --charset-error-file <charset>
    private String charsetErrorFile = UTF_8;
    // --charset-output-file <charset>
    private String charsetOutputFile = UTF_8;
    // --charset-wanted-list <charset>
    private String charsetWantedList = UTF_8;
    // --charset-stdout <charset>
    private String charsetStdout = null;
    // --charset-stderr <charset>
    private String charsetStderr = null;
    // -d
    private boolean dontFollowSymlinksToDirectories = false;
    // -e <text>
    private String expected = null;
    byte[] expectedAsBytes = null;
    // -E default
    private Encoding encoding = null;
    // -f
    private boolean dontFollowSymlinksToFiles = false;
    // -F
    private String format = null;
    // -g
    private int groupcount = 0;
    // -G
    private Character groupingChar = null;
    // -h
    private boolean help = false;
    // -h lang
    private String helpLanguage = null;
    // -h [lang] search
    private String helpSearchString = null;
    // --info
    private boolean infoMode = false;
    // -I
    private String commentChars = null;
    // -l, --list
    private boolean list = false;
    // --list-filter
    private ListFilter listFilter;
    // --list-filter-wanted
    private WantedListFilter wantedListFilter;
    // --legacy-stdin-name
    private String stdinName = "<stdin>";
    // -L <file>
    private String filelistFilename = null;
    // keeps all the filenames that have been specified by -L
    private List<String> filenamesFromFilelist = new ArrayList<>();
    // --file-list-format
    private String filelistFormat = null;
    // -O/-o
    private String outputFile = null;
    // -U/-u
    private String errorFile = null;
    // -O
    private boolean outputFileOverwrite = false;
    // -U
    private boolean errorFileOverwrite = false;
    // -P
    private Character pathChar = File.separatorChar;
    // -q
    private String sequenceAsString = null;
    private byte[] sequenceAsBytes = null;
    // -r
    private boolean recursive = false;
    // -r <depth>
    private int depth = Integer.MAX_VALUE;
    // -s
    private String separator = null;
    // -t
    private String timestampFormat = null;
    // -v
    private boolean versionWanted = false;
    // -V
    private Verbose verbose;
    // -w
    private String wantedList = null;
    // -
    private boolean stdin = false;
    // --utf8
    private boolean utf8 = false;
    // --line-separator
    private String lineSeparator = System.lineSeparator();

    // --license
    private boolean licenseWanted = false;

    // --copyright
    private boolean copyrightWanted = false;

    // --header
    private boolean headerWanted = false;

    // --bom
    private boolean bom = false;

    // --scan-all-unix-file-types
    private boolean scanAllUnixFileTypes = false;

    // --scan-ntfs-ads
    private boolean scanNtfsAds = false;

    // keeps all the filenames that have been specified at the command line
    private List<String> filenamesFromArgs;

    // --no-path
    private boolean noPath = false;

    // --path-absolute
    private boolean pathAbsolute = false;

    // --path-relative-to
    private Path pathRelativeTo = null;
    private String pathRelativeToAsString = null;

    // --path-relative-to-entry
    private int pathRelativeToEntry = 0;

    // --threads-hashing
    private int threadsHashing = ThreadControl.getThreadsHashing();

    // --threads-reading
    private int threadsReading = ThreadControl.getThreadsReading();

    private final Messenger messenger;

    private int filesizeWanted = -1;

    // --gnu-filename-escaping
    private boolean gnuEscaping = !OSControl.isWindows(); // enabled by default on non-Windows systems
    private boolean gnuEscapingSetByUser = false;

    // implicit parameters

    // keeps all the filenames that have been specified by -c
    private List<String> filenamesFromCheckFile = null;

    private boolean parameterModifiedByAPI = false;


    // ************************************** constructors *********************************************************

    /**
     * Parameterless Parameters Constructor.
     */
    public Parameters() {
        this.filenamesFromArgs = new ArrayList<>();
        verbose = new Verbose();
        messenger = new Messenger(verbose);
        listFilter = new ListFilter();
        wantedListFilter = new WantedListFilter();
    }

    /**
     * Parameters Constructor.
     *
     * @param args all arguments
     * @throws ParameterException if a parameter error occurs
     */
    public Parameters(String[] args) throws ParameterException {
        this();
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
        checkParameters();
        return this;
    }

    public Parameters unchecked() {
        return this;
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

        } else if (getCheckFile() != null || getCheckLine() != null) {
            return ActionType.CHECK;

        } else if (findAlgorithm) {
            return ActionType.FIND_ALGO;

        } else if (isSequence()) {
            return ActionType.QUICK;

        } else if (isList() && algorithm != null) {
            return ActionType.INFO_ALGO;

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
            if (isWantedList()) {
                return ActionType.WANTED_LIST;
            } else {
                return ActionType.HASH_FILES;
            }

        } else { // default
            return ActionType.HELP;
        }
    }

    /**
     * @return the filelistFormat
     */
    public String getFilelistFormat() {
        return filelistFormat;
    }

    /**
     * @param filelistFormat the filelistFormat to set
     */
    public void setFilelistFormat(String filelistFormat) {
        this.filelistFormat = filelistFormat;
    }


    public void setCLIParameters(String[] args) {
        this.cliParameters = args;
    }

    @Override
    public String[] getCLIParameters() {
        return cliParameters;
    }

    public void setParameterModifiedByAPI(boolean parameterModifiedByAPI) {
        this.parameterModifiedByAPI = parameterModifiedByAPI;
    }

    public String[] getCLIParametersWithQuotes() {
        List<String> list = new ArrayList<>();
        // if the parameter object has been modified by the API (e.g. by an GUI),
        // the original args passed to the app aren't valid anymore, we need to
        // build the args by the values of the current parameter object values.
        String[] source = parameterModifiedByAPI ? this.toStringArrayList().toArray(new String[0]) : cliParameters;
        for (String param : source) {
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

    public void setFilenamesFromArgs(List<String> filenamesFromArgs) {
        this.filenamesFromArgs = filenamesFromArgs;
    }

    public void setCommentChars(String commentChars) {
        this.commentChars = commentChars;
    }

    public String getCommentChars() {
        return commentChars;
    }

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

    private String separatorRaw = null;

    private String getSeparatorRaw() {
        return separatorRaw;
    }

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
    public void setDontFollowSymlinksToFiles(boolean dontFollowSymlinksToFiles) {
        this.dontFollowSymlinksToFiles = dontFollowSymlinksToFiles;
    }

    // -r
    @Override
    public boolean isRecursive() {
        return recursive;
    }

    // -r
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    @Override
    public boolean isStdinForFilenamesFromArgs() {
        return stdin;
    }

    public void setStdinForFilenamesFromArgs(boolean stdin) {
        this.stdin = stdin;
    }

    // -x, -X, -E <encoding>
    @Override
    public Encoding getEncoding() {
        return encoding;
    }

    // -x, -X, -E <encoding>
    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    public void setEncoding(String encoding) throws IllegalArgumentException {
        this.encoding = Encoding.string2Encoding(encoding);
    }

    @Override
    public boolean isEncodingSet() {
        return encoding != null;
    }

    // -q
    @Override
    public byte[] getSequenceAsBytes() {
        return sequenceAsBytes;
    }

    public boolean isSequence() {
        return sequenceAsBytes != null;
    }

    public String getSequenceAsString() {
        return sequenceAsString;
    }

    // deprecated, use getSequenceAsBytes()
    public byte[] getSequence() {
        return getSequenceAsBytes();
    }

    // -q
    public void setSequence(String sequence) throws IllegalArgumentException {
        this.sequenceAsString = sequence;
        String indicator = sequence.toLowerCase();

        if (indicator.startsWith("txt:")) {
            this.sequenceAsBytes = sequence2bytes(SequenceType.TXT, sequence.substring(4));
        } else if (indicator.startsWith("txtf:")) {
            this.sequenceAsBytes = sequence2bytes(SequenceType.TXTF, sequence.substring(5));
        } else if (indicator.startsWith("dec:")) {
            this.sequenceAsBytes = sequence2bytes(SequenceType.DEC, sequence.substring(4));
        } else if (indicator.startsWith("hex:")) {
            this.sequenceAsBytes = sequence2bytes(SequenceType.HEX, sequence.substring(4));
        } else if (indicator.startsWith("bin:")) {
            this.sequenceAsBytes = sequence2bytes(SequenceType.BIN, sequence.substring(4));
        } else if (indicator.startsWith("file:")) {
            this.sequenceAsBytes = sequence2bytes(SequenceType.FILE, sequence.substring(5));
        } else {
            this.sequenceAsBytes = sequence2bytes(SequenceType.HEX, sequence);
        }
    }

    // -g
    @Override
    public int getGrouping() {
        return groupcount;
    }

    // -g
    public void setGrouping(int grouping) throws IllegalArgumentException {
        if (grouping < 0) {
            throw new IllegalArgumentException("Grouping is out of range, must be >= 0");
        }
        this.groupcount = grouping;
    }

    public void setGrouping(String grouping) throws IllegalArgumentException {
        try {
            setGrouping(Integer.parseInt(grouping));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public boolean isGroupingSet() {
        return groupcount > 0;
    }

    public void setGroupChar(String string) throws IllegalArgumentException {
        if (string.length() != 1) {
            throw new IllegalArgumentException(string + "is longer than one character");
        } else {
            setGroupChar(string.charAt(0));
        }
    }

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

    public void setPathChar(String arg) throws IllegalArgumentException {
        if (arg.length() != 1) {
            throw new IllegalArgumentException("Exactly one character is required");
        } else {
            setPathChar(arg.charAt(0));
        }
    }

    public void setPathChar(Character arg) throws IllegalArgumentException {
        if (arg == '/' || arg == '\\') {
            pathChar = arg;
        } else {
            throw new IllegalArgumentException("Option -P requires / or \\");
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

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String getCheckFile() {
        return checkFile;
    }

    public void setCheckFile(String checkFile) {
        this.checkFile = checkFile;
    }

    @Override
    public String getExpectedString() {
        return expected;
    }

    @Override
    public byte[] getExpectedBytes() throws UnsupportedOperationException {
        if (expectedAsBytes == null && isEncodingSet() && getEncoding().equals(Encoding.HEX)
                && !isGroupingSet()) {
            expectedAsBytes = sequence2bytes(SequenceType.HEX, expected);
        } else {
            throw new UnsupportedOperationException();
            // TODO: transform BubbleBabble to bytes
            // TODO: transform Hex with grouping chars to bytes
            // TODO: transform dec, oct, base32, base64, etc. to bytes
        }
        return expectedAsBytes;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    @Override
    public boolean isExpectation() {
        return getExpectedString() != null;
    }

    @Override
    public String getTimestampFormat() {
        return timestampFormat;
    }

    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
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
     * @param helpLanguage the language of the help
     */
    public void setHelpLanguage(String helpLanguage) {
        this.helpLanguage = helpLanguage;
    }

    @Override
    public String getHelpSearchString() {
        return helpSearchString;
    }

    public void setHelpSearchString(String helpSearchString) {
        this.helpSearchString = helpSearchString;
    }

    @Override
    public boolean isAlternateImplementationWanted() {
        return alternate;
    }

    public void setAlternateImplementationWanted(boolean alternate) {
        this.alternate = alternate;
    }

    @Override
    public String getAlgorithmIdentifier() {
        return algorithmIdentifier;
    }

    public boolean isAlgorithmSetByUser() {
        return algorithm != null;
    }

    public String getAlgorithm() {
        return algorithm;
    }


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

    public void setList(boolean list) {
        this.list = list;
    }

    @Override
    public boolean isDontFollowSymlinksToDirectories() {
        return dontFollowSymlinksToDirectories;
    }

    public void setDontFollowSymlinksToDirectories(boolean dontFollowSymlinksToDirectories) {
        this.dontFollowSymlinksToDirectories = dontFollowSymlinksToDirectories;
    }

    public String getOutputFile() {
        return outputFile;
    }

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

    public void setScanNtfsAds(boolean scanNtfsAds) {
        this.scanNtfsAds = scanNtfsAds;
    }

    public void setScanAllUnixFileTypes(boolean scanAllUnixFileTypes) {
        this.scanAllUnixFileTypes = scanAllUnixFileTypes;
    }

    public void setErrorFile(String errorFile) {
        this.errorFile = errorFile;
    }

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

    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    public boolean isBom() {
        return bom;
    }

    public void setBom(boolean bom) {
        this.bom = bom;
    }

    public boolean isHeaderWanted() {
        return headerWanted;
    }

    public void setHeaderWanted(boolean headerWanted) {
        this.headerWanted = headerWanted;
    }

    public boolean isPathAbsolute() {
        return pathAbsolute;
    }

    public void setPathAbsolute(boolean pathAbsolute) {
        this.pathAbsolute = pathAbsolute;
    }

    public void setNoPath(boolean noPath) {
        this.noPath = noPath;
    }

    public String getPathRelativeToAsString() {
        return pathRelativeToAsString;
    }

    public void setPathRelativeToAsString(String pathRelativeToAsString) {
        this.pathRelativeToAsString = pathRelativeToAsString;
    }

    public Path getPathRelativeTo() {
        return pathRelativeTo;
    }

    public void setPathRelativeTo(Path pathRelativeTo) {
        this.pathRelativeTo = pathRelativeTo;
    }

    public int getThreadsHashing() {
        return threadsHashing;
    }

    public void setThreadsHashing(int threadsHashing) {
        this.threadsHashing = threadsHashing;
        ThreadControl.setThreadsHashing(threadsHashing);
    }

    public int getThreadsReading() {
        return threadsReading;
    }

    public void setThreadsReading(int threadsReading) {
        this.threadsReading = threadsReading;
        ThreadControl.setThreadsReading(threadsReading);
    }

    public int getPathRelativeToEntry() {
        return pathRelativeToEntry;
    }

    public boolean isPathRelativeToEntry() {
        return pathRelativeToEntry > 0;
    }

    public void setPathRelativeToEntry(int number) {
        this.pathRelativeToEntry = number;
    }


    public void setFilesizeWanted(int filesizeWanted) {
        this.filesizeWanted = filesizeWanted;
    }

    public int getFilesizeWanted() {
        return filesizeWanted;
    }

    public void setFilesizeWanted(boolean filesizeWanted) {
        this.filesizeWanted = filesizeWanted ? 1 : 0;
    }

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

    public void setGnuEscaping(boolean gnuEscaping) {
        this.gnuEscaping = gnuEscaping;
        this.gnuEscapingSetByUser = true;
    }

    public void setGnuEscapingToDefault() {
        this.gnuEscaping = !OSControl.isWindows();
        this.gnuEscapingSetByUser = false;
    }

    public String getWantedList() {
        return wantedList;
    }

    public boolean isWantedList() {
        return wantedList != null;
    }

    public void setWantedList(String wantedList) {
        this.wantedList = wantedList;
    }

    public String getCharsetWantedList() {
        return charsetWantedList;
    }

    public void setCharsetWantedList(String charsetWantedList) {
        this.charsetWantedList = charsetWantedList;
    }

    public WantedListFilter getWantedListFilter() {
        return wantedListFilter;
    }

    public void setWantedListFilter(WantedListFilter wantedListFilter) {
        this.wantedListFilter = wantedListFilter;
    }

    enum SequenceType {
        TXT, TXTF, DEC, HEX, BIN, FILE
    }

    public boolean isOutputFile() {
        return outputFile != null;
    }

    public boolean isOutputFileOverwrite() {
        return outputFileOverwrite;
    }

    public boolean isErrorFile() {
        return errorFile != null;
    }

    public boolean isErrorFileOverwrite() {
        return errorFileOverwrite;
    }

    public boolean isFindAlgorithm() {
        return findAlgorithm;
    }

    public String getFilelistFilename() {
        return filelistFilename;
    }

    /**
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
     * @param charsetCheckFile the charsetCheckFile to set
     */
    public void setCharsetCheckFile(String charsetCheckFile) {
        this.charsetCheckFile = charsetCheckFile;
    }

    /**
     * @return the charsetListFile
     */
    public String getCharsetFileList() {
        return charsetFileList;
    }

    /**
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
     * @return the charsetErrorFile
     */
    public String getCharsetErrorFile() {
        return charsetErrorFile;
    }

    /**
     * @param charsetErrorFile the charsetErrorFile to set
     */
    public void setCharsetErrorFile(String charsetErrorFile) {
        this.charsetErrorFile = charsetErrorFile;
    }

    /**
     * @return the charsetOutputFile
     */
    public String getCharsetOutputFile() {
        return charsetOutputFile;
    }

    /**
     * @param charsetOutputFile the charsetOutputFile to set
     */
    public void setCharsetOutputFile(String charsetOutputFile) {
        this.charsetOutputFile = charsetOutputFile;
    }

    /**
     * @return the charsetStdout
     */
    public String getCharsetStdout() {
        return charsetStdout;
    }

    /**
     * @param charsetStdout the charsetStdout to set
     */
    public void setCharsetStdout(String charsetStdout) {
        this.charsetStdout = charsetStdout;
    }

    /**
     * @return the charsetStderr
     */
    public String getCharsetStderr() {
        return charsetStderr;
    }

    /**
     * @param charsetStderr the charsetStderr to set
     */
    public void setCharsetStderr(String charsetStderr) {
        this.charsetStderr = charsetStderr;
    }


    /**
     * @return the versionWanted
     */
    public boolean isVersionWanted() {
        return versionWanted;
    }

    /**
     * @param versionWanted the versionWanted to set
     */
    public void setVersionWanted(boolean versionWanted) {
        this.versionWanted = versionWanted;
    }

    /**
     * @param errorFileOverwrite the errorFileOverwrite to set
     */
    public void setErrorFileOverwrite(boolean errorFileOverwrite) {
        this.errorFileOverwrite = errorFileOverwrite;
    }

    /**
     * @param outputFileOverwrite the outputFileOverwrite to set
     */
    public void setOutputFileOverwrite(boolean outputFileOverwrite) {
        this.outputFileOverwrite = outputFileOverwrite;
    }

    /**
     * @return the utf8
     */
    public boolean isUtf8() {
        return utf8;
    }

    /**
     * @param utf8 the utf8 to set
     */
    public void setUtf8(boolean utf8) {
        this.utf8 = utf8;
    }

    /**
     * @return the help
     */
    public boolean isHelp() {
        return help;
    }

    /**
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
     * @param listFilter the listFilter to set
     */
    public void setListFilter(ListFilter listFilter) {
        this.listFilter = listFilter;
    }

    /**
     * @return the checkLine
     */
    public String getCheckLine() {
        return checkLine;
    }

    /**
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
     * @param stdinName the stdinNameForOutput to set
     */
    public void setStdinName(String stdinName) {
        this.stdinName = stdinName;
    }

    public boolean isLicenseWanted() {
        return licenseWanted;
    }

    public void setLicenseWanted(boolean licenseWanted) {
        this.licenseWanted = licenseWanted;
    }

    public boolean isCopyrightWanted() {
        return copyrightWanted;
    }

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
        if (newParameters.isHeaderWanted()) {
            this.setHeaderWanted(true);
        }
        if (newParameters.getCheckFile() != null) {
            this.setCheckFile(newParameters.getCheckFile());
        }
        if (newParameters.getCheckLine() != null) {
            this.setCheckLine(newParameters.getCheckLine());
        }
        if (newParameters.isCheckStrict()) {
            this.setCheckStrict(true);
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
            this.setSequence(newParameters.getSequenceAsString());
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
        if (newParameters.getThreadsHashing() != ThreadControl.getThreadsMax()) {
            this.setThreadsHashing(newParameters.getThreadsHashing());
        }
        if (newParameters.getThreadsReading() > 1) {
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
        if (headerWanted) {
            list.add(__HEADER);
        }
        if (checkFile != null) {
            list.add(_CHECK_FILE);
            list.add(checkFile);
        }
        if (checkLine != null) {
            list.add(__CHECK_LINE);
            list.add(checkLine);
        }
        if (checkStrict) {
            list.add(__CHECK_STRICT);
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
        if (wantedListFilter.isFilterHasBeenSet()) {
            list.add(__WANTED_LIST_FILTER);
            list.add(wantedListFilter.toString());
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
            list.add(getSequenceAsString());
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
        if (getThreadsHashing() != ThreadControl.getThreadsMax()) {
            list.add(__THREADS_HASHING);
            list.add(String.valueOf(getThreadsHashing()));
        }
        if (getThreadsReading() > 1) {
            list.add(__THREADS_READING);
            list.add(String.valueOf(getThreadsReading()));
        }
        if (isTimestampWanted()) {
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

    // ignore/disable unsupported/unsuitable/incompatible parameters
    public void checkParameters() throws ParameterException, ExitException {
        expandFileList();
        handleCharsets();
        checkForNonsenseParameterCombinations();
        handleCompatibility();

        // validity check for --algorithm
        if (algorithm != null) {
            if (algorithm.startsWith("+")) {
                throw new ParameterException(String.format("The algorithm %s must not start with a + sign, but it can end with one.", algorithm));
            }
            if (algorithm.contains("++")) {
                throw new ParameterException(String.format("The algorithm %s must not contain ++.", algorithm));
            }
        }

        // validity check for --path-relative-to-entry
        if (isPathRelativeToEntry() && getFilenamesFromFilelist().size() > 0) {
            setPathRelativeToAsString(getFilenamesFromFilelist().get(getPathRelativeToEntry()-1));
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
                    throw new ParameterException(String.format("%s does not exist.\n", pathRelativeToAsString));
                }
            } catch (InvalidPathException ipe) {
                throw new ParameterException(String.format("%s is an invalid path.\n", pathRelativeToAsString));
            }
        }

        handleWarningsAndImplicitSettings();

    }


    // ************************************** private methods *********************************************************

    private static String decodeQuote(String format) {
        return GeneralString.replaceAllStrings(format, "#QUOTE", "\"");
    }

    private static String decodeSeparator(String format, String separator) {
        if (separator != null) {
            format = GeneralString.replaceAllStrings(format, "#SEPARATOR", separator);
        }
        return format;
    }

    private byte[] sequence2bytes(SequenceType sequenceType, String sequence)
            throws IllegalArgumentException {
        byte[] bytes;
        switch (sequenceType) {
            case TXT:
                bytes = ByteSequences.text2Bytes(sequence);
                break;
            case TXTF:
                bytes = ByteSequences.textf2Bytes(sequence);
                break;
            case DEC:
                bytes = ByteSequences.decText2Bytes(sequence);
                break;
            case HEX:
                bytes = ByteSequences.hexText2Bytes(sequence);
                //System.out.println(Service.format(bytes));
                break;
            case BIN:
                bytes = ByteSequences.binText2Bytes(sequence);
                break;
            case FILE:
                try {
                    Path p = Path.of(sequence);
                    if (Files.exists(p)) {
                        if (Files.size(p) > 128 * 1024 * 1024) {
                            throw new IllegalArgumentException(String.format("File %s is greater than 128 MiB which exceeds the limit for option -q file:<file>", sequence));
                        }
                        bytes = Files.readAllBytes(p);
                    } else {
                        throw new IllegalArgumentException(String.format("File %s does not exist.", p));
                    }
                } catch (IOException ioe) {
                    throw new IllegalArgumentException(ioe.getMessage());
                }
                break;
            default:
                throw new IllegalArgumentException("unknown sequence type: " + sequenceType);
        }
        return bytes;
    }

    public void restoreStdOut() {
        System.setOut(stdOutBackup);
    }

    public void restoreStdErr() {
        System.setErr(stdErrBackup);
    }

    private void handleCharsets() throws ParameterException, ExitException {
        if (isUtf8()) {
            setCharsetStdout(UTF_8);
            setCharsetStderr(UTF_8);
        }

        if (getCharsetStdout() != null) {
            // change stdout
            try {
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, getCharsetStdout()));
            } catch (UnsupportedEncodingException e) {
                throw new ExitException(String.format("Encoding %s for stdout is not supported by your JVM or OS.", getCharsetStdout()), ExitCode.IO_ERROR);
            }
        }

        if (getCharsetStderr() != null) {
            // change stderr
            try {
                System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, getCharsetStderr()));
            } catch (UnsupportedEncodingException e) {
                throw new ExitException(String.format("Encoding %s for stdout is not supported by your JVM or OS.", getCharsetStderr()), ExitCode.IO_ERROR);
            }
        }

        boolean outputFileAndErrorFileAreEqual = false;

        if (isOutputFile() && isErrorFile()) {
            Path outputFilePath = Paths.get(getOutputFile()).toAbsolutePath().normalize();
            Path errorFilePath = Paths.get(getErrorFile()).toAbsolutePath().normalize();
            outputFileAndErrorFileAreEqual = outputFilePath.equals(errorFilePath);
        }

        if (outputFileAndErrorFileAreEqual && !charsetOutputFile.equals(charsetErrorFile)) {
            throw new ParameterException("Output and error file are the same, but character sets for output and error file have been set differently.");
        }

        PrintStream streamShared = null;
        boolean isShared = false;
        if (outputFileAndErrorFileAreEqual) {
            try {
                streamShared = new PrintStream(new FileOutputStream(getOutputFile()), true, getCharsetOutputFile());
                isShared = true;
            } catch (UnsupportedEncodingException | FileNotFoundException e) {
                //Logger.getLogger(Parameters.class.getName()).log(Level.SEVERE, null, e);
                throw new ExitException(e.getMessage(), ExitCode.IO_ERROR);
            }
        }

        if (isOutputFile()) {
            try {
                File f = new File(getOutputFile());
                if (!isOutputFileOverwrite() && f.exists()) {
                    throw new ExitException(String.format("Jacksum: Error: the file %s already exists. Specify the file by -O to overwrite it.", f), ExitCode.IO_ERROR);
                }

                if (isShared) {
                    System.setOut(streamShared);
                } else {
                    PrintStream out;
                    out = new PrintStream(new FileOutputStream(getOutputFile()), true, getCharsetOutputFile());
                    //PrintStream tee = new TeeStream(System.out, out);
                    System.setOut(out);
                }
            } catch (UnsupportedEncodingException | FileNotFoundException | ExitException e) {
                // Logger.getLogger(Parameters.class.getName()).log(Level.SEVERE, null, e);
                throw new ExitException(e.getMessage(), ExitCode.IO_ERROR);
            }
        }

        if (isErrorFile()) {
            try {
                File f = new File(getErrorFile());
                if (!isErrorFileOverwrite() && f.exists()) {
                    throw new ExitException(String.format("Jacksum: Error: the file %s already exists. Specify the file by -U to overwrite it.", f), ExitCode.IO_ERROR);
                }

                if (isShared) {
                    System.setErr(streamShared);
                } else {
                    PrintStream err;
                    err = new PrintStream(new FileOutputStream(getErrorFile()), true, getCharsetErrorFile());
                    //PrintStream tee = new TeeStream(System.out, err);
                    System.setErr(err);
                }
            } catch (UnsupportedEncodingException | FileNotFoundException | ExitException e) {
                Logger.getLogger(Parameters.class.getName()).log(Level.SEVERE, null, e);
                throw new ExitException(e.getMessage(), ExitCode.IO_ERROR);
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


    private void checkForNonsenseParameterCombinations() throws ParameterException, ExitException {
        // exit if selected parameters make no sense
        if ((expected != null) && getAlgorithmIdentifier().equals("none")) {
            throw new ParameterException("-a none and -e cannot go together.");
        }

        if (stdin && isSequence()) {
            throw new ParameterException("Cannot read from both standard input and -q.");
        }

        if (findAlgorithm) {
            if (sequenceAsBytes == null) {
                throw new ParameterException("Option -a unknown:<width> requires option -q");
            }
            if (expected == null) {
                throw new ParameterException("Option -a unknown:<width> requires option -e");
            }
            if (encoding == null) {
                throw new ParameterException("Option -a unknown:<width> requires option -E");
            }
        }


        if (getCheckFile() != null && !getCheckFile().equals("-")) { // the - means: read from stdin
            File f = new File(getCheckFile());
            if (f.isDirectory()) {
                throw new ParameterException(String.format("Parameter -c %s is a directory, but a filename was expected.", getCheckFile()));
            }
            if (!f.exists()) {
                throw new ExitException(String.format("Jacksum: %s: No such file or directory. Exit.", getCheckFile()), ExitCode.IO_ERROR);
            }
        }

        int pathOptions = 0;
        if (isPathAbsolute()) {
            pathOptions++;
        }
        if (getPathRelativeToAsString() != null) {
            pathOptions++;
        }
        if (isNoPath()) {
            pathOptions++;
        }
        if (pathRelativeToEntry > 0) {
            pathOptions++;
        }
        if (pathOptions > 1) {
            throw new ParameterException(String.format("Only one of the following options is allowed: %s, %s, %s, or %s",
                    __NO_PATH, __PATH_ABSOLUTE, __PATH_RELATIVE_TO, __PATH_RELATIVE_TO_ENTRY));
        }

        try {
            if (timestampFormat != null &&
                    !timestampFormat.equals("default") &&
                    !timestampFormat.equals("unixtime") &&
                    !timestampFormat.equals("unixtime-ms") &&
                    !timestampFormat.equals("iso8601")) {
                // #QUOTE and #SEPARATOR should be replaced
                this.timestampFormat = decodeQuote(this.timestampFormat);
                this.timestampFormat = decodeSeparator(this.timestampFormat, this.separator);
                // test, if the timestampformat is valid
                Format timestampFormatter = new SimpleDateFormat(this.timestampFormat);
                // ... ignore the return value, just force an IllegalArgumentException if format is invalid
                timestampFormatter.format(new Date());
            }
        } catch (IllegalArgumentException e) {
            throw new ExitException(e.getMessage(), ExitCode.PARAMETER_ERROR);
        }

        if (isPathRelativeToEntry() && getFilelistFilename() == null) {
            throw new ParameterException(String.format("Option %s requires option %s", __PATH_RELATIVE_TO_ENTRY, __FILE_LIST));
        }

    }


    private void handleWarningsAndImplicitSettings() {

        // warnings
        if (groupcount > 0 && encoding == null) {
            setEncoding(Encoding.HEX);
            messenger.print(WARNING, "-g has been set, but -E has not been set. Setting -E hex implicitly.");
        }

        if ((groupcount > 0) && (encoding != null && !encoding.equals(Encoding.HEX) && !encoding.equals(Encoding.HEX_UPPERCASE))) {
            messenger.print(WARNING, "-g expects a hex encoding, but -E is not set to hex or hex-uppercase. Ignoring -g.");
        }

        if (groupingChar != null && encoding == null) {
            setEncoding(Encoding.HEX);
            messenger.print(WARNING, "-G has been set, but -E has not bee set. Setting -E hex implicitly.");
        }

        if ((groupingChar != null) && (encoding != null && !encoding.equals(Encoding.HEX) && !encoding.equals(Encoding.HEX_UPPERCASE))) {
            messenger.print(WARNING, "-G expects a hex encoding, but -E is not set to hex or hex-uppercase. Ignoring -G.");
        }

        // ignoring flags
        if (checkFile != null && format != null) {
            messenger.print(WARNING, "Option -F will be ignored, because option -c is used.");
            setFormat(null);
        }

        // both timestamp and sequence have been specified
        if (timestampFormat != null && sequenceAsBytes != null) {
            messenger.print(WARNING, "A sequence (-q) has been specified, timestamp (-t) will be ignored.");
        }

        // verification mode, but format has been set using -F <format>
        if (checkFile != null && format != null) {
            format = null;
            messenger.print(WARNING, "Ignoring -F, because -c has been specified.");
        }

        // verification mode, no compat file has been given, and no algorithm id
        if (checkFile != null && compatibilityID == null && algorithm == null) {
            setAlgorithm(ALGORITHM_IDENTIFIER_DEFAULT);
            messenger.print(INFO, String.format("Option -a has not been given, it is set implicitly to %s. Alternatively set -C <compatibility>.", ALGORITHM_IDENTIFIER_DEFAULT));
        }

        if (stdin && timestampFormat != null) {
            setTimestampFormat(null);
            messenger.print(WARNING, String.format("Option %s has been ignored, because standard input is used.", __TIMESTAMP));
        }

        if (this.isGnuEscapingSetByUser() && this.isGnuEscaping() && OSControl.isWindows()) {
            gnuEscaping = false;
            gnuEscapingSetByUser = false;
            messenger.print(WARNING, String.format("Ignoring option %s, because GNU file name escaping is not supported on Microsoft Windows.", __GNU_FILENAME_ESCAPING));
        }

        // implicit settings
        if (isRecursive() && getFilenamesFromArgs().isEmpty() && getFilenamesFromFilelist().isEmpty()) {
            messenger.print(WARNING, "Option -r has been set, but no files have been given, reading files recursively, starting with current working directory ...");
            filenamesFromArgs.add(".");
        }

        if (!isHelp()
                && !isLicenseWanted()
                && !isCopyrightWanted()
                && sequenceAsBytes == null
                && getFilenamesFromArgs().isEmpty()
                && getFilenamesFromFilelist().isEmpty()
                && checkFile == null
                && checkLine == null
                && !isRecursive()
                && !stdin
                && !infoMode
                && !list
                && !versionWanted) {
            messenger.print(WARNING, "No files have been specified, reading from standard input stream (stdin) ...");
            stdin = true;
        }

    }

    private void handleCompatibility() throws ExitException {

        if (this.getCompatibilityID() != null) {
            try {
                compatibilityProperties = new CompatibilityProperties(this.getCompatibilityID());

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

                if (this.isGnuEscapingSetByUser()) {
                    // if the style allows overwriting GnuEscaping and GnuEscaping has been set using --gnu-filename-escaping ...
                    if (compatibilityProperties.isGnuEscapingSupported() && compatibilityProperties.isGnuEscapingUserSelectable()) {
                        // ... we overwrite the default in the compatibilityProperties object ...
                        compatibilityProperties.setGnuEscapingEnabled(this.isGnuEscaping());
                    } else {
                        messenger.print(WARNING, String.format("Ignoring option %s, because the style \"%s\" doesn't support or allow to enable GNU escaping.", __GNU_FILENAME_ESCAPING, compatibilityID));
                    }
                }

                if (this.isFilesizeWantedSet()) {
                    if (!compatibilityProperties.isFilesizeSupported()) {
                        messenger.print(WARNING, String.format("Ignoring option %s, because the style \"%s\" doesn't support file sizes.", __FILESIZE, compatibilityID));
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
                        messenger.print(INFO, String.format("Option --compat/-style has been set, setting implicitly -a %s -E %s -F \"%s\", stdin-name=%s",
                                compatibilityProperties.getHashAlgorithm(),
                                compatibilityProperties.getHashEncoding(),
                                fmt,
                                compatibilityProperties.getStdinName()));

                        this.setFormat(fmt);
                    }
                }
                this.setAlgorithm(compatibilityProperties.getHashAlgorithm());
                this.setEncoding(compatibilityProperties.getHashEncoding());
                this.setStdinName(compatibilityProperties.getStdinName());
                this.setLineSeparator(compatibilityProperties.getLineSeparator());
                gnuEscaping = compatibilityProperties.isGnuEscapingEnabled();
                if (this.getCommentChars() == null && compatibilityProperties.getIgnoreLinesStartingWithString() != null) {
                    this.setCommentChars(compatibilityProperties.getIgnoreLinesStartingWithString());
                }

                AbstractChecksum.setStdinName(compatibilityProperties.getStdinName());

            } catch (IOException | InvalidCompatibilityPropertiesException ex) {
                throw new ExitException("Jacksum: " + ex.getMessage(), ExitCode.IO_ERROR);
            }
        } else { // -C hasn't been set, we want to use the default output formatter

            // on Linux and Unix: enable GNU filename escaping for the default output formatter
            // if the user didn't make a selection explicitly on GNU filename escaping
            if (!OSControl.isWindows() && !this.isGnuEscapingSetByUser()) {
                setGnuEscaping(true);
            }
        }
    }


    public void expandFileList() throws ParameterException {
        // processing list that has been specified with -L
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
                        Help.printHelp(HELP_DEFAULT_LANGUAGE, __FILE_LIST_FORMAT);
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
                        Help.printHelp(HELP_DEFAULT_LANGUAGE, __FILE_LIST_FORMAT);
                        throw new ParameterException(String.format("File list format \"%s\" is unsupported.", this.getFilelistFormat()));
                    }
                }
            } catch (UnsupportedCharsetException uce) {
                throw new ParameterException(String.format("Charset \"%s\" is unsupported. Check the supported character sets with jacksum --info.", this.getCharsetFileList()));
            } catch (IOException ex) {
                throw new ParameterException(String.format("File %s not found or cannot be read.", this.getFilelistFilename()));
            }
        }
    }

}

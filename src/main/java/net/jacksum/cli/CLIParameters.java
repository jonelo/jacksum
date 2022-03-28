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
package net.jacksum.cli;

import net.jacksum.actions.help.Help;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;
import net.jacksum.multicore.ThreadControl;
import net.jacksum.parameters.ParameterException;
import net.jacksum.parameters.Parameters;
import org.n16n.sugar.io.GeneralIO;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * All parameters for the Command Line Interface (CLI)
 *
 * @author Johann N. Loefflmann
 */
public class CLIParameters {

    String[] args;

    public static final String LONG_OPTION__ALGORITHM = "--algorithm";
    public static final String SHORT_OPTION__ALGORITHM = "-a";
    public static final String LONG_OPTION__ALTERNATIVE = "--alternative";
    public static final String SHORT_OPTION__ALTERNATIVE = "-A";


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
            while (firstfile < args.length && args[firstfile].startsWith("-") && !dashdash) {
                arg = args[firstfile++];

                // --algorithm
                if (arg.equals(SHORT_OPTION__ALGORITHM) || arg.equals(LONG_OPTION__ALGORITHM)) {
                    if (firstfile < args.length) {
                        parameters.setAlgorithm(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, LONG_OPTION__ALGORITHM);
                    }

                // --alternative
                } else if (arg.equals(SHORT_OPTION__ALTERNATIVE) || arg.equals(LONG_OPTION__ALTERNATIVE)) {
                    parameters.setAlternateImplementationWanted(true);

                // --absolute
                } else if (arg.equals("--path-absolute") || (arg.equals("--absolute"))) {
                    parameters.setPathAbsolute(true);

                } else if (arg.equals("--path-relative-to") || arg.equals("--relative-to")) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        parameters.setPathRelativeToAsString(arg);
                    } else {
                        handleUserParamError(arg, "--path-relative-to");
                    }

                } else if (arg.equals("--path-relative-to-entry") || arg.equals("--relative-to-entry")) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        try {
                            int number = Integer.parseInt(arg);
                            if (number < 1) {
                                throw new ParameterException("line number has to be > 0.");
                            }
                            parameters.setPathRelativeToLine(number);
                        } catch (NumberFormatException nfe) {
                            throw new ParameterException(nfe.getMessage());
                        }
                    } else {
                        handleUserParamError(arg, "--path-relative-to-entry");
                    }

                } else if (arg.equals("-8") || arg.equals("--utf8")) {
                    parameters.setUtf8(true);

                } else if (arg.equals("-")) {
                    parameters.setStdinForFilenamesFromArgs(true);

                } else if (arg.equals("--")) {
                    dashdash = true;

                } else if (arg.equals("--copyright")) {
                    parameters.setCopyrightWanted(true);

                } else if (arg.equals("--license")) {
                    parameters.setLicenseWanted(true);

                } else if (arg.equals("--header")) {
                    parameters.setHeaderWanted(true);

                } else if (arg.equals("-c") || arg.equals("--check-file")) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        parameters.setCheckFile(arg);
                        parameters.getVerbose().enableAll();
                    } else {
                        handleUserParamError(arg, "--check-file");
                    }

                } else if (arg.equals("--check-line")) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        parameters.setCheckLine(arg);
                        parameters.getVerbose().enableAll();
                    } else {
                        handleUserParamError(arg, "--check-line");
                    }

                } else if (arg.equals("--check-strict")) {
                    parameters.setCheckStrict(true);

                } else if (arg.equals("-C") || arg.equals("--compat") || arg.equals("--style")) {
                    if (firstfile < args.length) {
                        parameters.setCompatibilityID(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, "--compat");
                    }

                } else if (arg.equals("--bom")) {
                    parameters.setBom(true);

                } else if (arg.equals("-d") || arg.equals("--dontFollowSymlinksToDirectories")) {
                    parameters.setDontFollowSymlinksToDirectories(true);

                } else if (arg.equals("-e") || (arg.equals("--expect-hash"))) {
                    if (firstfile < args.length) {
                        parameters.setExpected(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, "--expect-hash");
                    }

                } else if (arg.equals("-E") || arg.equals("--encoding")) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        try {
                            parameters.setEncoding(arg);
                        } catch (IllegalArgumentException e) {
                            throw new ParameterException(e.getMessage());
                        }
                    } else {
                        handleUserParamError(arg, "--encoding");
                    }

                } else if (arg.equals("-f") || arg.equals("--dont-follow-symlinks-to-files")) {
                    parameters.setDontFollowSymlinksToFiles(true);

                } else if (arg.equals("--filesize")) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];

                        if (arg.equals("yes") || arg.equals("on") || arg.equals("true") || arg.equals("1") || arg.equals("enabled")) {
                            parameters.setFilesizeWanted(true);
                        } else
                        if (arg.equals("no") || arg.equals("off") || arg.equals("false") || arg.equals("0") || arg.equals("disabled")) {
                            parameters.setFilesizeWanted(false);
                        } else
                        if (arg.equals("default")) {
                            parameters.setFilesizeWanted(-1);
                        } else {
                            throw new ParameterException("filesize value is invalid.");
                        }
                    } else {
                        handleUserParamError(arg, "--filesize");
                    }

                } else if (arg.equals("-F") || arg.equals("--format")) {
                    if (firstfile < args.length) {
                        parameters.setFormat(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, "--format");
                    }

                } else if (arg.equals("-g") || arg.equals("--group-bytes")) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        try {
                            parameters.setGrouping(arg);
                        } catch (IllegalArgumentException e) {
                            throw new ParameterException(arg + " is not a decimal number.");
                        }
                    } else {
                        handleUserParamError(arg, "--group-bytes");
                    }

                } else if (arg.equals("-G") || arg.equals("--group-bytes-separator")) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        try {
                            parameters.setGroupChar(arg);
                        } catch (IllegalArgumentException e) {
                            throw new ParameterException(e.getMessage());
                        }
                    } else {
                        handleUserParamError(arg, "--group-bytes-separator");
                    }

                } else if (arg.equals("-h") || arg.equals("--help")) {
                    // default inits
                    parameters.setHelp(true);
                    parameters.setHelpLanguage("en");
                    parameters.setHelpSearchString(null);
                    if (firstfile < args.length) {
                        String helpLanguageOrSearchString = args[firstfile++];

                        if (helpLanguageOrSearchString.equalsIgnoreCase("en")) {
                            parameters.setHelpLanguage(helpLanguageOrSearchString);
                            if (firstfile < args.length) {
                                parameters.setHelpSearchString(args[firstfile++]);
                            }
                        } else {
                            parameters.setHelpSearchString(helpLanguageOrSearchString);
                        }
                    }

                } else if (arg.equals("--info")) {
                    parameters.setInfoMode(true);

                } else if (arg.equals("-I") || arg.equals("--ignore-lines-starting-with-string")) {
                    if (firstfile < args.length) {
                        parameters.setCommentChars(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, "--ignore-lines-starting-with-string");
                    }

                } else if (arg.equals("-l") || arg.equals("--list")) {
                    parameters.setList(true);

                } else if (arg.equals("--list-filter")) {
                    if (firstfile < args.length) {
                        try {
                            parameters.getListFilter().setFilter(args[firstfile++]);
                        } catch (IllegalArgumentException e) {
                            Help.printHelp("en", "--list-filter");
                            throw new ParameterException(e.getMessage());
                        }
                    } else {
                        handleUserParamError(arg, "--list-filter");
                    }

                } else if (arg.equals("--legacy-stdin-name")) {
                    parameters.setStdinName("-");
                    AbstractChecksum.setStdinName("-");

                } else if (arg.equals("-L") || arg.equals("--file-list")) {
                    if (firstfile < args.length) {
                        parameters.setFilelistFilename(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, "--file-list");
                    }

                } else if (arg.equals("--file-list-format")) {
                    if (firstfile < args.length) {
                        parameters.setFilelistFormat(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, "--file-list-format");
                    }

                } else if (arg.equals("--no-path")) {
                    parameters.setNoPath(true);

                } else if (arg.equals("-o") || arg.equals("--output-file")) {
                    parameters.setOutputFileOverwrite(false);

                    if (firstfile < args.length) {
                        parameters.setOutputFile(args[firstfile++]);

                    } else {
                        handleUserParamError(arg, "--output-file");
                    }

                } else if (arg.equals("-O") || arg.equals("--output-file-overwrite")) {
                    parameters.setOutputFileOverwrite(true);

                    if (firstfile < args.length) {
                        parameters.setOutputFile(args[firstfile++]);

                    } else {
                        handleUserParamError(arg, "--output-file-overwrite");
                    }

                } else if (arg.equals("-P") || arg.equals("--path-separator")) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        try {
                            parameters.setPathChar(arg);
                        } catch (IllegalArgumentException e) {
                            throw new ParameterException(e.getMessage());
                        }
                    } else {
                        handleUserParamError(arg, "--path-separator");
                    }

                } else if (arg.equals("-q") || arg.equals("--quick")) {
                    if (firstfile < args.length) {
                        arg = args[firstfile++];
                        try {
                            parameters.setSequence(arg);
                        } catch (IllegalArgumentException e) {
                            throw new ParameterException(e.getMessage());
                        }
                    } else {
                        handleUserParamError(arg, "--quick");
                    }

                } else if (arg.equals("-r") || arg.equals("--recursive")) {
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
                        handleUserParamError(arg, "--recursive");
                    }

                } else if (arg.equals("--scan-all-unix-file-types")) {
                    parameters.setScanAllUnixFileTypes(true);

                } else if (arg.equals("--scan-ntfs-ads")) {
                    parameters.setScanNtfsAds(true);

                } else if (arg.equals("-s") || arg.equals("--separator")) {
                    if (firstfile < args.length) {
                        parameters.setSeparator(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, "--separator");
                    }

                } else if (arg.equals("--threads-hashing")) {
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
                        handleUserParamError(arg, "--threads-hashing");
                    }

                } else if (arg.equals("--threads-reading")) {
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
                        handleUserParamError(arg, "--threads-reading");
                    }

                } else if (arg.equals("-t") || arg.equals("--timestamp")) {
                    if (firstfile < args.length) {
                        try {
                            parameters.setTimestampFormat(args[firstfile++]);
                        } catch (IllegalArgumentException e) {
                            throw new ParameterException("Option -t is wrong (" + e.getMessage() + ")");
                        }
                    } else {
                        handleUserParamError(arg, "--timestamp");
                    }

                } else if (arg.equals("-u") || arg.equals("--error-file")) {
                    parameters.setErrorFileOverwrite(false);

                    if (firstfile < args.length) {
                        parameters.setErrorFile(args[firstfile++]);

                    } else {
                        handleUserParamError(arg, "--error-file");
                    }

                } else if (arg.equals("-U") || arg.equals("--error-file-overwrite")) {
                    parameters.setErrorFileOverwrite(true);
                    if (firstfile < args.length) {
                        parameters.setErrorFile(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, "--error-file-overwrite");
                    }

                } else if (arg.equals("-v") || arg.equals("--version")) {
                    parameters.setVersionWanted(true);
                    parameters.getVerbose().setInfo(false); // no details if we don't want details explicitly

                } else if (arg.equals("-V") || arg.equals("--verbose")) {
                    if (firstfile < args.length) {
                        verboseControl = args[firstfile++];
                    } else {
                        handleUserParamError(arg, "--verbose");
                    }

                } else if (arg.equals("-x") || arg.equals("--hex-lowercase")) {
                    parameters.setEncoding(Encoding.HEX);

                } else if (arg.equals("-X") || arg.equals("--hex-uppercase")) {
                    parameters.setEncoding(Encoding.HEX_UPPERCASE);

                } else if (arg.equals("--charset-file-list") || arg.equals("--file-list-charset")) {
                    if (firstfile < args.length) {
                        parameters.setCharsetFileList(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, "--charset-file-list");
                    }

                } else if (arg.equals("--charset-check-file") || arg.equals("--check-file-charset")) {
                    if (firstfile < args.length) {
                        parameters.setCharsetCheckFile(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, "--charset-check-file");
                    }

                } else if (arg.equals("--charset-error-file") || arg.equals("--error-file-charset")) {
                    if (firstfile < args.length) {
                        parameters.setCharsetErrorFile(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, "--charset-error-file");
                    }

                } else if (arg.equals("--charset-output-file") || arg.equals("--output-file-charset")) {
                    if (firstfile < args.length) {
                        parameters.setCharsetOutputFile(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, "--charset-output-file");
                    }

                } else if (arg.equals("--charset-stdout") || arg.equals("--stdout-charset")) {
                    if (firstfile < args.length) {
                        parameters.setCharsetStdout(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, "--charset-stdout");
                    }

                } else if (arg.equals("--charset-stderr") || arg.equals("--stderr-charset")) {
                    if (firstfile < args.length) {
                        parameters.setCharsetStderr(args[firstfile++]);
                    } else {
                        handleUserParamError(arg, "--charset-stderr");
                    }

                } else {
                    throw new ParameterException(String.format("Unknown argument: %s. Use -h for help.", arg));
                }

            } // end while

            // if --info has been set, enable a potentially disabled verbose object again
            if (parameters.isInfoMode()) {
                parameters.getVerbose().setDefault();
            }

            // parsing the verboseControl at the end of the while loop,
            // because some options change the default of the verbose object
            if (verboseControl != null) {
                try {
                    parameters.getVerbose().setVerbose(verboseControl);
                } catch (IllegalArgumentException e) {
                    Help.printHelp("en", "--verbose");
                    throw new ParameterException(e.getMessage());
                }
            }

            // processing arguments file list            
            for (int i = firstfile; i < args.length; i++) {
                if (args[i].equals("-") && !dashdash) { // the dash (stdin) could come even between normal filenames
                    parameters.setStdinForFilenamesFromArgs(true);
                } else {
                    parameters.getFilenamesFromArgs().add(args[i]);
                }
            }

            // processing list that has been specified with -L            
            if (parameters.getFilelistFilename() != null) {
                try {
                    if (parameters.getFilelistFilename().equals("-")) { // stdin

                        if (parameters.getFilelistFormat().equals("list")) {

                            parameters.getFilenamesFromFilelist().addAll(
                                    GeneralIO.readLinesFromStdin(
                                            Charset.forName(parameters.getCharsetFileList()),
                                            true,
                                            parameters.getCommentChars(), false));
                        } else if (parameters.getFilelistFormat().equals("ssv")) { // space separated values
                            parameters.getFilenamesFromFilelist().addAll(
                                    GeneralIO.readLinesFromStdin(
                                            Charset.forName(parameters.getCharsetFileList()),
                                            true,
                                            parameters.getCommentChars(), true));
                        } else {
                            Help.printHelp("en", "--file-list-format");
                            throw new ParameterException(String.format("Filelist format \"%s\" is unsupported.", parameters.getFilelistFormat()));
                        }
                    } else {
                        if (parameters.getFilelistFormat().equals("list")) {

                            parameters.getFilenamesFromFilelist().addAll(
                                    GeneralIO.readLinesFromTextFile(
                                            parameters.getFilelistFilename(),
                                            Charset.forName(parameters.getCharsetFileList()),
                                            true,
                                            parameters.getCommentChars(), false));
                        } else if (parameters.getFilelistFormat().equals("ssv")) { // space separated values
                            parameters.getFilenamesFromFilelist().addAll(
                                    GeneralIO.readLinesFromTextFile(
                                            parameters.getFilelistFilename(),
                                            Charset.forName(parameters.getCharsetFileList()),
                                            true,
                                            parameters.getCommentChars(), true));

                        } else {
                            Help.printHelp("en", "--file-list-format");
                            throw new ParameterException(String.format("Filelist format \"%s\" is unsupported.", parameters.getFilelistFormat()));
                        }
                    }
                } catch (UnsupportedCharsetException uce) {
                    throw new ParameterException(String.format("Charset \"%s\" is unsupported. Check the supported character sets with jacksum --info.", parameters.getCharsetFileList()));
                } catch (IOException ex) {
                    throw new ParameterException(String.format("File %s not found or cannot be read.", parameters.getFilelistFilename()));
                }
            }

        } // end-if args.length > 0
        // end parsing arguments
        return parameters;
    }

}

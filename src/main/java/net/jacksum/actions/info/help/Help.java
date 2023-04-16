/*


  Jacksum 3.7.0 - a checksum utility in Java
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
package net.jacksum.actions.info.help;

import java.io.*;
import java.util.Locale;

import net.jacksum.actions.info.version.VersionAction;

/**
 * The Help system for the command line interface.
 */
public class Help {

    /**
     * Print help on standard output.
     *
     * @param code   the language code (e.g. en)
     * @param search the search string, can be null
     */
    public static void printHelp(String code, String search) {
        try {
            System.out.print(searchHelp(code, search));
        } catch (NothingFoundException nfe) {
            System.err.print(nfe.getMessage());
        } catch (FileNotFoundException fnfe) {
            System.err.print(fnfe.getMessage());
        } catch (IOException ioe) {
            System.err.print(ioe.getMessage());
        }
    }

    public static String searchHelp(String code, String search) throws NothingFoundException, IOException {
        return searchHelp(code, search, false);
    }

    public static String searchHelp(String code, String search, boolean strict) throws NothingFoundException, IOException {
        String filename = "/net/jacksum/help/help_" + code + ".txt";
        try {
            return searchLongHelp(filename, search, strict);
        } catch (FileNotFoundException fnfe) {
            // A note for maintainers of platform specific packages:
            // please keep the help file, don't remove it. The help file is important, because the
            // error system and --help options of Jacksum relies on it.
            throw new FileNotFoundException(String.format("FATAL: help file %s is not bundled with .jar file. Please file a bug for the maintainer of this package.%n", filename));
        } catch (IOException ioe) {
            throw new IOException(String.format("FATAL: problem while reading help file %s.%n", filename));
        }

    }

    /**
     * Print help on standard output.
     *
     * @param code the language code (e.g. en)
     */
    public static void printHelp(String code) {
        printHelp(code, null);
    }

    public static void printShortUsage() {
        System.out.printf(
                "Usage:%n"
                        + "    jacksum [option]... [file|directory]...%n%n"

                        + "For more help type%n"

                        + "    jacksum -h%n%n"
        );

    }

    /**
     * Print copyright and short license info
     */
    public static void printShortHelp() {
        VersionAction.printAppVersionFull();
        System.out.println();
        printShortUsage();
    }

    /**
     * Print the documentation.
     *
     * @param filename the flat file in the jar file containing the
     *                 documentation
     * @param search   the search string
     * @throws FileNotFoundException if the help file cannot be
     *                               foundAtLeastOneSection
     * @throws IOException           if an I/O exception occurs
     */
    public static void printLongHelp(String filename, String search) throws
            FileNotFoundException, IOException {
        try {
            System.out.print(searchLongHelp(filename, search, false));
        } catch (NothingFoundException nfe) {
            System.err.print(nfe.getMessage());
        }
    }

    private final static String NEW_LINE = String.format("%n");

    private static String searchLongHelp(String filename, String search, boolean strict) throws
            FileNotFoundException, IOException, NothingFoundException {
        StringBuilder out = new StringBuilder();


        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            // open the stream
            is = Help.class.getResourceAsStream(filename);
            if (is == null) {
                throw new FileNotFoundException(filename);
            }
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);

            String line;
            if (search == null) {
                // print out the entire help
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("\\") || line.startsWith("#")) {
                        // those lines are treated as empty lines
                        out.append(NEW_LINE);
                    } else {
                        out.append(String.format("%s%n", line));
                    }
                }
            } else {
                boolean foundAtLeastOneSection = false; // we have found at least one match
                boolean blockOutput = false;
                // print out only the block which matches the search
                StringBuilder buffer = new StringBuilder();
                boolean found = false;
                boolean inSearchableSection = false;
                String searchUppercase = search.toUpperCase(Locale.US);

                String section = "";
                while ((line = br.readLine()) != null) {

                    // an empty line indicates the start of a new block
                    boolean startOfAnewBlock = line.trim().length() == 0;

                    // e.g. #OPTIONS
                    boolean entireSection = found && line.startsWith("#" + searchUppercase);


                    // we are at the begin of a searchable section
                    if (line.startsWith("#") && line.endsWith("-BEGIN")) {
                        section = line.substring(1, line.length() - 6);

                        inSearchableSection = true;
                        startOfAnewBlock = true;

                        // e.g. jacksum -h options
                        if (entireSection) {
                            startOfAnewBlock = false;
                            blockOutput = true;
                        }
                        // we have reached the end of a searchable section
                    } else if (line.startsWith("#") && line.endsWith("-END")) {
                        section = "";
                        inSearchableSection = false;
                        startOfAnewBlock = true;

                        if (entireSection) {
                            blockOutput = false;
                        }
                    }

                    // One backslash at pos 1 indicates that it belongs
                    // to the block
                    if (line.startsWith("\\") || line.startsWith("#")) {
                        line = "";
                    }

                    //if (!startOfAnewBlock) {
                    // put the current line to buffer
                    buffer.append(String.format("%s%n", line));
                    //}

                    if (!found) {
                        // search string is an exact option (e. g. "jacksum -h -a")
                        // a fraction of an option (e. g. "jacksum -h --path")
                        if (inSearchableSection // we are in a searchable section (options or algorithms)
                                && line.substring(0, Math.min(line.length(), 12)).trim().length() > 0) { // in line there is an option or an algo

                            if (section.equals("OPTIONS") && search.startsWith("-")) {
                                String trimmedLine = line.trim();
                                if (strict && trimmedLine.equals(search) || !strict && trimmedLine.startsWith(search)) {
                                    found = true;
                                }
                            } else if (section.equals("ALGORITHMS")) { // in a line there could be many algorithms ids, separated by a comma

                                String[] tokens = line.trim().split(",");
                                for (String token : tokens) {
                                    String trimmedToken = token.trim()
                                            .replaceAll("\\[", "")
                                            .replaceAll("\\]", "");


                                    // if "<" is part of the token, it is a candidate for a regex,
                                    // e.g. haval_128_3 for "haval, haval_<width>_<rounds>"
                                    if (trimmedToken.contains("<")) {
                                        // convert the trimmedToken to a regex
                                        String pattern = trimmedToken
                                                .replaceAll("<.+?>", "([0-9]+)");

                                        if (search.matches(pattern)) {
                                            found = true;
                                            break;
                                        } else {
                                            if (!strict && trimmedToken.startsWith(search)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                    } else if (strict && trimmedToken.equals(search) || !strict && trimmedToken.startsWith(search)) {
                                        found = true;
                                        break;
                                    }
                                }
                            }

                        } else

                            // we are out of a searchable section, but the user could search for
                            // a header or a fraction of a header, e.g. "jacksum -h examples",
                            // "jacksum -h example", or "jacksum -h ex"
                            if (line.toLowerCase(Locale.US).startsWith(search.toLowerCase(Locale.US))) {
                                found = true;
                            }
                    }

                    // an empty line indicates the start of a new block
                    // that means, we must print out the buffer
                    if (startOfAnewBlock && !blockOutput) {
                        // put out the buffer that has been filled with lines
                        if (found && buffer.length() > 0) {
                            out.append(buffer);
                            foundAtLeastOneSection = true;
                        }
                        // new chance again
                        found = false;
                        // clear the buffer
                        buffer.delete(0, buffer.length());
                    }

                } // end-while
                // is there still something in the buffer?
                if (found && buffer.length() > 0) {
                    out.append(buffer);
                    foundAtLeastOneSection = true;
                }
                if (!foundAtLeastOneSection) {
                    throw new NothingFoundException(String.format("Your search \"%s\" did not match any section in the help.%n", search));
                }

            } // end-if

        } finally {
            if (br != null) {
                br.close();
            }
            if (isr != null) {
                isr.close();
            }
            if (is != null) {
                is.close();
            }
        } // end-try

        return out.toString();
    }


}

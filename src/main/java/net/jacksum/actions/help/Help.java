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
package net.jacksum.actions.help;

import java.io.*;
import java.util.Locale;
import net.jacksum.actions.version.VersionAction;

/**
 * The Help system for the command line interface.
 */
public class Help {


    /**
     * Print help on standard output.
     *
     * @param code the language code (e.g. en)
     * @param search the search string, can be null
     */
    public static void printHelp(String code, String search) {
        String filename = "/net/jacksum/help/help_" + code + ".txt";
        try {
            printLongHelp(filename, search);
        } catch (FileNotFoundException fnfe) {
            System.err.printf("FATAL: Helpfile %s in .jar file not found.\n%n", filename);
        } catch (IOException ioe) {
            System.err.println("FATAL: problem while reading helpfile " + filename);
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
        System.out.print(
                  "Usage:\n"
                + "    jacksum [option]... [file|directory]...\n\n"

                + "For more help type\n"

                + "    jacksum -h\n\n"
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
     * documentation
     * @param search the search string
     * @throws FileNotFoundException if the help file cannot be
     * foundAtLeastOneSection
     * @throws IOException if an I/O exception occurs
     */
    public static void printLongHelp(String filename, String search) throws
            FileNotFoundException, IOException {
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
                        System.out.println();
                    } else {
                        System.out.println(line);
                    }
                }
            } else {
                boolean foundAtLeastOneSection = false; // we have found at least one match
                boolean blockOutput = false;
                // print out only the block which matches the search
                StringBuilder buffer = new StringBuilder();
                boolean found = false;
                boolean inSearchableSection = false;
                while ((line = br.readLine()) != null) {

                    boolean startOfAnewBlock = line.trim().length() == 0;

                    if (line.startsWith("#") && line.endsWith("-BEGIN")) {
                        inSearchableSection = true;
                        startOfAnewBlock = true;

                        // e.g. jacksum -h options
                        if (found && line.startsWith("#"+search.toUpperCase(Locale.US))) {
                            startOfAnewBlock = false;
                            blockOutput = true;
                        }
                    } else if (line.startsWith("#") && line.endsWith("-END")) {
                        inSearchableSection = false;
                        startOfAnewBlock = true;

                        if (found && line.startsWith("#"+search.toUpperCase(Locale.US))) {
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
                        buffer.append(line);
                        buffer.append('\n');
                    //}

                    if (!found
                            && // e. g. jacksum -h -a
                            ((inSearchableSection
                            && line.substring(0, Math.min(12, line.length())).trim().length() > 0
                            && line.trim().startsWith(search))
                            // e. g. jacksum -h examples
                            || (line.toLowerCase(Locale.US).startsWith(search.toLowerCase(Locale.US))))) {
                        found = true;
                    }

                    // an empty line indicates the start of a new block
                    // that means, we must print out the buffer
                    if (startOfAnewBlock && !blockOutput) {
                        // put out the buffer that has been filled with lines
                        if (found && buffer.length() > 0) {
                            System.out.print(buffer);
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
                    System.out.print(buffer);
                    foundAtLeastOneSection = true;
                }
                if (!foundAtLeastOneSection) {
                    System.err.printf("Your search \"%s\" did not match any section in the help.\n", search);
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
    }

}

/*

 Sugar for Java 1.6.0
 Copyright (c) 2001-2026  Dipl.-Inf. (FH) Johann N. Löfflmann,
 All Rights Reserved, https://johann.loefflmann.net

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 @author Johann N. Löfflmann

 */
package net.loefflmann.sugar.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import net.loefflmann.sugar.util.GeneralString;

/**
 * Provides general I/O helper methods, e.g. to read lines from a text file, from a
 * resource in the jar file, or from standard input.
 *
 * @author Johann N. Löfflmann
 */
public class GeneralIO {

    /**
     * Creates a new GeneralIO.
     */
    public GeneralIO() {
    }

    /**
     * Reads all lines from a text file.
     * The default charset is used; empty lines are not ignored; no lines are ignored
     * because of a prefix; the lines are not tokenized.
     *
     * @param filename the name of the text file
     * @return the lines that have been read
     * @throws IOException if an I/O error occurs
     */
    public static List<String> readLinesFromTextFile(String filename) throws IOException {
        return readLinesFromTextFile(filename, Charset.defaultCharset(), false, null, false);
    }

    /**
     * Reads all lines from a text file.
     * Empty lines are not ignored; no lines are ignored because of a prefix; the lines
     * are not tokenized.
     *
     * @param filename the name of the text file
     * @param charset the character set used to decode the bytes
     * @return the lines that have been read
     * @throws IOException if an I/O error occurs
     */
    public static List<String> readLinesFromTextFile(String filename, Charset charset) throws IOException {
        return readLinesFromTextFile(filename, charset, false, null, false);
    }

    /**
     * Reads all lines from a text file.
     * No lines are ignored because of a prefix; the lines are not tokenized.
     *
     * @param filename the name of the text file
     * @param charset the character set used to decode the bytes
     * @param ignoreEmptyLines if true, lines that are empty or contain only whitespace are ignored
     * @return the lines that have been read
     * @throws IOException if an I/O error occurs
     */
    public static List<String> readLinesFromTextFile(String filename, Charset charset, boolean ignoreEmptyLines) throws IOException {
        return readLinesFromTextFile(filename, charset, ignoreEmptyLines, null, false);
    }
    
    /**
     * Reads all lines from a text file.
     * The lines are not tokenized.
     *
     * @param filename the name of the text file
     * @param charset the character set used to decode the bytes
     * @param ignoreEmptyLines if true, lines that are empty or contain only whitespace are ignored
     * @param ignorePrefix lines that start with this prefix are ignored, can be null
     * @return the lines that have been read
     * @throws IOException if an I/O error occurs
     */
    public static List<String> readLinesFromTextFile(String filename, Charset charset, boolean ignoreEmptyLines, String ignorePrefix) throws IOException {
        return readLinesFromTextFile(filename, charset, ignoreEmptyLines, ignorePrefix, false);
    }

    

    
    /**
     * Reads all lines from a resource in the jar file.
     * The default charset is used; empty lines are not ignored; no lines are ignored
     * because of a prefix; the lines are not tokenized.
     *
     * @param filename the absolute path of the resource in the jar file
     * @return the lines that have been read
     * @throws IOException if an I/O error occurs or the resource has not been found
     */
    public static List<String> readLinesFromJarFile(String filename) throws IOException {
        return readLinesFromJarFile(filename, Charset.defaultCharset(), false, null, false);
    }
    
    /**
     * Reads all lines from a resource in the jar file.
     * Empty lines are not ignored; no lines are ignored because of a prefix; the lines
     * are not tokenized.
     *
     * @param filename the absolute path of the resource in the jar file
     * @param charset the character set that is used to decode the resource
     * @return the lines that have been read
     * @throws IOException if an I/O error occurs or the resource has not been found
     */
    public static List<String> readLinesFromJarFile(String filename, Charset charset) throws IOException {
        return readLinesFromJarFile(filename, charset, false, null, false);
    }
    
    /**
     * Reads all lines from a resource in the jar file.
     * No lines are ignored because of a prefix; the lines are not tokenized.
     *
     * @param filename the absolute path of the resource in the jar file
     * @param charset the character set that is used to decode the resource
     * @param ignoreEmptyLines if true, lines that are empty or contain only whitespace are ignored
     * @return the lines that have been read
     * @throws IOException if an I/O error occurs or the resource has not been found
     */
    public static List<String> readLinesFromJarFile(String filename, Charset charset, boolean ignoreEmptyLines) throws IOException {
        return readLinesFromJarFile(filename, charset, ignoreEmptyLines, null, false);
    }
    
    /**
     * Reads all lines from a resource in the jar file.
     * The lines are not tokenized.
     *
     * @param filename the absolute path of the resource in the jar file
     * @param charset the character set that is used to decode the resource
     * @param ignoreEmptyLines if true, lines that are empty or contain only whitespace are ignored
     * @param ignorePrefix lines that start with this prefix are ignored, can be null
     * @return the lines that have been read
     * @throws IOException if an I/O error occurs or the resource has not been found
     */
    public static List<String> readLinesFromJarFile(String filename, Charset charset, boolean ignoreEmptyLines, String ignorePrefix) throws IOException {
        return readLinesFromJarFile(filename, charset, ignoreEmptyLines, ignorePrefix, false);
    }


    /**
     * Reads all lines from standard input.
     * The default charset is used; empty lines are not ignored; no lines are ignored
     * because of a prefix; the lines are not tokenized.
     *
     * @return the lines that have been read
     * @throws IOException if an I/O error occurs
     */
    public static List<String> readLinesFromStdin() throws IOException {
        return readLinesFromStdin(Charset.defaultCharset(), false, null, false);
    }

    /**
     * Reads all lines from standard input.
     * Empty lines are not ignored; no lines are ignored because of a prefix; the lines
     * are not tokenized.
     *
     * @param charset the character set used to decode the bytes
     * @return the lines that have been read
     * @throws IOException if an I/O error occurs
     */
    public static List<String> readLinesFromStdin(Charset charset) throws IOException {
        return readLinesFromStdin(charset, false, null, false);
    }

    /**
     * Reads all lines from standard input.
     * No lines are ignored because of a prefix; the lines are not tokenized.
     *
     * @param charset the character set used to decode the bytes
     * @param ignoreEmptyLines if true, lines that are empty or contain only whitespace are ignored
     * @return the lines that have been read
     * @throws IOException if an I/O error occurs
     */
    public static List<String> readLinesFromStdin(Charset charset, boolean ignoreEmptyLines) throws IOException {
        return readLinesFromStdin(charset, ignoreEmptyLines, null, false);
    }
    
    /**
     * Reads all lines from standard input.
     * The lines are not tokenized.
     *
     * @param charset the character set used to decode the bytes
     * @param ignoreEmptyLines if true, lines that are empty or contain only whitespace are ignored
     * @param ignorePrefix lines that start with this prefix are ignored, can be null
     * @return the lines that have been read
     * @throws IOException if an I/O error occurs
     */
    public static List<String> readLinesFromStdin(Charset charset, boolean ignoreEmptyLines, String ignorePrefix) throws IOException {
        return readLinesFromStdin(charset, ignoreEmptyLines, ignorePrefix, false);
    }

    
    
    /**
     * Reads all lines from a resource in the jar file.
     *
     * @param filename the absolute path of the resource in the jar file
     * @param charset the character set that is used to decode the resource
     * @param ignoreEmptyLines if true, lines that are empty or contain only whitespace are ignored
     * @param ignorePrefix lines that start with this prefix are ignored, can be null
     * @param linesContainNormalAndQuotedStringsSeparatedByWhiteSpaceChars if true, each line is split into normal and
     *        quoted strings separated by whitespace characters, and those tokens are
     *        returned instead of the lines
     * @return the lines that have been read
     * @throws IOException if an I/O error occurs or the resource has not been found
     */
    public static List<String> readLinesFromJarFile(String filename, Charset charset, boolean ignoreEmptyLines, String ignorePrefix,
            boolean linesContainNormalAndQuotedStringsSeparatedByWhiteSpaceChars) throws IOException {
        List<String> lines = new ArrayList<>();

        // the check must come before the reader is created, because an InputStreamReader
        // cannot be created for a null stream
        InputStream is = GeneralIO.class.getResourceAsStream(filename);
        if (is == null) {
            throw new IOException(String.format("%s not found.", filename));
        }
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, charset))) {
            
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (ignoreEmptyLines && line.trim().length() == 0) {
                    continue;
                }
                if (ignorePrefix != null && line.startsWith(ignorePrefix)) {
                    continue;
                }
                if (linesContainNormalAndQuotedStringsSeparatedByWhiteSpaceChars) {
                    lines.addAll(GeneralString.tokenize(line));
                } else {
                    lines.add(line);
                }
            }

            
            return lines;
        }

    }

    /**
     * Reads all lines from a text file.
     *
     * @param filename the name of the text file
     * @param charset the character set used to decode the bytes
     * @param ignoreEmptyLines if true, lines that are empty or contain only whitespace are ignored
     * @param ignorePrefix lines that start with this prefix are ignored, can be null
     * @param linesContainNormalAndQuotedStringsSeparatedByWhiteSpaceChars if true, each line is split into normal and
     *        quoted strings separated by whitespace characters, and those tokens are
     *        returned instead of the lines
     * @return the lines that have been read
     * @throws IOException if an I/O error occurs
     */
    public static List<String> readLinesFromTextFile(String filename, Charset charset, boolean ignoreEmptyLines, String ignorePrefix, 
            boolean linesContainNormalAndQuotedStringsSeparatedByWhiteSpaceChars) throws IOException {
        List<String> lines = new ArrayList<>();

        File file = new File(filename);
        try (
                FileReader fileReader = new FileReader(file, charset);
                BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (ignoreEmptyLines && line.trim().length() == 0) {
                    continue;
                }
                if (ignorePrefix != null && line.startsWith(ignorePrefix)) {
                    continue;
                }
                if (linesContainNormalAndQuotedStringsSeparatedByWhiteSpaceChars) {
                    lines.addAll(GeneralString.tokenize(line));
                } else {
                    lines.add(line);
                }
            }

            return lines;
        }
    }
    
    
    /**
     * Reads all lines from standard input.
     *
     * @param charset the character set used to decode the bytes
     * @param ignoreEmptyLines if true, lines that are empty or contain only whitespace are ignored
     * @param ignorePrefix lines that start with this prefix are ignored, can be null
     * @param linesContainNormalAndQuotedStringsSeparatedByWhiteSpaceChars if true, each line is split into normal and
     *        quoted strings separated by whitespace characters, and those tokens are
     *        returned instead of the lines
     * @return the lines that have been read
     * @throws IOException if an I/O error occurs
     */
    public static List<String> readLinesFromStdin(Charset charset, boolean ignoreEmptyLines, String ignorePrefix, 
            boolean linesContainNormalAndQuotedStringsSeparatedByWhiteSpaceChars) throws IOException {
        List<String> lines = new ArrayList<>();

        // The reader is deliberately neither closed nor wrapped in try-with-resources:
        // closing it would close System.in as well, and standard input has to stay readable
        // for a further read in the same process. It holds no resource of its own that would
        // have to be released, only the buffer, which the garbage collector takes care of.
        InputStreamReader inputStreamReader = new InputStreamReader(System.in, charset);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (ignoreEmptyLines && line.trim().length() == 0) {
                continue;
            }
            if (ignorePrefix != null && line.startsWith(ignorePrefix)) {
                continue;
            }
            if (linesContainNormalAndQuotedStringsSeparatedByWhiteSpaceChars) {
                lines.addAll(GeneralString.tokenize(line));
            } else {
                lines.add(line);
            }
        }
        return lines;
    }
     
    
    /**
     * Tells whether a file is a symbolic link, by comparing its canonical path with its
     * absolute path. On Windows, false is always returned.
     *
     * @param file the file
     * @return true if the file is a symbolic link, or if its canonical path cannot be
     *         determined
     */
    public static boolean isSymbolicLink(File file) {
        // there are no symbolic links on Windows.
        // On Windows a link is always a file.
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            return false;
        }

        try {
            String cnnpath = file.getCanonicalPath();
            String abspath = file.getAbsolutePath();
            abspath = GeneralString.replaceString(abspath, "/./", "/");
            if (abspath.endsWith("/.")) {
                return false;
            }
            return !abspath.equals(cnnpath);
        } catch (IOException ex) {
            System.err.println(ex);
            return true;
        }
    }
}

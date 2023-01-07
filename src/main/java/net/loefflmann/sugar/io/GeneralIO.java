/*

 Sugar for Java 1.6.0
 Copyright (C) 2001-2023  Dipl.-Inf. (FH) Johann N. Löfflmann,
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
 *
 * @author Johann N. Löfflmann
 */
public class GeneralIO {

    public static List<String> readLinesFromTextFile(String filename) throws IOException {
        return readLinesFromTextFile(filename, Charset.defaultCharset(), false, null, false);
    }

    public static List<String> readLinesFromTextFile(String filename, Charset charset) throws IOException {
        return readLinesFromTextFile(filename, charset, false, null, false);
    }

    public static List<String> readLinesFromTextFile(String filename, Charset charset, boolean ignoreEmptyLines) throws IOException {
        return readLinesFromTextFile(filename, charset, ignoreEmptyLines, null, false);
    }
    
    public static List<String> readLinesFromTextFile(String filename, Charset charset, boolean ignoreEmptyLines, String ignorePrefix) throws IOException {
        return readLinesFromTextFile(filename, charset, ignoreEmptyLines, ignorePrefix, false);
    }

    

    
    public static List<String> readLinesFromJarFile(String filename) throws IOException {
        return readLinesFromJarFile(filename, Charset.defaultCharset(), false, null, false);
    }
    
    public static List<String> readLinesFromJarFile(String filename, Charset charset) throws IOException {
        return readLinesFromJarFile(filename, charset, false, null, false);
    }
    
    public static List<String> readLinesFromJarFile(String filename, Charset charset, boolean ignoreEmptyLines) throws IOException {
        return readLinesFromJarFile(filename, charset, ignoreEmptyLines, null, false);
    }
    
    public static List<String> readLinesFromJarFile(String filename, Charset charset, boolean ignoreEmptyLines, String ignorePrefix) throws IOException {
        return readLinesFromJarFile(filename, charset, ignoreEmptyLines, ignorePrefix, false);
    }


    public static List<String> readLinesFromStdin() throws IOException {
        return readLinesFromStdin(Charset.defaultCharset(), false, null, false);
    }

    public static List<String> readLinesFromStdin(Charset charset) throws IOException {
        return readLinesFromStdin(charset, false, null, false);
    }

    public static List<String> readLinesFromStdin(Charset charset, boolean ignoreEmptyLines) throws IOException {
        return readLinesFromStdin(charset, ignoreEmptyLines, null, false);
    }
    
    public static List<String> readLinesFromStdin(Charset charset, boolean ignoreEmptyLines, String ignorePrefix) throws IOException {
        return readLinesFromStdin(charset, ignoreEmptyLines, ignorePrefix, false);
    }

    
    
    public static List<String> readLinesFromJarFile(String filename, Charset charset, boolean ignoreEmptyLines, String ignorePrefix,
            boolean linesContainNormalAndQuotedStringsSeparatedByWhiteSpaceChars) throws IOException {
        List<String> lines = new ArrayList<>();
        
        try (InputStream is = GeneralIO.class.getResourceAsStream(filename);
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader bufferedReader = new BufferedReader(isr)
        ) {
            if (is == null) {
                throw new IOException(String.format("%s not found.", filename));
            }
            
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
    
    
    public static List<String> readLinesFromStdin(Charset charset, boolean ignoreEmptyLines, String ignorePrefix, 
            boolean linesContainNormalAndQuotedStringsSeparatedByWhiteSpaceChars) throws IOException {
        List<String> lines = new ArrayList<>();

        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader;
        
        try {
            // don't use try-with-resources here, because we only want to close the BufferedReader,
            // but we don't want to close System.in
            inputStreamReader = new InputStreamReader(System.in, charset);
            bufferedReader = new BufferedReader(inputStreamReader);

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
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return lines;
    }
     
    
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

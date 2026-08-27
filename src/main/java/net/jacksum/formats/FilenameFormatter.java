/*


  Jacksum 4.0.1 - a checksum/hash tool written in Java
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


package net.jacksum.formats;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.jacksum.multicore.OSControl;
import net.jacksum.parameters.base.FilenameFormatParameters;
import net.loefflmann.sugar.util.GeneralString;

public class FilenameFormatter implements FilenameFormatParameters {

    private final FilenameFormatParameters parameters;
    
    public FilenameFormatter(FilenameFormatParameters parameters) {
        this.parameters = parameters;
    }
    
    /**
     * @return the parameters
     */
    public FilenameFormatParameters getParameters() {
        return parameters;
    }


    private String fixPathChar(String filename) {
        if (filename == null) return "";
        if (parameters.isPathCharSet()) {
            return filename.replace(File.separatorChar, parameters.getPathChar());
        } else {
            return filename;
        }
    }

    // if the filename contains a backslash, newline, or carriage return, the line is started with a backslash,
    // and each problematic character in the file name is escaped with a backslash, making the output unambiguous
    // even in the presence of arbitrary file names.
    public static String gnuEscapeProblematicCharsInFilename(String filename) {
        if (filename == null) return "";
        StringBuilder buffer = new StringBuilder(filename);
        GeneralString.replaceAllStrings(buffer, "\\", "\\\\"); // backslash
        GeneralString.replaceAllStrings(buffer, "\n", "\\n"); // new line
        GeneralString.replaceAllStrings(buffer, "\r", "\\r"); // carriage return
        return buffer.toString();
    }

    // the inverse of gnuEscapeProblematicCharsInFilename(): the escape sequences \\, \n and \r are
    // translated back to the character they stand for. The file name is processed in one pass from
    // left to right, so the backslash that a \\ stands for is never interpreted as the start of
    // another escape sequence, i.e. the file name a\\nb is unescaped to a\nb and not to a<newline>b.
    // Any other sequence that starts with a backslash stays unchanged, because a backslash is a
    // valid character in a file name, real life example:
    // /lib/systemd/system/system-systemd\x2dcryptsetup.slice
    public static String gnuUnescapeProblematicCharsInFilename(String filename) {
        if (filename == null) return "";
        int length = filename.length();
        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = filename.charAt(i);
            if (c != '\\' || i + 1 == length) { // a normal char, or a trailing backslash
                buffer.append(c);
                continue;
            }
            char next = filename.charAt(++i);
            switch (next) {
                case '\\': buffer.append('\\'); break; // backslash
                case 'n': buffer.append('\n'); break; // new line
                case 'r': buffer.append('\r'); break; // carriage return
                default: // not an escape sequence that has been created by the escaping, keep it
                    buffer.append(c).append(next);
            }
        }
        return buffer.toString();
    }

    public boolean filenameContainedProblematicChars = false;

    public boolean didTheFormatMethodChangeProblematicChars() {
        return filenameContainedProblematicChars;
    }

    public String gnuEscapeProblematicCharsInFilenameWithResult(String filename) {
        if (filename == null) return "";
        String newFilename = gnuEscapeProblematicCharsInFilename(filename);
        // if there was a problematic character being replaced the length of the string will be larger
        filenameContainedProblematicChars = newFilename.length() != filename.length();
        return newFilename;
    }

    public static void replaceAliases(StringBuilder format) {
        GeneralString.replaceAllStrings(format, "#MESSAGE", "#FILENAME");
    }

    public String format(String filename) {
        if (filename == null) return "";
        filenameContainedProblematicChars = false;
        boolean escape = parameters.isGnuEscaping() && !OSControl.isWindows();

        if (parameters.isNoPath()) {
            try {
                String filenameWithoutPath = Paths.get(filename).getFileName().toString();
                return escape ? gnuEscapeProblematicCharsInFilenameWithResult(filenameWithoutPath) : filenameWithoutPath;
            } catch (InvalidPathException ipe) {
                return filename;
            }
        }

        if (parameters.getPathRelativeTo() != null) {
            try {
                // Get the relative path from two absolute paths
                Path path1 = Paths.get(filename).toAbsolutePath().normalize();
                Path path2 = parameters.getPathRelativeTo();

                // Convert the absolute path to a relative path, and fix the path char
                // It throws an IllegalArgumentException if path1 is not a Path that can be relativized against path2
                // e.g. if path1 and path2 have different roots (on Microsoft Windows)
                String filenameNew = path2.relativize(path1).toString();
                return fixPathChar(escape ? gnuEscapeProblematicCharsInFilenameWithResult(filenameNew) : filenameNew);

            } catch (InvalidPathException ipe) {
                return filename;
            } catch (IllegalArgumentException iae) {
                return fixPathChar(escape ? gnuEscapeProblematicCharsInFilenameWithResult(filename) : filename);
            }
        }
        return fixPathChar(escape ? gnuEscapeProblematicCharsInFilenameWithResult(filename) : filename);
    }

    @Override
    public Character getPathChar() {
        return parameters.getPathChar();
    }

    @Override
    public boolean isPathCharSet() {
        return parameters.isPathCharSet();
    }

    @Override
    public boolean isNoPath() {
        return parameters.isNoPath();
    }

    @Override
    public Path getPathRelativeTo() {
        return parameters.getPathRelativeTo();
    }

    @Override
    public boolean isGnuEscaping() {
        return parameters.isGnuEscaping();
    }

    @Override
    public boolean isGnuEscapingSetByUser() {
        return parameters.isGnuEscapingSetByUser();
    }

}

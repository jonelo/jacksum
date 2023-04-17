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

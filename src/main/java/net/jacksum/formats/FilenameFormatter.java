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


package net.jacksum.formats;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.jacksum.parameters.base.FilenameFormatParameters;

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
        if (parameters.isPathCharSet()) {
            return filename.replace(File.separatorChar, parameters.getPathChar());
        } else {
            return filename;
        }
    }


    public String format(String filename) {
        if (parameters.isNoPath()) {
            try {
                return Paths.get(filename).getFileName().toString();
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
                return fixPathChar(path2.relativize(path1).toString());

            } catch (InvalidPathException ipe) {
                return filename;
            }
        }
        return fixPathChar(filename);
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

}

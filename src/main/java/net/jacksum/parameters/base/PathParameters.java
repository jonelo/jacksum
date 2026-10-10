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
package net.jacksum.parameters.base;

import java.util.List;

/**
 * Parameters that control which paths are processed and how they are printed.
 */
public interface PathParameters {
    /**
     * Returns the file names that have been specified on the command line.
     *
     * @return the file names from the command line arguments
     */
    List<String> getFilenamesFromArgs();
    /**
     * Tells whether directories should be processed recursively (-r).
     *
     * @return true if directories should be processed recursively
     */
    boolean isRecursive();
    /**
     * Tells whether symbolic links to files should not be followed (-f).
     *
     * @return true if symbolic links to files should not be followed
     */
    boolean isDontFollowSymlinksToFiles();
    /**
     * Tells whether symbolic links to directories should not be followed.
     *
     * @return true if symbolic links to directories should not be followed
     */
    boolean isDontFollowSymlinksToDirectories();
    /**
     * Returns the maximum depth for recursion (-r).
     *
     * @return the maximum depth for recursion
     */
    int getDepth();
    /**
     * Tells whether absolute paths should be used (--path-absolute).
     *
     * @return true if absolute paths should be used
     */
    boolean isPathAbsolute();
}

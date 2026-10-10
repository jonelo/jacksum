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
 * Parameters for walking the file tree.
 */
public interface FileWalkerParameters {
    /**
     * Returns the file names that have been specified as command line arguments.
     *
     * @return the list of file names from the command line arguments
     */
    List<String> getFilenamesFromArgs();
    /**
     * Tells whether directories are processed recursively.
     *
     * @return true if directories are processed recursively
     */
    boolean isRecursive();
    /**
     * Tells whether symbolic links to files are not followed.
     *
     * @return true if symbolic links to files are not followed
     */
    boolean isDontFollowSymlinksToFiles();
}

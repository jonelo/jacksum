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

import java.nio.file.Path;

/**
 * Parameters that control how file names are formatted.
 *
 * @author Johann
 */
public interface FilenameFormatParameters {
    /**
     * Returns the character that separates the elements of a path (option -P).
     *
     * @return the path separator character
     */
    Character getPathChar();
    /**
     * Tells whether the path separator character differs from the default one of the system.
     *
     * @return true if the path separator character has been changed
     */
    boolean isPathCharSet();
    /**
     * Tells whether file names should be printed without path (option --no-path).
     *
     * @return true if the path should be omitted
     */
    boolean isNoPath();
    /**
     * Returns the path that file names should be relative to.
     *
     * @return the path, or null if not set
     */
    Path getPathRelativeTo();
    /**
     * Tells whether GNU filename escaping is enabled (option --gnu-filename-escaping).
     *
     * @return true if GNU filename escaping is enabled
     */
    boolean isGnuEscaping();
    /**
     * Tells whether GNU filename escaping has been set by the user explicitly.
     *
     * @return true if the user has set GNU filename escaping
     */
    boolean isGnuEscapingSetByUser();
}

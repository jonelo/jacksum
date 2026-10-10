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

import net.jacksum.formats.Encoding;

/**
 * The parameters that control how a fingerprint (a hash value) is formatted:
 * the encoding and the grouping of its characters.
 */
public interface FingerprintFormatParameters {

    /**
     * Tells whether an encoding has been set.
     *
     * @return true if an encoding has been set
     */
    boolean isEncodingSet();
    /**
     * Returns the encoding of the hash value.
     *
     * @return the encoding
     */
    Encoding getEncoding();

    /**
     * Tells whether a grouping has been set.
     *
     * @return true if a grouping has been set
     */
    boolean isGroupingSet();
    /**
     * Returns the number of characters per group of the hash value.
     *
     * @return the number of characters per group
     */
    int getGrouping();

    /**
     * Tells whether a group separator character has been set.
     *
     * @return true if a group separator character has been set
     */
    boolean isGroupCharSet();
    /**
     * Returns the character that separates the groups of the hash value.
     *
     * @return the group separator character
     */
    Character getGroupChar();

}

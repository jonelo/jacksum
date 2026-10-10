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

package net.jacksum.actions.io.compare;

/**
 * The parameters for an action that compares a computed hash value with an expected one.
 */
public interface CompareActionInterface {

    /**
     * Gets the expected hash value as it has been specified by the user.
     *
     * @return the expected hash value as a string
     */
    String getExpectedString();

    /**
     * Gets the expected hash value decoded to bytes.
     * Implementations may throw an {@code UnsupportedOperationException} if the
     * expected value cannot be decoded to bytes (e.g. because of the encoding), in which
     * case the string representation must be compared instead.
     *
     * @return the expected hash value as a byte array
     */
    byte[] getExpectedBytes();

    /**
     * Gets the format for the output.
     *
     * @return the format, or null if none has been set
     */
    String getFormat();
}

/*

  Jacksum 3.8.0 - a checksum utility in Java
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
package net.jacksum.compats.defs;

/**
 *
 * @author Johann N. Loefflmann
 */
public class InvalidCompatibilityPropertiesException extends Exception {

    /**
     * Creates a new instance of <code>InvalidCompatibilityPropertiesException</code> without detail
     * message.
     */
    public InvalidCompatibilityPropertiesException() {
    }

    /**
     * Constructs an instance of <code>InvalidCompatibilityPropertiesException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidCompatibilityPropertiesException(String msg) {
        super(msg);
    }
}
